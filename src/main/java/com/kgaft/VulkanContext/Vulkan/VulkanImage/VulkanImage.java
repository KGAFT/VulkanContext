package com.kgaft.VulkanContext.Vulkan.VulkanImage;

import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.vulkan.VK13.*;
public class VulkanImage {

    public static VulkanImage createImage(VulkanDevice device, int width, int height, int format, int imageLayout){
        try(MemoryStack stack = MemoryStack.stackPush()){
            long image = device.createImage(width, height, format, VK_IMAGE_TILING_OPTIMAL, VK_IMAGE_USAGE_SAMPLED_BIT
                    | VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT , true);
            long imageMemory = createImageMemory(stack, device, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, image);
            transitionImageLayout(stack, device, image, format, VK_IMAGE_LAYOUT_UNDEFINED, imageLayout);
            return new VulkanImage(device, image, imageMemory, format, imageLayout, width, height);
        }
    }

    public static VulkanImage loadTexture(String pathToTexture, VulkanDevice device){
        int[] imageWidth = new int[1];
        int[] imageHeight = new int[1];
        int[] imageChannels = new int[1];
        ByteBuffer imageData = stbi_load(pathToTexture, imageWidth, imageHeight, imageChannels, STBI_rgb_alpha);
        VulkanImage image = loadBinTexture(device, imageData, imageWidth[0], imageHeight[0], imageChannels[0]);
        stbi_image_free(imageData);
        return image;
    }

    public static VulkanImage loadBinTexture(VulkanDevice device, ByteBuffer imageData,
                                 int imageWidth, int imageHeight, int numChannelsAmount){
        try(MemoryStack stack = MemoryStack.stackPush()){
            int imageSize = imageWidth * imageHeight * 4;

            long stagingBuffer;
            long stagingBufferMemory;
            long[] buffers = device.createBuffer(imageSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
            stagingBuffer = buffers[0];
            stagingBufferMemory = buffers[1];

            PointerBuffer data = stack.callocPointer(1);
            vkMapMemory(device.getDevice(), stagingBufferMemory, 0, imageSize, 0, data);
            device.memcpy(data.getByteBuffer(0, imageSize), imageData, imageSize);
            vkUnmapMemory(device.getDevice(), stagingBufferMemory);

            long image;
            long imageMemory;
            image = device.createImage(imageWidth, imageHeight, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_TILING_OPTIMAL, VK_IMAGE_USAGE_SAMPLED_BIT
                    | VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT , true);

            imageMemory = createImageMemory(stack, device, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, image);
            transitionImageLayout(stack, device, image, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
            device.copyBufferToImage(stack, stagingBuffer, image, imageWidth, imageHeight, 1);
            transitionImageLayout(stack, device, image, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

            vkDestroyBuffer(device.getDevice(), stagingBuffer, null);
            vkFreeMemory(device.getDevice(), stagingBufferMemory, null);

            return new VulkanImage(device, image, imageMemory, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, imageWidth, imageHeight);
        }
    }

    private static long createImageMemory(MemoryStack stack, VulkanDevice device, int properties, long image){
        VkMemoryRequirements memRequirements = VkMemoryRequirements.calloc(stack);
        vkGetImageMemoryRequirements(device.getDevice(), image, memRequirements);

        VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack);
        allocInfo.sType$Default();
        allocInfo.allocationSize(memRequirements.size());
        allocInfo.memoryTypeIndex(device.findMemoryType(memRequirements.memoryTypeBits(), properties, stack));
        long[] res = new long[1];
        if (vkAllocateMemory(device.getDevice(), allocInfo, null, res) != VK_SUCCESS)
        {
            throw new RuntimeException("failed to allocate image memory!");
        }

        vkBindImageMemory(device.getDevice(), image, res[0], 0);
        return res[0];
    }

    private static void transitionImageLayout(MemoryStack stack, VulkanDevice device, long image, int format, int oldLayout, int newLayout) {
            VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack);
            barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
            barrier.oldLayout(oldLayout);
            barrier.newLayout(newLayout);
            barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
            barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
            barrier.image(image);
            barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            barrier.subresourceRange().baseMipLevel(0);
            barrier.subresourceRange().levelCount(1);
            barrier.subresourceRange().baseArrayLayer(0);
            barrier.subresourceRange().layerCount(1);

            int sourceStage;
            int destinationStage;

            if(oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {

                barrier.srcAccessMask(0);
                barrier.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);

                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;

            } else if(oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {

                barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

                sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
                destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;

            } else {
                throw new IllegalArgumentException("Unsupported layout transition");
            }

            VkCommandBuffer commandBuffer = device.beginSingleTimeCommands();

            vkCmdPipelineBarrier(commandBuffer,
                    sourceStage, destinationStage,
                    0,
                    null,
                    null,
                    barrier);

            device.endSingleTimeCommands(commandBuffer);

    }

    private VulkanDevice device;
    private long image;
    private long imageMemory;
    private long imageView;
    private int format;
    private int imageLayout;
    private int width;
    private int height;
    private VkImageCopy imageCopyRegion;
    private VkImageSubresourceRange imageSubresourceRange;

    public VulkanImage(VulkanDevice device, long image, long imageMemory, int format, int imageLayout, int width, int height) {
        this.device = device;
        this.image = image;
        this.imageMemory = imageMemory;
        this.format = format;
        this.imageLayout = imageLayout;
        this.width = width;
        this.height = height;
        this.imageView = device.createImageView(image, format);
    }
}
