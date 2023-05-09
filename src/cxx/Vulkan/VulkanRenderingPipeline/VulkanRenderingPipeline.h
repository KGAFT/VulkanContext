//
// Created by KGAFT on 3/15/2023.
//
#pragma once
#include "VulkanRenderPipelineControl.h"
class VulkanRenderingPipeline{
private:
    VulkanRenderPipelineControl* control;
    VulkanDevice* device;
    VulkanRenderPass* renderPass;
    VulkanGraphicsPipeline* graphicsPipeline;
    float clearColorValues[4] = {0,0,0,1};
public:
    VulkanRenderingPipeline(VulkanDevice *device, VulkanRenderPass *renderPass, VulkanSyncManager* syncManager, VulkanGraphicsPipeline* graphicsPipeline);

    void setBackBufferColor(float red, float green, float blue, float alpha);

};
