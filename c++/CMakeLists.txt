cmake_minimum_required (VERSION 2.6)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

project (simple)

file (GLOB cpp_files "${CMAKE_CURRENT_SOURCE_DIR}/src/*.cpp")

set(DLLEXPORT_STATIC "HR_PROTO_EXPORT= ")

include_directories(${CMAKE_CURRENT_BINARY_DIR})
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include)

add_executable(simple ${cpp_files})

set_target_properties(simple PROPERTIES COMPILE_DEFINITIONS "${DLLEXPORT_STATIC}")
target_link_libraries(simple hotrod)
