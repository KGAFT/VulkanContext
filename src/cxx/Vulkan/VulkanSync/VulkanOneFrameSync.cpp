#include "VulkanOneFrameSync.h"

VulkanOneFrameSync::VulkanOneFrameSync(VulkanDevice *device) : device(device)
{
    createSyncObjects();
}

void VulkanOneFrameSync::destroy()
{
    if (!destroyed)
    {
        vkDeviceWaitIdle(device->getDevice());
        vkDestroySemaphore(device->getDevice(), availableSemaphore, nullptr);
        vkDestroySemaphore(device->getDevice(), waitSemaphore, nullptr);
    }
    destroyed = true;
}

void VulkanOneFrameSync::submitCommandBuffer(VkCommandBuffer commandBuffer)
{
    vkResetFences(device->getDevice(), 1, &fence);
    VkPipelineStageFlags waitStages[] = {VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT};
    VkSubmitInfo submitInfo = {};
    submitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
    submitInfo.signalSemaphoreCount = 1;
    submitInfo.waitSemaphoreCount = !firstFrame;
    submitInfo.pWaitSemaphores = firstFrame ? VK_NULL_HANDLE : &waitSemaphore;
    submitInfo.pSignalSemaphores = &availableSemaphore;
    submitInfo.commandBufferCount = 1;
    submitInfo.pCommandBuffers = &commandBuffer;
    submitInfo.pWaitDstStageMask = waitStages;
    vkQueueSubmit(device->getGraphicsQueue(), 1, &submitInfo, fence);
    vkWaitForFences(device->getDevice(), 1, &fence, VK_TRUE, UINT64_MAX);
    firstFrame = false;
    std::swap(availableSemaphore, waitSemaphore);
}

VulkanOneFrameSync::~VulkanOneFrameSync()
{
    if (!destroyed)
    {
        destroy();
    }
}

void VulkanOneFrameSync::createSyncObjects()
{
    VkSemaphoreCreateInfo semaphoreInfo = {};
    semaphoreInfo.sType = VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;
    vkCreateSemaphore(device->getDevice(), &semaphoreInfo, nullptr, &availableSemaphore);
    vkCreateSemaphore(device->getDevice(), &semaphoreInfo, nullptr, &waitSemaphore);

    VkFenceCreateInfo fenceInfo = {};
    fenceInfo.sType = VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
    fenceInfo.flags = VK_FENCE_CREATE_SIGNALED_BIT;
    vkCreateFence(device->getDevice(), &fenceInfo, nullptr, &fence);
}