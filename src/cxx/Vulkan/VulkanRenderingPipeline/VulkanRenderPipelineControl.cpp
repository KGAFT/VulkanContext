#include "VulkanRenderingPipeline.h"

VulkanRenderPipelineControl::VulkanRenderPipelineControl(VulkanSyncManager *syncManager,
                                                         VulkanDevice *device, VulkanRenderPass *renderPass) : syncManager(syncManager),
                                                                                                               device(device),
                                                                                                               renderPass(renderPass)
{
    createCommandBuffer(syncManager->getCurrentMode());
}

std::pair<VkCommandBuffer, VkFramebuffer> VulkanRenderPipelineControl::beginRender()
{
    currentCmd = syncManager->prepareFrame();
    syncManager->beginCommandBuffer(commandBuffers[currentCmd]);

    return std::pair(commandBuffers[currentCmd], renderPass->getFrameBuffers()[currentCmd]);
}

void VulkanRenderPipelineControl::endRender()
{
    vkEndCommandBuffer(commandBuffers[currentCmd]);
    syncManager->endRender(commandBuffers[currentCmd]);
}

void VulkanRenderPipelineControl::setRenderPass(VulkanRenderPass *renderPass)
{
    VulkanRenderPipelineControl::renderPass = renderPass;
}

unsigned int VulkanRenderPipelineControl::getCurrentCmd()
{
    return currentCmd;
}

void VulkanRenderPipelineControl::createCommandBuffer(unsigned int amount)
{
    commandBuffers.resize(amount);

    VkCommandBufferAllocateInfo allocInfo{};
    allocInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
    allocInfo.level = VK_COMMAND_BUFFER_LEVEL_PRIMARY;
    allocInfo.commandPool = device->getCommandPool();
    allocInfo.commandBufferCount = static_cast<uint32_t>(amount);

    if (vkAllocateCommandBuffers(device->getDevice(), &allocInfo, commandBuffers.data()) !=
        VK_SUCCESS)
    {
        throw std::runtime_error("failed to allocate command buffers!");
    }
}