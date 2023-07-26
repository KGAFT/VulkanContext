//
// Created by kgaft on 7/26/23.
//


#include "VulkanInstance.h"
#include <cstring>
#include "VulkanContext/Exceptions/BuilderNotPopulatedException.h"
#include "VulkanContext/Exceptions/NotSupportedExtensionException.h"
#include "VulkanContext/Exceptions/NotSupportedLayerException.h"

VulkanInstanceBuilder *VulkanInstance::getBuilderInstance() {
    if (builderInstance == nullptr) {
        builderInstance = new VulkanInstanceBuilder();
    }

    return builderInstance;
}

VkResult VulkanInstance::createInstance(VulkanInstance **pOutput) {
    if (builderInstance->appInfoEnabled) {
        if(!builderInstance->enabledExtensions.empty()){
            checkExtensions(builderInstance->enabledExtensions);
        }
        if(!builderInstance->enabledLayers.empty()){
            checkLayers(builderInstance->enabledLayers);
        }

        VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = {};
        if (!builderInstance->enabledLayers.empty()) {
            VulkanLogger::describeLogger(debugCreateInfo);
            builderInstance->createInfo.pNext = &debugCreateInfo;
        }
        VkInstance instance;
        VkResult res = vkCreateInstance(&builderInstance->createInfo, nullptr, &instance);
        if(res!=VK_SUCCESS){
            return res;
        }
        if(!builderInstance->enabledLayers.empty()){
            (*pOutput)->logger = new VulkanLogger;
            (*pOutput)->logger->init(instance);
        }
        *pOutput = new VulkanInstance();
        (*pOutput)->instance = instance;
        return res;
    }
    throw BuilderNotPopulatedException("Error, you have not specified application info");
}

void VulkanInstance::checkExtensions(std::vector<const char *> &toCheck) {
    uint32_t extensionCount = 0;
    vkEnumerateInstanceExtensionProperties(nullptr, &extensionCount, nullptr);
    std::vector<VkExtensionProperties> extensions(extensionCount);
    vkEnumerateInstanceExtensionProperties(nullptr, &extensionCount, extensions.data());
    for (auto &el : toCheck) {
        bool found = false;
        for (auto &cel : extensions) {
            if (!strcmp(cel.extensionName, el)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw NotSupportedExtensionException(el);
        }
    }
}

void VulkanInstance::checkLayers(std::vector<const char *> &toCheck) {
    uint32_t layerCount = 0;
    vkEnumerateInstanceLayerProperties(&layerCount, nullptr);
    std::vector<VkLayerProperties> layers(layerCount);
    vkEnumerateInstanceLayerProperties(&layerCount, layers.data());
    for (auto &el : toCheck) {
        bool found = false;
        for (auto &cel : layers) {
            if (!strcmp(cel.layerName, el)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw NotSupportedLayerException(el);
        }
    }
}

void VulkanInstance::destroy() {
    delete logger;
    vkDestroyInstance(instance, nullptr);
    destroyed = true;
}


VulkanInstanceBuilder::VulkanInstanceBuilder() {
    createInfo.pApplicationInfo = &appInfo;
    createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    createInfo.pNext = nullptr;
}

VulkanInstanceBuilder *
VulkanInstanceBuilder::setApplicationInfo(const char *appName, const char *engineName, uint32_t apiVersion,
                                          uint32_t appVersion, uint32_t engineVersion) {
    appInfoEnabled = true;
    appInfo.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    appInfo.apiVersion = apiVersion;
    appInfo.engineVersion = engineVersion;
    appInfo.applicationVersion = appVersion;
    appInfo.pApplicationName = appName;
    appInfo.pEngineName = engineName;
    appInfo.pNext = nullptr;
    return this;
}

VulkanInstanceBuilder *VulkanInstanceBuilder::addExtension(const char *extension) {
    enabledExtensions.push_back(extension);
    createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    createInfo.ppEnabledExtensionNames = enabledExtensions.data();
    createInfo.enabledExtensionCount = enabledExtensions.size();
    createInfo.pNext = nullptr;

    return this;
}

VulkanInstanceBuilder *VulkanInstanceBuilder::addLayer(const char *layer) {
    enabledLayers.push_back(layer);
    createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    createInfo.ppEnabledLayerNames = enabledLayers.data();
    createInfo.enabledLayerCount = enabledLayers.size();
    createInfo.pNext = nullptr;
    return this;
}

VulkanInstanceBuilder *VulkanInstanceBuilder::setInstanceFlags(VkInstanceCreateFlags flags) {
    createInfo.flags = flags;
    createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    createInfo.pNext = nullptr;
    return this;
}


