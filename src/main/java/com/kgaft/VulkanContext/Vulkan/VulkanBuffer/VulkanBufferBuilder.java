package com.kgaft.VulkanContext.Vulkan.VulkanBuffer;

import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;

public class VulkanBufferBuilder {
  private static final int NOT_POPULATED = -951904341;

  private int requiredSize = NOT_POPULATED;
  private int requiredUsage = NOT_POPULATED;
  private boolean createMapped = false;
  private int requiredProperties = NOT_POPULATED;
  private int requiredSharingMode = NOT_POPULATED;
  private int mapFlags = 0;

  public int getRequiredSize() {
    return requiredSize;
  }

  public void setRequiredSize(int requiredSize) {
    this.requiredSize = requiredSize;
  }

  public int getRequiredUsage() {
    return requiredUsage;
  }


  /**
   * @param requiredUsage use vulkan defs, like VK_BUFFER_USAGE_TRANSFER_SRC_BIT
   */
  public void setRequiredUsage(int requiredUsage) {
    this.requiredUsage = requiredUsage;
  }

  public boolean isCreateMapped() {
    return createMapped;
  }

  /**
   * @param createMapped enables feature that decreases time to populate buffer
   *                     with data, but increase ram usage, required for uniform
   *                     buffer
   */
  public void setCreateMapped(boolean createMapped) {
    this.createMapped = createMapped;
  }

  public int getRequiredProperties() {
    return requiredProperties;
  }

  /**
   * @param requiredProperties use vulkan defs, like
   *                           VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
   */
  public void setRequiredProperties(int requiredProperties) {
    this.requiredProperties = requiredProperties;
  }

  public int getRequiredSharingMode() {
    return requiredSharingMode;
  }

  

  /**
   * @param requiredSharingMode use vulkan defs, like VK_SHARING_MODE_EXCLUSIVE
   */
  public void setRequiredSharingMode(int requiredSharingMode) {
    this.requiredSharingMode = requiredSharingMode;
  }

  protected void isPopulated() throws BuilderNotPopulatedException {
    if (requiredSize == NOT_POPULATED) {
      throw new BuilderNotPopulatedException("Error you forgot to specify required size for VkBuffer");
    }
    if (requiredUsage == NOT_POPULATED) {
      throw new BuilderNotPopulatedException("Error you forgot to specify required usage for VkBuffer");
    }
    if (requiredProperties == NOT_POPULATED) {
      throw new BuilderNotPopulatedException("Error you forgot to specify required properties for VkBuffer");
    }
    if (requiredSharingMode == NOT_POPULATED) {
      throw new BuilderNotPopulatedException("Error you forgot to specify required sharing mode for VkBuffer");
    }
  }

  public int getMapFlags() {
    return mapFlags;
  }

  public void setMapFlags(int mapFlags) {
    this.mapFlags = mapFlags;
  }



}