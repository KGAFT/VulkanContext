//
// Created by kgaft on 5/23/23.
//
#pragma once

#include "Vulkan/VulkanDevice/VulkanDevice.h"
#include "../../External/ImGUI/imgui.h"
#include "../../External/ImGUI/imgui_impl_vulkan.h"
#include "../../External/ImGUI/imgui_impl_glfw.h"
#include <GLFW/glfw3.h>

class ImGUIVulkan{
public:
    static ImGUIVulkan* initializeForVulkan(VulkanDevice* device, VkRenderPass renderPass, GLFWwindow* window);
private:
    ImGUIVulkan(VulkanDevice *device, VkDescriptorPool descriptorPool);

    VulkanDevice* device;
    VkDescriptorPool descriptorPool;


public:

    void populateCommandBuffer(VkCommandBuffer cmd);

    ~ImGUIVulkan();
};

