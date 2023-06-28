package com.kgaft.VulkanContext.Vulkan.VulkanImage;

import com.kgaft.VulkanContext.DestroyableObject;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.vulkan.VK13.*;

public class VulkanCubeMapImage extends DestroyableObject {

    public static VulkanCubeMapImage createCubeMap(VulkanDevice device, CubeMapTextureInfo info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer[] imageData = new ByteBuffer[6];
            int[] width = new int[1];
            int[] height = new int[1];
            int[] numChannelsAmount = new int[1];

            imageData[0] = stbi_load(info.pathToFrontFace, width, height, numChannelsAmount, STBI_rgb_alpha);
            imageData[1] = stbi_load(info.pathToBackFace, width, height, numChannelsAmount, STBI_rgb_alpha);
            imageData[2] = stbi_load(info.pathToUpFace, width, height, numChannelsAmount, STBI_rgb_alpha);
            imageData[3] = stbi_load(info.pathToDownFace, width, height, numChannelsAmount, STBI_rgb_alpha);
            imageData[4] = stbi_load(info.pathToRightFace, width, height, numChannelsAmount, STBI_rgb_alpha);
            imageData[5] = stbi_load(info.pathToLeftFace, width, height, numChannelsAmount, STBI_rgb_alpha);

            int layerSize = width[0] * height[0] * 4;
            int imageSize = layerSize * 6;
            long[] image = new long[1];
            long[] imageMemory = new long[1];
            createImage(stack, device, image, imageMemory, VK_FORMAT_R8G8B8A8_SRGB, VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT, VK_IMAGE_TILING_OPTIMAL, width[0], height[0], 6, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

            long stagingBuffer;
            long stagingBufferMemory;
            long[] stageBuffer = device.createBuffer(imageSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
            stagingBuffer = stageBuffer[0];
            stagingBufferMemory = stageBuffer[1];

            PointerBuffer data = stack.callocPointer(1);

            for (int i = 0; i < 6; i++)
            {
                vkMapMemory(device.getDevice(), stagingBufferMemory, (long) layerSize * i, layerSize, 0, data);
                device.memcpy(data.getByteBuffer(0, imageSize), imageData[i], layerSize);
                vkUnmapMemory(device.getDevice(), stagingBufferMemory);
            }

            transitionImageLayout(stack, device, image[0], VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, 6);
            device.copyBufferToImage(stack, stagingBuffer, image[0], width[0], height[0], 6);
            transitionImageLayout(stack, device, image[0], VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_GENERAL, 6);
            vkDestroyBuffer(device.getDevice(), stagingBuffer, null);
            vkFreeMemory(device.getDevice(), stagingBufferMemory, null);
            for (int i = 0; i < 6; i++)
            {
                stbi_image_free(imageData[i]);
            }
            return new VulkanCubeMapImage(device, image[0], imageMemory[0], VK_FORMAT_R8G8B8A8_SRGB);
        }

    }

    private static void transitionImageLayout(MemoryStack stack, VulkanDevice device, long image, int oldLayout, int newLayout, int layerAmount) {
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
        barrier.subresourceRange().layerCount(layerAmount);

        int sourceStage;
        int destinationStage;

        if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {

            barrier.srcAccessMask(0);
            barrier.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);

            sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
            destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;

        } else if (oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {

            barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
            barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT);

            sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
            destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;

        } else {
            barrier.srcAccessMask(0);
            barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT);

            sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
            destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
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

    private static void createImage(MemoryStack stack, VulkanDevice device, long[] image, long[] imageMemory, int imageFormat, int usageFlags, int imageTilling, int width, int height, int amountOfImages, int properties) {
        VkImageCreateInfo imageInfo = VkImageCreateInfo.calloc(stack);
        imageInfo.sType$Default();
        imageInfo.flags(VK_IMAGE_CREATE_CUBE_COMPATIBLE_BIT);
        imageInfo.imageType(VK_IMAGE_TYPE_2D);
        imageInfo.extent().width(width);
        imageInfo.extent().height(height);
        imageInfo.extent().depth(1);
        imageInfo.mipLevels(1);
        imageInfo.arrayLayers(amountOfImages);
        imageInfo.format(imageFormat);
        imageInfo.tiling(imageTilling);
        imageInfo.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
        imageInfo.usage(usageFlags);
        imageInfo.samples(VK_SAMPLE_COUNT_1_BIT);
        imageInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
        device.createImageWithInfo(stack, imageInfo, properties, image, imageMemory);
    }

    private VulkanDevice device;
    private long image;
    private long imageMemory;
    private long imageView;
    private int format;

    public VulkanCubeMapImage(VulkanDevice device, long image, long imageMemory, int format) {
        this.device = device;
        this.image = image;
        this.imageMemory = imageMemory;
        this.format = format;
        VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.calloc();
        createInfo.sType$Default();
        createInfo.viewType(VK_IMAGE_VIEW_TYPE_CUBE);
        createInfo.format(format);
        createInfo.components(VkComponentMapping.calloc());
        createInfo.components().set(VK_COMPONENT_SWIZZLE_R, VK_COMPONENT_SWIZZLE_G, VK_COMPONENT_SWIZZLE_B, VK_COMPONENT_SWIZZLE_A);
        createInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
        createInfo.subresourceRange().baseMipLevel(0);
        createInfo.subresourceRange().baseArrayLayer(0);
        createInfo.subresourceRange().layerCount(6);
        createInfo.subresourceRange().levelCount(1);
        createInfo.image(image);
        long[] view = new long[1];
        vkCreateImageView(device.getDevice(), createInfo, null, view);
        this.imageView = view[0];
        createInfo.components().free();
        createInfo.free();
    }

    public long getImageView() {
        return imageView;
    }

    @Override
    public void destroy() {
        vkDestroyImageView(device.getDevice(), imageView, null);
        if (imageMemory != VK_NULL_HANDLE)
        {
            vkFreeMemory(device.getDevice(), imageMemory, null);
        }
        vkDestroyImage(device.getDevice(), image, null);
        super.destroy();
    }
}
