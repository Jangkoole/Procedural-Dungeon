#pragma once

#include <string>
#include <vector>
#include <memory>
#include "Map.h"
#include "Player.h"
#include "Entity.h"

namespace dungeon {

// 渲染器类 - 负责所有游戏渲染逻辑
class Renderer {
public:
    Renderer();

    // 渲染完整游戏画面
    std::string renderGame(const Map& map,
                           const Player& player,
                           const std::vector<std::unique_ptr<Entity>>& entities,
                           const std::vector<std::string>& logs) const;

private:
    // 渲染地图部分
    std::string renderMap(const Map& map,
                          const Player& player,
                          const std::vector<std::unique_ptr<Entity>>& entities) const;

    // 渲染状态栏
    std::string renderStatus(const Player& player) const;

    // 渲染日志
    std::string renderLogs(const std::vector<std::string>& logs) const;

    // 在指定位置查找实体字符
    char getEntitySymbolAt(int x, int y,
                           const std::vector<std::unique_ptr<Entity>>& entities) const;

    // 检查是否有实体在指定位置
    bool hasEntityAt(int x, int y,
                     const std::vector<std::unique_ptr<Entity>>& entities) const;
};

} // namespace dungeon
