#pragma once

#include <vulkan/vulkan.h>
#include <vector>

namespace PipelineConfiguration
{
    struct PipelineConfigInfo
    {
        VkViewport viewport;
        VkRect2D scissor;
        VkPipelineInputAssemblyStateCreateInfo inputAssemblyInfo;
        VkPipelineRasterizationStateCreateInfo rasterizationInfo;
        VkPipelineMultisampleStateCreateInfo multisampleInfo;
        std::vector<VkPipelineColorBlendAttachmentState> colorBlendAttachments;
        VkPipelineColorBlendStateCreateInfo colorBlendInfo;
        VkPipelineDepthStencilStateCreateInfo depthStencilInfo;

        uint32_t subpass = 0;
    };

    void defaultPipelineConfigInfo(PipelineConfigInfo& output, unsigned int width, unsigned int height, int attachmentCount, bool alphaBlending);
}