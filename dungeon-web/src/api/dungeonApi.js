import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

export async function generateDungeon(width = 40, height = 20) {
  const { data } = await api.post('/dungeon/generate', { width, height });
  return data;
}

export async function getGameState(sessionId) {
  const { data } = await api.get(`/dungeon/${sessionId}/state`);
  return data;
}

export async function movePlayer(sessionId, direction) {
  const { data } = await api.post(`/dungeon/${sessionId}/move`, { direction });
  return data;
}

export async function pickupItem(sessionId) {
  const { data } = await api.post(`/dungeon/${sessionId}/pickup`);
  return data;
}

export async function usePotion(sessionId) {
  const { data } = await api.post(`/dungeon/${sessionId}/use-potion`);
  return data;
}

export async function waitTurn(sessionId) {
  const { data } = await api.post(`/dungeon/${sessionId}/wait`);
  return data;
}
