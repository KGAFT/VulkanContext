#pragma once

#include <vulkan/vulkan.h>
#include <vector>
#include <map>
#include "../../../Util/ShaderConfParser.h"

class VulkanShader
{
private:
    std::vector<VkPipelineShaderStageCreateInfo> stages;
    bool destroyed = false;
    VulkanDevice* device;
public:
    VulkanShader(VulkanDevice* device, std::map<VkShaderModule, int> &toCreate) : device(device)
    {
        for (const auto pair : toCreate)
        {
            VkPipelineShaderStageCreateInfo stage = {};
            stage.sType = VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
            stage.stage = getShaderStage(pair.second);
            stage.module = pair.first;
            stage.pName = "main";
            stage.flags = 0;
            stage.pNext = nullptr;
            stages.push_back(stage);
        }
    }
    std::vector<VkPipelineShaderStageCreateInfo> &getStages()
    {
        return stages;
    }
    void destroy(){
        if(!destroyed){
            for(const auto& el : stages){
                vkDestroyShaderModule(device->getDevice(), el.module, nullptr);
            }
            destroyed = true;
        }
    }
    ~VulkanShader(){
        destroy();
    }
private:
    VkShaderStageFlagBits getShaderStage(int type)
    {
        switch (type)
        {
        case FRAGMENT_SHADER:
            return VK_SHADER_STAGE_FRAGMENT_BIT;
        case GEOMETRY_SHADER:
            return VK_SHADER_STAGE_GEOMETRY_BIT;
        case VERTEX_SHADER:
            return VK_SHADER_STAGE_VERTEX_BIT;
        default:
            return VK_SHADER_STAGE_VERTEX_BIT;
        }
    }
};