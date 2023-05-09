//
// Created by KGAFT on 3/11/2023.
//
#pragma once

#include <vulkan/vulkan.h>
#include <stdexcept>
#include <vector>
#include <limits>
#include "../VulkanDevice/VulkanDevice.h"

#define MAX_FRAMES_IN_FLIGHT 2

class VulkanThreeFrameSync
{

public:
    VulkanThreeFrameSync(VulkanDevice *device);

private:
    std::vector<VkSemaphore> imageAvailableSemaphores;
    std::vector<VkSemaphore> renderFinishedSemaphores;
    std::vector<VkFence> inFlightFences;
    std::vector<VkFence> imagesInFlight;
    VulkanDevice *device;
    bool destroyed = false;
    int currentFrame = 0;

public:
    unsigned int prepareForNextImage(VkSwapchainKHR swapChain);

    void submitCommandBuffers(VkCommandBuffer *buffers, VkSwapchainKHR swapChain, unsigned int *currentImage);

    void destroy();

private:
    void createSyncObjects();
};