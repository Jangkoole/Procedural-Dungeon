#include "InputHandler.h"
#include <algorithm>

namespace dungeon {

InputHandler::InputHandler() {}

Command InputHandler::parseCommand(const std::string& input) const {
    if (input.empty()) return Command::Unknown;
    
    char c = input[0];
    return charToCommand(c);
}

Command InputHandler::charToCommand(char c) const {
    c = std::tolower(c);
    
    switch (c) {
        case 'w': return Command::MoveUp;
        case 's': return Command::MoveDown;
        case 'a': return Command::MoveLeft;
        case 'd': return Command::MoveRight;
        case ' ': return Command::Wait;
        case 'p': return Command::UsePotion;
        case 'q': return Command::Quit;
        default: return Command::Unknown;
    }
}

void InputHandler::executeMoveCommand(Game& game, Command cmd) const {
    switch (cmd) {
        case Command::MoveUp:
            game.tryMovePlayer(0, -1);
            break;
        case Command::MoveDown:
            game.tryMovePlayer(0, 1);
            break;
        case Command::MoveLeft:
            game.tryMovePlayer(-1, 0);
            break;
        case Command::MoveRight:
            game.tryMovePlayer(1, 0);
            break;
        default:
            break;
    }
}

} // namespace dungeon
