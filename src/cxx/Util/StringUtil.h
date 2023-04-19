#pragma once

#include <string>
#include <vector>
#include <cstring>
#include <cmath>

namespace StringUtil{
    void split(std::string& source, std::vector<std::string>& out, char regex){
        out.clear();
        std::string currentString = "";
        for(char element : source){
            if(element!=regex){
                currentString+=element;
            }
            else{
                out.push_back(currentString);
                currentString = "";
            }
        }
        if(currentString.size()>0){
            out.push_back(currentString);
        }
    }

    std::string toLowerCase(std::string& source){
        std::string result = "";
        for(char el : source){
            result+=std::tolower(el);
        }
        return result;
    }

    bool parseBoolean(std::string& source){

        std::string lowerCase = toLowerCase(source);
        if(lowerCase.find("true")!=std::string::npos){
            return true;
        }
        else{
            return false;
        }
    }
}