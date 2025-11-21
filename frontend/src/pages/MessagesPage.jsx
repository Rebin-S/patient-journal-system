import { useEffect, useState } from "react";
import { MessageApi } from "../api.js";

export default function MessagesPage({ me }) {
  const [contacts, setContacts] = useState([]);
  const [selected, setSelected] = useState(null); // vald kontakt
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState("");
  const [error, setError] = useState("");
  const [loadingThread, setLoadingThread] = useState(false);

  useEffect(() => {
    loadContacts();
  }, []);

  async function loadContacts() {
    setError("");
    try {
      const list = await MessageApi.getContacts();
      setContacts(list);
    } catch (e) {
      setError(e.message || "Kunde inte hämta kontakter");
    }
  }

  async function openThread(contact) {
    setSelected(contact);
    setMessages([]);
    setError("");
    setLoadingThread(true);
    try {
      const msgs = await MessageApi.getThread(contact.id);
      setMessages(msgs);
    } catch (e) {
      setError(e.message || "Kunde inte hämta meddelanden");
    } finally {
      setLoadingThread(false);
    }
  }

  async function send(e) {
    e.preventDefault();
    if (!selected || !text.trim()) return;
    setError("");
    try {
      const sent = await MessageApi.send({
        receiverId: selected.id,
        content: text.trim(),
      });
      setMessages((prev) => [...prev, sent]);
      setText("");
    } catch (e) {
      setError(e.message || "Kunde inte skicka meddelande");
    }
  }

  return (
    <div style={{ display: "flex", gap: 16, marginTop: 24 }}>
      {/* Vänster kolumn: kontakter */}
      <div style={{ width: 220 }}>
        <h2>Meddelanden</h2>
        <p style={{ fontSize: 12, opacity: 0.7 }}>
          {me.role === "PATIENT"
            ? "Du kan skriva till läkare och personal."
            : "Du kan skriva till patienter."}
        </p>
        <ul style={{ listStyle: "none", padding: 0 }}>
          {contacts.map((c) => (
            <li key={c.id}>
              <button
                onClick={() => openThread(c)}
                style={{
                  width: "100%",
                  textAlign: "left",
                  padding: "4px 8px",
                  marginBottom: 4,
                  background:
                    selected && selected.id === c.id ? "#333" : "#222",
                  borderRadius: 4,
                  border: "1px solid #444",
                  color: "white",
                  cursor: "pointer",
                }}
              >
                {c.username} ({c.role})
              </button>
            </li>
          ))}
        </ul>
      </div>

      {/* Höger kolumn: tråd */}
      <div style={{ flex: 1 }}>
        {error && <div style={{ color: "crimson", marginBottom: 8 }}>{error}</div>}

        {!selected ? (
          <p>Välj en kontakt till vänster för att se meddelanden.</p>
        ) : (
          <>
            <h3>
              Konversation med {selected.username} ({selected.role})
            </h3>
            {loadingThread ? (
              <p>Laddar meddelanden...</p>
            ) : (
              <div
                style={{
                  border: "1px solid #444",
                  borderRadius: 4,
                  padding: 8,
                  height: 260,
                  overflowY: "auto",
                  marginBottom: 8,
                  background: "#111",
                }}
              >
                {messages.length === 0 ? (
                  <p>Inga meddelanden ännu.</p>
                ) : (
                  messages.map((m) => (
                    <div
                      key={m.id}
                      style={{
                        marginBottom: 6,
                        textAlign:
                          m.senderId === me.id ? "right" : "left",
                      }}
                    >
                      <div
                        style={{
                          display: "inline-block",
                          padding: "4px 8px",
                          borderRadius: 4,
                          background:
                            m.senderId === me.id ? "#2b6cb0" : "#333",
                        }}
                      >
                        <div style={{ fontSize: 11, opacity: 0.8 }}>
                          {m.senderName} • {m.sentAt}
                        </div>
                        <div>{m.content}</div>
                      </div>
                    </div>
                  ))
                )}
              </div>
            )}

            <form onSubmit={send}>
              <textarea
                value={text}
                onChange={(e) => setText(e.target.value)}
                rows={3}
                style={{ width: "100%", marginBottom: 8 }}
                placeholder="Skriv ett meddelande..."
              />
              <button type="submit" disabled={!selected}>
                Skicka
              </button>
            </form>
          </>
        )}
      </div>
    </div>
  );
}
