#include "Renderer.h"
#include <sstream>

namespace dungeon {

Renderer::Renderer() {}

std::string Renderer::renderGame(const Map& map,
                                   const Player& player,
                                   const std::vector<std::unique_ptr<Entity>>& entities,
                                   const std::vector<std::string>& logs) const {
    std::string result;
    
    // 渲染地图
    result += renderMap(map, player, entities);
    
    // 渲染状态
    result += renderStatus(player);
    
    // 渲染日志
    result += renderLogs(logs);
    
    // 渲染控制提示
    result += "\nCommands: W/A/S/D move, Space wait, P potion, Q quit\n";
    
    return result;
}

std::string Renderer::renderMap(const Map& map,
                                 const Player& player,
                                 const std::vector<std::unique_ptr<Entity>>& entities) const {
    std::string result;

    for (int y = 0; y < map.height(); y++) {
        for (int x = 0; x < map.width(); x++) {
            const Tile& tile = map.tile(x, y);

            // 检查玩家位置
            if (player.x() == x && player.y() == y) {
                result += dungeon::CHAR_PLAYER;
                continue;
            }

            // 检查其他实体
            if (tile.visible) {
                char entitySymbol = getEntitySymbolAt(x, y, entities);
                if (entitySymbol != ' ') {
                    result += entitySymbol;
                    continue;
                }
            }

            // 根据可见性渲染地图
            if (tile.visible) {
                switch (tile.type) {
                    case TileType::Wall: result += '#'; break;
                    case TileType::Floor: result += '.'; break;
                    case TileType::Water: result += '~'; break;
                }
            } else if (tile.explored) {
                result += ' ';  // 已探索但不可见
            } else {
                result += ' ';  // 未探索
            }
        }
        result += '\n';
    }
    
    return result;
}

std::string Renderer::renderStatus(const Player& player) const {
    std::string result;
    result += "\n=== Status ===\n";
    result += "HP: " + std::to_string(player.hp()) + "/" + std::to_string(player.maxHp());
    result += "  ATK: " + std::to_string(player.totalAtk());
    result += "  DEF: " + std::to_string(player.def());
    result += "  LV: " + std::to_string(player.level());
    result += "  Potions: " + std::to_string(player.potionCount()) + "\n";
    return result;
}

std::string Renderer::renderLogs(const std::vector<std::string>& logs) const {
    std::string result;
    result += "\n=== Log ===\n";
    
    size_t startIdx = (logs.size() > 5) ? logs.size() - 5 : 0;
    for (size_t i = startIdx; i < logs.size(); i++) {
        result += logs[i] + "\n";
    }
    
    return result;
}

char Renderer::getEntitySymbolAt(int x, int y,
                                  const std::vector<std::unique_ptr<Entity>>& entities) const {
    for (const auto& entity : entities) {
        if (entity->x() == x && entity->y() == y) {
            return entity->symbol();
        }
    }
    return ' ';
}

bool Renderer::hasEntityAt(int x, int y,
                            const std::vector<std::unique_ptr<Entity>>& entities) const {
    for (const auto& entity : entities) {
        if (entity->x() == x && entity->y() == y) {
            return true;
        }
    }
    return false;
}

} // namespace dungeon
