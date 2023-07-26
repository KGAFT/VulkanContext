//
// Created by kgaft on 7/26/23.
//
#pragma once

#include <stdexcept>

class BuilderNotPopulatedException : public std::exception {
public:
    BuilderNotPopulatedException(const char *message);

private:
    const char* message;
public:
    const char *what();
};


