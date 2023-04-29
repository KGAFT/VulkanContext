//
// Created by KGAFT on 3/16/2023.
//
#pragma once

#include <array>
#include <vulkan/vulkan.h>
#include "VulkanSync/VulkanSyncManager.h"
#include "VulkanDevice/VulkanDevice.h"
#include "VulkanGraphicsPipeline/VulkanShader/VulkanShader.h"
#include "VulkanGraphicsPipeline/PipelineEndConfiguration.h"
#include "VulkanImmediateShaderData/UniformBuffers/VulkanUniformBuffer.h"
#include "VulkanImmediateShaderData/VulkanSampler/VulkanSampler.h"
#include "VulkanImmediateShaderData/PushConstants/VulkanPushConstant.h"
#include "VulkanRenderingPipeline/VulkanRenderPass.h"
#include "VulkanGraphicsPipeline/VulkanGraphicsPipeline.h"
#include "VulkanRenderingPipeline/VulkanRenderPipelineControl.h"
#include "VulkanDescriptors/VulkanDescriptors.h"
#include "VulkanImmediateShaderData/PushConstants/VulkanPushConstantManager.h"

class VulkanEndRenderPipeline
{
private:
    VulkanDevice *device;
    VulkanSyncManager *syncManager;
    VulkanShader *shader;
    std::vector<VkImageView> imageViews;
    int currentWidth;
    int currentHeight;
    bool destroyed = false;

private:
    VulkanRenderPass *renderPass;
    VulkanGraphicsPipeline *graphicsPipeline;
    GraphicsPipelineConfigurer *configurer;
    VulkanRenderPipelineControl *control;

    std::vector<VulkanUniformBuffer *> uniformBuffers;
    std::vector<VulkanSampler *> samplers;
    std::vector<VulkanPushConstant *> pushConstants;

    int imagePerStepAmount = 0;
    VkFormat imageFormat;

    VkRenderPassBeginInfo renderPassInfo{};
    float clearColorValues[4] = {0, 0, 0, 1};
    VkCommandBuffer currentCommandBuffer;
    VulkanDescriptors *descriptors = nullptr;
    VulkanPushConstantManager *manager;
    bool alphaBlendEnabled = false;

public:
    VulkanEndRenderPipeline(VulkanDevice *device, VulkanSyncManager *syncManager, VulkanShader *shader,
                            PipelineEndConfig *endConfig, int startFrameBufferWidth, int startFrameBufferHeight,
                            std::vector<VkImageView> &imageViews, int imagePerStepAmount, VkFormat imageFormat)
        : imagePerStepAmount(imagePerStepAmount), device(device), imageFormat(imageFormat),
          syncManager(syncManager), shader(shader), currentWidth(startFrameBufferWidth), currentHeight(startFrameBufferHeight)
    {
        this->imageViews.clear();
        for (auto item: imageViews){
            this->imageViews.push_back(item);
        }
        createRenderPass(startFrameBufferWidth, startFrameBufferHeight, imagePerStepAmount, imageFormat);
        this->alphaBlendEnabled = endConfig->alphaBlend;
        createGraphicsPipeline(endConfig, startFrameBufferWidth, startFrameBufferHeight, alphaBlendEnabled);
        createControl();
        if (configurer->getDescriptorSetLayout() != VK_NULL_HANDLE)
        {
            descriptors = new VulkanDescriptors(device, configurer->getDescriptorSetLayout(),
                                                syncManager->getCurrentMode());
        }

        manager = new VulkanPushConstantManager();
        createPushConstants(endConfig);

        for (const auto &item : pushConstants)
        {
            manager->registerPushConstant(item);
        }

        createSamplers(endConfig);
        createUniforms(endConfig);
        renderPassInfo.sType = VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
    }

    /**
     * Update immediate  data before this
     */
    VkCommandBuffer beginRender()
    {
        std::pair rendInfo = control->beginRender();
        renderPassInfo.renderPass = renderPass->getRenderPass();
        renderPassInfo.framebuffer = rendInfo.second;
        renderPassInfo.renderArea.offset = {0, 0};
        renderPassInfo.renderArea.extent = {static_cast<uint32_t>(currentWidth), static_cast<uint32_t>(currentHeight)};
        unsigned int clearValuesCount = prepareClearValues(nullptr);
        std::vector<VkClearValue> clearValuesData;
        clearValuesData.resize(clearValuesCount);

        prepareClearValues(clearValuesData.data());
        renderPassInfo.clearValueCount = clearValuesCount;
        renderPassInfo.pClearValues = clearValuesData.data();

        vkCmdBeginRenderPass(rendInfo.first, &renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
        vkCmdBindPipeline(rendInfo.first, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline->getGraphicsPipeline());

        currentCommandBuffer = rendInfo.first;
        return rendInfo.first;
    }
    std::vector<VkImageView>& getDepthImageViews(){
        return renderPass->getDepthImageViews();
    }
    std::vector<VkImage>& getDepthImages(){
        return renderPass->getDepthImages();
    }
    void updatePcs()
    {
        manager->loadConstantsToShader(currentCommandBuffer, configurer->getPipelineLayout());
    }

    void updateSamplers()
    {
        for (int i = 0; i < syncManager->getCurrentMode(); i++)
        {
            descriptors->writeDescriptorObjects(reinterpret_cast<IDescriptorObject **>(samplers.data()), samplers.size(), i);
        }
    }

    void updateUniforms(){
        for (int i = 0; i < syncManager->getCurrentMode(); i++)
        {
            descriptors->writeDescriptorObjects(reinterpret_cast<IDescriptorObject **>(uniformBuffers.data()), uniformBuffers.size(), i);
        }
    }
    void bindImmediate(){
        descriptors->bind(control->getCurrentCmd(), currentCommandBuffer, configurer->getPipelineLayout());
    }

    void endRender()
    {
        vkCmdEndRenderPass(currentCommandBuffer);

        control->endRender();
    }

    void resized(int width, int height, std::vector<VkImageView> &newImageViews, int imagePerStepAmount,
                 VkFormat imageFormat)
    {
        vkDeviceWaitIdle(device->getDevice());
        currentWidth = width;
        currentHeight = height;
        if (syncManager->getSwapChain() != nullptr)
        {
            syncManager->getSwapChain()->recreate(width, height);
            imageViews = syncManager->getSwapChain()->getSwapChainImageViews();
            this->imagePerStepAmount = 1;
            this->imageFormat = syncManager->getSwapChain()->getSwapChainImageFormat();
        }
        else
        {
            this->imageViews.clear();
            for (auto item: newImageViews){
                this->imageViews.push_back(item);
            }
            this->imagePerStepAmount = imagePerStepAmount;
            this->imageFormat = imageFormat;
        }
        renderPass->recreate(imageViews, width, height, this->imagePerStepAmount, &this->imageFormat, 1);
        graphicsPipeline->recreate(
            PipelineConfiguration::defaultPipelineConfigInfo(width, height, renderPass->getAttachmentCount(), alphaBlendEnabled),
            renderPass);
        control->setRenderPass(renderPass);
    }

    void resized(int width, int height)
    {
        std::vector<VkImageView> img;
        resized(width, height, img, this->imagePerStepAmount, VK_FORMAT_R32G32B32_SFLOAT);
    }

    VkImageView getCurrentImage()
    {
        return imageViews[control->getCurrentCmd()];
    }

    void destroy()
    {
        if (!destroyed)
        {
            vkDeviceWaitIdle(device->getDevice());
            for (const auto &item : uniformBuffers)
            {
                delete item;
            }
            uniformBuffers.clear();
            for (const auto &item : samplers)
            {
                delete item;
            }
            samplers.clear();
            for (const auto &item : pushConstants)
            {
                delete item;
            }
            pushConstants.clear();
            if (manager != nullptr)
            {
                delete manager;
            }
            if (descriptors != nullptr)
            {
                delete descriptors;
            }
            delete control;
            delete graphicsPipeline;
            delete renderPass;
            delete configurer;
            destroyed = true;
        }
    }

    ~VulkanEndRenderPipeline()
    {
        destroy();
    }

    std::vector<VulkanSampler *> &getSamplers()
    {
        return samplers;
    }

    std::vector<VulkanUniformBuffer *> &getUniformBuffers()
    {
        return uniformBuffers;
    }

    std::vector<VulkanPushConstant *> &getPushConstants()
    {
        return pushConstants;
    }

private:
    const unsigned int prepareClearValues(VkClearValue *result) const
    {
        if (result == nullptr)
        {
            return renderPass->getAttachmentCount() + 1;
        }
        for (int i = 0; i < renderPass->getAttachmentCount(); ++i)
        {
            result[i].color = {clearColorValues[0], clearColorValues[1], clearColorValues[2], clearColorValues[3]};
        }
        result[renderPass->getAttachmentCount()].depthStencil = {1.0f, 0};
        return renderPass->getAttachmentCount() + 1;
    }

    void createPushConstants(PipelineEndConfig *endConfig)
    {
        for (const auto &item : endConfig->pushConstantInfos)
        {
            pushConstants.push_back(new VulkanPushConstant(item.size, item.shaderStages));
        }
    }

    void createUniforms(PipelineEndConfig *endConfig)
    {
        for (const auto &item : endConfig->uniformBuffers)
        {
            uniformBuffers.push_back(new VulkanUniformBuffer(device, item.size, item.shaderStages, item.binding,
                                                             syncManager->getCurrentMode()));
        }
    }

    void createSamplers(PipelineEndConfig *endConfig)
    {
        for (const auto &item : endConfig->samplers)
        {
            samplers.push_back(new VulkanSampler(device, item.binding));
        }
    }

    void createRenderPass(int width, int height, int imagePerStepAmount, VkFormat imageFormat)
    {
        this->imagePerStepAmount = imagePerStepAmount;
        renderPass = new VulkanRenderPass(device, imageViews, width, height, imagePerStepAmount, &imageFormat, 1,
                                          syncManager->getSwapChain() !=
                                              nullptr);
    }

    void createGraphicsPipeline(PipelineEndConfig *endConfig, int width, int height, bool alphaBlending)
    {
        configurer = new GraphicsPipelineConfigurer(device, endConfig);
        graphicsPipeline = new VulkanGraphicsPipeline(device, configurer, shader,
                                                      PipelineConfiguration::defaultPipelineConfigInfo(width, height,
                                                                                                       renderPass->getAttachmentCount(), alphaBlending),
                                                      renderPass);
    }

    void createControl()
    {
        control = new VulkanRenderPipelineControl(syncManager, device, renderPass);
    }
};