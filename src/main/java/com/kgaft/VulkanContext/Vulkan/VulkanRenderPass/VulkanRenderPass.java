package com.kgaft.VulkanContext.Vulkan.VulkanRenderPass;

import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import com.kgaft.VulkanContext.Vulkan.VulkanImage.VulkanImage;

import java.util.ArrayList;
import java.util.List;

public class VulkanRenderPass {
    private VulkanDevice device;
    private long renderPass;
    private List<VulkanFrameBuffer> frameBuffers = new ArrayList<>();
    private List<VulkanImage> depthImages = new ArrayList<>();

    
}
