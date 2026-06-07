"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ReactNode } from "react";

import { useAuth } from "./auth-provider";

const links = [
  { href: "/", label: "Dashboard" },
  { href: "/transactions", label: "Transactions" },
  { href: "/dividends", label: "Dividends" },
  { href: "/watchlist", label: "Watchlist" },
  { href: "/theses", label: "Theses" },
  { href: "/auth", label: "Auth" }
];

export function AppShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const { session, isAuthenticated, logout } = useAuth();

  return (
    <div className="shell">
      <header className="masthead">
        <div>
          <p className="eyebrow">Manual-first IDX tracker</p>
          <h1>SahamLog</h1>
        </div>
        <div className="nav-meta">
          {isAuthenticated ? (
            <>
              <span className="muted">Signed in as {session?.user.email}</span>
              <button type="button" className="secondary-button" onClick={logout}>
                Logout
              </button>
            </>
          ) : (
            <span className="muted">JWT session is stored only in this browser.</span>
          )}
        </div>
      </header>

      <nav className="nav">
        {links.map((link) => (
          <Link key={link.href} href={link.href} className={pathname === link.href ? "active" : undefined}>
            {link.label}
          </Link>
        ))}
      </nav>

      {children}
    </div>
  );
}
