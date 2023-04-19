#pragma once

#include "VulkanOneFrameSync.h"
#include "VulkanThreeFrameSync.h"
#include "../VulkanSwapChain.h"

class VulkanSyncManager
{
private:
    VulkanOneFrameSync *oneFrameSync = nullptr;
    VulkanThreeFrameSync *threeFrameSync = nullptr;
    VulkanSwapChain *swapChain = nullptr;
    bool destroyed = false;
    unsigned int currentImage = 0;

public:
    VulkanSyncManager(VulkanDevice *device, VulkanSwapChain *swapChain) : swapChain(swapChain)
    {
        if (swapChain != nullptr)
        {
            threeFrameSync = new VulkanThreeFrameSync(device);
        }
        else
        {
            oneFrameSync = new VulkanOneFrameSync(device);
        }
    }
    /**
     * First step to begin render
     *@returns index to current command buffer
     */
    unsigned int prepareFrame()
    {
        if (threeFrameSync != nullptr)
        {
            currentImage = threeFrameSync->prepareForNextImage(swapChain->swapChain);
            return currentImage;
        }
        else
        {
            return 0;
        }
    }

    unsigned int getCurrentImage() {
        return currentImage;
    }

    /**
     * Second step to begin render
     */
    void beginCommandBuffer(VkCommandBuffer commandBuffer)
    {
        VkCommandBufferBeginInfo beginInfo{};
        beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
        if (vkBeginCommandBuffer(commandBuffer, &beginInfo) != VK_SUCCESS)
        {
            throw std::runtime_error("failed to begin recording command buffer!");
        }
    }
    void endRender(VkCommandBuffer commandBuffer)
    {
        if (threeFrameSync != nullptr)
        {
            threeFrameSync->submitCommandBuffers(&commandBuffer, swapChain->swapChain, &currentImage);
        }
        else{
            oneFrameSync->submitCommandBuffer(commandBuffer);
        }
    }

    VulkanSwapChain *getSwapChain() {
        return swapChain;
    }

    void destroy()
    {
        if (!destroyed)
        {
            if (threeFrameSync != nullptr)
            {
                delete threeFrameSync;
            }
            else
            {
                delete oneFrameSync;
            }
            destroyed = true;
        }
    }

    unsigned int getCurrentMode(){
        if(threeFrameSync!=nullptr){
            return 3;
        }
        else{
            return 1;
        }
    }

    ~VulkanSyncManager()
    {
        destroy();
    }
};
