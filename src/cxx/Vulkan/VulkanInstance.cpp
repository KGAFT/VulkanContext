#include "VulkanInstance.h"

bool VulkanInstance::createInstance(const char *appName, bool enableLogging, std::vector<const char *> &baseExtensions)
{
    VkApplicationInfo appInfo = {};
    appInfo.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    appInfo.pApplicationName = appName;
    appInfo.applicationVersion = VK_MAKE_VERSION(1, 0, 0);
    appInfo.pEngineName = "KGAFTEngine";
    appInfo.engineVersion = VK_MAKE_VERSION(1, 0, 0);
    appInfo.apiVersion = VK_API_VERSION_1_3;

    VkInstanceCreateInfo createInfo = {};
    createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    createInfo.pApplicationInfo = &appInfo;

    getRequiredExtensions(enableLogging, baseExtensions);
    createInfo.enabledExtensionCount = static_cast<uint32_t>(baseExtensions.size());
    createInfo.ppEnabledExtensionNames = baseExtensions.data();

    VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo;
    if (enableLogging)
    {
        VulkanLogger::describeLogger(debugCreateInfo, &createInfo);
        createInfo.pNext = &debugCreateInfo;
    }
    else
    {
        createInfo.enabledLayerCount = 0;
        createInfo.pNext = nullptr;
    }

    if (vkCreateInstance(&createInfo, nullptr, &instance) != VK_SUCCESS)
    {
        return false;
    }
    if (enableLogging && checkExtensions(true, baseExtensions))
    {
        return VulkanLogger::init(instance);
    }
    else if (enableLogging)
    {
        return false;
    }
    return true;
}
bool VulkanInstance::checkExtensions(bool logging, std::vector<const char *> &toCheck)
{
    uint32_t extensionCount = 0;
    vkEnumerateInstanceExtensionProperties(nullptr, &extensionCount, nullptr);
    std::vector<VkExtensionProperties> extensions(extensionCount);
    vkEnumerateInstanceExtensionProperties(nullptr, &extensionCount, extensions.data());

    std::unordered_set<std::string> available;
    for (const auto &extension : extensions)
    {
        available.insert(extension.extensionName);
    }

    for (const auto &required : toCheck)
    {
        if (available.find(required) == available.end())
        {
            return false;
        }
    }
    return true;
}

void VulkanInstance::getRequiredExtensions(bool enableValidationLayers, std::vector<const char *> output)
{

    if (enableValidationLayers)
    {
        output.push_back(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
    }
}
VkInstance VulkanInstance::getInstance()
{
    return instance;
}