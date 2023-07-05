#include "VulkanImage.h"

VulkanImage *VulkanImage::createImage(VulkanDevice *device, unsigned int width, unsigned int height)
{
    VkImage image;
    device->createImage(width, height, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_TILING_OPTIMAL,
                        VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT,
                        image, true);
    VkDeviceMemory imageMemory;
    createImageMemory(device, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, imageMemory, image);
    transitionImageLayout(device, image, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_UNDEFINED,
                          VK_IMAGE_LAYOUT_GENERAL);
    return new VulkanImage(image, device, imageMemory, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_GENERAL, width, height);
}

VulkanImage *VulkanImage::createImageWithFormat(VulkanDevice *device, unsigned int width, unsigned int height, VkFormat format)
{
    VkImage image;
    device->createImage(width, height, format, VK_IMAGE_TILING_OPTIMAL,
                        VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT,
                        image, true);
    VkDeviceMemory imageMemory;
    createImageMemory(device, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, imageMemory, image);
    transitionImageLayout(device, image, format, VK_IMAGE_LAYOUT_UNDEFINED,
                          VK_IMAGE_LAYOUT_GENERAL);
    return new VulkanImage(image, device, imageMemory, format, VK_IMAGE_LAYOUT_GENERAL, width, height);
}

VulkanImage *VulkanImage::loadTexture(const char *pathToTexture, VulkanDevice *device)
{
    int imageWidth, imageHeight, imageChannels;
    stbi_uc *imageData = stbi_load(pathToTexture, &imageWidth, &imageHeight, &imageChannels, STBI_rgb_alpha);

    VulkanImage *image = loadBinTexture(device, reinterpret_cast<const char *>(imageData), imageWidth, imageHeight,
                                        imageChannels);
    stbi_image_free(imageData);
    return image;
}

VulkanImage *VulkanImage::loadBinTexture(VulkanDevice *device, const char *imageData, int imageWidth, int imageHeight,
                                                int numChannelsAmount)
{
    VkDeviceSize imageSize = imageWidth * imageHeight * 4;

    if (!imageData)
    {
        throw std::runtime_error("failed to load texture image!");
    }

    VkBuffer stagingBuffer;
    VkDeviceMemory stagingBufferMemory;
    device->createBuffer(imageSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                         VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBuffer,
                         stagingBufferMemory);

    void *data;
    vkMapMemory(device->getDevice(), stagingBufferMemory, 0, imageSize, 0, &data);
    memcpy(data, imageData, static_cast<size_t>(imageSize));
    vkUnmapMemory(device->getDevice(), stagingBufferMemory);

    VkImage image;
    VkDeviceMemory imageMemory;
    device->createImage(imageWidth, imageHeight, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_TILING_OPTIMAL,
                        VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                        image, false);

    createImageMemory(device, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, imageMemory, image);
    transitionImageLayout(device, image, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_UNDEFINED,
                          VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
    device->copyBufferToImage(stagingBuffer, image, imageWidth, imageHeight, 1);
    transitionImageLayout(device, image, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                          VK_IMAGE_LAYOUT_GENERAL);

    vkDestroyBuffer(device->getDevice(), stagingBuffer, nullptr);
    vkFreeMemory(device->getDevice(), stagingBufferMemory, nullptr);

    return new VulkanImage(image, device, imageMemory, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_GENERAL, imageWidth, imageHeight);
}

void VulkanImage::createImageMemory(VulkanDevice *device, VkMemoryPropertyFlags properties,
                                           VkDeviceMemory &imageMemory, VkImage &image)
{
    VkMemoryRequirements memRequirements;
    vkGetImageMemoryRequirements(device->getDevice(), image, &memRequirements);

    VkMemoryAllocateInfo allocInfo{};
    allocInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
    allocInfo.allocationSize = memRequirements.size;
    allocInfo.memoryTypeIndex = device->findMemoryType(memRequirements.memoryTypeBits, properties);

    if (vkAllocateMemory(device->getDevice(), &allocInfo, nullptr, &imageMemory) != VK_SUCCESS)
    {
        throw std::runtime_error("failed to allocate image memory!");
    }

    vkBindImageMemory(device->getDevice(), image, imageMemory, 0);
}

void VulkanImage::transitionImageLayout(VulkanDevice *device, VkImage image, VkFormat format, VkImageLayout oldLayout,
                                               VkImageLayout newLayout)
{
    VkCommandBuffer commandBuffer = device->beginSingleTimeCommands();

    VkImageMemoryBarrier barrier{};
    barrier.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
    barrier.oldLayout = oldLayout;
    barrier.newLayout = newLayout;
    barrier.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    barrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    barrier.image = image;
    barrier.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
    barrier.subresourceRange.baseMipLevel = 0;
    barrier.subresourceRange.levelCount = 1;
    barrier.subresourceRange.baseArrayLayer = 0;
    barrier.subresourceRange.layerCount = 1;

    VkPipelineStageFlags sourceStage;
    VkPipelineStageFlags destinationStage;

    if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
    {
        barrier.srcAccessMask = 0;
        barrier.dstAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;

        sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
        destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
    }
    else if (oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL &&
             newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
    {
        barrier.srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
        barrier.dstAccessMask = VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT;

        sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
        destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
    }
    else
    {
        barrier.srcAccessMask = 0;
        barrier.dstAccessMask = VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT;

        sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
        destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
    }

    vkCmdPipelineBarrier(
        commandBuffer,
        sourceStage, destinationStage,
        0,
        0, nullptr,
        0, nullptr,
        1, &barrier);

    device->endSingleTimeCommands(commandBuffer);
}

VulkanImage::VulkanImage(VkImage image, VulkanDevice *device,
                         VkDeviceMemory imageMemory, VkFormat format, VkImageLayout imageLayout, int width, int height) : image(image),
                                                                                               device(device),
                                                                                               imageMemory(imageMemory),
                                                                                               format(format),
                                                                                               width(width),
                                                                                               height(height),
                                                                                               imageLayout(imageLayout)
{
  view = device->createImageView(image, format);
}

int VulkanImage::getWidth()
{
  return width;
}

int VulkanImage::getHeight()
{
  return height;
}

VkImage VulkanImage::getImage()
{
    return image;
}

VulkanDevice *VulkanImage::getDevice()
{
    return device;
}

VkImageView VulkanImage::getView()
{
    return view;
}

void VulkanImage::copyToImage(VulkanImage *target, bool isDepth)
{
    VkCommandBuffer cmd = device->beginSingleTimeCommands();
    copyToImage(target, cmd, isDepth);
    device->endSingleTimeCommands(cmd);
}

void VulkanImage::copyToImage(VulkanImage *target, VkCommandBuffer cmd, bool isDepth)
{
    imageCopyRegion = {};
    imageCopyRegion.srcSubresource.aspectMask = isDepth? VK_IMAGE_ASPECT_DEPTH_BIT : VK_IMAGE_ASPECT_COLOR_BIT ;
    imageCopyRegion.srcSubresource.layerCount = 1;
    imageCopyRegion.dstSubresource.aspectMask = isDepth? VK_IMAGE_ASPECT_DEPTH_BIT : VK_IMAGE_ASPECT_COLOR_BIT;
    imageCopyRegion.dstSubresource.layerCount = 1;
    imageCopyRegion.extent.width = width;
    imageCopyRegion.extent.height = height;
    imageCopyRegion.extent.depth = 1;
    vkCmdCopyImage(
        cmd,
        this->image, this->imageLayout,
        target->image, target->imageLayout,
        1,
        &imageCopyRegion);
}

void VulkanImage::copyFromImage(VkImage image, VkImageLayout imageLayout, VkCommandBuffer cmd, bool isDepth)
{
    imageCopyRegion = {};
    imageCopyRegion.srcSubresource.aspectMask = isDepth? VK_IMAGE_ASPECT_DEPTH_BIT : VK_IMAGE_ASPECT_COLOR_BIT;
    imageCopyRegion.srcSubresource.layerCount = 1;
    imageCopyRegion.dstSubresource.aspectMask = isDepth? VK_IMAGE_ASPECT_DEPTH_BIT : VK_IMAGE_ASPECT_COLOR_BIT;
    imageCopyRegion.dstSubresource.layerCount = 1;
    imageCopyRegion.extent.width = width;
    imageCopyRegion.extent.height = height;
    imageCopyRegion.extent.depth = 1;
    vkCmdCopyImage(
        cmd,
        image, imageLayout,
        this->image, this->imageLayout,
        1,
        &imageCopyRegion);
}

void VulkanImage::copyFromImage(VkImage image, VkImageLayout imageLayout, bool isDepth)
{
    VkCommandBuffer cmd = device->beginSingleTimeCommands();
    copyFromImage(image, imageLayout, cmd, isDepth);
    device->endSingleTimeCommands(cmd);
}

void VulkanImage::clearImage(float r, float g, float b, float a)
{
    VkCommandBuffer cmd = device->beginSingleTimeCommands();
    clearImage(r, g, b, a, cmd);
    device->endSingleTimeCommands(cmd);
}

void VulkanImage::clearImage(float r, float g, float b, float a, VkCommandBuffer cmd)
{
    clearColorValue = {};
    clearColorValue.float32[0] = r;
    clearColorValue.float32[1] = g;
    clearColorValue.float32[2] = b;
    clearColorValue.float32[3] = a;
    imageSubresourceRange = {};
    imageSubresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
    imageSubresourceRange.baseMipLevel = 0;
    imageSubresourceRange.levelCount = 1;
    imageSubresourceRange.baseArrayLayer = 0;
    imageSubresourceRange.layerCount = 1;
    vkCmdClearColorImage(cmd, this->image, this->imageLayout, &clearColorValue, 1, &imageSubresourceRange);
}

VkFormat VulkanImage::getFormat()
{
    return format;
}

void VulkanImage::resize(int width, int height)
{
    destroy();
    destroyed = false;
    device->createImage(width, height, this->format, VK_IMAGE_TILING_OPTIMAL,
                        VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
                        image, true);
    createImageMemory(device, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, imageMemory, image);
    transitionImageLayout(device, image, this->format, VK_IMAGE_LAYOUT_UNDEFINED,
                          VK_IMAGE_LAYOUT_GENERAL);
    view = device->createImageView(image, this->format);
}

void VulkanImage::destroy()
{
    if (!destroyed)
    {
        vkDestroyImageView(device->getDevice(), view, nullptr);
        if (imageMemory != VK_NULL_HANDLE)
        {
            vkFreeMemory(device->getDevice(), imageMemory, nullptr);
        }
        vkDestroyImage(device->getDevice(), image, nullptr);
        destroyed = true;
    }
}

VulkanImage::~VulkanImage()
{
    destroy();
}
