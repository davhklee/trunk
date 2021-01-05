
#include <assert.h>
#include <stdint.h>
#include <stdio.h>
#include <netdb.h>
#include <dlfcn.h>

typedef void* DLL_TYPE;

uint32_t (*foo)(uint32_t a) = NULL;
uint32_t (*bar)(uint32_t a) = NULL;

int main(void)
{
    DLL_TYPE g_dll = dlopen("./libmathoperators.so", RTLD_NOW);
    if (g_dll == NULL)
    {
        printf("g_dll is NOK\n");
    }
    else
    {
        printf("g_dll is OK\n");
    }

    assert(g_dll);

    *(void**)(&foo) = dlsym(g_dll, "foo");
    *(void**)(&bar) = dlsym(g_dll, "bar");

    assert(foo);
    assert(bar);

    printf("foo:%u\n", foo(2));
    printf("bar:%u\n", bar(2));

    dlclose(g_dll);

    return 0;
}

