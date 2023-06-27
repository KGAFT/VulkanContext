#include <stdlib.h>
#include "global_types.h"

struct MemoryStackManagerConfig {
	size_t defaultStackSize;
	unsigned int maxStackAmount;
	MU_BOOL unlimitedStackAmount;
};

typedef struct MuStack {
	unsigned int id;
	MU_BOOL isSafe;
	size_t maxSize;
	size_t freeSize;
} MuStack;

MU_RESULT muInitializeStackManager(struct MemoryStackManagerConfig* pConfig);

MU_RESULT muAcquireStackForFreeSize(size_t needSize, MuStack* pOutput);
