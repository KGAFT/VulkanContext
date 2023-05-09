#pragma once

#include <vulkan/vulkan.h>
#include "../VulkanDevice/VulkanDevice.h"
#include "PipelineEndConfiguration.h"

class GraphicsPipelineConfigurer
{
    friend class VulkanGraphicsPipeline;

public:
    GraphicsPipelineConfigurer(VulkanDevice *device, PipelineEndConfig *endConfiguration);

private:
    VkPipelineLayout pipelineLayout;
    VkDescriptorSetLayout descriptorSetLayout = VK_NULL_HANDLE;
    VulkanDevice *device;
    VkVertexInputBindingDescription inputBindDesc;
    std::vector<VkVertexInputAttributeDescription> inputAttribDescs;
    bool destroyed = false;

public:
    VkPipelineLayout getPipelineLayout();

    VkDescriptorSetLayout getDescriptorSetLayout();

    void destroy();

    ~GraphicsPipelineConfigurer();

private:
    void loadDescriptorSetLayout(PipelineEndConfig *endConfiguration);

    void loadPipelineLayout(PipelineEndConfig *endConfiguration);

    void prepareBinding(std::vector<VertexInput> &inputs);

    void prepareInputAttribs(std::vector<VertexInput> &inputs);

    VkPushConstantRange infoToRange(PushConstantInfo &info);

    VkDescriptorSetLayoutBinding uboToBind(UniformBufferInfo &bufferInfo);

    VkDescriptorSetLayoutBinding samplerToBind(SamplerInfo &samplerInfo);
};