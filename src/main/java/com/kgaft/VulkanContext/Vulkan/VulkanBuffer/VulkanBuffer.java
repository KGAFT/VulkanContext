package com.kgaft.VulkanContext.Vulkan.VulkanBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import com.kgaft.VulkanContext.Exceptions.BufferException;
import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;
import com.kgaft.VulkanContext.MemoryUtils.DestroyableObject;
import com.kgaft.VulkanContext.MemoryUtils.MemoryUtils;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanQueue;

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
  private int propertiesFlags;

  public VulkanBuffer(VulkanDevice device, VulkanBufferBuilder bufferBuilder) throws BuilderNotPopulatedException {
    this.device = device;
    bufferBuilder.isPopulated();
    createBuffer(bufferBuilder);
    mapPoint = PointerBuffer.allocateDirect(1);
    if (bufferBuilder.isCreateMapped()) {
      vkMapMemory(device.getDevice(), bufferMemory, 0, size, bufferBuilder.getMapFlags(), mapPoint);
      mapped = true;
    }
    this.propertiesFlags = bufferBuilder.getRequiredProperties();
    this.defaultMapFlags = bufferBuilder.getMapFlags();
  }

  public void writeData(ByteBuffer data, long offset, int mapFlags) throws BufferException {
    if ((propertiesFlags & VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT) == 0) {
      throw new BufferException("Error you cannot read and write data to not visible host buffer!");
    }
    if (!mapped) {

      vkMapMemory(device.getDevice(), bufferMemory, offset, size, mapFlags == 0 ? defaultMapFlags : mapFlags, mapPoint);
      MemoryUtils.memcpy(mapPoint.getByteBuffer(0, data.remaining()), data, data.remaining());
      vkUnmapMemory(device.getDevice(), bufferMemory);
    } else {
      if (offset != 0) {
        vkUnmapMemory(device.getDevice(), bufferMemory);
        vkMapMemory(device.getDevice(), bufferMemory, offset, size, mapFlags == 0 ? defaultMapFlags : mapFlags,
            mapPoint);
        MemoryUtils.memcpy(mapPoint.getByteBuffer(0, data.remaining()), data, data.remaining());
        vkUnmapMemory(device.getDevice(), bufferMemory);
        vkMapMemory(device.getDevice(), bufferMemory, 0, size, defaultMapFlags, mapPoint);
      } else {
        MemoryUtils.memcpy(mapPoint.getByteBuffer(0, data.remaining()), data, data.remaining());
      }
    }
    mapPoint.rewind();
  }

  public long getBuffer() {
    return buffer;
  }

  public void getData(ByteBuffer output, long offset, long size, int mapFlags) throws BufferException {
    if ((propertiesFlags & VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT) == 0) {
      throw new BufferException("Error you cannot read and write data to not visible host buffer!");
    }
    if (!mapped) {
      vkMapMemory(device.getDevice(), bufferMemory, offset, Math.min(this.size, offset + size),
          mapFlags == 0 ? defaultMapFlags : mapFlags, mapPoint);
      MemoryUtils.memcpy(output, mapPoint.getByteBuffer(0, (int) size), (int) size);
      vkUnmapMemory(device.getDevice(), bufferMemory);
    } else {
      if (offset != 0) {
        vkUnmapMemory(device.getDevice(), bufferMemory);
        vkMapMemory(device.getDevice(), bufferMemory, offset, size, mapFlags == 0 ? defaultMapFlags : mapFlags,
            mapPoint);
        MemoryUtils.memcpy(output, mapPoint.getByteBuffer(0, (int) size), (int) size);
        vkUnmapMemory(device.getDevice(), bufferMemory);
        vkMapMemory(device.getDevice(), bufferMemory, 0, size, defaultMapFlags, mapPoint);
      } else {
        MemoryUtils.memcpy(output, mapPoint.getByteBuffer(0, (int) size), (int) size);
      }
    }
    mapPoint.rewind();
  }

  public void copyToBuffer(VulkanBuffer dst, VulkanQueue queue, int srcOffset, int dstOffset, int size) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkCommandBuffer commandBuffer = queue.beginSingleTimeCommands(stack);
      copyToBuffer(commandBuffer, stack, dst, srcOffset, dstOffset, size);
      queue.endSingleTimeCommands(commandBuffer, stack);
    }
  }

  public void copyToBuffer(MemoryStack stack, VulkanBuffer dst, VulkanQueue queue, int srcOffset, int dstOffset,
      int size) {
    VkCommandBuffer commandBuffer = queue.beginSingleTimeCommands(stack);
    copyToBuffer(commandBuffer, stack, dst, srcOffset, dstOffset, size);
    queue.endSingleTimeCommands(commandBuffer, stack);
  }

  public void copyToBuffer(VkCommandBuffer cmd, MemoryStack stack, VulkanBuffer dst, int srcOffset, int dstOffset,
      int size) {
    VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc(1, stack);
    copyRegion.srcOffset(srcOffset); // Optional
    copyRegion.dstOffset(dstOffset); // Optional
    copyRegion.size(size);
    vkCmdCopyBuffer(cmd, buffer, dst.buffer, copyRegion);
  }

  public void copyFromBuffer(VulkanBuffer src, VulkanQueue queue, int srcOffset, int dstOffset,
      int size) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      copyFromBuffer(stack, src, queue, srcOffset, dstOffset, size);
    }
  }

  public void copyFromBuffer(MemoryStack stack, VulkanBuffer src, VulkanQueue queue, int srcOffset, int dstOffset,
      int size) {
    VkCommandBuffer commandBuffer = queue.beginSingleTimeCommands(stack);
    copyFromBuffer(commandBuffer, stack, src, srcOffset, dstOffset, size);
    queue.endSingleTimeCommands(commandBuffer, stack);
  }

  public void copyFromBuffer(VkCommandBuffer cmd, MemoryStack stack, VulkanBuffer src, int srcOffset, int dstOffset,
      int size) {
    VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc(1, stack);
    copyRegion.srcOffset(srcOffset); // Optional
    copyRegion.dstOffset(dstOffset); // Optional
    copyRegion.size(size);
    vkCmdCopyBuffer(cmd, src.buffer, buffer, copyRegion);
  }

  public void copyBufferFromImage(VulkanQueue queue, long image, int width, int height,
      int layerCount, int imageLayout) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      copyBufferFromImage(stack, queue, image, width, height, layerCount, imageLayout);
    }
  }

  public void copyBufferFromImage(MemoryStack stack, VulkanQueue queue, long image, int width, int height,
      int layerCount, int imageLayout) {
    VkCommandBuffer cmd = queue.beginSingleTimeCommands(stack);
    copyBufferFromImage(cmd, stack, image, width, height, layerCount, imageLayout);
    queue.endSingleTimeCommands(cmd, stack);
  }

  public void copyBufferFromImage(VkCommandBuffer cmd, MemoryStack stack, long image, int width, int height,
      int layerCount, int imageLayout) {
    VkBufferImageCopy.Buffer region = VkBufferImageCopy.calloc(1, stack);
    region.bufferOffset(0);
    region.bufferRowLength(0);
    region.bufferImageHeight(0);
    region.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
    region.imageSubresource().mipLevel(0);
    region.imageSubresource().baseArrayLayer(0);
    region.imageSubresource().layerCount(layerCount);
    region.imageOffset(VkOffset3D.calloc(stack).x(0).y(0).z(0));
    region.imageExtent(VkExtent3D.calloc(stack).width(width).height(height).depth(1));

    vkCmdCopyImageToBuffer(cmd, image, imageLayout, buffer, region);
  }

  // 2

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
