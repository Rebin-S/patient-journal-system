import { useState } from "react";
import { JournalApi } from "../api.js";

export default function PatientRecordViewer() {
    const [name, setName] = useState("");
    const [data, setData] = useState(null);   // { patient, notes, conditions }
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    async function handleSearch(e) {
        e.preventDefault();
        setError("");
        setData(null);
        if (!name.trim()) {
            setError("Skriv ett patientnamn.");
            return;
        }
        setLoading(true);
        try {
            const res = await JournalApi.getRecordByName(name.trim());
            setData(res);
        } catch (e) {
            setError(e.message || "Kunde inte hämta journal");
        } finally {
            setLoading(false);
        }
    }

    return (
        <section style={{ marginTop: 32 }}>
            <h2>Visa patientjournal (läkare/personal)</h2>

            <form onSubmit={handleSearch} style={{ marginBottom: 16 }}>
                <label>
                    Patientnamn{" "}
                    <input
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        style={{ marginLeft: 8 }}
                    />
                </label>
                <button type="submit" style={{ marginLeft: 8 }}>
                    Visa journal
                </button>
            </form>

            {loading && <p>Laddar...</p>}
            {error && <p style={{ color: "crimson" }}>{error}</p>}
            {!data && !loading && !error && <p>Sök en patient för att se journal.</p>}

            {data && (
                <div style={{ marginTop: 16 }}>
                    <div style={{ marginBottom: 16 }}>
                        <h3>Patientinformation</h3>
                        <p><strong>Namn:</strong> {data.patient.name}</p>
                        {data.patient.personnummer && (
                            <p><strong>Personnummer:</strong> {data.patient.personnummer}</p>
                        )}
                    </div>

                    <div style={{ marginBottom: 16 }}>
                        <h3>Noteringar</h3>
                        {!data.notes || data.notes.length === 0 ? (
                            <p>Inga noteringar.</p>
                        ) : (
                            <ul>
                                {data.notes.map((n) => (
                                    <li key={n.id}>
                                        <div>
                                            <strong>{n.startTime}</strong>
                                        </div>
                                        <div>{n.notes}</div>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>

                    <div>
                        <h3>Diagnoser</h3>
                        {!data.conditions || data.conditions.length === 0 ? (
                            <p>Inga diagnoser.</p>
                        ) : (
                            <ul>
                                {data.conditions.map((c) => (
                                    <li key={c.id}>
                                        <strong>{c.code}</strong> – {c.display}{" "}
                                        {c.onsetDate && <span>({c.onsetDate})</span>}
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>
                </div>
            )}
        </section>
    );
}
