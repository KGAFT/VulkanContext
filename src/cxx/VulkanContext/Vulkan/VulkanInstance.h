//
// Created by kgaft on 7/26/23.
//
#pragma once

#include <vulkan/vulkan.h>
#include <vector>
#include "VulkanContext/Vulkan/VulkanLogger/VulkanLogger.h"
#include "VulkanContext/MemoryUtils/IDestroyableObject.h"

#define VK_LAYER_KHRONOS_validation "VK_LAYER_KHRONOS_validation"
#define VK_LAYER_KHRONOS_synchronization2 "VK_LAYER_KHRONOS_synchronization2"
#define VK_LAYER_KHRONOS_profiles "VK_LAYER_KHRONOS_profiles"
#define VK_LAYER_LUNARG_api_dump "VK_LAYER_LUNARG_api_dump"
#define VK_LAYER_LUNARG_gfxreconstruct "VK_LAYER_LUNARG_gfxreconstruct"
#define VK_LAYER_LUNARG_monitor "VK_LAYER_LUNARG_monitor"
#define VK_LAYER_LUNARG_screenshot "VK_LAYER_LUNARG_screenshot"

class VulkanInstanceBuilder;

class VulkanInstance : public IDestroyableObject{
private:
    static inline VulkanInstanceBuilder* builderInstance = nullptr;

public:
    static VulkanInstanceBuilder* getBuilderInstance();
    static VkResult createInstance(VulkanInstance** pOutput);
private:
    static void checkExtensions(std::vector<const char*>& toCheck);
    static void checkLayers(std::vector<const char*>& toCheck);
private:
    VkInstance instance;
    VulkanLogger* logger = nullptr;
public:
    void destroy() override;
};

class VulkanInstanceBuilder{
    friend class VulkanInstance;
private:
    VulkanInstanceBuilder();
    VkApplicationInfo appInfo = {};
    VkInstanceCreateInfo createInfo = {};
    std::vector<const char*> enabledExtensions;
    std::vector<const char*> enabledLayers;
    bool appInfoEnabled = false;
public:
    /**
     * @param apiVersion use one of vulkan api version definition, like VK_API_VERSION_1_3
     * @param appVersion use macro VK_MAKE_VERSION
     * @param engineVersion use macro VK_MAKE_VERSION
     */
    VulkanInstanceBuilder* setApplicationInfo(const char* appName, const char* engineName, uint32_t apiVersion, uint32_t appVersion, uint32_t engineVersion);
    /**
     * @param extension use one of vulkan defined extensions like VK_EXT_DEBUG_UTILS_EXTENSION_NAME
     */
    VulkanInstanceBuilder* addExtension(const char* extension);

    /**
     * @param layer use one defined in library layers, like VK_LAYER_KHRONOS_validation, or any another layer
     */
    VulkanInstanceBuilder* addLayer(const char* layer);

    VulkanInstanceBuilder* setInstanceFlags(VkInstanceCreateFlags flags);
private:
    void clear();
};