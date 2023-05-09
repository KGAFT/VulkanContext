#include "VulkanPushConstantManager.h"

void VulkanPushConstantManager::registerPushConstant(VulkanPushConstant *pushConstant)
{
    pushConstants.push_back(pushConstant);
}

void VulkanPushConstantManager::loadConstantsToShader(VkCommandBuffer commandBuffer, VkPipelineLayout pipelineLayout)
{
    for (const auto &item : pushConstants)
    {
        vkCmdPushConstants(
            commandBuffer,
            pipelineLayout,
            item->getShaderStages(),
            0,
            item->getSize(),
            item->getData());
    }
}