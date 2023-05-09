//
// Created by KGAFT on 3/15/2023.
//
#pragma once

#include <vulkan/vulkan.h>
#include <vector>
#include "VulkanPushConstant.h"
class VulkanPushConstantManager
{
private:
    std::vector<VulkanPushConstant *> pushConstants;

public:
    void registerPushConstant(VulkanPushConstant *pushConstant);

    void loadConstantsToShader(VkCommandBuffer commandBuffer, VkPipelineLayout pipelineLayout);
};