import useGame from './hooks/useGame';
import StartScreen from './components/StartScreen';
import GameCanvas from './components/GameCanvas';
import StatusPanel from './components/StatusPanel';
import MessageLog from './components/MessageLog';
import ControlPanel from './components/ControlPanel';
import GameOverModal from './components/GameOverModal';

export default function App() {
  const {
    sessionId,
    gameState,
    loading,
    error,
    newGame,
    move,
    pickup,
    usePotion,
    wait,
    reset,
  } = useGame();

  const hasGame = !!sessionId;
  const isGameOver = gameState && gameState.status !== 'PLAYING';

  const handleNewGame = (w, h) => newGame(w, h);

  const handleReset = () => {
    reset();
  };

  return (
    <div className="app">
      {!hasGame ? (
        <StartScreen onStart={handleNewGame} loading={loading} />
      ) : (
        <div className="game-layout">
          {error && <div className="error-banner">{error}<button onClick={() => window.location.reload()}>Dismiss</button></div>}
          <GameCanvas gameState={gameState} />
          <div className="sidebar">
            <StatusPanel gameState={gameState} />
            <MessageLog messages={gameState?.messages} />
            <ControlPanel
              onMove={move}
              onPickup={pickup}
              onUsePotion={usePotion}
              onWait={wait}
              onNewGame={handleReset}
              disabled={loading || isGameOver}
              hasGame={hasGame}
            />
          </div>
        </div>
      )}
      <GameOverModal
        status={gameState?.status}
        player={gameState?.player}
        turn={gameState?.turn}
        onNewGame={handleReset}
      />
    </div>
  );
}
