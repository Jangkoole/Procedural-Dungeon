const TILE_COLORS = {
  FLOOR: '#1a1a2e',
  WALL: '#4a4a5a',
  WATER: '#16213e',
  CORRIDOR: '#2a2a3e',
  FLOOR_VISIBLE: '#2d2d44',
  WALL_VISIBLE: '#6a6a7a',
  WATER_VISIBLE: '#1a3a5c',
  CORRIDOR_VISIBLE: '#3d3d55',
};

const TILE_SYMBOLS = {
  FLOOR: '',
  WALL: '#',
  WATER: '~',
  CORRIDOR: '',
  PLAYER: '@',
  MONSTER: 'G',
  WEAPON: '/',
  POTION: '!',
  UNEXPLORED: ' ',
};

const CELL_SIZE = 24;
const FONT_SIZE = 16;

export function getTileColor(type, visible, explored) {
  if (!visible && !explored) return '#0a0a0a';
  if (!visible && explored) return {
    FLOOR: '#1a1a2e',
    WALL: '#3a3a4a',
    WATER: '#0f1a2e',
    CORRIDOR: '#202038',
  }[type] || '#1a1a2e';

  return {
    FLOOR: '#2d2d44',
    WALL: '#6a6a7a',
    WATER: '#1a3a5c',
    CORRIDOR: '#3d3d55',
  }[type] || '#2d2d44';
}

export function drawTile(ctx, x, y, type, visible, explored, entity) {
  const px = x * CELL_SIZE;
  const py = y * CELL_SIZE;

  ctx.fillStyle = getTileColor(type, visible, explored);
  ctx.fillRect(px, py, CELL_SIZE, CELL_SIZE);

  if (type === 'WALL') {
    ctx.fillStyle = visible ? '#5a5a6a' : '#2a2a3a';
    ctx.fillRect(px + 2, py + 2, CELL_SIZE - 4, CELL_SIZE - 4);
  }

  if (entity && visible) {
    drawEntity(ctx, px, py, entity);
  }
}

function drawEntity(ctx, px, py, entity) {
  let color = '#ffffff';
  let char = entity;

  switch (entity) {
    case '@': color = '#ffd700'; break;
    case 'G': color = '#ff4444'; break;
    case '/': color = '#44aaff'; break;
    case '!': color = '#44ff44'; break;
  }

  ctx.fillStyle = color;
  ctx.font = `${FONT_SIZE}px monospace`;
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillText(char, px + CELL_SIZE / 2, py + CELL_SIZE / 2 + 1);
}

export function drawHPBar(ctx, px, py, hp, maxHp) {
  const barWidth = CELL_SIZE - 4;
  const barHeight = 3;
  const bx = px + 2;
  const by = py + CELL_SIZE - 5;

  ctx.fillStyle = '#333';
  ctx.fillRect(bx, by, barWidth, barHeight);

  const ratio = hp / maxHp;
  ctx.fillStyle = ratio > 0.5 ? '#44ff44' : ratio > 0.25 ? '#ffff44' : '#ff4444';
  ctx.fillRect(bx, by, barWidth * ratio, barHeight);
}

export { CELL_SIZE };
