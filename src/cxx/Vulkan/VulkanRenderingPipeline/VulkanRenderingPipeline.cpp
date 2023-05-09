#include "VulkanRenderingPipeline.h"

VulkanRenderingPipeline::VulkanRenderingPipeline(VulkanDevice *device, VulkanRenderPass *renderPass, VulkanSyncManager *syncManager, VulkanGraphicsPipeline *graphicsPipeline) : device(device),
                                                                                                                                                                                 renderPass(renderPass), graphicsPipeline(graphicsPipeline)
{
    control = new VulkanRenderPipelineControl(syncManager, device, renderPass);
}

void VulkanRenderingPipeline::setBackBufferColor(float red, float green, float blue, float alpha)
{
    clearColorValues[0] = red;
    clearColorValues[1] = green;
    clearColorValues[2] = blue;
    clearColorValues[3] = alpha;
}