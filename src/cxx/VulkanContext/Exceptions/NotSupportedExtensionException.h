//
// Created by kgaft on 7/26/23.
//

#pragma once

#include <stdexcept>

class NotSupportedExtensionException : public std::exception  {
public:
    NotSupportedExtensionException(const char *message);

private:
    const char* message;
public:
    const char *what();
};


