"use client";

import { FormEvent, useState, useTransition } from "react";
import { useRouter } from "next/navigation";

import { useAuth } from "../../components/auth-provider";

type AuthMode = "register" | "login";

export default function AuthPage() {
  const router = useRouter();
  const { session, error, clearError, login, register, logout } = useAuth();
  const [isPending, startTransition] = useTransition();
  const [registerForm, setRegisterForm] = useState({ email: "", password: "" });
  const [loginForm, setLoginForm] = useState({ email: "", password: "" });
  const [statusMessage, setStatusMessage] = useState<string | null>(null);

  function submit(mode: AuthMode, event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    clearError();
    setStatusMessage(null);

    startTransition(() => {
      const handler = mode === "register" ? register : login;
      const form = mode === "register" ? registerForm : loginForm;

      void handler(form)
        .then(() => {
          setStatusMessage(mode === "register" ? "Registration complete. Session is active." : "Login successful.");
          router.push("/");
        })
        .catch(() => undefined);
    });
  }

  return (
    <div className="grid two">
      <section className="card">
        <h2>Session</h2>
        {session ? (
          <div className="stack">
            <p className="muted">Current user</p>
            <div className="stat-small">{session.user.email}</div>
            <div className="inline-actions">
              <button type="button" className="secondary-button" onClick={() => router.push("/")}>
                Go to dashboard
              </button>
              <button type="button" onClick={logout}>
                Logout
              </button>
            </div>
          </div>
        ) : (
          <p className="muted">No active JWT session in this browser yet.</p>
        )}

        {statusMessage ? <p className="status success">{statusMessage}</p> : null}
        {error ? <p className="status error">{error}</p> : null}
      </section>

      <section className="grid">
        <div className="card">
          <h2>Register</h2>
          <form onSubmit={(event) => submit("register", event)}>
            <label>
              Email
              <input
                value={registerForm.email}
                onChange={(event) => setRegisterForm((current) => ({ ...current, email: event.target.value }))}
                placeholder="sam@example.com"
                type="email"
                required
              />
            </label>
            <label>
              Password
              <input
                value={registerForm.password}
                onChange={(event) => setRegisterForm((current) => ({ ...current, password: event.target.value }))}
                placeholder="password123"
                type="password"
                minLength={8}
                required
              />
            </label>
            <button type="submit" disabled={isPending}>
              {isPending ? "Submitting..." : "Register"}
            </button>
          </form>
        </div>

        <div className="card">
          <h2>Login</h2>
          <form onSubmit={(event) => submit("login", event)}>
            <label>
              Email
              <input
                value={loginForm.email}
                onChange={(event) => setLoginForm((current) => ({ ...current, email: event.target.value }))}
                placeholder="sam@example.com"
                type="email"
                required
              />
            </label>
            <label>
              Password
              <input
                value={loginForm.password}
                onChange={(event) => setLoginForm((current) => ({ ...current, password: event.target.value }))}
                placeholder="password123"
                type="password"
                minLength={8}
                required
              />
            </label>
            <button type="submit" disabled={isPending}>
              {isPending ? "Submitting..." : "Login"}
            </button>
          </form>
        </div>
      </section>
    </div>
  );
}
