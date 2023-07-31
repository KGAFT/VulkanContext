package com.kgaft.VulkanContext.Vulkan.VulkanBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;
import com.kgaft.VulkanContext.MemoryUtils.DestroyableObject;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;

import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkCreateBuffer;
import static org.lwjgl.vulkan.VK10.vkGetBufferMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;
import static org.lwjgl.vulkan.VK13.*;

import java.nio.ByteBuffer;

public class VulkanBuffer extends DestroyableObject {
  private long buffer;
  private long bufferMemory;
  private int size;
  private VulkanDevice device;
  private PointerBuffer mapPoint;
  private boolean mapped = false;
  private int defaultMapFlags;

  public VulkanBuffer(VulkanDevice device, VulkanBufferBuilder bufferBuilder) throws BuilderNotPopulatedException {
    this.device = device;
    bufferBuilder.isPopulated();
    createBuffer(bufferBuilder);
    mapPoint = PointerBuffer.allocateDirect(1);
    if (bufferBuilder.isCreateMapped()) {
      vkMapMemory(device.getDevice(), bufferMemory, 0, size, bufferBuilder.getMapFlags(), mapPoint);
      mapped = true;
    }
    this.defaultMapFlags = bufferBuilder.getMapFlags();
  }

  public void writeData(ByteBuffer data, long offset, int mapFlags) {
    data.limit(size);
    if (!mapped) {
      
      vkMapMemory(device.getDevice(), bufferMemory, offset, size, mapFlags == 0 ? defaultMapFlags : mapFlags, mapPoint);
      mapPoint.put(data);
      vkUnmapMemory(device.getDevice(), bufferMemory);
    } else {
      if (offset != 0) {
        vkUnmapMemory(device.getDevice(), bufferMemory);
        vkMapMemory(device.getDevice(), bufferMemory, offset, size, mapFlags == 0 ? defaultMapFlags : mapFlags,
            mapPoint);
        mapPoint.put(data);
        vkUnmapMemory(device.getDevice(), bufferMemory);
        vkMapMemory(device.getDevice(), bufferMemory, 0, size, defaultMapFlags, mapPoint);
      }
    }
    mapPoint.rewind();
    data.limit(size).rewind();
  }

  public void getData(ByteBuffer output, long offset, long size, int mapFlags){
    if(!mapped){
      vkMapMemory(device.getDevice(), bufferMemory, offset, Math.min(this.size, size), mapFlags == 0 ? defaultMapFlags : mapFlags, mapPoint);
      
      ByteBuffer temp = mapPoint.getByteBuffer(0,size);
      output.put(temp);
      vkUnmapMemory(device.getDevice(), bufferMemory);
    }
    else{
      output.put(0, mapPoint.getByteBuffer(this.size), (int)offset, (int)size);
    }
    mapPoint.rewind();
  }

  private void createBuffer(VulkanBufferBuilder bufferBuilder) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkBufferCreateInfo createInfo = VkBufferCreateInfo.calloc(stack);
      createInfo.sType$Default();
      createInfo.size(bufferBuilder.getRequiredSize());
      createInfo.usage(bufferBuilder.getRequiredUsage());
      createInfo.sharingMode(bufferBuilder.getRequiredSharingMode());
      long[] tempRes = new long[1];
      if (vkCreateBuffer(device.getDevice(), createInfo, null, tempRes) != VK_SUCCESS) {
        throw new RuntimeException("Failed to create buffer");
      }
      this.buffer = tempRes[0];
      VkMemoryRequirements memRequirements = VkMemoryRequirements.calloc(stack);
      vkGetBufferMemoryRequirements(device.getDevice(), tempRes[0], memRequirements);

      VkMemoryAllocateInfo allocateInfo = VkMemoryAllocateInfo.calloc(stack);
      allocateInfo.sType$Default();
      allocateInfo.allocationSize(memRequirements.size());
      allocateInfo.memoryTypeIndex(
          findMemoryType(memRequirements.memoryTypeBits(), bufferBuilder.getRequiredProperties(), stack));
      tempRes[0] = 0;
      if (vkAllocateMemory(device.getDevice(), allocateInfo, null, tempRes) != VK_SUCCESS) {
        throw new RuntimeException("Failed to allocate memory for buffer");
      }
      this.bufferMemory = tempRes[0];
      this.size = bufferBuilder.getRequiredSize();
    }
  }

  private int findMemoryType(int typeFilter, int properties, MemoryStack stack) {
    VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.calloc(stack);
    vkGetPhysicalDeviceMemoryProperties(device.getBaseDevice(), memProperties);
    for (int i = 0; i < memProperties.memoryTypeCount(); i++) {
      if ((typeFilter & (int) (1 << i)) > 0
          && (memProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
        return i;
      }
    }
    throw new RuntimeException("Failed to find suitable memory type");
  }

  @Override
  public void destroy() {
    if (mapped) {
      vkUnmapMemory(device.getDevice(), bufferMemory);
    }
    vkDestroyBuffer(device.getDevice(), buffer, null);
    vkFreeMemory(device.getDevice(), bufferMemory, null);
    super.destroy();
  }

}
