import { useState } from "react";
import { AuthApi } from "../api.js";

const ROLES = ["PATIENT", "DOCTOR", "STAFF"];

export default function Register({ onDone }) {
    const [username, setU] = useState("");
    const [password, setP] = useState("");
    const [role, setRole] = useState("PATIENT");
    const [error, setError] = useState("");
    const [ok, setOk] = useState("");

    async function handleSubmit(e) {
        e.preventDefault();
        setError("");
        setOk("");

        const payload = {
            username,
            password,
            role,
        };

        try {
            await AuthApi.register(payload);
            setOk("Account created! You can log in now.");
            setTimeout(() => onDone?.(), 800); // tillbaka till login
        } catch (e) {
            setError(e.message || "Registration failed");
        }
    }

    return (
        <div style={{ maxWidth: 420, margin: "4rem auto", fontFamily: "system-ui" }}>
            <h2>Create account</h2>
            <form onSubmit={handleSubmit}>
                <div style={{ margin: "8px 0" }}>
                    <label>Username</label>
                    <input
                        value={username}
                        onChange={(e) => setU(e.target.value)}
                        style={{ width: "100%" }}
                        required
                    />
                </div>

                <div style={{ margin: "8px 0" }}>
                    <label>Password</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setP(e.target.value)}
                        style={{ width: "100%" }}
                        required
                    />
                </div>

                <div style={{ margin: "8px 0" }}>
                    <label>Role</label>
                    <select
                        value={role}
                        onChange={(e) => setRole(e.target.value)}
                        style={{ width: "100%" }}
                    >
                        {ROLES.map((r) => (
                            <option key={r} value={r}>
                                {r}
                            </option>
                        ))}
                    </select>
                </div>

                {error && <div style={{ color: "crimson", marginTop: 8 }}>{error}</div>}
                {ok && <div style={{ color: "seagreen", marginTop: 8 }}>{ok}</div>}

                <div style={{ display: "flex", gap: 8, marginTop: 12 }}>
                    <button type="submit">Create account</button>
                    <button type="button" onClick={() => onDone?.()}>
                        Back to login
                    </button>
                </div>
            </form>
        </div>
    );
}
