#pragma once

#include <string>
#include "Game.h"

namespace dungeon {

// 游戏类 - 前向声明
class Game;

// 输入处理器 - 负责解析用户输入
class InputHandler {
public:
    InputHandler();

    // 解析输入字符串为游戏命令
    Command parseCommand(const std::string& input) const;

    // 根据命令执行玩家移动
    void executeMoveCommand(Game& game, Command cmd) const;

private:
    // 字符转命令
    Command charToCommand(char c) const;
};

} // namespace dungeon
