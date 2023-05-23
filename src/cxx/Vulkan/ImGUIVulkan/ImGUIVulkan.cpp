//
// Created by kgaft on 5/23/23.
//
#include "ImGUIVulkan.h"
ImGUIVulkan *ImGUIVulkan::initializeForVulkan(VulkanDevice *device, VkRenderPass renderPass, GLFWwindow *window) {
    VkDescriptorPoolSize poolSizes[] =
            {
                    { VK_DESCRIPTOR_TYPE_SAMPLER, 1000 },
                    { VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 1000 },
                    { VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE, 1000 },
                    { VK_DESCRIPTOR_TYPE_STORAGE_IMAGE, 1000 },
                    { VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER, 1000 },
                    { VK_DESCRIPTOR_TYPE_STORAGE_TEXEL_BUFFER, 1000 },
                    { VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 1000 },
                    { VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, 1000 },
                    { VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, 1000 },
                    { VK_DESCRIPTOR_TYPE_STORAGE_BUFFER_DYNAMIC, 1000 },
                    { VK_DESCRIPTOR_TYPE_INPUT_ATTACHMENT, 1000 }
            };


    VkDescriptorPoolCreateInfo poolInfo = {};
    poolInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
    poolInfo.flags = VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT;
    poolInfo.maxSets = 1000;
    poolInfo.poolSizeCount = std::size(poolSizes);
    poolInfo.pPoolSizes = poolSizes;

    VkDescriptorPool imguiPool;
    vkCreateDescriptorPool(device->getDevice(), &poolInfo, nullptr, &imguiPool);

    ImGui::CreateContext();
    ImGui_ImplGlfw_InitForVulkan(window, true);
    ImGui_ImplVulkan_InitInfo initInfo{};
    initInfo.Instance = device->getVkInstance();
    initInfo.PhysicalDevice = device->getDeviceToCreate();
    initInfo.Device = device->getDevice();
    initInfo.Queue = device->getGraphicsQueue();
    initInfo.DescriptorPool = imguiPool;
    initInfo.MinImageCount = 3;
    initInfo.ImageCount = 3;
    initInfo.MSAASamples = VK_SAMPLE_COUNT_1_BIT;
    ImGui_ImplVulkan_Init(&initInfo, renderPass);
    VkCommandBuffer cmd = device->beginSingleTimeCommands();
    ImGui_ImplVulkan_CreateFontsTexture(cmd);
    device->endSingleTimeCommands(cmd);

    ImGui_ImplVulkan_DestroyFontUploadObjects();

    return new ImGUIVulkan(device, imguiPool);
}

ImGUIVulkan::ImGUIVulkan(VulkanDevice *device, VkDescriptorPool descriptorPool) : device(device),
                                                                                  descriptorPool(descriptorPool) {}


void ImGUIVulkan::populateCommandBuffer(VkCommandBuffer cmd) {
    ImGui_ImplVulkan_RenderDrawData(ImGui::GetDrawData(), cmd);
}

ImGUIVulkan::~ImGUIVulkan() {
    vkDestroyDescriptorPool(device->getDevice(), descriptorPool, nullptr);
    ImGui_ImplVulkan_Shutdown();
}





