package com.kgaft.VulkanContext.Vulkan.VulkanBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkOffset3D;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import com.kgaft.VulkanContext.Exceptions.BufferException;
import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;
import com.kgaft.VulkanContext.MemoryUtils.DestroyableObject;
import com.kgaft.VulkanContext.MemoryUtils.MemoryUtils;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanQueue;

import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBufferToImage;
import static org.lwjgl.vulkan.VK10.vkCreateBuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetBufferMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
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

  public void copyBufferToImage(VulkanQueue queue, long image, int width, int height,
      int layerCount, int imageLayout) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      copyBufferToImage(stack, queue, image, width, height, layerCount, imageLayout);
    }
  }

  public void copyBufferToImage(MemoryStack stack, VulkanQueue queue, long image, int width, int height,
      int layerCount, int imageLayout) {
    VkCommandBuffer cmd = queue.beginSingleTimeCommands(stack);
    copyBufferToImage(cmd, stack, image, width, height, layerCount, imageLayout);
    queue.endSingleTimeCommands(cmd, stack);
  }

  public void copyBufferToImage(VkCommandBuffer cmd, MemoryStack stack, long image, int width, int height,
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

    vkCmdCopyBufferToImage(cmd, buffer, image, imageLayout, region);
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
