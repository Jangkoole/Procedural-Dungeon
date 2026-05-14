import { useRef, useEffect } from 'react';
import { CELL_SIZE, getTileColor } from '../utils/tileColors';

export default function GameCanvas({ gameState }) {
  const canvasRef = useRef(null);

  useEffect(() => {
    if (!gameState) return;

    const { dungeon, player } = gameState;
    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');

    const width = dungeon.width * CELL_SIZE;
    const height = dungeon.height * CELL_SIZE;
    canvas.width = width;
    canvas.height = height;

    // Draw tiles
    for (let y = 0; y < dungeon.height; y++) {
      for (let x = 0; x < dungeon.width; x++) {
        const tile = dungeon.tiles[y]?.[x];
        if (!tile) continue;

        let entity = null;
        if (player && player.x === x && player.y === y) {
          entity = '@';
        } else if (tile.visible) {
          const monster = dungeon.monsters?.find((m) => m.x === x && m.y === y);
          if (monster) entity = 'G';
          else {
            const item = dungeon.items?.find((it) => it.x === x && it.y === y);
            if (item) entity = item.type === 'WEAPON' ? '/' : '!';
          }
        }

        const bgColor = getTileColor(tile.type, tile.visible, tile.explored);
        const px = x * CELL_SIZE;
        const py = y * CELL_SIZE;

        ctx.fillStyle = bgColor;
        ctx.fillRect(px, py, CELL_SIZE, CELL_SIZE);

        if (tile.type === 'WALL' && (tile.visible || tile.explored)) {
          ctx.fillStyle = tile.visible ? '#5a5a6a' : '#2a2a3a';
          ctx.fillRect(px + 2, py + 2, CELL_SIZE - 4, CELL_SIZE - 4);
        }

        if (entity && tile.visible) {
          const colors = { '@': '#ffd700', G: '#ff4444', '/': '#44aaff', '!': '#44ff44' };
          ctx.fillStyle = colors[entity] || '#fff';
          ctx.font = '16px monospace';
          ctx.textAlign = 'center';
          ctx.textBaseline = 'middle';
          ctx.fillText(entity, px + CELL_SIZE / 2, py + CELL_SIZE / 2 + 1);
        }
      }
    }

    // Draw grid lines
    ctx.strokeStyle = 'rgba(255,255,255,0.03)';
    ctx.lineWidth = 1;
    for (let x = 0; x <= dungeon.width; x++) {
      ctx.beginPath();
      ctx.moveTo(x * CELL_SIZE, 0);
      ctx.lineTo(x * CELL_SIZE, dungeon.height * CELL_SIZE);
      ctx.stroke();
    }
    for (let y = 0; y <= dungeon.height; y++) {
      ctx.beginPath();
      ctx.moveTo(0, y * CELL_SIZE);
      ctx.lineTo(dungeon.width * CELL_SIZE, y * CELL_SIZE);
      ctx.stroke();
    }
  }, [gameState]);

  if (!gameState) return null;

  const width = gameState.dungeon.width * CELL_SIZE;
  const height = gameState.dungeon.height * CELL_SIZE;

  return (
    <div className="game-canvas-wrapper">
      <canvas
        ref={canvasRef}
        width={width}
        height={height}
        className="game-canvas"
      />
    </div>
  );
}
