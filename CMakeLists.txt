cmake_minimum_required(VERSION 3.25)
project(VulkanContext)

set(CMAKE_CXX_STANDARD 17)

file(GLOB_RECURSE H_FILES src/cxx/*.h)
file(GLOB_RECURSE CPP_FILES src/cxx/*.cpp)

if(WIN32)
    include_directories(${VulkanSDKPath}/Include)
    link_directories(${VulkanSDKPath}/Bin ${VulkanSDKPath}/Lib)
    set(VulkanLibList shaderc_shared vulkan-1)
endif()

if(UNIX)
    include_directories(${VulkanSDKPath}/include)
    link_directories(${VulkanSDKPath}/bin ${VulkanSDKPath}/lib)
    set(VulkanLibList vulkan shaderc_shared)

endif()

add_library(VulkanContext ${H_FILES} ${CPP_FILES})


if(WIN32)
    target_include_directories(VulkanContext PUBLIC src/cxx ${VulkanSDKPath}/Include)

endif()

if(UNIX)
    target_include_directories(VulkanContext PUBLIC src/cxx ${VulkanSDKPath}/include)
endif()


target_link_libraries(VulkanContext ${VulkanLibList})
