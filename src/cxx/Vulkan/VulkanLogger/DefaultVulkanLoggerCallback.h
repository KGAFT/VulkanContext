//
// Created by Daniil on 01.02.2023.
//
#pragma once

#include <chrono>
#include <iostream>
#include "IVulkanLoggerCallback.h"

class DefaultVulkanLoggerCallback : public IVulkanLoggerCallback
{
public:
    int getCallBackMode() override;

    void messageRaw(VkDebugUtilsMessageSeverityFlagBitsEXT severity, VkDebugUtilsMessageTypeFlagsEXT type,
                    const VkDebugUtilsMessengerCallbackDataEXT *pCallbackData, void *pUserData) override;

    void translatedMessage(const char *severity, const char *type, std::string message) override;
};
