#pragma once

#include <string>
#include <vector>
#include <cstring>
#include <cmath>

namespace StringUtil{
    void split(std::string& source, std::vector<std::string>& out, char regex);

    std::string toLowerCase(std::string& source);

    bool parseBoolean(std::string& source);
    
}