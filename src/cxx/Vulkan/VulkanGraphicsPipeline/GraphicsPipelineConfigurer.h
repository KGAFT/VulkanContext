#pragma once

#include <vulkan/vulkan.h>
#include "../VulkanDevice/VulkanDevice.h"
#include "PipelineEndConfiguration.h"

class GraphicsPipelineConfigurer
{
    friend class VulkanGraphicsPipeline;
private:
    VkPipelineLayout pipelineLayout;
    VkDescriptorSetLayout descriptorSetLayout = VK_NULL_HANDLE;
    VulkanDevice *device;
    VkVertexInputBindingDescription inputBindDesc;
    std::vector<VkVertexInputAttributeDescription> inputAttribDescs;
    bool destroyed = false;
public:
    GraphicsPipelineConfigurer(VulkanDevice *device, PipelineEndConfig* endConfiguration) : device(device)
    {
        loadDescriptorSetLayout(endConfiguration);
        loadPipelineLayout(endConfiguration);
        prepareBinding(endConfiguration->vertexInputs);
        prepareInputAttribs(endConfiguration->vertexInputs);
    }

     VkPipelineLayout getPipelineLayout(){
        return pipelineLayout;
    }

    VkDescriptorSetLayout getDescriptorSetLayout(){
        return descriptorSetLayout;
    }
    void destroy(){
        if(!destroyed){
            vkDestroyPipelineLayout(device->getDevice(), pipelineLayout, nullptr);
            if(descriptorSetLayout!=VK_NULL_HANDLE){
                vkDestroyDescriptorSetLayout(device->getDevice(), descriptorSetLayout, nullptr);
            }
            destroyed = true;
        }
    }
    ~GraphicsPipelineConfigurer(){
        destroy();
    }
private:
    void loadDescriptorSetLayout(PipelineEndConfig *endConfiguration)
    {
        std::vector<VkDescriptorSetLayoutBinding> bindings;
        for (auto &element : endConfiguration->uniformBuffers)
        {
            bindings.push_back(uboToBind(element));
        }
        for (auto &element : endConfiguration->samplers)
        {
            bindings.push_back(samplerToBind(element));
        }
        if (bindings.size() != 0)
        {
            VkDescriptorSetLayoutCreateInfo layoutInfo{};
            layoutInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
            layoutInfo.bindingCount = bindings.size();
            layoutInfo.pBindings = bindings.data();

            if (vkCreateDescriptorSetLayout(device->getDevice(), &layoutInfo, nullptr, &descriptorSetLayout) !=
                VK_SUCCESS)
            {
                throw std::runtime_error("failed to create descriptor set layout!");
            }
        }
    }

    void loadPipelineLayout(PipelineEndConfig *endConfiguration)
    {
        std::vector<VkPushConstantRange> pushConstantRanges;
        for (auto &element : endConfiguration->pushConstantInfos)
        {
            pushConstantRanges.push_back(infoToRange(element));
        }
        VkPipelineLayoutCreateInfo pipelineLayoutInfo{};
        pipelineLayoutInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
        pipelineLayoutInfo.setLayoutCount = 0;
        if (descriptorSetLayout != VK_NULL_HANDLE)
        {
            pipelineLayoutInfo.setLayoutCount = 1;
            pipelineLayoutInfo.pSetLayouts = &descriptorSetLayout;
        }

        pipelineLayoutInfo.pPushConstantRanges = pushConstantRanges.data();
        pipelineLayoutInfo.pushConstantRangeCount = pushConstantRanges.size();

        if (vkCreatePipelineLayout(device->getDevice(), &pipelineLayoutInfo, nullptr, &pipelineLayout) !=
            VK_SUCCESS)
        {
            throw std::runtime_error("failed to create pipeline layout!");
        }
    }

    void prepareBinding(std::vector<VertexInput>& inputs){
        size_t size = 0;
        for(const auto& element: inputs){
            size+=element.typeSize*element.coordinatesAmount;
        }
        inputBindDesc.binding = 0;
        inputBindDesc.stride = size;
        inputBindDesc.inputRate = VK_VERTEX_INPUT_RATE_VERTEX;
    }

    void prepareInputAttribs(std::vector<VertexInput>& inputs){
        size_t offsetCount = 0;
        for(const auto& element: inputs){
            VkVertexInputAttributeDescription attribDesc{};
            attribDesc.binding = 0;
            attribDesc.location = element.location;
            attribDesc.offset = offsetCount;
            attribDesc.format = element.format;
            inputAttribDescs.push_back(attribDesc);
            offsetCount+=element.typeSize*element.coordinatesAmount;
        }
    }


    VkPushConstantRange infoToRange(PushConstantInfo &info)
    {
        VkPushConstantRange range{};
        range.size = info.size;
        range.offset = 0;
        range.stageFlags = info.shaderStages;
        return range;
    }

    VkDescriptorSetLayoutBinding uboToBind(UniformBufferInfo &bufferInfo)
    {
        VkDescriptorSetLayoutBinding result{};
        result.binding = bufferInfo.binding;
        result.descriptorType = VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
        result.descriptorCount = 1;
        result.stageFlags = bufferInfo.shaderStages;
        return result;
    }
    VkDescriptorSetLayoutBinding samplerToBind(SamplerInfo &samplerInfo)
    {
        VkDescriptorSetLayoutBinding result{};
        result.binding = samplerInfo.binding;
        result.descriptorType = VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
        result.descriptorCount = 1;
        result.stageFlags = samplerInfo.shaderStages;
        return result;
    }
};