#pragma once

#include <fstream>
#include <string>
#include <vector>
#include <cstring>
#include "StringUtil.h"

#define VERTEX_SHADER 0
#define FRAGMENT_SHADER 1
#define GEOMETRY_SHADER 2

struct ShaderInfo
{
    bool needToCompile;
    std::string path;
    int type;
};

class ShaderConfParser
{
public:
    static std::vector<ShaderInfo> parseShader(const char *pathToDirectory)
    {
        std::fstream file(std::string(pathToDirectory)+"/.shaderconf");
        std::vector<ShaderInfo> result;
        parseShaders(result, file, std::string(pathToDirectory));
        file.close();
        return result;
    }

private:
    static void parseShaders(std::vector<ShaderInfo> &result, std::fstream &file, std::string workDirectory)
    {
        if (file.is_open())
        {
            std::string currentLine = "";
            while (std::getline(file, currentLine))
            {
                if (currentLine.size() > 5)
                {
                    ShaderInfo info{};
                    std::vector<std::string> parsedLine;
                    StringUtil::split(currentLine, parsedLine, ' ');
                    info.needToCompile = StringUtil::parseBoolean(parsedLine[2]);
                    info.type = getShaderType(parsedLine[0]);
                    info.path = workDirectory+"/"+parsedLine[1];
                    result.push_back(info);
                }
            }
        }
    }
    static int getShaderType(std::string &rawType)
    {
        if (!std::strcmp(rawType.c_str(), "VERTEX_SHADER"))
        {
            return VERTEX_SHADER;
        }
        if (!std::strcmp(rawType.c_str(), "FRAGMENT_SHADER"))
        {
            return FRAGMENT_SHADER;
        }
        if (!std::strcmp(rawType.c_str(), "GEOMETRY_SHADER"))
        {
            return GEOMETRY_SHADER;
        }
        return -1;
    }
};