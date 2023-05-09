//
// Created by Daniil on 30.04.2023.
//
#pragma once

#include <vulkan/vulkan.h>
#include <vector>
#include "../VulkanDevice/VulkanDevice.h"
#include "../VulkanGraphicsPipeline/PipelineEndConfiguration.h"
#include "../VulkanImmediateShaderData/VulkanSampler/VulkanSampler.h"
#include "../VulkanImmediateShaderData/UniformBuffers/VulkanUniformBuffer.h"
class VulkanDescriptorSet
{
    friend class VulkanDescriptors;

private:
    VulkanDescriptorSet(VulkanDevice *device);

    std::vector<VkDescriptorSet> descriptorSets;
    std::vector<VulkanUniformBuffer *> uniformBuffers;
    std::vector<VulkanSampler *> samplers;
    VulkanDevice *device;
    void *attachInstance = nullptr;
    void initImmediate(PipelineEndConfig *endConfig);

public:
    void updateDescriptorSet(unsigned int currentDescriptor);

    void bind(unsigned int instanceNumber, VkCommandBuffer commandBuffer, VkPipelineLayout layout);

    void attachToObject(void *attachment);

    std::vector<VulkanUniformBuffer *> &getUniformBuffers();

    std::vector<VulkanSampler *> &getSamplers();

private:
    std::pair<VkDescriptorBufferInfo, VkDescriptorImageInfo>
    getChildOfObject(IDescriptorObject *object, unsigned int currentInstance);
};