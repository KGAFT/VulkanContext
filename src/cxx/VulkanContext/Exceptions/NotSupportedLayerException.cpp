//
// Created by kgaft on 7/26/23.
//

#include "NotSupportedLayerException.h"

NotSupportedLayerException::NotSupportedLayerException(const char *message) : message(message) {

}

const char *NotSupportedLayerException::what() {
    return message;
}
