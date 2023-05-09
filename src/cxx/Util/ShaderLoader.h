#pragma once

#include <shaderc/shaderc.h>
#include <vulkan/vulkan.h>

#include "ShaderConfParser.h"
#include "../Vulkan/VulkanDevice/VulkanDevice.h"
#include "../Vulkan/VulkanGraphicsPipeline/VulkanShader/VulkanShader.h"

class ShaderLoader
{
public:
    static VulkanShader *loadShaders(const char *pathToDir, VulkanDevice *device);

private:
    static shaderc_compiler_t compiler;
    static VkShaderModule compileAndCreateModule(ShaderInfo &info, VulkanDevice *device);

    static const char *
    compileShader(const char *pathToShader, shaderc_shader_kind shaderType, const char *fileName, size_t *size);

    static std::string readCode(const char *filePath);

    static std::vector<char> readBinaryFile(std::string &filepath);

    static shaderc_shader_kind getShaderType(int shaderType);
};