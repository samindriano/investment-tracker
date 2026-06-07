"use client";

import Link from "next/link";
import type { ReactNode } from "react";

import { useAuth } from "./auth-provider";

export function AuthGate({
  title,
  description,
  children
}: {
  title?: string;
  description?: string;
  children: ReactNode;
}) {
  const { isReady, isAuthenticated } = useAuth();

  if (!isReady) {
    return (
      <div className="card">
        <h2>Checking session</h2>
        <p className="muted">Loading your local JWT session before rendering this page.</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="card">
        <h2>{title ?? "Login required"}</h2>
        <p className="muted">
          {description ?? "This screen uses authenticated SahamLog APIs. Sign in first to continue."}
        </p>
        <div className="inline-actions">
          <Link href="/auth" className="button-link">
            Open auth page
          </Link>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}
