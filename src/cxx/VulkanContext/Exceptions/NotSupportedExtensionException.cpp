//
// Created by kgaft on 7/26/23.
//

#include "NotSupportedExtensionException.h"

NotSupportedExtensionException::NotSupportedExtensionException(const char *message) : message(message){

}

const char *NotSupportedExtensionException::what() {
    return message;
}
