#include "DefaultVulkanLoggerCallback.h"

int DefaultVulkanLoggerCallback::getCallBackMode() 
{
    return TRANSLATED_DEFS;
}

void DefaultVulkanLoggerCallback::messageRaw(VkDebugUtilsMessageSeverityFlagBitsEXT severity, VkDebugUtilsMessageTypeFlagsEXT type,
                                             const VkDebugUtilsMessengerCallbackDataEXT *pCallbackData, void *pUserData) 
{
}

void DefaultVulkanLoggerCallback::translatedMessage(const char *severity, const char *type, std::string message) 
{
    auto currentTime = std::chrono::system_clock::now();
    auto time = std::chrono::system_clock::to_time_t(currentTime);
    std::string outputTime = std::string(ctime(&time));
    outputTime[outputTime.size() - 1] = '\0';
    std::string outputMessage = outputTime + "VULKAN " + " [" + severity + "] " + type + " " + message;
    std::string severityS = severity;
    if (!severityS.compare("ERROR"))
    {
        std::cerr << outputMessage << std::endl;
    }
    else
    {
        std::cout << outputMessage << std::endl;
    }
}