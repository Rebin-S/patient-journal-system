import { useEffect, useState } from "react";
import { JournalApi } from "../api.js";

export default function MyJournal() {
    const [data, setData] = useState(null);   // { patient, notes, conditions }
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let cancelled = false;
        async function load() {
            try {
                const res = await JournalApi.getMyRecord();
                if (!cancelled) {
                    setData(res);
                }
            } catch (e) {
                if (!cancelled) setError(e.message || "Kunde inte hämta journal");
            } finally {
                if (!cancelled) setLoading(false);
            }
        }
        load();
        return () => { cancelled = true; };
    }, []);

    if (loading) return <p>Laddar din journal...</p>;
    if (error) return <p style={{ color: "crimson" }}>{error}</p>;
    if (!data) return null;

    const { patient, notes, conditions } = data;

    return (
        <section style={{ marginTop: 32 }}>
            <h2>Min journal</h2>

            <div style={{ marginBottom: 16 }}>
                <h3>Patientinformation</h3>
                {patient.personnummer && <p><strong>Personnummer:</strong> {patient.personnummer}</p>}
                {patient.birthDate && <p><strong>Födelsedatum:</strong> {patient.birthDate}</p>}
                {patient.gender && <p><strong>Kön:</strong> {patient.gender}</p>}
                {patient.contactInfo && <p><strong>Kontakt:</strong> {patient.contactInfo}</p>}
            </div>

            <div style={{ marginBottom: 16 }}>
                <h3>Noteringar</h3>
                {!notes || notes.length === 0 ? (
                    <p>Inga noteringar.</p>
                ) : (
                    <ul>
                        {notes.map((n) => (
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
                {!conditions || conditions.length === 0 ? (
                    <p>Inga diagnoser.</p>
                ) : (
                    <ul>
                        {conditions.map((c) => (
                            <li key={c.id}>
                                <strong>{c.code}</strong> – {c.display}{" "}
                                {c.onsetDate && <span>({c.onsetDate})</span>}
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </section>
    );
}
