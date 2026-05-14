import { useState, useCallback } from 'react';
import * as api from '../api/dungeonApi';

export default function useGame() {
  const [sessionId, setSessionId] = useState(null);
  const [gameState, setGameState] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const updateState = useCallback((state) => {
    setGameState(state);
    setError(null);
  }, []);

  const newGame = useCallback(async (width, height) => {
    setLoading(true);
    setError(null);
    try {
      const state = await api.generateDungeon(width, height);
      setSessionId(state.sessionId);
      setGameState(state);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  const refreshState = useCallback(async () => {
    if (!sessionId) return;
    try {
      const state = await api.getGameState(sessionId);
      setGameState(state);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    }
  }, [sessionId]);

  const move = useCallback(async (direction) => {
    if (!sessionId) return;
    setLoading(true);
    setError(null);
    try {
      const state = await api.movePlayer(sessionId, direction);
      setGameState(state);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  }, [sessionId]);

  const pickup = useCallback(async () => {
    if (!sessionId) return;
    setLoading(true);
    setError(null);
    try {
      const state = await api.pickupItem(sessionId);
      setGameState(state);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  }, [sessionId]);

  const usePotion = useCallback(async () => {
    if (!sessionId) return;
    setLoading(true);
    setError(null);
    try {
      const state = await api.usePotion(sessionId);
      setGameState(state);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  }, [sessionId]);

  const wait = useCallback(async () => {
    if (!sessionId) return;
    setLoading(true);
    setError(null);
    try {
      const state = await api.waitTurn(sessionId);
      setGameState(state);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
    } finally {
      setLoading(false);
    }
  }, [sessionId]);

  const reset = useCallback(() => {
    setSessionId(null);
    setGameState(null);
    setError(null);
    setLoading(false);
  }, []);

  return {
    sessionId,
    gameState,
    loading,
    error,
    newGame,
    refreshState,
    move,
    pickup,
    usePotion,
    wait,
    reset,
  };
}
