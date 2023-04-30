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
class VulkanDescriptorSet{
    friend class VulkanDescriptors;
private:
    std::vector<VkDescriptorSet> descriptorSets;
    std::vector<VulkanUniformBuffer*> uniformBuffers;
    std::vector<VulkanSampler*> samplers;
    VulkanDevice* device;
    VulkanDescriptorSet(VulkanDevice *device) : device(device) {

    }

    void initImmediate(PipelineEndConfig* endConfig){
        for (const auto &item: endConfig->samplers){
            auto* sampler = new VulkanSampler(device, item.binding);
            samplers.push_back(sampler);
        }
        for (const auto &item: endConfig->uniformBuffers){
            auto* uniformBuffer = new VulkanUniformBuffer(device, item.size, item.shaderStages, item.binding, descriptorSets.size());
            uniformBuffers.push_back(uniformBuffer);
        }
    }

    void* attachInstance = nullptr;
public:

    void updateDescriptorSet(unsigned int currentDescriptor){
        std::vector<VkWriteDescriptorSet> writes;
        std::vector<std::pair<VkDescriptorBufferInfo, VkDescriptorImageInfo> *> infos;
        for (const auto &item: samplers){
            auto *info = new std::pair<VkDescriptorBufferInfo, VkDescriptorImageInfo>(
                    getChildOfObject(item, currentDescriptor));
            VkWriteDescriptorSet write{};
            write.sType = VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
            write.dstSet = descriptorSets[currentDescriptor];
            write.dstBinding = item->getBinding();
            write.dstArrayElement = 0;
            write.descriptorType = item->getDescriptorType();
            write.descriptorCount = 1;
            write.pImageInfo = &info->second;
            writes.push_back(write);
            infos.push_back(info);
        }
        for (const auto &item: uniformBuffers){
            auto *info = new std::pair<VkDescriptorBufferInfo, VkDescriptorImageInfo>(
                    getChildOfObject(item, currentDescriptor));
            VkWriteDescriptorSet write{};
            write.sType = VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
            write.dstSet = descriptorSets[currentDescriptor];
            write.dstBinding = item->getBinding();
            write.dstArrayElement = 0;
            write.descriptorType = item->getDescriptorType();
            write.descriptorCount = 1;
            write.pBufferInfo = &info->first;
            writes.push_back(write);
            infos.push_back(info);
        }
        vkUpdateDescriptorSets(device->getDevice(), writes.size(), writes.data(), 0, nullptr);
        for (const auto &item: infos) {
            delete item;
        }
    }

    void bind(unsigned int instanceNumber, VkCommandBuffer commandBuffer, VkPipelineLayout layout) {
        if(attachInstance!=nullptr){
            vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, layout, 0, 1,
                                    &descriptorSets[instanceNumber], 0, nullptr);
        }
        else{
            throw std::runtime_error("You cannot bind not attached descriptor set");
        }
    }

    void attachToObject(void* attachment){
        this->attachInstance = attachment;
    }

    std::vector<VulkanUniformBuffer *> &getUniformBuffers()  {
        return uniformBuffers;
    }

     std::vector<VulkanSampler *> &getSamplers()  {
        return samplers;
    }

private:
    std::pair<VkDescriptorBufferInfo, VkDescriptorImageInfo>
    getChildOfObject(IDescriptorObject *object, unsigned int currentInstance) {
        std::pair<VkDescriptorBufferInfo, VkDescriptorImageInfo> result;
        if (object->getDescriptorType() == VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER) {
            result.second = {};
            result.second.imageLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
            result.second.imageView = object->getImageView();
            result.second.sampler = object->getSampler();
        } else {
            result.first = {};
            result.first.buffer = object->getBuffer(currentInstance);
            result.first.offset = 0;
            result.first.range = object->getBufferSize();
        }
        return result;
    }
};