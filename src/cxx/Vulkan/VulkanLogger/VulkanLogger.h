#pragma once

#include <vulkan/vulkan.h>
#include <iostream>
#include <vector>
#include "IVulkanLoggerCallback.h"



class VulkanLogger {
    friend class VulkanInstance;

public:
    static const std::vector<const char *> validationLayers;

    static VkBool32 debugCallback(
            VkDebugUtilsMessageSeverityFlagBitsEXT messageSeverity,
            VkDebugUtilsMessageTypeFlagsEXT messageType,
            const VkDebugUtilsMessengerCallbackDataEXT *pCallbackData,
            void *pUserData);

private:
    static const char *translateSeverity(VkDebugUtilsMessageSeverityFlagBitsEXT severity);

    static const char *translateType(VkDebugUtilsMessageTypeFlagsEXT type);

private:
    static VkDebugUtilsMessengerEXT debugMessenger;

    static std::vector<IVulkanLoggerCallback *> rawCallbacks;
    static std::vector<IVulkanLoggerCallback *> translatedCallbacks;
    static std::vector<IVulkanLoggerCallback *> bothCallbacks;

    static void describeLogger(VkDebugUtilsMessengerCreateInfoEXT &createInfo, VkInstanceCreateInfo *instanceInfo);

    static VkResult CreateDebugUtilsMessengerEXT(
            VkInstance instance,
            VkDebugUtilsMessengerCreateInfoEXT *pCreateInfo,
            VkAllocationCallbacks *pAllocator,
            VkDebugUtilsMessengerEXT *pDebugMessenger);

public:
    static bool init(VkInstance instance);

    static void registerCallback(IVulkanLoggerCallback *callback);

    static void removeCallback(IVulkanLoggerCallback *callback);
};