package com.kgaft.VulkanContext.Vulkan.VulkanImmediateShaderData.VulkanPushConstants;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.*;
import org.lwjgl.vulkan.VK13;

public class VulkanPushConstantManager {
    private List<VulkanPushConstant> pushConstants = new ArrayList<>();
    
    public void registerPushConstant(VulkanPushConstant pushConstant){
        pushConstants.add(pushConstant);
    }
    
    public void loadConstantsToShader(VkCommandBuffer cmd, long pipelineLayout){
        for(VulkanPushConstant item : pushConstants){
            VK13.vkCmdPushConstants(cmd, pipelineLayout, item.getShaderStages(), 0, item.getData());
        }
    }
}
