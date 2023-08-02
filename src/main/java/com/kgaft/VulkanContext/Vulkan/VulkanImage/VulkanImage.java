package com.kgaft.VulkanContext.Vulkan.VulkanImage;

import org.lwjgl.vulkan.*;

import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;

import org.lwjgl.system.MemoryStack;

import static org.lwjgl.vulkan.VK10.VK_IMAGE_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindImageMemory;
import static org.lwjgl.vulkan.VK10.vkCreateImage;
import static org.lwjgl.vulkan.VK10.vkGetImageMemoryRequirements;
import static org.lwjgl.vulkan.VK13.*;

public class VulkanImage {
    private VulkanDevice device;
    private long image;
    private long imageMemory;
    private int imageLayout;
    private int imageTiling;
    private int sharingMode;
    private int samples;
    public VulkanImage(VulkanDevice device, VulkanImageBuilder builder) throws BuilderNotPopulatedException {
        this.device = device;
        builder.checkBuilder();
        
        this.imageLayout = builder.getInitialLayout();
        this.imageTiling = builder.getTiling();
        this.sharingMode = builder.getSharingMode();
        this.samples = builder.getSamples();

        try(MemoryStack stack = MemoryStack.stackPush()){
            this.image = createImage(stack, builder);
            this.imageMemory = createImageMemory(stack, device, builder.getImageMemoryProperties(), image);
        }
        
    }

    private long createImage(MemoryStack stack, VulkanImageBuilder builder) {

        VkImageCreateInfo createInfo = VkImageCreateInfo.calloc(stack);
        createInfo.sType$Default();
        createInfo.imageType(VK_IMAGE_TYPE_2D);
        createInfo.extent().width(builder.getWidth());
        createInfo.extent().height(builder.getHeight());
        createInfo.extent().depth(1);
        createInfo.mipLevels(builder.getMipLevels());
        createInfo.arrayLayers(1);
        createInfo.format(builder.getFormat());
        createInfo.tiling(builder.getTiling());
        createInfo.initialLayout(builder.getInitialLayout());
        createInfo.samples(builder.getSamples());
        createInfo.sharingMode(builder.getSharingMode());
        long[] result = new long[1];
        if (vkCreateImage(device.getDevice(), createInfo, null, result) != VK_SUCCESS) {
            throw new RuntimeException("Failed to create image");
        }
        return result[0];

    }

    private long createImageMemory(MemoryStack stack, VulkanDevice device, int properties, long image) {
        VkMemoryRequirements memRequirements = VkMemoryRequirements.calloc(stack);
        vkGetImageMemoryRequirements(device.getDevice(), image, memRequirements);

        VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack);
        allocInfo.sType$Default();
        allocInfo.allocationSize(memRequirements.size());
        allocInfo.memoryTypeIndex(device.findMemoryType(memRequirements.memoryTypeBits(), properties, stack));
        long[] res = new long[1];
        if (vkAllocateMemory(device.getDevice(), allocInfo, null, res) != VK_SUCCESS) {
            throw new RuntimeException("failed to allocate image memory!");
        }

        vkBindImageMemory(device.getDevice(), image, res[0], 0);
        return res[0];
    }
}
