package com.kgaft.VulkanContext.Vulkan.VulkanImage;

public class VulkanImageView {
    private int type;
    private int arrayLayerIndex;
    private long handle;
    private int layerCount;
    private int mipLevel;
    private int mipLevelAmount;

    protected VulkanImageView(int type, int arrayLayerIndex, int layerCount, long handle) {
        this.type = type;
        this.arrayLayerIndex = arrayLayerIndex;
        this.handle = handle;
        this.layerCount = layerCount;
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

    protected void setMipLevelAmount(int mipLevelAmount) {
        this.mipLevelAmount = mipLevelAmount;
    }
}
