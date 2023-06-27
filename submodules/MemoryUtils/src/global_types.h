typedef enum MU_BOOL {
	MU_TRUE = 1,
	MU_FALSE = 0
} MU_BOOL;

typedef enum MU_RESULT {
	MU_SUCCESS = 0,
	MU_FAILED_UNKNOWN = 1,
	MU_FAILED_MEMORY_STACK_MANAGER_ALLREADY_INITIALiZED = 2,
	MU_FAILED_MAX_STACK_AMOUNT_ZERO = 3,
	MU_FAILED_DEFAULT_STACK_SIZE_ZERO = 4,
	MU_FAILED_MAX_STACK_AMOUNT_EXCEEDED = 5
} MU_RESULT;