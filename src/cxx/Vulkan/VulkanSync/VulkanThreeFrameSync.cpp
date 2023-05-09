#include "VulkanThreeFrameSync.h"

VulkanThreeFrameSync::VulkanThreeFrameSync(VulkanDevice *device) : device(device)
{
    createSyncObjects();
}

unsigned int VulkanThreeFrameSync::prepareForNextImage(VkSwapchainKHR swapChain)
{
    unsigned int result = 0;
    vkWaitForFences(
        device->getDevice(),
        1,
        &inFlightFences[currentFrame],
        VK_TRUE,
        std::numeric_limits<uint64_t>::max());

    vkAcquireNextImageKHR(
        device->getDevice(),
        swapChain,
        std::numeric_limits<uint64_t>::max(),
        imageAvailableSemaphores[currentFrame], // must be a not signaled semaphore
        VK_NULL_HANDLE,
        &result);
    while (result > 2)
    {
        vkWaitForFences(
            device->getDevice(),
            1,
            &inFlightFences[currentFrame],
            VK_TRUE,
            std::numeric_limits<uint64_t>::max());

        vkAcquireNextImageKHR(
            device->getDevice(),
            swapChain,
            std::numeric_limits<uint64_t>::max(),
            imageAvailableSemaphores[currentFrame], // must be a not signaled semaphore
            VK_NULL_HANDLE,
            &result);
    }

    return result;
}

void VulkanThreeFrameSync::submitCommandBuffers(VkCommandBuffer *buffers, VkSwapchainKHR swapChain, unsigned int *currentImage)
{
    if (imagesInFlight[*currentImage] != VK_NULL_HANDLE)
    {
        vkWaitForFences(device->getDevice(), 1, &imagesInFlight[*currentImage], VK_TRUE, UINT64_MAX);
    }
    imagesInFlight[*currentImage] = inFlightFences[currentFrame];

    VkSubmitInfo submitInfo = {};
    submitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;

    VkSemaphore waitSemaphores[] = {imageAvailableSemaphores[currentFrame]};
    VkPipelineStageFlags waitStages[] = {VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT};
    submitInfo.waitSemaphoreCount = 1;
    submitInfo.pWaitSemaphores = waitSemaphores;
    submitInfo.pWaitDstStageMask = waitStages;

    submitInfo.commandBufferCount = 1;
    submitInfo.pCommandBuffers = buffers;

    VkSemaphore signalSemaphores[] = {renderFinishedSemaphores[currentFrame]};
    submitInfo.signalSemaphoreCount = 1;
    submitInfo.pSignalSemaphores = signalSemaphores;

    vkResetFences(device->getDevice(), 1, &inFlightFences[currentFrame]);
    if (vkQueueSubmit(device->getGraphicsQueue(), 1, &submitInfo, inFlightFences[currentFrame]) !=
        VK_SUCCESS)
    {
        throw std::runtime_error("failed to submit draw command buffer!");
    }

    VkPresentInfoKHR presentInfo = {};
    presentInfo.sType = VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;

    presentInfo.waitSemaphoreCount = 1;
    presentInfo.pWaitSemaphores = signalSemaphores;

    VkSwapchainKHR swapChains[] = {swapChain};
    presentInfo.swapchainCount = 1;
    presentInfo.pSwapchains = swapChains;

    presentInfo.pImageIndices = currentImage;

    auto result = vkQueuePresentKHR(device->getPresentQueue(), &presentInfo);

    currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
}

void VulkanThreeFrameSync::destroy()
{
    vkDeviceWaitIdle(device->getDevice());
    for (int i = 0; i < 2; i++)
    {
        vkDestroySemaphore(device->getDevice(), imageAvailableSemaphores[i], nullptr);
        vkDestroySemaphore(device->getDevice(), renderFinishedSemaphores[i], nullptr);
        vkDestroyFence(device->getDevice(), inFlightFences[i], nullptr);
    }
    destroyed = true;
}

VulkanThreeFrameSync::~VulkanThreeFrameSync()
{
    if (!destroyed)
    {
        destroy();
    }
}

void VulkanThreeFrameSync::createSyncObjects()
{
    imageAvailableSemaphores.resize(MAX_FRAMES_IN_FLIGHT);
    renderFinishedSemaphores.resize(MAX_FRAMES_IN_FLIGHT);
    inFlightFences.resize(MAX_FRAMES_IN_FLIGHT);
    imagesInFlight.resize(3, VK_NULL_HANDLE);

    VkSemaphoreCreateInfo semaphoreInfo = {};
    semaphoreInfo.sType = VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;

    VkFenceCreateInfo fenceInfo = {};
    fenceInfo.sType = VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
    fenceInfo.flags = VK_FENCE_CREATE_SIGNALED_BIT;

    for (size_t i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
    {
        if (vkCreateSemaphore(device->getDevice(), &semaphoreInfo, nullptr, &imageAvailableSemaphores[i]) !=
                VK_SUCCESS ||
            vkCreateSemaphore(device->getDevice(), &semaphoreInfo, nullptr, &renderFinishedSemaphores[i]) !=
                VK_SUCCESS ||
            vkCreateFence(device->getDevice(), &fenceInfo, nullptr, &inFlightFences[i]) != VK_SUCCESS)
        {
            throw std::runtime_error("failed to create synchronization objects for a frame!");
        }
    }
}