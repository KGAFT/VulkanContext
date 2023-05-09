#pragma once

#include <vulkan/vulkan.h>
#include "PipelineImmediateConfig.h"
#include "VulkanShader/VulkanShader.h"
#include "GraphicsPipelineConfigurer.h"
#include "../VulkanRenderingPipeline/VulkanRenderPass.h"

class VulkanGraphicsPipeline
{
public:
    VulkanGraphicsPipeline(VulkanDevice *device,
                           GraphicsPipelineConfigurer *configurer, VulkanShader *shader,
                           PipelineConfiguration::PipelineConfigInfo configInfo, VulkanRenderPass *renderPass);

private:
    VkPipeline graphicsPipeline;
    VulkanDevice *device;
    GraphicsPipelineConfigurer *configurer;
    VulkanShader *shader;
    PipelineConfiguration::PipelineConfigInfo configInfo;
    VulkanRenderPass *renderPass;
    bool destroyed = false;

public:
    VkPipeline getGraphicsPipeline();

    GraphicsPipelineConfigurer *getConfigurer();

    ~VulkanGraphicsPipeline();

    void recreate(PipelineConfiguration::PipelineConfigInfo configInfo, VulkanRenderPass *renderPass);

    void destroy();

private:
    void create();
};