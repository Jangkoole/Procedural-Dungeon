# 源文件
set(SOURCES
    main.cpp
    Game.cpp
    Map.cpp
    Player.cpp
    Entity.cpp
    MonsterAI.cpp
    Renderer.cpp
    InputHandler.cpp
)

# 头文件
set(HEADERS
    ../include/Game.h
    ../include/Map.h
    ../include/Player.h
    ../include/Entity.h
    ../include/Constants.h
    ../include/MonsterAI.h
    ../include/Renderer.h
    ../include/InputHandler.h
)

# 创建可执行文件
add_executable(DeepDungeon ${SOURCES} ${HEADERS})

# 包含目录
target_include_directories(DeepDungeon PRIVATE ${CMAKE_SOURCE_DIR}/include)

# 链接 ncurses/PDCurses (如果启用)
if(USE_CURSES)
    find_package(Curses REQUIRED)
    target_include_directories(DeepDungeon PRIVATE ${CURSES_INCLUDE_DIR})
    target_link_libraries(DeepDungeon PRIVATE ${CURSES_LIBRARIES})
    target_compile_definitions(DeepDungeon PRIVATE USE_CURSES)
endif()

# Windows 特定设置
if(WIN32)
    target_compile_definitions(DeepDungeon PRIVATE WIN32_LEAN_AND_MEAN NOMINMAX)
endif()
