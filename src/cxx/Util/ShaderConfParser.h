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
    static std::vector<ShaderInfo> parseShader(const char *pathToDirectory);

private:
    static void parseShaders(std::vector<ShaderInfo> &result, std::fstream &file, std::string workDirectory);

    static int getShaderType(std::string &rawType);
};