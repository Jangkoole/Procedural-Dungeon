import { useEffect, useRef } from 'react';

export default function MessageLog({ messages }) {
  const logEndRef = useRef(null);

  useEffect(() => {
    logEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  return (
    <div className="message-log">
      <h3>Log</h3>
      <div className="log-entries">
        {(messages || []).map((msg, i) => (
          <div key={i} className="log-entry">
            {msg}
          </div>
        ))}
        <div ref={logEndRef} />
      </div>
    </div>
  );
}
