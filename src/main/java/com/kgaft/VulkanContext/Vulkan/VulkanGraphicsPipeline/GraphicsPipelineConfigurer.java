package com.kgaft.VulkanContext.Vulkan.VulkanGraphicsPipeline;

import com.kgaft.VulkanContext.DestroyableObject;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import java.util.List;

public class GraphicsPipelineConfigurer extends DestroyableObject {
    private long pipelineLayout;
    private long descriptorSetLayout;
    private VulkanDevice device;
    private VkVertexInputBindingDescription inputBindDesc;
    private List<VkVertexInputAttributeDescription> inputAttributeDesc;

    public GraphicsPipelineConfigurer(VulkanDevice device, PipelineBuilder builder) {
        this.device = device;
    }
}
