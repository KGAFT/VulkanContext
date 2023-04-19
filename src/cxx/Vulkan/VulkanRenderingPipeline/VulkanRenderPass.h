//
// Created by KGAFT on 3/10/2023.
//
#pragma once

#include <vulkan/vulkan.h>

#include "../VulkanDevice/VulkanDevice.h"


class VulkanRenderPass {
private:
    VulkanDevice *device;
    VkRenderPass renderPass;
    std::vector<VkFramebuffer> frameBuffers;
    std::vector<VkImage> depthImages;
    std::vector<VkDeviceMemory> depthImageMemories;
    std::vector<VkImageView> depthImageViews;
    VkFormat depthFormat;
    bool output = false;
    bool destroyed =false;
    int attachmentCount = 0;
public:
    VulkanRenderPass(VulkanDevice* device, std::vector<VkImageView>& images, int width, int height, int imagePerStepAmount, VkFormat* imagesFormat, int formatCount, bool output) : attachmentCount(imagePerStepAmount), output(output), device(device){
        createRenderPass(imagesFormat, formatCount, imagePerStepAmount, output);
        createDepthResources(width, height, images.size()/imagePerStepAmount);
        createFrameBuffers(images.size()/imagePerStepAmount, width, height, images, imagePerStepAmount);
    }

    void destroy() {
        for (const auto &item: frameBuffers) {
            vkDestroyFramebuffer(device->getDevice(), item, nullptr);
        }
        for (const auto &item: depthImageViews) {
            vkDestroyImageView(device->getDevice(), item, nullptr);
        }
        for (const auto &item: depthImages){
            vkDestroyImage(device->getDevice(), item, nullptr);
        }
        for (const auto &item: depthImageMemories) {
            vkFreeMemory(device->getDevice(), item, nullptr);
        }
        vkDestroyRenderPass(device->getDevice(), renderPass, nullptr);
        depthImages.clear();
        frameBuffers.clear();
        depthImageViews.clear();
        depthImageMemories.clear();
        destroyed = true;
    }
    ~VulkanRenderPass() {
        if(!destroyed){
            destroy();
        }

    }

    const std::vector<VkFramebuffer> &getFrameBuffers() const {
        return frameBuffers;
    }

    void recreate(std::vector<VkImageView>& images, int width, int height, int imagePerStepAmount, VkFormat* imagesFormat, int formatCount) {
        this->attachmentCount = imagePerStepAmount;
        vkDeviceWaitIdle(device->getDevice());
        destroy();
        destroyed = false;
        createRenderPass(imagesFormat, formatCount, imagePerStepAmount, output);
        createDepthResources(width, height, images.size()/imagePerStepAmount);
        createFrameBuffers(images.size()/imagePerStepAmount, width, height, images, imagePerStepAmount);
    }

    int getAttachmentCount() const {
        return attachmentCount;
    }

    VkRenderPass getRenderPass(){
        return renderPass;
    }

private:
    void createDepthResources(int width, int height, int imagesAmount) {
        VkFormat depthFormat = findDepthFormat();
        this->depthFormat = depthFormat;


        depthImages.resize(imagesAmount);
        depthImageMemories.resize(imagesAmount);
        depthImageViews.resize(imagesAmount);

        for (int i = 0; i < depthImages.size(); i++) {
            VkImageCreateInfo imageInfo{};
            imageInfo.sType = VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;
            imageInfo.imageType = VK_IMAGE_TYPE_2D;
            imageInfo.extent.width = width;
            imageInfo.extent.height = height;
            imageInfo.extent.depth = 1;
            imageInfo.mipLevels = 1;
            imageInfo.arrayLayers = 1;
            imageInfo.format = depthFormat;
            imageInfo.tiling = VK_IMAGE_TILING_OPTIMAL;
            imageInfo.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;
            imageInfo.usage = VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT;
            imageInfo.samples = VK_SAMPLE_COUNT_1_BIT;
            imageInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
            imageInfo.flags = 0;

            device->createImageWithInfo(
                    imageInfo,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    depthImages[i],
                    depthImageMemories[i]);

            VkImageViewCreateInfo viewInfo{};
            viewInfo.sType = VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
            viewInfo.image = depthImages[i];
            viewInfo.viewType = VK_IMAGE_VIEW_TYPE_2D;
            viewInfo.format = depthFormat;
            viewInfo.subresourceRange.aspectMask = VK_IMAGE_ASPECT_DEPTH_BIT;
            viewInfo.subresourceRange.baseMipLevel = 0;
            viewInfo.subresourceRange.levelCount = 1;
            viewInfo.subresourceRange.baseArrayLayer = 0;
            viewInfo.subresourceRange.layerCount = 1;

            if (vkCreateImageView(device->getDevice(), &viewInfo, nullptr, &depthImageViews[i]) != VK_SUCCESS) {
                throw std::runtime_error("failed to create texture image view!");
            }
        }
    }


    std::vector<VkAttachmentDescription>
    prepareColorAttachmentDescription(VkFormat *imageFormat, int formatCount, int amount, bool output) {
        std::vector<VkAttachmentDescription> result;
        bool useOneFormat = formatCount == 1;
        for (int i = 0; i < amount; ++i) {
            VkAttachmentDescription colorAttachment = {};
            if (useOneFormat) {
                colorAttachment.format = *imageFormat;
            } else {
                colorAttachment.format = imageFormat[i];
            }
            colorAttachment.samples = VK_SAMPLE_COUNT_1_BIT;
            colorAttachment.loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;
            colorAttachment.storeOp = VK_ATTACHMENT_STORE_OP_STORE;
            colorAttachment.stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
            colorAttachment.stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;
            colorAttachment.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;
            colorAttachment.finalLayout = output?VK_IMAGE_LAYOUT_PRESENT_SRC_KHR:VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
            result.push_back(colorAttachment);
        }
        return result;
    }

    std::vector<VkAttachmentReference> prepareColorReference(int refCount) {
        std::vector<VkAttachmentReference> result;
        for (int i = 0; i < refCount; ++i) {
            VkAttachmentReference colorAttachmentRef = {};
            colorAttachmentRef.attachment = i;
            colorAttachmentRef.layout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
            result.push_back(colorAttachmentRef);
        }
        return result;
    }

    void createRenderPass(VkFormat *imageFormat, int formatCount, int attachImagesAmount, bool output) {
        VkAttachmentDescription depthAttachment{};
        depthAttachment.format = findDepthFormat();
        depthAttachment.samples = VK_SAMPLE_COUNT_1_BIT;
        depthAttachment.loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;
        depthAttachment.storeOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
        depthAttachment.stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;
        depthAttachment.stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
        depthAttachment.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;
        depthAttachment.finalLayout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;



        std::vector<VkAttachmentDescription> attachments = prepareColorAttachmentDescription(imageFormat, formatCount,
                                                                                             attachImagesAmount, output);
        std::vector<VkAttachmentReference> references = prepareColorReference(attachImagesAmount);

        VkAttachmentReference depthAttachmentRef{};
        depthAttachmentRef.attachment = attachments.size();
        depthAttachmentRef.layout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;

        VkSubpassDescription subpass = {};
        subpass.pipelineBindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS;
        subpass.colorAttachmentCount = references.size();
        subpass.pColorAttachments = references.data();
        subpass.pDepthStencilAttachment = &depthAttachmentRef;
        subpass.inputAttachmentCount = 0;
        subpass.pInputAttachments = nullptr;
        subpass.pResolveAttachments = nullptr;

        VkSubpassDependency dependency = {};
        dependency.dstSubpass = 0;
        dependency.dstAccessMask =
                VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT;
        dependency.dstStageMask =
                VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;
        dependency.srcSubpass = VK_SUBPASS_EXTERNAL;
        dependency.srcAccessMask = 0;
        dependency.srcStageMask =
                VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;

        attachments.push_back(depthAttachment);
        VkRenderPassCreateInfo renderPassInfo = {};
        renderPassInfo.sType = VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO;
        renderPassInfo.attachmentCount = static_cast<uint32_t>(attachments.size());
        renderPassInfo.pAttachments = attachments.data();
        renderPassInfo.subpassCount = 1;
        renderPassInfo.pSubpasses = &subpass;
        renderPassInfo.dependencyCount = 1;
        renderPassInfo.pDependencies = &dependency;

        if (vkCreateRenderPass(device->getDevice(), &renderPassInfo, nullptr, &renderPass) != VK_SUCCESS) {
            throw std::runtime_error("failed to create render pass!");
        }
    }

    void createFrameBuffers(int amount, int width, int height, std::vector<VkImageView>& imagesToAttach,
                            const int imagePerStepAmount) {
        frameBuffers.resize(amount);
        for (unsigned int i = 0; i < amount; i++) {
            std::vector<VkImageView> attachments;
            for (int si = i * imagePerStepAmount; si < i * imagePerStepAmount + imagePerStepAmount; ++si) {
                attachments.push_back(imagesToAttach[si]);
            }
            attachments.push_back(depthImageViews[i]);
            VkFramebufferCreateInfo framebufferInfo = {};
            framebufferInfo.sType = VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
            framebufferInfo.renderPass = renderPass;
            framebufferInfo.attachmentCount = attachments.size();
            framebufferInfo.pAttachments = attachments.data();
            framebufferInfo.width = width;
            framebufferInfo.height = height;
            framebufferInfo.layers = 1;

            if (vkCreateFramebuffer(
                    device->getDevice(),
                    &framebufferInfo,
                    nullptr,
                    &frameBuffers[i]) != VK_SUCCESS) {
                throw std::runtime_error("failed to create framebuffer!");
            }
        }
    }

    VkFormat findDepthFormat() {
        return device->findSupportedFormat(
                {VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT},
                VK_IMAGE_TILING_OPTIMAL,
                VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT);
    }

};