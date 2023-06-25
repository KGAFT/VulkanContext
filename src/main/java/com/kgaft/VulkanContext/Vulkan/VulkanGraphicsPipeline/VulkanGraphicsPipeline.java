package com.kgaft.VulkanContext.Vulkan.VulkanGraphicsPipeline;

import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import com.kgaft.VulkanContext.Vulkan.VulkanRenderingPipeline.VulkanRenderPass;

public class VulkanGraphicsPipeline {
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
    }
}
