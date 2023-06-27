#pragma once

#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include "global_types.h"

typedef struct MuArray{
	size_t typeSize;
	unsigned int size;
	MU_BOOL isDestroyed;
    void* pData;
} MuArray;

void muCreateArray(size_t typeSize, MuArray* pOutput);

void muPutToArray(MuArray* pTarget, void* data);

void muInsertToArray(MuArray* pTarget, void* data, unsigned int index);

void muGetArrayValue(MuArray* pTarget, void* pOutput, unsigned int index);

void muEraseArrayElement(MuArray* pTarget, unsigned int index);

void muDestroyArray(MuArray* pTarget);