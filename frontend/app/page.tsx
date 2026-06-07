"use client";

import { useEffect, useState } from "react";

import { AuthGate } from "../components/auth-gate";
import { useAuth } from "../components/auth-provider";
import {
  apiRequest,
  DashboardSummaryResponse,
  DividendSummaryResponse,
  getErrorMessage,
  isUnauthorizedError
} from "../lib/api";
import { formatCurrency, formatNumber, formatPercent } from "../lib/format";

export default function DashboardPage() {
  const { session, logout } = useAuth();
  const [dashboard, setDashboard] = useState<DashboardSummaryResponse | null>(null);
  const [dividendSummary, setDividendSummary] = useState<DividendSummaryResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!session?.accessToken) {
      return;
    }

    let active = true;

    async function loadDashboard() {
      const accessToken = session?.accessToken;
      if (!accessToken) {
        return;
      }

      try {
        setIsLoading(true);
        setError(null);
        const year = new Date().getFullYear();
        const [portfolioResponse, dividendResponse] = await Promise.all([
          apiRequest<DashboardSummaryResponse>("/reports/portfolio", { token: accessToken }),
          apiRequest<DividendSummaryResponse>(`/reports/dividends?year=${year}`, { token: accessToken })
        ]);

        if (!active) {
          return;
        }

        setDashboard(portfolioResponse);
        setDividendSummary(dividendResponse);
      } catch (requestError) {
        if (!active) {
          return;
        }
        if (isUnauthorizedError(requestError)) {
          logout();
        }
        setError(getErrorMessage(requestError));
      } finally {
        if (active) {
          setIsLoading(false);
        }
      }
    }

    void loadDashboard();

    return () => {
      active = false;
    };
  }, [logout, session?.accessToken]);

  return (
    <AuthGate description="Dashboard membutuhkan sesi aktif untuk memuat portfolio, dividend, dan holdings report.">
      <div className="grid">
        <section className="hero card">
          <p className="eyebrow">Backend-first finance workflow</p>
          <h2 className="hero-title">End-to-end flow sekarang bisa dimulai dari browser ini.</h2>
          <p className="muted">
            Registrasi atau login, input stock dan transaksi, set manual price, lalu semua ringkasan akan update dari API Spring Boot yang sama.
          </p>
        </section>

        {error ? <p className="status error">{error}</p> : null}

        <section className="grid stats-grid">
          <div className="card">
            <p className="muted">Total modal</p>
            <div className="stat">{formatCurrency(dashboard?.totalModal)}</div>
          </div>
          <div className="card">
            <p className="muted">Market value</p>
            <div className="stat">{formatCurrency(dashboard?.totalMarketValue)}</div>
          </div>
          <div className="card">
            <p className="muted">Unrealized P&amp;L</p>
            <div className="stat">{formatCurrency(dashboard?.totalUnrealizedGainLoss)}</div>
            <p className="muted">{formatPercent(dashboard?.totalUnrealizedGainLossPercentage)}</p>
          </div>
          <div className="card">
            <p className="muted">Dividend net this year</p>
            <div className="stat">{formatCurrency(dividendSummary?.totalNetDividend)}</div>
          </div>
        </section>

        <section className="card">
          <div className="section-heading">
            <h2>Portfolio holdings</h2>
            <span className="muted">{isLoading ? "Refreshing..." : `${dashboard?.holdings.length ?? 0} position(s)`}</span>
          </div>
          <table className="table">
            <thead>
              <tr>
                <th>Stock</th>
                <th>Lot</th>
                <th>Avg Price</th>
                <th>Current</th>
                <th>Market Value</th>
                <th>Unrealized</th>
                <th>Alloc</th>
              </tr>
            </thead>
            <tbody>
              {dashboard?.holdings.length ? (
                dashboard.holdings.map((item) => (
                  <tr key={item.stockId}>
                    <td>
                      <strong>{item.stockCode}</strong>
                      <div className="muted small-text">{item.stockName}</div>
                    </td>
                    <td>{formatNumber(item.totalLot)}</td>
                    <td>{formatCurrency(item.averagePrice)}</td>
                    <td>{formatCurrency(item.currentPrice)}</td>
                    <td>{formatCurrency(item.marketValue)}</td>
                    <td>{formatCurrency(item.unrealizedGainLoss)}</td>
                    <td>{formatPercent(item.allocationPercentage)}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={7} className="muted empty-cell">
                    No holdings yet. Start from the Transactions page.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </section>

        <section className="card">
          <div className="section-heading">
            <h2>Dividend by stock</h2>
            <span className="muted">{dividendSummary?.year ?? new Date().getFullYear()}</span>
          </div>
          <table className="table">
            <thead>
              <tr>
                <th>Stock</th>
                <th>Gross</th>
                <th>Tax</th>
                <th>Net</th>
                <th>Yield on Cost</th>
              </tr>
            </thead>
            <tbody>
              {dividendSummary?.byStock.length ? (
                dividendSummary.byStock.map((item) => (
                  <tr key={item.stockId}>
                    <td>{item.stockCode}</td>
                    <td>{formatCurrency(item.totalGrossDividend)}</td>
                    <td>{formatCurrency(item.totalTax)}</td>
                    <td>{formatCurrency(item.totalNetDividend)}</td>
                    <td>{formatPercent(item.yieldOnCostPercentage)}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={5} className="muted empty-cell">
                    No dividends recorded for this year yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </section>
      </div>
    </AuthGate>
  );
}
