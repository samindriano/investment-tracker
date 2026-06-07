import "./globals.css";
import type { ReactNode } from "react";

import { AppShell } from "../components/app-shell";
import { AuthProvider } from "../components/auth-provider";

export const metadata = {
  title: "SahamLog",
  description: "Manual IDX portfolio tracker"
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en">
      <body>
        <AuthProvider>
          <AppShell>{children}</AppShell>
        </AuthProvider>
      </body>
    </html>
  );
}
