//
// Created by KGAFT on 3/15/2023.
//
#pragma once

#include <vulkan/vulkan.h>
#include <vector>
#include "VulkanPushConstant.h"
class VulkanPushConstantManager{
private:
    std::vector<VulkanPushConstant *> pushConstants;
public:
    void registerPushConstant(VulkanPushConstant *pushConstant) {
        pushConstants.push_back(pushConstant);
    }
    void loadConstantsToShader(VkCommandBuffer commandBuffer, VkPipelineLayout pipelineLayout) {
        for (const auto &item: pushConstants) {
            vkCmdPushConstants(
                    commandBuffer,
                    pipelineLayout,
                    item->getShaderStages(),
                    0,
                    item->getSize(),
                    item->getData());
        }

    }
};