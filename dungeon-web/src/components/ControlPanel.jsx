import { useEffect } from 'react';

export default function ControlPanel({ onMove, onPickup, onUsePotion, onWait, onNewGame, disabled, hasGame }) {
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (!hasGame || disabled) return;

      const keyMap = {
        ArrowUp: () => onMove('up'),
        ArrowDown: () => onMove('down'),
        ArrowLeft: () => onMove('left'),
        ArrowRight: () => onMove('right'),
        w: () => onMove('up'),
        W: () => onMove('up'),
        s: () => onMove('down'),
        S: () => onMove('down'),
        a: () => onMove('left'),
        A: () => onMove('left'),
        d: () => onMove('right'),
        D: () => onMove('right'),
        e: () => onPickup(),
        E: () => onPickup(),
        p: () => onUsePotion(),
        P: () => onUsePotion(),
        ' ': (e) => { e.preventDefault(); onWait(); },
      };

      const action = keyMap[e.key];
      if (action) {
        e.preventDefault();
        action(e);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [hasGame, disabled, onMove, onPickup, onUsePotion, onWait]);

  if (!hasGame) return null;

  return (
    <div className="control-panel">
      <div className="dpad">
        <button className="dpad-btn dpad-up" onClick={() => onMove('up')} disabled={disabled}>&#9650;</button>
        <button className="dpad-btn dpad-left" onClick={() => onMove('left')} disabled={disabled}>&#9664;</button>
        <button className="dpad-btn dpad-center" disabled></button>
        <button className="dpad-btn dpad-right" onClick={() => onMove('right')} disabled={disabled}>&#9654;</button>
        <button className="dpad-btn dpad-down" onClick={() => onMove('down')} disabled={disabled}>&#9660;</button>
      </div>
      <div className="action-buttons">
        <button onClick={onPickup} disabled={disabled}>
          Pick Up (E)
        </button>
        <button onClick={onUsePotion} disabled={disabled}>
          Use Potion (P)
        </button>
        <button onClick={onWait} disabled={disabled}>
          Wait (Space)
        </button>
      </div>
      <button className="btn-new-game" onClick={onNewGame}>
        New Game
      </button>
    </div>
  );
}
