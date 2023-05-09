//
// Created by KGAFT on 3/17/2023.
//
#pragma once

#include <cstring>
#include <vulkan/vulkan.h>
#include "../VulkanDevice/VulkanDevice.h"
#include "../../External/stb_image.h"

class VulkanImage
{
public:
    static VulkanImage *createImage(VulkanDevice *device, unsigned int width, unsigned int height);

    static VulkanImage *createImageWithFormat(VulkanDevice *device, unsigned int width, unsigned int height, VkFormat format);

    static VulkanImage *loadTexture(const char *pathToTexture, VulkanDevice *device);

    static VulkanImage *loadBinTexture(VulkanDevice *device, const char *imageData, int imageWidth, int imageHeight,
                                       int numChannelsAmount);

    static void createImageMemory(VulkanDevice *device, VkMemoryPropertyFlags properties,
                                  VkDeviceMemory &imageMemory, VkImage &image);

    static void transitionImageLayout(VulkanDevice *device, VkImage image, VkFormat format, VkImageLayout oldLayout,
                                      VkImageLayout newLayout);

public:
    VulkanImage(VkImage image, VulkanDevice *device,
                VkDeviceMemory imageMemory, VkFormat format, int width, int height);

private:
    VkImage image;
    VulkanDevice *device;
    VkImageView view;
    VkDeviceMemory imageMemory = VK_NULL_HANDLE;
    VkFormat format;
    int width;
    int height;
    bool destroyed = false;
    VkImageCopy imageCopyRegion{};
    VkClearColorValue clearColorValue{};
    VkImageSubresourceRange imageSubresourceRange{};
public:
    VkImage getImage();

    VulkanDevice *getDevice();

    VkImageView getView();

    void copyToImage(VulkanImage *target);

    void copyToImage(VulkanImage *target, VkCommandBuffer cmd);

    void copyFromImage(VkImage image, VkCommandBuffer cmd);

    void copyFromImage(VkImage image);

    void clearImage(float r, float g, float b, float a);

    void clearImage(float r, float g, float b, float a, VkCommandBuffer cmd);

    VkFormat getFormat();
    
    void resize(int width, int height);

    void destroy();

    ~VulkanImage();
};
