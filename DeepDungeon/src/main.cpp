#include "Game.h"
#include "InputHandler.h"
#include <iostream>
#include <string>

using namespace dungeon;

int main() {
    // 设置 UTF-8 输出（Windows 控制台）
    #ifdef _WIN32
    system("chcp 65001 > nul");
    #endif

    std::cout << "=== Deep Dungeon (深度地牢) ===\n\n";
    std::cout << "Controls: W/A/S/D to move, Space to wait, P for potion, Q to quit\n\n";

    Game game;
    game.init();
    
    InputHandler inputHandler;

    while (!game.isGameOver()) {
        // 渲染游戏
        std::cout << game.render();

        // 获取输入
        std::string input;
        std::getline(std::cin, input);

        // 处理输入
        Command cmd = inputHandler.parseCommand(input);

        if (cmd == Command::Quit) {
            std::cout << "Game quit.\n";
            return 0;
        }

        switch (cmd) {
            case Command::MoveUp:
            case Command::MoveDown:
            case Command::MoveLeft:
            case Command::MoveRight:
                inputHandler.executeMoveCommand(game, cmd);
                break;
            case Command::Wait:
                std::cout << "You wait a turn.\n";
                break;
            case Command::UsePotion:
                game.usePlayerPotion();
                break;
            case Command::Unknown:
            default:
                std::cout << "Invalid command. Use W/A/S/D to move, Q to quit.\n";
                continue;
        }

        // 更新游戏状态
        game.update();
    }

    // 游戏结束
    std::cout << game.render();

    return 0;
}
