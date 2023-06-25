package com.kgaft.VulkanContext.Vulkan.VulkanImmediateShaderData.VulkanPushConstants;

import java.nio.ByteBuffer;

public class VulkanPushConstant {
   private long size;
    private int shaderStages;
    private ByteBuffer data;
    
    public VulkanPushConstant(long size, int shaderStages){
        this.size = size;
        this.shaderStages = shaderStages;
        data = ByteBuffer.allocateDirect((int)size);
    }

    public long getSize() {
        return size;
    }

    public int getShaderStages() {
        return shaderStages;
    }

    public ByteBuffer getData() {
        return data;
    }
    
    
}
