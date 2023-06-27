#include <stdio.h>
#include "src/mu_array.h"

int main(){
    MuArray tmp;
	MuArray array;
	muCreateArray(sizeof(long long), &tmp);
    muCreateArray(sizeof(long long), &tmp);
    muCreateArray(sizeof(long long), &tmp);
    muCreateArray(sizeof(long long), &array);
    muCreateArray(sizeof(long long), &tmp);
    muCreateArray(sizeof(long long), &tmp);
	for(long long counter = 0; counter<1000; counter++){
        muPutToArray(&array, &counter);
    }
    long long temp = 5;
    muInsertToArray(&array, &temp, 3);
    muEraseArrayElement(&array, 5);
    muEraseArrayElement(&array, 0);
    for(int counter = 0; counter<1000; counter++){
        muGetArrayValue(&array, &temp, counter);
        printf("%lli", temp);
        printf("\n");
    }

	return 0;
}
