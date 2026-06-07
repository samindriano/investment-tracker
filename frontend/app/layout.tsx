import "./globals.css";
import Link from "next/link";
import type { ReactNode } from "react";

export const metadata = {
  title: "SahamLog",
  description: "Manual IDX portfolio tracker"
};

const links = [
  { href: "/", label: "Dashboard" },
  { href: "/auth", label: "Auth" },
  { href: "/transactions", label: "Transactions" },
  { href: "/dividends", label: "Dividends" },
  { href: "/watchlist", label: "Watchlist" },
  { href: "/theses", label: "Theses" }
];

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <body>
        <div className="shell">
          <nav className="nav">
            {links.map((link) => (
              <Link key={link.href} href={link.href}>
                {link.label}
              </Link>
            ))}
          </nav>
          {children}
        </div>
      </body>
    </html>
  );
}
