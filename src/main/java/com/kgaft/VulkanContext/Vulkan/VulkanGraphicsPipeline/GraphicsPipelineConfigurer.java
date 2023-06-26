package com.kgaft.VulkanContext.Vulkan.VulkanGraphicsPipeline;

import com.kgaft.VulkanContext.DestroyableObject;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.util.List;

import static org.lwjgl.vulkan.VK13.*;

public class GraphicsPipelineConfigurer extends DestroyableObject {
    protected long pipelineLayout;
    protected long descriptorSetLayout = 0;
    protected VulkanDevice device;
    protected VkVertexInputBindingDescription.Buffer inputBindDesc;
    protected VkVertexInputAttributeDescription.Buffer inputAttributeDesc;

    public GraphicsPipelineConfigurer(VulkanDevice device, PipelineBuilder builder) {
        this.device = device;
        loadDescriptorSetLayout(builder);
        loadPipelineLayout(builder);
        prepareBinding(builder.vertexInputs);
        prepareInputAttribs(builder.vertexInputs);
    }

    private void loadDescriptorSetLayout(PipelineBuilder builder) {
        if (builder.samplers.size() + builder.uniformBuffers.size() > 0) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc(builder.samplers.size() + builder.uniformBuffers.size(), stack);
                builder.samplers.forEach(element -> {
                    samplerToBind(element, bindings);
                    bindings.get();
                });
                builder.uniformBuffers.forEach(element -> {
                    uboToBind(element, bindings);
                    bindings.get();
                });
                bindings.rewind();

                VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack);
                layoutInfo.sType$Default();
                layoutInfo.pBindings(bindings);
                long[] res = new long[1];
                if (vkCreateDescriptorSetLayout(device.getDevice(), layoutInfo, null, res) != VK_SUCCESS) {
                    throw new RuntimeException("failed to create descriptor set layout!");
                }
                descriptorSetLayout = res[0];
            }
        }
    }

    @Override
    public void destroy() {
        vkDestroyPipelineLayout(device.getDevice(), pipelineLayout, null);
        if (descriptorSetLayout != VK_NULL_HANDLE)
        {
            vkDestroyDescriptorSetLayout(device.getDevice(), descriptorSetLayout, null);
        }
        super.destroy();
    }

    private void prepareBinding(List<VertexInput> inputs){
        int size = 0;
        for (VertexInput input : inputs) {
            size+=input.typeSize*input.coordinatesAmount;
        }
        inputBindDesc = VkVertexInputBindingDescription.calloc(1);
        inputBindDesc.binding(0);
        inputBindDesc.stride(size);
        inputBindDesc.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
    }
    private void prepareInputAttribs(List<VertexInput> inputs){
        inputAttributeDesc = VkVertexInputAttributeDescription.calloc(inputs.size());
        int offsetCount = 0;
        for (VertexInput element : inputs) {
            inputAttributeDesc.binding(0);
            inputAttributeDesc.location(element.location);
            inputAttributeDesc.offset(offsetCount);
            inputAttributeDesc.format(element.format);
            inputAttributeDesc.get();
            offsetCount += element.typeSize * element.coordinatesAmount;
        }
        inputAttributeDesc.rewind();
    }
    private void loadPipelineLayout(PipelineBuilder builder) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPushConstantRange.Buffer pushConstantRanges = null;
            if (builder.pushConstants.size() > 0) {
                pushConstantRanges = VkPushConstantRange.calloc(builder.pushConstants.size(), stack);
                for (PushConstantInfo element : builder.pushConstants) {
                    pushConstantToRange(element, pushConstantRanges);
                    pushConstantRanges.get();
                }
            }
            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
            pipelineLayoutInfo.sType$Default();
            pipelineLayoutInfo.setLayoutCount(0);
            if (descriptorSetLayout != 0) {
                pipelineLayoutInfo.setLayoutCount(1);
                pipelineLayoutInfo.pSetLayouts(stack.callocLong(1));
                pipelineLayoutInfo.pSetLayouts().put(descriptorSetLayout);
                pipelineLayoutInfo.pSetLayouts().rewind();
            }
            pipelineLayoutInfo.pPushConstantRanges(pushConstantRanges);
            long[] res = new long[1];
            if (vkCreatePipelineLayout(device.getDevice(), pipelineLayoutInfo, null, res) != VK_SUCCESS){
                throw new RuntimeException("Failed to create pipeline layout");
            }
            pipelineLayout = res[0];
        }
    }

    private void pushConstantToRange(PushConstantInfo info, VkPushConstantRange.Buffer range) {
        range.size(info.size);
        range.offset(0);
        range.stageFlags(info.shaderStages);
    }

    private void uboToBind(UniformBufferInfo bufferInfo, VkDescriptorSetLayoutBinding.Buffer result) {
        result.binding(bufferInfo.binding);
        result.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        result.descriptorCount(1);
        result.stageFlags(bufferInfo.shaderStages);
    }

    private void samplerToBind(SamplerInfo samplerInfo, VkDescriptorSetLayoutBinding.Buffer result) {
        result.binding(samplerInfo.binding);
        result.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
        result.descriptorCount(1);
        result.stageFlags(samplerInfo.shaderStages);
    }
}
