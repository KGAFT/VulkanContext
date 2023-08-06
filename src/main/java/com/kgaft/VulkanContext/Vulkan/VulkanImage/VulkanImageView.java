package com.kgaft.VulkanContext.Vulkan.VulkanImage;

import com.kgaft.VulkanContext.MemoryUtils.DestroyableObject;
import com.kgaft.VulkanContext.Vulkan.VulkanDevice.VulkanDevice;
import org.lwjgl.vulkan.VK13;

public class VulkanImageView extends DestroyableObject {
    private int type;
    private int arrayLayerIndex;
    private long handle;
    private int layerCount;
    private int mipLevel;
    private int mipLevelAmount;
    private VulkanDevice device;

    private VulkanImage image;
    protected VulkanImageView(VulkanDevice device, VulkanImage image, int type, int arrayLayerIndex, int layerCount, long handle) {
        this.device = device;
        this.type = type;
        this.arrayLayerIndex = arrayLayerIndex;
        this.handle = handle;
        this.layerCount = layerCount;
        this.image = image;
    }

    protected void setType(int type) {
        this.type = type;
    }

    protected void setArrayLayerIndex(int arrayLayerIndex) {
        this.arrayLayerIndex = arrayLayerIndex;
    }

    protected void setHandle(long handle) {
        this.handle = handle;
    }


    public int getType() {
        return type;
    }

    public int getArrayLayerIndex() {
        return arrayLayerIndex;
    }

    public long getHandle() {
        return handle;
    }

    public int getLayerCount() {
        return layerCount;
    }

    protected void setLayerCount(int layerCount) {
        this.layerCount = layerCount;
    }

    public int getMipLevel() {
        return mipLevel;
    }

    protected void setMipLevel(int mipLevel) {
        this.mipLevel = mipLevel;
    }

    public int getMipLevelAmount() {
        return mipLevelAmount;
    }

    public VulkanImage getImage() {
        return image;
    }



    protected void setMipLevelAmount(int mipLevelAmount) {
        this.mipLevelAmount = mipLevelAmount;
    }

    @Override
    public void destroy() {
        VK13.vkDestroyImageView(device.getDevice(), this.handle, null);
        super.destroy();
    }
}
