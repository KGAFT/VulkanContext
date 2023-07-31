package com.kgaft.VulkanContext.Vulkan.VulkanBuffer;

import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;

public class VulkanBuffer {
  private long buffer;
  private long bufferMemory;
  private int size;
  private VulkanDevice device;
  
  public VulkanBuffer(VulkanDevice device, VulkanBufferBuilder bufferBuilder) throws BuilderNotPopulatedException{
    bufferBuilder.isPopulated();
    
  }
}
