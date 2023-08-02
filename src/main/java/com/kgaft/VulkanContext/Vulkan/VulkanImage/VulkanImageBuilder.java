package com.kgaft.VulkanContext.Vulkan.VulkanImage;

import com.kgaft.VulkanContext.Exceptions.BuilderNotPopulatedException;

public class VulkanImageBuilder{
    private static final int NOT_POPULATED = -951904341;

    private int width = NOT_POPULATED;
    private int height = NOT_POPULATED;
    private int mipLevels = NOT_POPULATED;
    private int format = NOT_POPULATED;
    private int tiling = NOT_POPULATED;
    private int initialLayout = NOT_POPULATED;
    private int samples = NOT_POPULATED;
    private int sharingMode = NOT_POPULATED;
    private int imageMemoryProperties = NOT_POPULATED;

    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public int getMipLevels() {
        return mipLevels;
    }
    public void setMipLevels(int mipLevels) {
        this.mipLevels = mipLevels;
    }
    public int getFormat() {
        return format;
    }
    public void setFormat(int format) {
        this.format = format;
    }
    public int getTiling() {
        return tiling;
    }
    public void setTiling(int tiling) {
        this.tiling = tiling;
    }
    public int getInitialLayout() {
        return initialLayout;
    }
    public void setInitialLayout(int initialLayout) {
        this.initialLayout = initialLayout;
    }
    public int getSamples() {
        return samples;
    }
    public void setSamples(int samples) {
        this.samples = samples;
    }
    public int getSharingMode() {
        return sharingMode;
    }
    public void setSharingMode(int sharingMode) {
        this.sharingMode = sharingMode;
    }

    public int getImageMemoryProperties() {
        return imageMemoryProperties;
    }
    public void setImageMemoryProperties(int imageMemoryProperties) {
        this.imageMemoryProperties = imageMemoryProperties;
    }
    

    protected void checkBuilder() throws BuilderNotPopulatedException{
        if(this.width == NOT_POPULATED || this.height==NOT_POPULATED){
            throw new BuilderNotPopulatedException("Error: you forgot to specify image extent");
        }
        if(this.initialLayout==NOT_POPULATED){
            throw new BuilderNotPopulatedException("Error: you forgot specify initial layout");
        }
        if(this.format==NOT_POPULATED){
            throw new BuilderNotPopulatedException("Error: you forgot to specify your image format");
        }
        if(this.mipLevels==NOT_POPULATED){
            throw new BuilderNotPopulatedException("Errpr: you forgot to specify initial mip levels");
        }
        if(this.samples==NOT_POPULATED){
            throw new BuilderNotPopulatedException("Error: you forgot to specify image samples");
        }
        if(this.sharingMode==NOT_POPULATED){
            throw new BuilderNotPopulatedException("Error: you forgot to specify image sharing mode");
        }
        if(this.tiling==NOT_POPULATED){
            throw new BuilderNotPopulatedException("Error: you forgot to specify image tiling");
        }
        if(this.imageMemoryProperties==NOT_POPULATED){
            throw new BuilderNotPopulatedException("Error: you forgot to specify image memory properties");
        }
    }
    
}