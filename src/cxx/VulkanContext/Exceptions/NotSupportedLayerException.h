//
// Created by kgaft on 7/26/23.
//
#pragma once

#include <stdexcept>

class NotSupportedLayerException : public std::exception {
public:
    NotSupportedLayerException(const char *message);
private:
    const char *message;
public:
    const char *what();
};
