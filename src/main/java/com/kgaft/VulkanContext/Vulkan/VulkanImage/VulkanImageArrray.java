package com.kgaft.VulkanContext.Vulkan.VulkanImage;

import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.vulkan.VK13.*;

public class VulkanImageArrray {
    private VulkanDevice device;
    private long image;
    private long imageMemory;
    private long imageView;
    private int imageLayout;
    private int imageTiling;
    private int imageFormat;
    private int sharingMode;
    private int samples;
    private int accessMask = 0;
    private int shaderStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
}
