export default function GameOverModal({ status, player, turn, onNewGame }) {
  if (!status || status === 'PLAYING') return null;

  const isVictory = status === 'VICTORY';

  return (
    <div className="modal-overlay">
      <div className={`modal ${isVictory ? 'victory' : 'defeat'}`}>
        <h2>{isVictory ? 'VICTORY!' : 'GAME OVER'}</h2>
        {player && (
          <div className="modal-stats">
            <p>Level: {player.level}</p>
            <p>Turns: {turn}</p>
            <p>Final HP: {player.hp}/{player.maxHp}</p>
          </div>
        )}
        <p className="modal-message">
          {isVictory
            ? 'You have defeated all monsters! The dungeon is clear.'
            : 'You have fallen in the depths of the dungeon...'}
        </p>
        <button className="btn-start" onClick={onNewGame}>
          Play Again
        </button>
      </div>
    </div>
  );
}
