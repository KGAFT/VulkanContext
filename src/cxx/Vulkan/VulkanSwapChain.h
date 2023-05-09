//
// Created by daniil on 25.01.23.
//

#pragma once

#include <vulkan/vulkan.h>
#include "VulkanDevice/VulkanDevice.h"

class VulkanSwapChain
{
    friend class VulkanSyncManager;

public:
    VulkanSwapChain(VulkanDevice *device, unsigned int width, unsigned int height);

private:
    std::vector<VkImage> swapChainImages;
    std::vector<VkImageView> swapChainImageViews;
    unsigned int width;
    unsigned int height;
    VulkanDevice *device;
    VkSwapchainKHR swapChain;
    VkFormat swapChainImageFormat;
    VkExtent2D swapChainExtent;

public:
    VkFormat getSwapChainImageFormat();

    void destroy();

    void recreate(unsigned int width, unsigned int height);

    ~VulkanSwapChain();

    std::vector<VkImageView> &getSwapChainImageViews();

private:
    void createSwapChain();

    void createImageViews();

    VkSurfaceFormatKHR chooseSwapSurfaceFormat(
        const std::vector<VkSurfaceFormatKHR> &availableFormats);

    VkPresentModeKHR chooseSwapPresentMode(const std::vector<VkPresentModeKHR> &availablePresentModes);

    VkExtent2D chooseSwapExtent(const VkSurfaceCapabilitiesKHR &capabilities);
};
