package com.kgaft.VulkanContext.Vulkan.VulkanImage;



public class ImageTarget {
    private int startLayerIndex;
    private int layersAmount;
    private int mipLevel;
    private int mipLevelCount;

    public int getStartLayerIndex() {
        return startLayerIndex;
    }

    public void setStartLayerIndex(int startLayerIndex) {
        this.startLayerIndex = startLayerIndex;
    }

    public int getLayersAmount() {
        return layersAmount;
    }

    public void setLayersAmount(int layersAmount) {
        this.layersAmount = layersAmount;
    }

    public int getMipLevel() {
        return mipLevel;
    }

    public void setMipLevel(int mipLevel) {
        this.mipLevel = mipLevel;
    }

    public int getMipLevelCount() {
        return mipLevelCount;
    }

    public void setMipLevelCount(int mipLevelCount) {
        this.mipLevelCount = mipLevelCount;
    }
}
