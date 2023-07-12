#pragma once

#include <vulkan/vulkan.h>
#include <vector>
#include <map>
#include "Util/ShaderConfParser.h"
#include "Vulkan/VulkanDevice/VulkanDevice.h"

class VulkanShader
{
public:
    VulkanShader(VulkanDevice *device, std::map<VkShaderModule, int> &toCreate);

private:
    std::vector<VkPipelineShaderStageCreateInfo> stages;
    bool destroyed = false;
    VulkanDevice *device;

public:
    std::vector<VkPipelineShaderStageCreateInfo> &getStages();

    void destroy();

    ~VulkanShader();

private:
    VkShaderStageFlagBits getShaderStage(int type);
};