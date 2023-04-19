#pragma once

#include <vulkan/vulkan.h>
#include "PipelineImmediateConfig.h"
#include "VulkanShader/VulkanShader.h"
#include "GraphicsPipelineConfigurer.h"
#include "../VulkanRenderingPipeline/VulkanRenderPass.h"

class VulkanGraphicsPipeline{
private:
    VkPipeline graphicsPipeline;
    VulkanDevice* device;
    GraphicsPipelineConfigurer* configurer;
    VulkanShader* shader;
    PipelineConfiguration::PipelineConfigInfo configInfo;
    VulkanRenderPass* renderPass;
    bool destroyed = false;

public:
    VulkanGraphicsPipeline(VulkanDevice *device,
                           GraphicsPipelineConfigurer* configurer, VulkanShader *shader,
                           PipelineConfiguration::PipelineConfigInfo configInfo, VulkanRenderPass *renderPass)
            : device(device), configurer(configurer), shader(shader),
              configInfo(configInfo), renderPass(renderPass) {
        create();
    }

     VkPipeline getGraphicsPipeline()  {
        return graphicsPipeline;
    }

    GraphicsPipelineConfigurer *getConfigurer() {
        return configurer;
    }
    ~VulkanGraphicsPipeline(){
        destroy();
    }

    void recreate(PipelineConfiguration::PipelineConfigInfo configInfo, VulkanRenderPass *renderPass) {
        this->configInfo = configInfo;
        this->renderPass = renderPass;
        destroy();
        destroyed = false;
        create();
    }

    void destroy(){
        if(!destroyed){
            vkDestroyPipeline(device->getDevice(), graphicsPipeline, nullptr);
        }
    }

private:
    void create() {


        VkPipelineVertexInputStateCreateInfo vertexInputInfo{};
        vertexInputInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
        vertexInputInfo.vertexAttributeDescriptionCount =configurer->inputAttribDescs.size();
        vertexInputInfo.vertexBindingDescriptionCount = 1;
        vertexInputInfo.pVertexAttributeDescriptions = configurer->inputAttribDescs.data();
        vertexInputInfo.pVertexBindingDescriptions = &configurer->inputBindDesc;

        VkPipelineViewportStateCreateInfo viewportInfo{};
        viewportInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
        viewportInfo.viewportCount = 1;
        viewportInfo.pViewports = &configInfo.viewport;
        viewportInfo.scissorCount = 1;
        viewportInfo.pScissors = &configInfo.scissor;

        VkGraphicsPipelineCreateInfo pipelineInfo{};
        pipelineInfo.sType = VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
        pipelineInfo.stageCount = shader->getStages().size();
        pipelineInfo.pStages = shader->getStages().data();
        pipelineInfo.pVertexInputState = &vertexInputInfo;
        pipelineInfo.pInputAssemblyState = &configInfo.inputAssemblyInfo;
        pipelineInfo.pViewportState = &viewportInfo;
        pipelineInfo.pRasterizationState = &configInfo.rasterizationInfo;
        pipelineInfo.pMultisampleState = &configInfo.multisampleInfo;
        pipelineInfo.pColorBlendState = &configInfo.colorBlendInfo;
        pipelineInfo.pDepthStencilState = &configInfo.depthStencilInfo;
        pipelineInfo.pDynamicState = nullptr;

        pipelineInfo.layout = configurer->pipelineLayout;
        pipelineInfo.renderPass = renderPass->getRenderPass();
        pipelineInfo.subpass = configInfo.subpass;

        pipelineInfo.basePipelineIndex = -1;
        pipelineInfo.basePipelineHandle = VK_NULL_HANDLE;
        if (vkCreateGraphicsPipelines(
                device->getDevice(),
                VK_NULL_HANDLE,
                1,
                &pipelineInfo,
                nullptr,
                &graphicsPipeline) != VK_SUCCESS) {
            throw std::runtime_error("failed to create graphics pipeline");
        }
    }
};