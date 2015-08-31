cmake_minimum_required (VERSION 2.6)
project (simple)

file (GLOB cpp_files "${CMAKE_CURRENT_SOURCE_DIR}/src/*.cpp")

include_directories(${CMAKE_CURRENT_BINARY_DIR})
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include)

add_executable(simple ${cpp_files})
target_link_libraries(simple hotrod)
