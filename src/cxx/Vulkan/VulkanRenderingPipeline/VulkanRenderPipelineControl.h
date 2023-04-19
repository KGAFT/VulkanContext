//
// Created by KGAFT on 3/14/2023.
//
#pragma once

#include "../VulkanGraphicsPipeline/VulkanGraphicsPipeline.h"
#include "../VulkanSync/VulkanSyncManager.h"

class VulkanRenderPipelineControl {
private:
    VulkanSyncManager *syncManager;
    VulkanDevice *device;
    VulkanRenderPass *renderPass;
    std::vector<VkCommandBuffer> commandBuffers;
    unsigned int currentCmd = 0;
public:
    VulkanRenderPipelineControl(VulkanSyncManager *syncManager,
                                VulkanDevice *device, VulkanRenderPass *renderPass) : syncManager(syncManager),
                                                                                      device(device),
                                                                                      renderPass(renderPass) {
        createCommandBuffer(syncManager->getCurrentMode());
    }
    std::pair<VkCommandBuffer, VkFramebuffer> beginRender(){
        currentCmd = syncManager->prepareFrame();
        syncManager->beginCommandBuffer(commandBuffers[currentCmd]);
        return std::pair(commandBuffers[currentCmd], renderPass->getFrameBuffers()[currentCmd]);
    }
    void endRender(){
        vkEndCommandBuffer(commandBuffers[currentCmd]);
        syncManager->endRender(commandBuffers[currentCmd]);
    }

    void setRenderPass(VulkanRenderPass *renderPass) {
        VulkanRenderPipelineControl::renderPass = renderPass;
    }

    unsigned int getCurrentCmd()  {
        return currentCmd;
    }

private:

    void createCommandBuffer(unsigned int amount) {
        commandBuffers.resize(amount);

        VkCommandBufferAllocateInfo allocInfo{};
        allocInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
        allocInfo.level = VK_COMMAND_BUFFER_LEVEL_PRIMARY;
        allocInfo.commandPool = device->getCommandPool();
        allocInfo.commandBufferCount = static_cast<uint32_t>(amount);

        if (vkAllocateCommandBuffers(device->getDevice(), &allocInfo, commandBuffers.data()) !=
            VK_SUCCESS) {
            throw std::runtime_error("failed to allocate command buffers!");
        }
    }
};