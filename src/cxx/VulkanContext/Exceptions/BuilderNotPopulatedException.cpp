//
// Created by kgaft on 7/26/23.
//

#include "BuilderNotPopulatedException.h"

const char *BuilderNotPopulatedException::what() {
    return message;
}

BuilderNotPopulatedException::BuilderNotPopulatedException(const char *message) : message(message) {}
