package com.kgaft.VulkanContext.Vulkan.VulkanBuffer;

import org.lwjgl.vulkan.VkBufferCreateInfo;

import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;

class VulkanBufferBuilder{
  private int requiredSize = -1;
  private int requiredUsage = -1;
  private boolean createMapped = false;
  private int requiredProperties = -1;
  private int requiredSharingMode = -1;

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
   * @param createMapped enables feature that decreases time to populate buffer with data, but increase ram usage, required for uniform buffer
   */
  public void setCreateMapped(boolean createMapped) {
    this.createMapped = createMapped;
  }
  public int getRequiredProperties() {
    return requiredProperties;
  }
  /**
   * @param requiredProperties use vulkan defs, like VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
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
  protected void isPopulated() throws BuilderNotPopulatedException{
    if(requiredSize==-1){
      throw new BuilderNotPopulatedException("Error you forgot to specify required size for VkBuffer");
    }
    if(requiredUsage==-1){
      throw new BuilderNotPopulatedException("Error you forgot to specify required usage for VkBuffer");
    }
    if(requiredProperties==-1){
      throw new BuilderNotPopulatedException("Error you forgot to specify required properties for VkBuffer");
    }
     if(requiredSharingMode==-1){
      throw new BuilderNotPopulatedException("Error you forgot to specify required sharing mode for VkBuffer");
    }
  } 
  
}