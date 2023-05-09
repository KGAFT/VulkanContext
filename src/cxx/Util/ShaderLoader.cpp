#include "ShaderLoader.h"

shaderc_compiler_t ShaderLoader::compiler = shaderc_compiler_initialize();

VulkanShader *ShaderLoader::loadShaders(const char *pathToDir, VulkanDevice *device)
{
    std::vector<ShaderInfo> shaders = ShaderConfParser::parseShader(pathToDir);
    std::map<VkShaderModule, int> shadersToCreate;
    for (auto &element : shaders)
    {
        shadersToCreate.insert(std::pair<VkShaderModule, int>(compileAndCreateModule(element, device), element.type));
    }
    return new VulkanShader(device, shadersToCreate);
}

VkShaderModule ShaderLoader::compileAndCreateModule(ShaderInfo &info, VulkanDevice *device)
{
    const char *content = nullptr;
    size_t size;
    std::vector<char> binary;
    if (info.needToCompile)
    {
        std::string fileName = info.path.substr(info.path.find_last_of("/") + 1);
        content = compileShader(info.path.c_str(), getShaderType(info.type), fileName.c_str(), &size);
    }
    else
    {
        binary = readBinaryFile(info.path);
    }
    if (content == nullptr)
    {
        content = binary.data();
        size = binary.size();
    }
    VkShaderModuleCreateInfo createInfo{};
    createInfo.sType = VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
    createInfo.codeSize = size;
    createInfo.pCode = reinterpret_cast<const uint32_t *>(content);
    VkShaderModule shaderModule;
    if (vkCreateShaderModule(device->getDevice(), &createInfo, nullptr, &shaderModule) != VK_SUCCESS)
    {
        throw std::runtime_error("failed to create shader module");
    }
    return shaderModule;
}

const char *
ShaderLoader::compileShader(const char *pathToShader, shaderc_shader_kind shaderType, const char *fileName, size_t *size)
{
    std::string shaderCode = readCode(pathToShader);
    shaderc_compilation_result_t result = shaderc_compile_into_spv(compiler, shaderCode.c_str(),
                                                                   shaderCode.size() * sizeof(char),
                                                                   shaderType, fileName, "main", nullptr);
    if (result == nullptr)
    {
        throw std::runtime_error("Failed to compile shader: " + std::string(fileName));
    }
    if (shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success)
    {
        std::cerr << shaderc_result_get_error_message(result) << std::endl;
        throw std::runtime_error("Failed to compile shader " + std::string(std::string(pathToShader) + "/" + fileName) + " into SPIR-V:\n " +
                                 shaderc_result_get_error_message(result));
    }
    const char *code = shaderc_result_get_bytes(result);
    *size = shaderc_result_get_length(result);
    return code;
}

std::string ShaderLoader::readCode(const char *filePath)
{
    std::ifstream fileReader(filePath, std::ios::binary);
    if (fileReader)
    {
        std::string content;
        fileReader.seekg(0, std::ios::end);
        content.resize(fileReader.tellg());
        fileReader.seekg(0, std::ios::beg);
        fileReader.read(&content[0], content.size());
        fileReader.close();
        return content;
    }
    return std::string();
}

std::vector<char> ShaderLoader::readBinaryFile(std::string &filepath)
{
    std::string enginePath = filepath;
    std::ifstream file{enginePath, std::ios::ate | std::ios::binary};

    if (!file.is_open())
    {
        throw std::runtime_error("failed to open file: " + enginePath);
    }

    size_t fileSize = static_cast<size_t>(file.tellg());
    std::vector<char> buffer(fileSize);

    file.seekg(0);
    file.read(buffer.data(), fileSize);

    file.close();
    return buffer;
}

shaderc_shader_kind ShaderLoader::getShaderType(int shaderType)
{

    if (shaderType == FRAGMENT_SHADER)
    {
        return shaderc_glsl_fragment_shader;
    }
    if (shaderType == VERTEX_SHADER)
    {
        return shaderc_glsl_vertex_shader;
    }
    if (shaderType == GEOMETRY_SHADER)
    {
        return shaderc_glsl_geometry_shader;
    }
    return shaderc_glsl_vertex_shader;
}
