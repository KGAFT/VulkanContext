package com.kgaft.VulkanContext.Vulkan.VulkanDescriptors;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

public interface IDescriptorObject {
    
    /**
     * 
     * @param output specify the dstSet before passing to method
     */
    
    void prepareWriteInfo(MemoryStack stack, VkWriteDescriptorSet.Buffer output, int currentInstanceIndex);
    
}
