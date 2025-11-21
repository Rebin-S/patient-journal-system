import { useState } from "react";
import "../css-styles/Login.css";

export default function Login({ onLogin, onShowRegister }) {
    const [username, setU] = useState("");
    const [password, setP] = useState("");
    const [error, setError] = useState("");

    async function handleSubmit(e) {
        e.preventDefault();
        setError("");

        try {
            const res = await fetch("/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password }),
            });

            if (!res.ok) throw new Error(await res.text());

            const data = await res.json();
            localStorage.setItem("token", data.token);
            localStorage.setItem("user", JSON.stringify(data.user));
            onLogin?.(data.user);
        } catch (err) {
            setError(err.message || "Login failed");
        }
    }

    return (
        <div style={{ maxWidth: 360, margin: "4rem auto", fontFamily: "system-ui" }}>
            <h2>Logga in</h2>
            <form onSubmit={handleSubmit}>
                <label>Användarnamn</label>
                <input
                    value={username}
                    onChange={(e) => setU(e.target.value)}
                    style={{ width: "100%", marginBottom: 8 }}
                />
                <label>Lösenord</label>
                <input
                    type="password"
                    value={password}
                    onChange={(e) => setP(e.target.value)}
                    style={{ width: "100%", marginBottom: 8 }}
                />
                {error && (
                    <div style={{ color: "crimson", marginBottom: 8 }}>
                        {error}
                    </div>
                )}
                <div style={{ display: "flex", gap: 8 }}>
                    <button type="submit">Logga in</button>
                    <button
                        type="button"
                        onClick={() => onShowRegister?.()}
                    >
                        Create account
                    </button>
                </div>
            </form>
        </div>
    );
}
