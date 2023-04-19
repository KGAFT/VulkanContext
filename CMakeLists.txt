cmake_minimum_required(VERSION 3.25)
project(VulkanContext)

set(CMAKE_CXX_STANDARD 17)

file(GLOB_RECURSE H_FILES src/cxx/*.h)
file(GLOB_RECURSE CPP_FILES src/cxx/*.cpp)
add_library(VulkanContext ${H_FILES} ${CPP_FILES})


if(WIN32)
    target_include_directories(VulkanContext PUBLIC src/cxx ${VulkanSDKPath}/Include)
    target_link_directories(VulkanContext PUBLIC ${VulkanSDKPath}/bin ${VulkanSDKPath}/lib)
    set(VulkanLibList shaderc_shared vulkan-1)
endif()

if(UNIX)
    target_include_directories(VulkanContext PUBLIC src/cxx ${VulkanSDKPath}/include)
    target_link_directories(VulkanContext PUBLIC ${VulkanSDKPath}/bin ${VulkanSDKPath}/lib)
    set(VulkanLibList vulkan shaderc_shared)
endif()


target_link_libraries(VulkanContext ${VulkanLibList})
