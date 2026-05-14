export default function StatusPanel({ gameState }) {
  if (!gameState) return null;

  const { player, status, turn, remainingMonsters } = gameState;
  const hpPercent = (player.hp / player.maxHp) * 100;

  return (
    <div className="status-panel">
      <h3>Status</h3>

      <div className="stat-row">
        <span className="stat-label">HP</span>
        <div className="hp-bar-outer">
          <div
            className="hp-bar-inner"
            style={{
              width: `${hpPercent}%`,
              backgroundColor: hpPercent > 50 ? '#44ff44' : hpPercent > 25 ? '#ffff44' : '#ff4444',
            }}
          />
        </div>
        <span className="stat-value">{player.hp}/{player.maxHp}</span>
      </div>

      <div className="stat-row"><span>ATK</span><span>{player.atk}{player.weaponName ? ` (+5)` : ''}</span></div>
      <div className="stat-row"><span>DEF</span><span>{player.def}</span></div>
      <div className="stat-row"><span>Level</span><span>{player.level}</span></div>
      <div className="stat-row"><span>EXP</span><span>{player.exp}/{player.expToNext}</span></div>
      <div className="stat-row"><span>Potions</span><span>{player.potions}</span></div>
      <div className="stat-row"><span>Weapon</span><span>{player.weaponName || 'None'}</span></div>
      <div className="stat-row"><span>Turn</span><span>{turn}</span></div>
      <div className="stat-row"><span>Monsters</span><span>{remainingMonsters}</span></div>

      {status !== 'PLAYING' && (
        <div className={`game-over-label ${status.toLowerCase()}`}>
          {status === 'VICTORY' ? 'VICTORY!' : 'DEFEAT'}
        </div>
      )}
    </div>
  );
}
