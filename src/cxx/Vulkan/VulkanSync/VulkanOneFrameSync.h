#pragma once

#include <vulkan/vulkan.h>
#include "../VulkanDevice/VulkanDevice.h"

class VulkanOneFrameSync
{
public:
    VulkanOneFrameSync(VulkanDevice *device);

private:
    VkSemaphore availableSemaphore;
    VkSemaphore waitSemaphore;
    VulkanDevice *device;
    bool destroyed = false;
    VkFence fence;
    bool firstFrame = true;

public:
    void submitCommandBuffer(VkCommandBuffer commandBuffer);

    void destroy();

    ~VulkanOneFrameSync();

private:
    void createSyncObjects();
};