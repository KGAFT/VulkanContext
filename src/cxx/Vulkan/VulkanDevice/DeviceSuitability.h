//
// Created by daniil on 24.01.23.
//
#include <vulkan/vulkan.h>
#include <vector>
#include <string>
#include <set>

#pragma once
namespace DeviceSuitability {
    const std::vector<const char *> deviceExtensions = {VK_KHR_SWAPCHAIN_EXTENSION_NAME};
    struct SwapChainSupportDetails {
        VkSurfaceCapabilitiesKHR capabilities;
        std::vector<VkSurfaceFormatKHR> formats;
        std::vector<VkPresentModeKHR> presentModes;
    };

    struct QueueFamilyIndices {
        uint32_t graphicsFamily;
        uint32_t presentFamily;
        bool graphicsFamilyHasValue = false;
        bool presentFamilyHasValue = false;

        bool isComplete() { return graphicsFamilyHasValue && presentFamilyHasValue; }
    };


    DeviceSuitability::QueueFamilyIndices findQueueFamilies(VkPhysicalDevice device, VkSurfaceKHR surface);
    

    bool checkDeviceExtensionSupport(VkPhysicalDevice device);

    SwapChainSupportDetails querySwapChainSupport(VkPhysicalDevice device, VkSurfaceKHR surface);

    bool isDeviceSuitable(VkPhysicalDevice device, VkSurfaceKHR surface);

}