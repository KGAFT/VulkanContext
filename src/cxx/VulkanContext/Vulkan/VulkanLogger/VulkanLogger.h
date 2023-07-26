#pragma once

#include <vulkan/vulkan.h>
#include <iostream>
#include <vector>
#include <VulkanContext/MemoryUtils/IDestroyableObject.h>
#include "IVulkanLoggerCallback.h"


class VulkanLogger : public IDestroyableObject{
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
    static std::vector<VulkanLogger*> loggers;

private:
    VkDebugUtilsMessengerEXT debugMessenger = NULL;
    bool initialized = false;
    std::vector<IVulkanLoggerCallback *> rawCallbacks;
    std::vector<IVulkanLoggerCallback *> translatedCallbacks;
    std::vector<IVulkanLoggerCallback *> bothCallbacks;
    VkInstance instance;
private:
    static void describeLogger(VkDebugUtilsMessengerCreateInfoEXT &createInfo);

    void callback(
            VkDebugUtilsMessageSeverityFlagBitsEXT messageSeverity,
            VkDebugUtilsMessageTypeFlagsEXT messageType,
            const VkDebugUtilsMessengerCallbackDataEXT *pCallbackData,
            void *pUserData);

    VkResult CreateDebugUtilsMessengerEXT(
            VkInstance instance,
            VkDebugUtilsMessengerCreateInfoEXT *pCreateInfo,
            VkAllocationCallbacks *pAllocator,
            VkDebugUtilsMessengerEXT *pDebugMessenger);

public:
    bool init(VkInstance instance);

    void destroy() override;

    void registerCallback(IVulkanLoggerCallback *callback);

    void removeCallback(IVulkanLoggerCallback *callback);
};