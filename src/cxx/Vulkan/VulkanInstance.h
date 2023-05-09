//
// Created by KGAFT on 3/10/2023.
//
#pragma once

#include <vulkan/vulkan.h>
#include "VulkanLogger/VulkanLogger.h"
#include <vector>
#include <unordered_set>

class VulkanInstance
{
private:
    VkInstance instance;

public:
    bool createInstance(const char *appName, bool enableLogging, std::vector<const char *> &baseExtensions);

    VkInstance getInstance();

private:
    bool checkExtensions(bool logging, std::vector<const char *> &toCheck);

    void getRequiredExtensions(bool enableValidationLayers, std::vector<const char *> output);
};
