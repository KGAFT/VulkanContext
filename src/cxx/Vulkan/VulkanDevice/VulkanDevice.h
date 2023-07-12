//
// Created by daniil on 24.01.23.
//

#pragma once

#include <vulkan/vulkan.h>
#include <map>
#include "DeviceSuitability.h"
#include <stdexcept>
#include "../VulkanLogger/VulkanLogger.h"
class VulkanDevice
{
public:
    static std::map<VkPhysicalDevice, VkPhysicalDeviceProperties>
    enumerateSupportedDevices(VkInstance instance, VkSurfaceKHR renderSurface, bool acquireCompute);

public:
    VulkanDevice(VkPhysicalDevice deviceToCreate, VkSurfaceKHR renderSurface, VkInstance vkInstance, bool logDevice, bool acquireCompute);

private:
    VkPhysicalDevice deviceToCreate;
    VkDevice device;
    VkQueue graphicsQueue;
    VkQueue presentQueue;
    VkQueue computeFamily = VK_NULL_HANDLE;
    VkInstance vkInstance;
    VkCommandPool commandPool;
    VkSampleCountFlags sampleCount;
    VkSurfaceKHR renderSurface;

public:
    void destroy();

    ~VulkanDevice();

    void createImageWithInfo(
        const VkImageCreateInfo &imageInfo,
        VkMemoryPropertyFlags properties,
        VkImage &image,
        VkDeviceMemory &imageMemory);

    void copyBufferToImage(VkBuffer buffer, VkImage image, uint32_t width, uint32_t height, int layerCount);

    VkFormat findSupportedFormat(
        const std::vector<VkFormat> &candidates, VkImageTiling tiling, VkFormatFeatureFlags features);

    unsigned int findMemoryType(uint32_t typeFilter, VkMemoryPropertyFlags properties);

    VkSampleCountFlagBits getSampleCountBit();

    void createImage(unsigned int width, unsigned int height, VkFormat format, VkImageTiling tiling,
                     VkImageUsageFlags usage, VkImage &image, bool isFrameBuffer);

    VkPhysicalDevice getDeviceToCreate();

    VkDevice getDevice();

    VkImageView createImageView(VkImage image, VkFormat format);

    VkQueue getGraphicsQueue();

    VkQueue getPresentQueue();

    VkQueue getComputeQueue();

    VkInstance getVkInstance();

    VkCommandPool getCommandPool();

    void createBuffer(
        VkDeviceSize size,
        VkBufferUsageFlags usage,
        VkMemoryPropertyFlags properties,
        VkBuffer &buffer,
        VkDeviceMemory &bufferMemory);

    void copyBuffer(VkBuffer srcBuffer, VkBuffer dstBuffer, VkDeviceSize size);

    VkCommandBuffer beginSingleTimeCommands();

    void endSingleTimeCommands(VkCommandBuffer commandBuffer);

    VkSurfaceKHR getRenderSurface();

private:
    void createLogicalDevice(bool logDevice, bool acquireCompute);

    void createCommandPool();
};
