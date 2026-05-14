import { useState } from 'react';
import { DEFAULT_WIDTH, DEFAULT_HEIGHT } from '../utils/constants';

export default function StartScreen({ onStart, loading }) {
  const [width, setWidth] = useState(DEFAULT_WIDTH);
  const [height, setHeight] = useState(DEFAULT_HEIGHT);

  const handleSubmit = (e) => {
    e.preventDefault();
    onStart(width, height);
  };

  return (
    <div className="start-screen">
      <div className="start-card">
        <h1>Procedural Dungeon</h1>
        <p className="subtitle">A Roguelike Dungeon Crawler</p>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="width">Width (20-80):</label>
            <input
              id="width"
              type="number"
              min="20"
              max="80"
              value={width}
              onChange={(e) => setWidth(Number(e.target.value))}
            />
          </div>
          <div className="form-group">
            <label htmlFor="height">Height (10-40):</label>
            <input
              id="height"
              type="number"
              min="10"
              max="40"
              value={height}
              onChange={(e) => setHeight(Number(e.target.value))}
            />
          </div>
          <button type="submit" disabled={loading} className="btn-start">
            {loading ? 'Generating...' : 'Enter the Dungeon'}
          </button>
        </form>
        <div className="controls-hint">
          <p>Controls: Arrow Keys / WASD to move</p>
          <p>E: Pick up item | P: Use potion | Space: Wait</p>
        </div>
      </div>
    </div>
  );
}
