"use client";

import type { ReactNode } from "react";
import { createContext, useContext, useEffect, useState } from "react";

import {
  apiRequest,
  AuthMeResponse,
  AuthResponse,
  getErrorMessage,
  isUnauthorizedError,
  UserSummary
} from "../lib/api";

const STORAGE_KEY = "sahamlog.auth.session";

export interface AuthSession {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserSummary;
}

interface Credentials {
  email: string;
  password: string;
}

interface AuthContextValue {
  session: AuthSession | null;
  isReady: boolean;
  isAuthenticated: boolean;
  error: string | null;
  login: (credentials: Credentials) => Promise<void>;
  register: (credentials: Credentials) => Promise<void>;
  logout: () => void;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [isReady, setIsReady] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const rawSession = window.localStorage.getItem(STORAGE_KEY);
    if (rawSession) {
      try {
        setSession(JSON.parse(rawSession) as AuthSession);
      } catch {
        window.localStorage.removeItem(STORAGE_KEY);
      }
    }
    setIsReady(true);
  }, []);

  useEffect(() => {
    const currentSession = session;
    const accessToken = currentSession?.accessToken;
    if (!accessToken) {
      return;
    }

    let active = true;
    void apiRequest<AuthMeResponse>("/auth/me", { token: accessToken })
      .then((user) => {
        if (!active) {
          return;
        }
        persistSession({ ...currentSession, user });
      })
      .catch((requestError) => {
        if (!active) {
          return;
        }
        if (isUnauthorizedError(requestError)) {
          persistSession(null);
          setError("Session expired. Please login again.");
        }
      });

    return () => {
      active = false;
    };
  }, [session?.accessToken]);

  function persistSession(nextSession: AuthSession | null) {
    setSession(nextSession);
    if (nextSession) {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(nextSession));
      return;
    }
    window.localStorage.removeItem(STORAGE_KEY);
  }

  async function authenticate(path: string, credentials: Credentials) {
    try {
      setError(null);
      const response = await apiRequest<AuthResponse>(path, {
        method: "POST",
        body: credentials
      });
      persistSession(response);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
      throw requestError;
    }
  }

  async function login(credentials: Credentials) {
    await authenticate("/auth/login", credentials);
  }

  async function register(credentials: Credentials) {
    await authenticate("/auth/register", credentials);
  }

  function logout() {
    setError(null);
    persistSession(null);
  }

  function clearError() {
    setError(null);
  }

  return (
    <AuthContext.Provider
      value={{
        session,
        isReady,
        isAuthenticated: session !== null,
        error,
        login,
        register,
        logout,
        clearError
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
