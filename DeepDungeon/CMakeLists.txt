cmake_minimum_required(VERSION 3.16)
project(DeepDungeon VERSION 1.0.0 LANGUAGES CXX)

# C++17 标准
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# 编译选项
option(USE_CURSES "Use ncurses/PDCurses for terminal control" OFF)

# 子目录
add_subdirectory(src)

# 测试（可选，通过 BUILD_TESTING 控制）
option(BUILD_TESTING "Build unit tests" ON)
if(BUILD_TESTING)
    enable_testing()
    add_subdirectory(tests)
endif()

# 安装规则
install(TARGETS DeepDungeon DESTINATION bin)
