#pragma once

#include "../../External/stb_image.h"
#include "../VulkanDevice/VulkanDevice.h"
#include "VulkanImage.h"

struct CubemapTextureInfo {
	const char* pathToFrontFace;
	const char* pathToBackFace;
	const char* pathToUpFace;
	const char* pathToDownFace;
	const char* pathToRightFace;
	const char* pathToLeftFace;
};

class VulkanCubemapImage {
public:
	static VulkanCubemapImage* createCubemap(VulkanDevice* device, CubemapTextureInfo& info) {
		stbi_uc* imageData[6];
		int width = {0};
		int height = {0};
		int numChannelsAmount = { 0 };
		imageData[0] = stbi_load(info.pathToFrontFace, &width, &height, &numChannelsAmount, STBI_rgb_alpha);
		imageData[1] = stbi_load(info.pathToBackFace, &width, &height, &numChannelsAmount, STBI_rgb_alpha);
		imageData[2] = stbi_load(info.pathToUpFace, &width, &height, &numChannelsAmount, STBI_rgb_alpha);
		imageData[3] = stbi_load(info.pathToDownFace, &width, &height, &numChannelsAmount, STBI_rgb_alpha);
		imageData[4] = stbi_load(info.pathToRightFace, &width, &height, &numChannelsAmount, STBI_rgb_alpha);
		imageData[5] = stbi_load(info.pathToLeftFace, &width, &height, &numChannelsAmount, STBI_rgb_alpha);
		

		VkDeviceSize imageSize = width * height * 4 * 6; 
		VkDeviceSize layerSize = imageSize / 6;

		VkBuffer stagingBuffer;
        VkDeviceMemory stagingBufferMemory;
        device->createBuffer(imageSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                             VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBuffer,
                             stagingBufferMemory);
		void* data;
        vkMapMemory(device->getDevice(), stagingBufferMemory, 0, imageSize, 0, &data);

		for(unsigned int i = 0; i<6; i++){
			memcpy((void*)(reinterpret_cast<long long>(data) + (layerSize * i)), imageData[i], layerSize);
		}
        vkUnmapMemory(device->getDevice(), stagingBufferMemory);

        VkImage image;
        VkDeviceMemory imageMemory;
        createImage(device ,image, imageMemory, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_USAGE_TRANSFER_DST_BIT|VK_IMAGE_USAGE_SAMPLED_BIT, VK_IMAGE_TILING_OPTIMAL, width, height, 6, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        transitionImageLayout(image, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, device, 6);
        device->copyBufferToImage(stagingBuffer, image, width, height, 6);
        transitionImageLayout(image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, device, 6);
        vkDestroyBuffer(device->getDevice(), stagingBuffer, nullptr);
        vkFreeMemory(device->getDevice(), stagingBufferMemory, nullptr);
        return new VulkanCubemapImage(device, image, imageMemory, VK_FORMAT_R8G8B8A8_SRGB, 6);
	}
private:
	static void transitionImageLayout(VkImage target, VkImageLayout oldLayout, VkImageLayout newLayout, VulkanDevice* device, int layerAmount)
    {
        VkCommandBuffer commandBuffer = device->beginSingleTimeCommands();

        VkImageMemoryBarrier barrier{};
        barrier.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
        barrier.oldLayout = oldLayout;
        barrier.newLayout = newLayout;
        barrier.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
        barrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
        barrier.image = target;
        barrier.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
        barrier.subresourceRange.baseMipLevel = 0;
        barrier.subresourceRange.levelCount = 1;
        barrier.subresourceRange.baseArrayLayer = 0;
        barrier.subresourceRange.layerCount = layerAmount;

        VkPipelineStageFlags sourceStage;
        VkPipelineStageFlags destinationStage;

        if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
        {
            barrier.srcAccessMask = 0;
            barrier.dstAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;

            sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
            destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
        }
        else if (oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
        {
            barrier.srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
            barrier.dstAccessMask = VK_ACCESS_SHADER_READ_BIT;

            sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
            destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
        }

        vkCmdPipelineBarrier
        (
            commandBuffer,
            sourceStage, destinationStage,
            0,
            0, nullptr,
            0, nullptr,
            1, &barrier
        );

        device->endSingleTimeCommands(commandBuffer);
    }
    static void createImage(VulkanDevice* device, VkImage& target, VkDeviceMemory& imageMemory, VkFormat imageFormat, VkImageUsageFlags usageFlags,VkImageTiling imageTilling,int width, int height, int amountOfImages, VkMemoryPropertyFlags properties){

        VkImageCreateInfo imageInfo{};
        imageInfo.sType = VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;
        imageInfo.flags = VK_IMAGE_CREATE_CUBE_COMPATIBLE_BIT;
        imageInfo.imageType = VK_IMAGE_TYPE_2D;
        imageInfo.extent.width = width;
        imageInfo.extent.height = height;
        imageInfo.extent.depth = 1;
        imageInfo.mipLevels = 1;
        imageInfo.arrayLayers = amountOfImages;
        imageInfo.format = imageFormat;
        imageInfo.tiling = imageTilling;
        imageInfo.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;
        imageInfo.usage = usageFlags;
        imageInfo.samples = VK_SAMPLE_COUNT_1_BIT;
        imageInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
        device->createImageWithInfo(imageInfo, properties, target, imageMemory);

    }
private:
    VulkanDevice* device;
    VkImage image;
    VkImageView view;
    VkDeviceMemory imageMemory;
    VkFormat imageFormat;
    VulkanCubemapImage(VulkanDevice* device, VkImage image, VkDeviceMemory imageMemory, VkFormat imageFormat, int layerCount) : device(device), image(image), imageMemory(imageMemory), imageFormat(imageFormat){
        VkImageViewCreateInfo createInfo{};
        createInfo.sType = VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
        createInfo.viewType = VK_IMAGE_VIEW_TYPE_CUBE;
        createInfo.format = imageFormat;
        createInfo.components = {VK_COMPONENT_SWIZZLE_R, VK_COMPONENT_SWIZZLE_G, VK_COMPONENT_SWIZZLE_B, VK_COMPONENT_SWIZZLE_A};
        createInfo.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
        createInfo.subresourceRange.baseMipLevel = 0;
        createInfo.subresourceRange.baseArrayLayer = 0;
        createInfo.subresourceRange.layerCount = layerCount;
        createInfo.subresourceRange.levelCount = 1;
        createInfo.image = image;
        vkCreateImageView(device->getDevice(), &createInfo, nullptr, &view);
    }
public:

    VkImageView getImageView(){
        return view;
    }
};
