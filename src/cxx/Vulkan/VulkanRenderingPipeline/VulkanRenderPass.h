//
// Created by KGAFT on 3/10/2023.
//
#pragma once

#include <vulkan/vulkan.h>

#include "../VulkanDevice/VulkanDevice.h"

class VulkanRenderPass
{
public:
    VulkanRenderPass(VulkanDevice *device, std::vector<VkImageView> &images, int width, int height, int imagePerStepAmount, VkFormat *imagesFormat, int formatCount, bool output);

private:
    VulkanDevice *device;
    VkRenderPass renderPass;
    std::vector<VkFramebuffer> frameBuffers;
    std::vector<VkImage> depthImages;
    std::vector<VkDeviceMemory> depthImageMemories;
    std::vector<VkImageView> depthImageViews;
    VkFormat depthFormat;
    bool output = false;
    bool destroyed = false;
    int attachmentCount = 0;

public:
    void destroy();

    ~VulkanRenderPass();

    std::vector<VkImageView> &getDepthImageViews();

    std::vector<VkImage> &getDepthImages();

    const std::vector<VkFramebuffer> &getFrameBuffers() const;

    void recreate(std::vector<VkImageView> &images, int width, int height, int imagePerStepAmount, VkFormat *imagesFormat, int formatCount);

    int getAttachmentCount() const;

    VkRenderPass getRenderPass();

private:
    void createDepthResources(int width, int height, int imagesAmount);

    std::vector<VkAttachmentDescription>
    prepareColorAttachmentDescription(VkFormat *imageFormat, int formatCount, int amount, bool output);

    std::vector<VkAttachmentReference> prepareColorReference(int refCount);

    void createRenderPass(VkFormat *imageFormat, int formatCount, int attachImagesAmount, bool output);

    void createFrameBuffers(int amount, int width, int height, std::vector<VkImageView> &imagesToAttach,
                            const int imagePerStepAmount);

    VkFormat findDepthFormat();
};