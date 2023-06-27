#include "mu_array.h"

void muCreateArray(size_t typeSize, MuArray* pOutput)
{
	pOutput->typeSize = typeSize;
	pOutput->size = 0;
	pOutput->isDestroyed = MU_FALSE;
    pOutput->pData = NULL;
}

void muPutToArray(MuArray* pTarget, void* data)
{
	if (!pTarget->isDestroyed) {
		if (pTarget->size == 0) {
			pTarget->pData = calloc(1, pTarget->typeSize);
			memcpy(pTarget->pData, data, pTarget->typeSize);
			pTarget->size++;
		}
		else {
			void* oldArray = pTarget->pData;
			pTarget->pData = calloc(pTarget->size + 1, pTarget->typeSize);
			memcpy(pTarget->pData, oldArray, pTarget->typeSize * pTarget->size);
			memcpy(pTarget->pData+pTarget->size*pTarget->typeSize, data, pTarget->typeSize);
			free(oldArray);
			pTarget->size++;
		}
	}
}

void muInsertToArray(MuArray* pTarget, void* data, unsigned int index)
{
	if (pTarget->size > index  && !pTarget->isDestroyed) {
		memcpy((void*)((uintptr_t)pTarget->pData+index*pTarget->typeSize), data, pTarget->typeSize);
	}
}

void muEraseArrayElement(MuArray* pTarget, unsigned int index)
{
	if (pTarget->size > index && !pTarget->isDestroyed) {
		void* newArray = calloc(pTarget->size - 1, pTarget->typeSize);
		memcpy(newArray, pTarget->pData, index * pTarget->typeSize);
		memcpy((void*)((uintptr_t)newArray + (index)*pTarget->typeSize),(void*)( (uintptr_t)pTarget->pData + (index + 1) * pTarget->typeSize), (pTarget->size -1 - index) * pTarget->typeSize);
		pTarget->size -= 1;
		free(pTarget->pData);
		pTarget->pData = newArray;
	}
}

void muGetArrayValue(MuArray* pTarget, void* pOutput, unsigned int index)
{
	if (pTarget->size > index && !pTarget->isDestroyed) {
		memcpy(pOutput, (void*)((uintptr_t)pTarget->pData + index * pTarget->typeSize), pTarget->typeSize);
	}
}

void muDestroyArray(MuArray* pTarget){
    pTarget->isDestroyed = MU_TRUE;
    free(pTarget->pData);
    pTarget->size = 0;
    pTarget->typeSize = 0;
    pTarget->pData = 0;
}
