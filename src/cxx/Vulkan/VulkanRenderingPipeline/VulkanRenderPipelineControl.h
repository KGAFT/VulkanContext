//
// Created by KGAFT on 3/14/2023.
//
#pragma once

#include "../VulkanGraphicsPipeline/VulkanGraphicsPipeline.h"
#include "../VulkanSync/VulkanSyncManager.h"

class VulkanRenderPipelineControl
{
public:
    VulkanRenderPipelineControl(VulkanSyncManager *syncManager,
                                VulkanDevice *device, VulkanRenderPass *renderPass);

private:
    VulkanSyncManager *syncManager;
    VulkanDevice *device;
    VulkanRenderPass *renderPass;
    std::vector<VkCommandBuffer> commandBuffers;
    unsigned int currentCmd = 0;

public:
    std::pair<VkCommandBuffer, VkFramebuffer> beginRender();

    void endRender();

    void setRenderPass(VulkanRenderPass *renderPass);

    unsigned int getCurrentCmd();

private:
    void createCommandBuffer(unsigned int amount);
};