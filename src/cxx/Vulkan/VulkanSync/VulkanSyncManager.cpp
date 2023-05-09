#include "VulkanSyncManager.h"

VulkanSyncManager::VulkanSyncManager(VulkanDevice *device, VulkanSwapChain *swapChain) : swapChain(swapChain)
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

unsigned int VulkanSyncManager::prepareFrame()
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
unsigned int VulkanSyncManager::getCurrentImage()
{
    return currentImage;
}

void VulkanSyncManager::beginCommandBuffer(VkCommandBuffer commandBuffer)
{
    VkCommandBufferBeginInfo beginInfo{};
    beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
    if (vkBeginCommandBuffer(commandBuffer, &beginInfo) != VK_SUCCESS)
    {
        throw std::runtime_error("failed to begin recording command buffer!");
    }
}

void VulkanSyncManager::endRender(VkCommandBuffer commandBuffer)
{
    if (threeFrameSync != nullptr)
    {
        threeFrameSync->submitCommandBuffers(&commandBuffer, swapChain->swapChain, &currentImage);
    }
    else
    {
        oneFrameSync->submitCommandBuffer(commandBuffer);
    }
}

VulkanSwapChain *VulkanSyncManager::getSwapChain()
{
    return swapChain;
}

void VulkanSyncManager::destroy()
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

unsigned int VulkanSyncManager::getCurrentMode()
{
    if (threeFrameSync != nullptr)
    {
        return 3;
    }
    else
    {
        return 1;
    }
}

VulkanSyncManager::~VulkanSyncManager()
{
    destroy();
}