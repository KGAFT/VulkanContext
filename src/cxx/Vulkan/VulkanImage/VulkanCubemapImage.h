#pragma once

#include "../../External/stb_image.h"
#include "../VulkanDevice/VulkanDevice.h"

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
	VulkanCubemapImage* createCubemap(VulkanDevice* device, CubemapTextureInfo& info) {
		stbi_uc* data[6];
		int width = {0};
		int height = {0};
		int numChannelsAmount = { 0 };
		data[0] = stbi_load(info.pathToFrontFace, &width, &height, &numChannelsAmount, STBI_rgb_alpha);
		data[1] = stbi_load(info.pathToBackFace, &width, &height, &numChannelsAmount, STBI_rgb_alpha);
		data[2] = stbi_load(info.pathToUpFace, &width, &height, &numChannelsAmount, STBI_rgb_alpha);
		data[3] = stbi_load(info.pathToDownFace, &width, &height, &numChannelsAmount, STBI_rgb_alpha);
		data[4] = stbi_load(info.pathToRightFace, &width, &height, &numChannelsAmount, STBI_rgb_alpha);
		data[5] = stbi_load(info.pathToLeftFace, &width, &height, &numChannelsAmount, STBI_rgb_alpha);
		

		VkDeviceSize imageSize = width * height * 4 * 6; 
		VkDeviceSize layerSize = imageSize / 6;
        return nullptr;
	}
};