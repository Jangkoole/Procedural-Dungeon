# 使用 FetchContent 自动下载 Google Test
include(FetchContent)
FetchContent_Declare(
    googletest
    URL https://github.com/google/googletest/archive/refs/tags/v1.15.2.zip
)
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

# 测试源文件
set(TEST_SOURCES
    test_main.cpp
    test_entity.cpp
    test_player.cpp
)

# 创建测试可执行文件
add_executable(DeepDungeonTests ${TEST_SOURCES})

# 包含目录（生产代码的头文件）
target_include_directories(DeepDungeonTests PRIVATE ${CMAKE_SOURCE_DIR}/include)

# 链接生产代码的源文件（直接编译，不链接主程序）
target_sources(DeepDungeonTests PRIVATE
    ${CMAKE_SOURCE_DIR}/src/Entity.cpp
    ${CMAKE_SOURCE_DIR}/src/Player.cpp
    ${CMAKE_SOURCE_DIR}/src/Map.cpp
)

# 链接 Google Test
target_link_libraries(DeepDungeonTests PRIVATE
    gtest
    gtest_main
    gmock
)

# 注册测试
include(GoogleTest)
gtest_discover_tests(DeepDungeonTests)
