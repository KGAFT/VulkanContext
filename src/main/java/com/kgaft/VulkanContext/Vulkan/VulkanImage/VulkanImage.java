package com.kgaft.VulkanContext.Vulkan.VulkanImage;

import org.lwjgl.vulkan.*;

import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanQueue;

import org.lwjgl.system.MemoryStack;

import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_TRANSFER_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindImageMemory;
import static org.lwjgl.vulkan.VK10.vkCmdPipelineBarrier;
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
    private int accessMask = 0;
    private int shaderStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;

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

    public VulkanDevice getDevice() {
        return device;
    }



    public long getImage() {
        return image;
    }



    public long getImageMemory() {
        return imageMemory;
    }



    public int getImageLayout() {
        return imageLayout;
    }



    public int getImageTiling() {
        return imageTiling;
    }



    public int getSharingMode() {
        return sharingMode;
    }



    public int getSamples() {
        return samples;
    }

    public void changeLayout(int targetLayout, int targetAccessMask, int targetShaderStage, VulkanQueue queue){
        try(MemoryStack stack = MemoryStack.stackPush()){
            changeLayout(stack, targetLayout, targetAccessMask, targetShaderStage, queue);
        }
    }

    public void changeLayout(MemoryStack stack, int targetLayout, int targetAccessMask, int targetShaderStage, VulkanQueue queue){
        VkCommandBuffer cmd = queue.beginSingleTimeCommands(stack);
        changeLayout(stack, targetLayout, targetAccessMask, targetShaderStage, cmd);
        queue.endSingleTimeCommands(cmd, stack);
    }

    public void changeLayout(MemoryStack stack, int targetLayout, int targetAccessMask, int targetShaderStage, VkCommandBuffer cmd){
        VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack);
        barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
        barrier.oldLayout(this.imageLayout);
        barrier.newLayout(targetLayout);
        barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        barrier.image(image);
        barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
        barrier.subresourceRange().baseMipLevel(0);
        barrier.subresourceRange().levelCount(1);
        barrier.subresourceRange().baseArrayLayer(0);
        barrier.subresourceRange().layerCount(1);

        barrier.srcAccessMask(this.accessMask);
        barrier.dstAccessMask(targetAccessMask);


        vkCmdPipelineBarrier(cmd,
                this.shaderStage, targetShaderStage,
                0,
                null,
                null,
                barrier);
        this.accessMask = targetAccessMask;
        this.imageLayout = targetLayout;
        this.shaderStage = targetShaderStage;        

    }


    public int getAccessMask() {
        return accessMask;
    }



    public int getShaderStage() {
        return shaderStage;
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
