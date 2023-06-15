#include "VulkanLogger.h"

const std::vector<const char *> VulkanLogger::validationLayers = {"VK_LAYER_KHRONOS_validation"};
VkDebugUtilsMessengerEXT VulkanLogger::debugMessenger = NULL;

std::vector<IVulkanLoggerCallback *> VulkanLogger::rawCallbacks = std::vector<IVulkanLoggerCallback *>();
std::vector<IVulkanLoggerCallback *> VulkanLogger::translatedCallbacks = std::vector<IVulkanLoggerCallback *>();
std::vector<IVulkanLoggerCallback *> VulkanLogger::bothCallbacks = std::vector<IVulkanLoggerCallback *>();

VkBool32 VulkanLogger::debugCallback(
    VkDebugUtilsMessageSeverityFlagBitsEXT messageSeverity,
    VkDebugUtilsMessageTypeFlagsEXT messageType,
    const VkDebugUtilsMessengerCallbackDataEXT *pCallbackData,
    void *pUserData)
{
    if (rawCallbacks.empty() && translatedCallbacks.empty() && bothCallbacks.empty())
    {
        std::cerr << "Vulkan: " << pCallbackData->pMessage << std::endl;
    }

    for (const auto &item : rawCallbacks)
    {
        item->messageRaw(messageSeverity, messageType, pCallbackData, pUserData);
    }
    for (const auto &item : bothCallbacks)
    {
        item->messageRaw(messageSeverity, messageType, pCallbackData, pUserData);
    }
    if (!translatedCallbacks.empty())
    {
        const char *severity = translateSeverity(messageSeverity);
        const char *type = translateType(messageType);
        std::string message = std::string(pCallbackData->pMessage);
        for (const auto &item : translatedCallbacks)
        {
            item->translatedMessage(severity, type, message);
        }

        for (const auto &item : bothCallbacks)
        {
            item->translatedMessage(severity, type, message);
        }
    }

    return VK_FALSE;
}

const char *VulkanLogger::translateSeverity(VkDebugUtilsMessageSeverityFlagBitsEXT severity)
{
    const char *res;
    switch (severity)
    {
    case VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT:
        res = "VERBOSE";
        break;
    case VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT:
        res = "ERROR";
        break;
    case VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT:
        res = "WARNING";
        break;
    case VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT:
        res = "INFO";
        break;
    default:
        res = "UNDEFINED";
        break;
    }
    return res;
}

const char *VulkanLogger::translateType(VkDebugUtilsMessageTypeFlagsEXT type)
{
    const char *res;
    switch (type)
    {
    case VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT:
        res = "GENERAL";
        break;
    case VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT:
        res = "PERFORMANCE";
        break;
    case VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT:
        res = "VALIDATION";
        break;
    default:
        res = "UNDEFINED";
        break;
    }
    return res;
}

void VulkanLogger::describeLogger(VkDebugUtilsMessengerCreateInfoEXT &createInfo, VkInstanceCreateInfo *instanceInfo)
{
    if (instanceInfo != nullptr)
    {
        instanceInfo->enabledLayerCount = static_cast<uint32_t>(validationLayers.size());
        instanceInfo->ppEnabledLayerNames = validationLayers.data();
    }

    createInfo = {};
    createInfo.sType = VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT;
    createInfo.messageSeverity = VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT |
                                 VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT;
    createInfo.messageType = VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
                             VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
                             VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;
    createInfo.pfnUserCallback = debugCallback;
    createInfo.pUserData = nullptr;
}

VkResult VulkanLogger::CreateDebugUtilsMessengerEXT(
    VkInstance instance,
    VkDebugUtilsMessengerCreateInfoEXT *pCreateInfo,
    VkAllocationCallbacks *pAllocator,
    VkDebugUtilsMessengerEXT *pDebugMessenger)
{
    auto func = (PFN_vkCreateDebugUtilsMessengerEXT)vkGetInstanceProcAddr(
        instance,
        "vkCreateDebugUtilsMessengerEXT");
    if (func != nullptr)
    {
        return func(instance, pCreateInfo, pAllocator, pDebugMessenger);
    }
    else
    {
        return VK_ERROR_EXTENSION_NOT_PRESENT;
    }
}

bool VulkanLogger::init(VkInstance instance)
{
    VkDebugUtilsMessengerCreateInfoEXT createInfo;
    describeLogger(createInfo, nullptr);
    if (CreateDebugUtilsMessengerEXT(instance, &createInfo, nullptr, &debugMessenger) != VK_SUCCESS)
    {
    }
    return true;
}

void VulkanLogger::registerCallback(IVulkanLoggerCallback *callback)
{
    
    switch (callback->getCallBackMode())
    {
    case RAW_VULKAN_DEFS:
        rawCallbacks.push_back(callback);
        break;
    case TRANSLATED_DEFS:
        translatedCallbacks.push_back(callback);
        break;
    case BOTH_DEFS:
        bothCallbacks.push_back(callback);
        break;
    }
    
}

void VulkanLogger::removeCallback(IVulkanLoggerCallback *callback)
{
    if (debugMessenger != NULL)
    {
        std::vector<IVulkanLoggerCallback *> *toRemove = nullptr;
        switch (callback->getCallBackMode())
        {
        case RAW_VULKAN_DEFS:
            toRemove = &rawCallbacks;
            break;
        case TRANSLATED_DEFS:
            toRemove = &translatedCallbacks;
            break;
        case BOTH_DEFS:
            toRemove = &bothCallbacks;
            break;
        }
        if (toRemove != nullptr)
        {
            std::vector<IVulkanLoggerCallback *> temp;
            bool found = false;
            for (IVulkanLoggerCallback *curCallback : *toRemove)
            {
                if (curCallback != callback)
                {
                    temp.push_back(callback);
                }
                else
                {
                    found = true;
                }
            }
            if (found)
            {
                toRemove->clear();
                for (IVulkanLoggerCallback *curCallback : temp)
                {
                    toRemove->push_back(curCallback);
                }
            }
        }
    }
}