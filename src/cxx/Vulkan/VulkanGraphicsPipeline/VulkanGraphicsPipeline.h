#pragma once

#include <vulkan/vulkan.h>
#include "PipelineImmediateConfig.h"
#include "../VulkanShader/VulkanShader.h"
#include "GraphicsPipelineConfigurer.h"
#include "../VulkanRenderingPipeline/VulkanRenderPass.h"

class VulkanGraphicsPipeline
{
private:
    static PipelineConfiguration::PipelineConfigInfo configInfo;
public:
    VulkanGraphicsPipeline(VulkanDevice *device,
                           GraphicsPipelineConfigurer *configurer, VulkanShader *shader,
                           unsigned int width, unsigned int height, int attachmentCount, bool alphaBlending, VkCullModeFlags culling, VkCompareOp depthSetup, VulkanRenderPass *renderPass);

private:
    VkPipeline graphicsPipeline;
    VulkanDevice *device;
    GraphicsPipelineConfigurer *configurer;
    VulkanShader *shader;

    VulkanRenderPass *renderPass;
    bool destroyed = false;

public:
    VkPipeline getGraphicsPipeline();

    GraphicsPipelineConfigurer *getConfigurer();

    ~VulkanGraphicsPipeline();

    void recreate(unsigned int width, unsigned int height, int attachmentCount, bool alphaBlending, VkCullModeFlags culling, VkCompareOp depthSetup, VulkanRenderPass *renderPass);

    void destroy();

    VulkanRenderPass *getRenderPass();



private:
    void create();
};