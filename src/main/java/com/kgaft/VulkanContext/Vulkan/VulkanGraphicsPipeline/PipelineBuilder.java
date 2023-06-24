package com.kgaft.VulkanContext.Vulkan.VulkanGraphicsPipeline;

import java.util.ArrayList;
import java.util.List;

class VertexInput {
    protected int location;
    protected int coordinatesAmount;
    protected int typeSize;
    protected int format;
}

class PushConstantInfo {
    protected int shaderStages;
    protected int size;
}

class UniformBufferInfo {
    protected int binding;
    protected int size;
    protected int shaderStages;
}

class SamplerInfo {
    protected int binding;
    protected int shaderStages;
}

public class PipelineBuilder {
    protected List<SamplerInfo> samplers = new ArrayList<>();
    protected List<PushConstantInfo> pushConstants = new ArrayList<>();
    protected List<UniformBufferInfo> uniformBuffers = new ArrayList<>();
    protected List<VertexInput> vertexInputs = new ArrayList<>();

    public void addVertexInput(int location, int coordinatesAmount, int typeSize, int format){
        VertexInput input = new VertexInput();
        input.coordinatesAmount = coordinatesAmount;
        input.location = location;
        input.typeSize = typeSize;
        input.format = format;
        vertexInputs.add(input);
    }

    public void addSampler(int binding, int shaderStages){
        SamplerInfo sampler = new SamplerInfo();
        sampler.binding = binding;
        sampler.shaderStages = shaderStages;
        samplers.add(sampler);
    }

    public void addPushConstant(int shaderStages, int size){
        PushConstantInfo pushConstantInfo = new PushConstantInfo();
        pushConstantInfo.shaderStages = shaderStages;
        pushConstantInfo.size = size;
        pushConstants.add(pushConstantInfo);
    }

    public void addUniformBuffer(int binding, int size, int shaderStages){
        UniformBufferInfo uniformBuffer = new UniformBufferInfo();
        uniformBuffer.binding = binding;
        uniformBuffer.size = size;
        uniformBuffer.shaderStages = shaderStages;
        uniformBuffers.add(uniformBuffer);
    }

}
