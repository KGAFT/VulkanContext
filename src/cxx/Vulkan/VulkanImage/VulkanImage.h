//
// Created by KGAFT on 3/17/2023.
//
#pragma once

#include <cstring>
#include <vulkan/vulkan.h>
#include "../VulkanDevice/VulkanDevice.h"
#include "../../External/stb_image.h"


class VulkanImage {
public:
    static VulkanImage *createImage(VulkanDevice *device, unsigned int width, unsigned int height) {
        VkImage image;
        device->createImage(width, height, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_TILING_OPTIMAL,
                            VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
                            image, true);
        VkDeviceMemory imageMemory;
        createImageMemory(device, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, imageMemory, image);
        transitionImageLayout(device, image, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_UNDEFINED,
                              VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        return new VulkanImage(image, device, imageMemory, VK_FORMAT_R8G8B8A8_SRGB, width, height);
    }

    static VulkanImage *loadTexture(const char *pathToTexture, VulkanDevice *device) {
        int imageWidth, imageHeight, imageChannels;
        stbi_uc *imageData = stbi_load(pathToTexture, &imageWidth, &imageHeight, &imageChannels, STBI_rgb_alpha);

        VulkanImage *image = loadBinTexture(device, reinterpret_cast<const char *>(imageData), imageWidth, imageHeight,
                                            imageChannels);
        stbi_image_free(imageData);
        return image;
    }

    static VulkanImage *loadBinTexture(VulkanDevice *device, const char *imageData, int imageWidth, int imageHeight,
                                       int numChannelsAmount) {
        VkDeviceSize imageSize = imageWidth * imageHeight * 4;

        if (!imageData) {
            throw std::runtime_error("failed to load texture image!");
        }

        VkBuffer stagingBuffer;
        VkDeviceMemory stagingBufferMemory;
        device->createBuffer(imageSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                             VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBuffer,
                             stagingBufferMemory);

        void *data;
        vkMapMemory(device->getDevice(), stagingBufferMemory, 0, imageSize, 0, &data);
        memcpy(data, imageData, static_cast<size_t>(imageSize));
        vkUnmapMemory(device->getDevice(), stagingBufferMemory);


        VkImage image;
        VkDeviceMemory imageMemory;
        device->createImage(imageWidth, imageHeight, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_TILING_OPTIMAL,
                            VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                            image, false);

        createImageMemory(device, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, imageMemory, image);
        transitionImageLayout(device, image, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_UNDEFINED,
                              VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
        copyBufferToImage(device, stagingBuffer, image, imageWidth, imageHeight);
        transitionImageLayout(device, image, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                              VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

        vkDestroyBuffer(device->getDevice(), stagingBuffer, nullptr);
        vkFreeMemory(device->getDevice(), stagingBufferMemory, nullptr);

        return new VulkanImage(image, device, imageMemory, VK_FORMAT_R8G8B8A8_SRGB, imageWidth, imageHeight);
    }

    static void createImageMemory(VulkanDevice *device, VkMemoryPropertyFlags properties,
                                  VkDeviceMemory &imageMemory, VkImage &image) {
        VkMemoryRequirements memRequirements;
        vkGetImageMemoryRequirements(device->getDevice(), image, &memRequirements);

        VkMemoryAllocateInfo allocInfo{};
        allocInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
        allocInfo.allocationSize = memRequirements.size;
        allocInfo.memoryTypeIndex = device->findMemoryType(memRequirements.memoryTypeBits, properties);

        if (vkAllocateMemory(device->getDevice(), &allocInfo, nullptr, &imageMemory) != VK_SUCCESS) {
            throw std::runtime_error("failed to allocate image memory!");
        }

        vkBindImageMemory(device->getDevice(), image, imageMemory, 0);
    }

    static void transitionImageLayout(VulkanDevice *device, VkImage image, VkFormat format, VkImageLayout oldLayout,
                                      VkImageLayout newLayout) {
        VkCommandBuffer commandBuffer = device->beginSingleTimeCommands();

        VkImageMemoryBarrier barrier{};
        barrier.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
        barrier.oldLayout = oldLayout;
        barrier.newLayout = newLayout;
        barrier.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
        barrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
        barrier.image = image;
        barrier.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
        barrier.subresourceRange.baseMipLevel = 0;
        barrier.subresourceRange.levelCount = 1;
        barrier.subresourceRange.baseArrayLayer = 0;
        barrier.subresourceRange.layerCount = 1;

        VkPipelineStageFlags sourceStage;
        VkPipelineStageFlags destinationStage;

        if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
            barrier.srcAccessMask = 0;
            barrier.dstAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;

            sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
            destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
        } else if (oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL &&
                   newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
            barrier.srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
            barrier.dstAccessMask = VK_ACCESS_SHADER_READ_BIT;

            sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
            destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
        } else if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
            barrier.srcAccessMask = 0;
            barrier.dstAccessMask = VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT;

            sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
            destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
        } else {
            throw std::invalid_argument("unsupported layout transition!");
        }

        vkCmdPipelineBarrier(
                commandBuffer,
                sourceStage, destinationStage,
                0,
                0, nullptr,
                0, nullptr,
                1, &barrier
        );

        device->endSingleTimeCommands(commandBuffer);
    }

    static void
    copyBufferToImage(VulkanDevice *device, VkBuffer buffer, VkImage image, uint32_t width, uint32_t height) {
        VkCommandBuffer commandBuffer = device->beginSingleTimeCommands();

        VkBufferImageCopy region{};
        region.bufferOffset = 0;
        region.bufferRowLength = 0;
        region.bufferImageHeight = 0;
        region.imageSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
        region.imageSubresource.mipLevel = 0;
        region.imageSubresource.baseArrayLayer = 0;
        region.imageSubresource.layerCount = 1;
        region.imageOffset = {0, 0, 0};
        region.imageExtent = {
                width,
                height,
                1
        };

        vkCmdCopyBufferToImage(commandBuffer, buffer, image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, 1, &region);

        device->endSingleTimeCommands(commandBuffer);
    }

private:
    VkImage image;
    VulkanDevice *device;
    VkImageView view;
    VkDeviceMemory imageMemory = VK_NULL_HANDLE;
    VkFormat format;
    int width;
    int height;
    bool destroyed = false;
public:
    VulkanImage(VkImage image, VulkanDevice *device,
                VkDeviceMemory imageMemory, VkFormat format, int width, int height) : image(image),
                                                                                      device(device),
                                                                                      view(view),
                                                                                      imageMemory(imageMemory),
                                                                                      format(format),
                                                                                      width(width),
                                                                                      height(height) {
        view = device->createImageView(image, VK_FORMAT_R8G8B8A8_SRGB);
    }

    VkImage getImage() {
        return image;
    }

    VulkanDevice *getDevice() {
        return device;
    }

    VkImageView getView() {
        return view;
    }

    void copyToImage(VulkanImage *target) {
        VkCommandBuffer cmd = device->beginSingleTimeCommands();
        VkImageCopy imageCopyRegion{};
        imageCopyRegion.srcSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
        imageCopyRegion.srcSubresource.layerCount = 1;
        imageCopyRegion.dstSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
        imageCopyRegion.dstSubresource.layerCount = 1;
        imageCopyRegion.extent.width = width;
        imageCopyRegion.extent.height = height;
        imageCopyRegion.extent.depth = 1;
        vkCmdCopyImage(
                cmd,
                this->image, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                target->image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                1,
                &imageCopyRegion);
        device->endSingleTimeCommands(cmd);
    }

    VkFormat getFormat() {
        return format;
    }

    void destroy() {
        if (!destroyed) {
            vkDestroyImageView(device->getDevice(), view, nullptr);
            if (imageMemory != VK_NULL_HANDLE) {
                vkFreeMemory(device->getDevice(), imageMemory, nullptr);
            }
            vkDestroyImage(device->getDevice(), image, nullptr);
            destroyed = true;
        }
    }

    ~VulkanImage() {
        destroy();
    }
};