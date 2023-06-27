set(CMAKE_C_COMPILER C:/mingw64/bin/clang.exe)

cmake_minimum_required(VERSION 3.24)

project(MemoryUtils C)

set(CMAKE_C_STANDARD 11)

file(GLOB_RECURSE HEADER_FILES src/*.h)
file(GLOB_RECURSE SOURCE_FILES src/*.c)

add_library(MemoryUtils ${HEADER_FILES} ${SOURCE_FILES})

add_executable(MemExec ${HEADER_FILES} ${SOURCE_FILES} main.c)
