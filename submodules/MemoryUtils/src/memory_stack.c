#include "memory_stack.h"

#define STACK_IN_USE 2
#define STACK_FREE 5

MU_BOOL initialized = MU_FALSE;
MU_BOOL isUnlimited = MU_FALSE;
size_t defaultStackSize = 0;
size_t maxStackAmount = 0;

MuStack* nonSafeStacks;
MuStack* userStacks;

unsigned int existingStacks = 0;
unsigned int existingUserStacks = 0;
unsigned int stackHeapsAmount = 0;
void** stackHeaps;

MU_RESULT muInitializeStackManager(struct MemoryStackManagerConfig* pConfig)
{
	if (!initialized) {
		isUnlimited = pConfig->unlimitedStackAmount;
		defaultStackSize = pConfig->defaultStackSize;
		maxStackAmount = pConfig->maxStackAmount;
		if (!defaultStackSize) {
			return MU_FAILED_DEFAULT_STACK_SIZE_ZERO;
		}
		if (!isUnlimited && !maxStackAmount) {
			return MU_FAILED_MAX_STACK_AMOUNT_ZERO;
		}
		initialized = MU_TRUE;
		return MU_SUCCESS;
	}
	return MU_FAILED_MEMORY_STACK_MANAGER_ALLREADY_INITIALiZED;
}
