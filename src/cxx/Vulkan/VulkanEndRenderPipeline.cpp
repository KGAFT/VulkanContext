#include "VulkanEndRenderPipeline.h"

VulkanEndRenderPipeline::VulkanEndRenderPipeline(VulkanDevice *device, VulkanSyncManager *syncManager, VulkanShader *shader,
                                                 PipelineEndConfig *endConfig, int startFrameBufferWidth, int startFrameBufferHeight,
                                                 std::vector<VkImageView> &imageViews, int imagePerStepAmount, VkFormat imageFormat)
    : imagePerStepAmount(imagePerStepAmount), device(device), imageFormat(imageFormat),
      syncManager(syncManager), shader(shader), currentWidth(startFrameBufferWidth), currentHeight(startFrameBufferHeight)
{
    this->imageViews.clear();
    for (auto item : imageViews)
    {
        this->imageViews.push_back(item);
    }
    createRenderPass(startFrameBufferWidth, startFrameBufferHeight, imagePerStepAmount, imageFormat);
    this->alphaBlendEnabled = endConfig->alphaBlend;
    createGraphicsPipeline(endConfig, startFrameBufferWidth, startFrameBufferHeight, alphaBlendEnabled);
    createControl();
    if (configurer->getDescriptorSetLayout() != VK_NULL_HANDLE)
    {
        descriptors = new VulkanDescriptors(device, endConfig, configurer->getDescriptorSetLayout(),
                                            syncManager->getCurrentMode());
    }

    manager = new VulkanPushConstantManager();
    createPushConstants(endConfig);

    for (const auto &item : pushConstants)
    {
        manager->registerPushConstant(item);
    }

    renderPassInfo.sType = VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
}

VkCommandBuffer VulkanEndRenderPipeline::beginRender()
{
    std::pair rendInfo = control->beginRender();
    renderPassInfo.renderPass = renderPass->getRenderPass();
    renderPassInfo.framebuffer = rendInfo.second;
    renderPassInfo.renderArea.offset = {0, 0};
    renderPassInfo.renderArea.extent = {static_cast<uint32_t>(currentWidth), static_cast<uint32_t>(currentHeight)};
    unsigned int clearValuesCount = prepareClearValues(nullptr);
    std::vector<VkClearValue> clearValuesData;
    clearValuesData.resize(clearValuesCount);

    prepareClearValues(clearValuesData.data());
    renderPassInfo.clearValueCount = clearValuesCount;
    renderPassInfo.pClearValues = clearValuesData.data();

    vkCmdBeginRenderPass(rendInfo.first, &renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
    vkCmdBindPipeline(rendInfo.first, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline->getGraphicsPipeline());

    currentCommandBuffer = rendInfo.first;
    return rendInfo.first;
}

void VulkanEndRenderPipeline::updatePushConstants()
{
    manager->loadConstantsToShader(currentCommandBuffer, configurer->getPipelineLayout());
}

VulkanDescriptorSet *VulkanEndRenderPipeline::acquireDescriptorSet()
{
    return descriptors->acquireDescriptorSet();
}

void VulkanEndRenderPipeline::bindImmediate(VulkanDescriptorSet *set)
{
    set->bind(control->getCurrentCmd(), currentCommandBuffer, getPipelineLayout());
}

VkPipelineLayout VulkanEndRenderPipeline::getPipelineLayout()
{
    return configurer->getPipelineLayout();
}

void VulkanEndRenderPipeline::endRender()
{
    if(uiInstance!=nullptr){
        uiInstance->populateCommandBuffer(currentCommandBuffer);
    }

    vkCmdEndRenderPass(currentCommandBuffer);

    control->endRender();
}

void VulkanEndRenderPipeline::resized(int width, int height, std::vector<VkImageView> &newImageViews, int imagePerStepAmount,
                                      VkFormat imageFormat)
{
    vkDeviceWaitIdle(device->getDevice());
    currentWidth = width;
    currentHeight = height;
    if (syncManager->getSwapChain() != nullptr)
    {
        syncManager->getSwapChain()->recreate(width, height);
        imageViews = syncManager->getSwapChain()->getSwapChainImageViews();
        this->imagePerStepAmount = 1;
        this->imageFormat = syncManager->getSwapChain()->getSwapChainImageFormat();
    }
    else
    {
        this->imageViews.clear();
        for (auto item : newImageViews)
        {
            this->imageViews.push_back(item);
        }
        this->imagePerStepAmount = imagePerStepAmount;
        this->imageFormat = imageFormat;
    }
    renderPass->recreate(imageViews, width, height, this->imagePerStepAmount, &this->imageFormat, 1);
    graphicsPipeline->recreate(
        PipelineConfiguration::defaultPipelineConfigInfo(width, height, renderPass->getAttachmentCount(), alphaBlendEnabled),
        renderPass);
    control->setRenderPass(renderPass);
}

void VulkanEndRenderPipeline::resized(int width, int height)
{
    std::vector<VkImageView> img;
    resized(width, height, img, this->imagePerStepAmount, VK_FORMAT_R32G32B32_SFLOAT);
}

VkImageView VulkanEndRenderPipeline::getCurrentImage()
{
    return imageViews[control->getCurrentCmd()];
}

std::vector<VkImageView> &VulkanEndRenderPipeline::getDepthImageViews()
{
    return renderPass->getDepthImageViews();
}

std::vector<VkImage> &VulkanEndRenderPipeline::getDepthImages()
{
    return renderPass->getDepthImages();
}

void VulkanEndRenderPipeline::destroy()
{
    if (!destroyed)
    {
        vkDeviceWaitIdle(device->getDevice());

        for (const auto &item : pushConstants)
        {
            delete item;
        }
        pushConstants.clear();
        if (manager != nullptr)
        {
            delete manager;
        }
        if (descriptors != nullptr)
        {
            delete descriptors;
        }
        delete control;
        delete graphicsPipeline;
        if(uiInstance!=nullptr){
            delete uiInstance;
        }
        delete renderPass;
        delete configurer;
        destroyed = true;
    }
}

VulkanEndRenderPipeline::~VulkanEndRenderPipeline()
{
    destroy();
}

std::vector<VulkanPushConstant *> &VulkanEndRenderPipeline::getPushConstants()
{
    return pushConstants;
}

const unsigned int VulkanEndRenderPipeline::prepareClearValues(VkClearValue *result) const
{
    if (result == nullptr)
    {
        return renderPass->getAttachmentCount() + 1;
    }
    for (int i = 0; i < renderPass->getAttachmentCount(); ++i)
    {
        result[i].color = {clearColorValues[0], clearColorValues[1], clearColorValues[2], clearColorValues[3]};
    }
    result[renderPass->getAttachmentCount()].depthStencil = {1.0f, 0};
    return renderPass->getAttachmentCount() + 1;
}

void VulkanEndRenderPipeline::createPushConstants(PipelineEndConfig *endConfig)
{
    for (const auto &item : endConfig->pushConstantInfos)
    {
        pushConstants.push_back(new VulkanPushConstant(item.size, item.shaderStages));
    }
}

void VulkanEndRenderPipeline::createRenderPass(int width, int height, int imagePerStepAmount, VkFormat imageFormat)
{
    this->imagePerStepAmount = imagePerStepAmount;
    renderPass = new VulkanRenderPass(device, imageViews, width, height, imagePerStepAmount, &imageFormat, 1,
                                      syncManager->getSwapChain() !=
                                          nullptr);
}

void VulkanEndRenderPipeline::createGraphicsPipeline(PipelineEndConfig *endConfig, int width, int height, bool alphaBlending)
{
    configurer = new GraphicsPipelineConfigurer(device, endConfig);
    graphicsPipeline = new VulkanGraphicsPipeline(device, configurer, shader,
                                                  PipelineConfiguration::defaultPipelineConfigInfo(width, height,
                                                                                                   renderPass->getAttachmentCount(), alphaBlending),
                                                  renderPass);
}

void VulkanEndRenderPipeline::createControl()
{
    control = new VulkanRenderPipelineControl(syncManager, device, renderPass);
}

VulkanGraphicsPipeline *VulkanEndRenderPipeline::getGraphicsPipeline() {
    return graphicsPipeline;
}

void VulkanEndRenderPipeline::initIMGUI(GLFWwindow *window) {
    uiInstance = ImGUIVulkan::initializeForVulkan(device,renderPass->getRenderPass(), window);
}
