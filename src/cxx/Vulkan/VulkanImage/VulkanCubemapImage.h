#pragma once

#include "External/stb/stb_image.h"
#include "../VulkanDevice/VulkanDevice.h"
#include "VulkanImage.h"
#include <cstdint>
struct CubemapTextureInfo
{
    const char *pathToFrontFace;
    const char *pathToBackFace;
    const char *pathToUpFace;
    const char *pathToDownFace;
    const char *pathToRightFace;
    const char *pathToLeftFace;
};

class VulkanCubemapImage
{
public:
    static VulkanCubemapImage *createCubemap(VulkanDevice *device, CubemapTextureInfo &info);

private:
    static void transitionImageLayout(VkImage target, VkImageLayout oldLayout, VkImageLayout newLayout, VulkanDevice *device, int layerAmount);

    static void createImage(VulkanDevice *device, VkImage &target, VkDeviceMemory &imageMemory, VkFormat imageFormat, VkImageUsageFlags usageFlags, VkImageTiling imageTilling, int width, int height, int amountOfImages, VkMemoryPropertyFlags properties);
    VulkanCubemapImage(VulkanDevice *device, VkImage image, VkDeviceMemory imageMemory, VkFormat imageFormat, int layerCount);

private:
    VulkanDevice *device;
    VkImage image;
    VkImageView view;
    VkDeviceMemory imageMemory;
    VkFormat imageFormat;
    bool destroyed = false;
public:
    VkImageView getImageView();

    void destroy();

    ~VulkanCubemapImage();
};
