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
#include "ImGUIVulkan/ImGUIVulkan.h"

class VulkanEndRenderPipeline
{
public:
    VulkanEndRenderPipeline(VulkanDevice *device, VulkanSyncManager *syncManager, VulkanShader *shader,
                            PipelineEndConfig *endConfig, int startFrameBufferWidth, int startFrameBufferHeight,
                            std::vector<VkImageView> &imageViews, int imagePerStepAmount, VkFormat imageFormat, bool alphaBlending, VkCullModeFlags culling, VkCompareOp depthSetup);

private:
    VulkanDevice *device;
    VulkanSyncManager *syncManager;
    VulkanShader *shader;
    std::vector<VkImageView> imageViews;
    int currentWidth;
    int currentHeight;
    bool destroyed = false;

    VulkanRenderPass *renderPass;
    VulkanGraphicsPipeline *graphicsPipeline;
    GraphicsPipelineConfigurer *configurer;
    VulkanRenderPipelineControl *control;

    std::vector<VulkanPushConstant *> pushConstants;

    int imagePerStepAmount = 0;
    VkFormat imageFormat;
    ImGUIVulkan* uiInstance = nullptr;
    VkRenderPassBeginInfo renderPassInfo{};
    float clearColorValues[4] = {0, 0, 0, 1};
    VkCommandBuffer currentCommandBuffer;
    VulkanDescriptors *descriptors = nullptr;
    VulkanPushConstantManager *manager;
    bool alphaBlendEnabled = false;
    VkCullModeFlags culling;
    VkCompareOp depthSetup;
public:
    /**
     * Update immediate  data before this
     */
    VkCommandBuffer beginRender();

    void updatePushConstants();

    VulkanDescriptorSet *acquireDescriptorSet();

    void bindImmediate(VulkanDescriptorSet *set);

    VkPipelineLayout getPipelineLayout();

    void endRenderPass();

    void endRender();

    void resized(int width, int height, std::vector<VkImageView> &newImageViews, int imagePerStepAmount,
                 VkFormat imageFormat);

    void resized(int width, int height);

    VkImageView getCurrentImage();

    std::vector<VkImageView> &getDepthImageViews();

    std::vector<VkImage> &getDepthImages();

    void destroy();

    ~VulkanEndRenderPipeline();

    std::vector<VulkanPushConstant *> &getPushConstants();

    VulkanGraphicsPipeline *getGraphicsPipeline();

    void initIMGUI(GLFWwindow* window);
private:
    const unsigned int prepareClearValues(VkClearValue *result) const;

    void createPushConstants(PipelineEndConfig *endConfig);

    void createRenderPass(int width, int height, int imagePerStepAmount, VkFormat imageFormat);

    void createGraphicsPipeline(PipelineEndConfig *endConfig, int width, int height, bool alphaBlending, VkCullModeFlags culling, VkCompareOp depthSetup);

    void createControl();
};