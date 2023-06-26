package com.kgaft.VulkanContext.Vulkan.VulkanGraphicsPipeline;

import com.kgaft.VulkanContext.DestroyableObject;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import com.kgaft.VulkanContext.Vulkan.VulkanRenderingPipeline.VulkanRenderPass;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;

import static org.lwjgl.vulkan.VK10.*;

public class VulkanGraphicsPipeline extends DestroyableObject {
    private static PipelineConfigInfo configInfo;
    private long graphicsPipeline;
    private VulkanDevice device;
    private GraphicsPipelineConfigurer configurer;
    private VulkanShader shader;
    private VulkanRenderPass renderPass;

    public VulkanGraphicsPipeline(VulkanDevice device, GraphicsPipelineConfigurer configurer, VulkanShader shader,
                                  VulkanRenderPass renderPass, int width, int height, int attachmentCount,
                                  boolean alphaBlending, int culling) {
        this.device = device;
        this.configurer = configurer;
        this.shader = shader;
        this.renderPass = renderPass;
        configInfo = new PipelineConfigInfo();
        PipelineConfigInfo.defaultPipelineConfigInfo(configInfo, width, height, attachmentCount, alphaBlending, true, culling);
        create();
    }

    public void recreate(int width, int height, int attachmentCount, boolean alphaBlending, int culling, VulkanRenderPass renderPass){
        this.renderPass = renderPass;
        destroy();
        super.destroyed = false;
        PipelineConfigInfo.defaultPipelineConfigInfo(configInfo, width, height, attachmentCount, alphaBlending, true, culling);
        create();
    }

    @Override
    public void destroy() {
        vkDestroyPipeline(device.getDevice(), graphicsPipeline, null);
        super.destroy();
    }

    private void create() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
            vertexInputInfo.sType$Default();
            vertexInputInfo.pVertexAttributeDescriptions(configurer.inputAttributeDesc);
            vertexInputInfo.pVertexBindingDescriptions(configurer.inputBindDesc);

            VkPipelineViewportStateCreateInfo viewportInfo = VkPipelineViewportStateCreateInfo.calloc(stack);
            viewportInfo.sType$Default();
            viewportInfo.pViewports(configInfo.viewport);
            viewportInfo.pScissors(configInfo.scissor);

            VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
            pipelineInfo.sType$Default();
            pipelineInfo.pStages(shader.getStages());
            pipelineInfo.pVertexInputState(vertexInputInfo);
            pipelineInfo.pInputAssemblyState(configInfo.inputAssemblyInfo);
            pipelineInfo.pViewportState(viewportInfo);
            pipelineInfo.pRasterizationState(configInfo.rasterizationInfo);
            pipelineInfo.pMultisampleState(configInfo.multisampleInfo);
            pipelineInfo.pColorBlendState(configInfo.colorBlendInfo);
            pipelineInfo.pDepthStencilState(configInfo.depthStencilInfo);
            pipelineInfo.pDynamicState(null);

            pipelineInfo.layout(configurer.pipelineLayout);
            pipelineInfo.renderPass(renderPass.getRenderPass());
            pipelineInfo.subpass(0);

            pipelineInfo.basePipelineIndex(-1);
            pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
            long[] res = new long[1];
            if (vkCreateGraphicsPipelines(
                    device.getDevice(),
                    VK_NULL_HANDLE,
                    pipelineInfo, null, res) != VK_SUCCESS) {
                throw new RuntimeException("failed to create graphics pipeline");
            }
            this.graphicsPipeline = res[0];
        }
    }
}
