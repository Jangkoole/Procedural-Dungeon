#pragma once

#include <vector>
#include <string>
#include "Constants.h"

namespace dungeon {

// 地图单元格类型
enum class TileType {
    Wall,
    Floor,
    Water
};

// 地图单元格
struct Tile {
    TileType type;
    bool explored;  // 是否已探索（用于战争迷雾）
    bool visible;   // 当前是否可见
    
    Tile() : type(TileType::Wall), explored(false), visible(false) {}
};

// 地图类
class Map {
public:
    Map(int width = dungeon::MAP_WIDTH, int height = dungeon::MAP_HEIGHT);
    
    // 尺寸
    int width() const { return width_; }
    int height() const { return height_; }
    
    // 访问单元格
    const Tile& tile(int x, int y) const;
    Tile& tile(int x, int y);
    
    // 检查是否可通行
    bool isWalkable(int x, int y) const;
    
    // 检查是否是水
    bool isWater(int x, int y) const;
    
    // 地图生成
    void generate(int seed = 0);
    
    // 渲染
    std::string render() const;
    
    // 清除可见性（每帧更新）
    void clearVisibility();
    
    // 设置可见性（由 FOV 算法调用）
    void setTileVisible(int x, int y);
    
    // 标记为已探索
    void setTileExplored(int x, int y);

private:
    int width_, height_;
    std::vector<std::vector<Tile>> tiles_;
    
    // 生成算法
    void generateRooms();
    void generateCorridors();
    void addWaterFeatures();
    
    // 辅助函数
    bool isValid(int x, int y) const;
};

} // namespace dungeon
