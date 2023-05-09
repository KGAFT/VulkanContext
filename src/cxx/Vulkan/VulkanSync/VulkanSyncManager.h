#pragma once

#include "VulkanOneFrameSync.h"
#include "VulkanThreeFrameSync.h"
#include "../VulkanSwapChain.h"

class VulkanSyncManager
{
public:
    VulkanSyncManager(VulkanDevice *device, VulkanSwapChain *swapChain);

private:
    VulkanOneFrameSync *oneFrameSync = nullptr;
    VulkanThreeFrameSync *threeFrameSync = nullptr;
    VulkanSwapChain *swapChain = nullptr;
    bool destroyed = false;
    unsigned int currentImage = 0;
public:
    /**
     * First step to begin render
     *@returns index to current command buffer
     */
    unsigned int prepareFrame();

    unsigned int getCurrentImage();

    /**
     * Second step to begin render
     */
    void beginCommandBuffer(VkCommandBuffer commandBuffer);

    void endRender(VkCommandBuffer commandBuffer);

    VulkanSwapChain *getSwapChain();

    void destroy();

    unsigned int getCurrentMode();

    ~VulkanSyncManager();
};
