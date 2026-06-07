"use client";

import { FormEvent, useEffect, useState, useTransition } from "react";

import { AuthGate } from "../../components/auth-gate";
import { useAuth } from "../../components/auth-provider";
import {
  apiRequest,
  DividendResponse,
  DividendSummaryResponse,
  getErrorMessage,
  isUnauthorizedError,
  PageResponse,
  StockResponse
} from "../../lib/api";
import { formatCurrency, formatPercent } from "../../lib/format";

interface DividendFormState {
  stockId: string;
  cumDate: string;
  paymentDate: string;
  dividendPerShare: string;
  sharesOwned: string;
  taxRate: string;
}

const currentYear = new Date().getFullYear();
const emptyDividendForm: DividendFormState = {
  stockId: "",
  cumDate: "",
  paymentDate: new Date().toISOString().slice(0, 10),
  dividendPerShare: "",
  sharesOwned: "",
  taxRate: "10"
};

export default function DividendsPage() {
  const { session, logout } = useAuth();
  const [stocks, setStocks] = useState<StockResponse[]>([]);
  const [dividends, setDividends] = useState<DividendResponse[]>([]);
  const [summary, setSummary] = useState<DividendSummaryResponse | null>(null);
  const [form, setForm] = useState<DividendFormState>(emptyDividendForm);
  const [error, setError] = useState<string | null>(null);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [isPending, startTransition] = useTransition();

  useEffect(() => {
    if (!session?.accessToken) {
      return;
    }

    void loadData();
  }, [session?.accessToken]);

  async function loadData() {
    const accessToken = session?.accessToken;
    if (!accessToken) {
      return;
    }

    try {
      setError(null);
      const [stocksResponse, dividendsResponse, summaryResponse] = await Promise.all([
        apiRequest<StockResponse[]>("/stocks", { token: accessToken }),
        apiRequest<PageResponse<DividendResponse>>(`/dividends?page=0&size=20&year=${currentYear}`, {
          token: accessToken
        }),
        apiRequest<DividendSummaryResponse>(`/dividends/summary?year=${currentYear}`, { token: accessToken })
      ]);

      setStocks(stocksResponse);
      setDividends(dividendsResponse.items);
      setSummary(summaryResponse);
      setForm((current) => ({
        ...current,
        stockId: current.stockId || (stocksResponse[0] ? String(stocksResponse[0].id) : "")
      }));
    } catch (requestError) {
      if (isUnauthorizedError(requestError)) {
        logout();
      }
      setError(getErrorMessage(requestError));
    }
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const accessToken = session?.accessToken;
    if (!accessToken) {
      return;
    }

    startTransition(() => {
      void apiRequest<DividendResponse>("/dividends", {
        method: "POST",
        token: accessToken,
        body: {
          stockId: Number(form.stockId),
          cumDate: form.cumDate || null,
          paymentDate: form.paymentDate,
          dividendPerShare: Number(form.dividendPerShare),
          sharesOwned: Number(form.sharesOwned),
          taxRate: Number(form.taxRate)
        }
      })
        .then(async () => {
          setStatusMessage("Dividend recorded.");
          setForm((current) => ({
            ...emptyDividendForm,
            stockId: current.stockId,
            paymentDate: current.paymentDate
          }));
          await loadData();
        })
        .catch((requestError) => {
          if (isUnauthorizedError(requestError)) {
            logout();
          }
          setError(getErrorMessage(requestError));
        });
    });
  }

  function handleDelete(dividendId: number) {
    const accessToken = session?.accessToken;
    if (!accessToken || !window.confirm("Delete this dividend record?")) {
      return;
    }

    startTransition(() => {
      void apiRequest<null>(`/dividends/${dividendId}`, {
        method: "DELETE",
        token: accessToken
      })
        .then(async () => {
          setStatusMessage("Dividend deleted.");
          await loadData();
        })
        .catch((requestError) => {
          if (isUnauthorizedError(requestError)) {
            logout();
          }
          setError(getErrorMessage(requestError));
        });
    });
  }

  return (
    <AuthGate description="Input dividend manual di sini, lalu summary tahunan akan otomatis pakai hasil hitungan backend.">
      <div className="grid">
        {statusMessage ? <p className="status success">{statusMessage}</p> : null}
        {error ? <p className="status error">{error}</p> : null}

        <section className="grid two">
          <div className="card">
            <h2>Record dividend</h2>
            <form onSubmit={handleSubmit}>
              <label>
                Stock
                <select
                  value={form.stockId}
                  onChange={(event) => setForm((current) => ({ ...current, stockId: event.target.value }))}
                  required
                >
                  <option value="">Select stock</option>
                  {stocks.map((stock) => (
                    <option key={stock.id} value={stock.id}>
                      {stock.code} - {stock.name}
                    </option>
                  ))}
                </select>
              </label>
              <div className="grid two">
                <label>
                  Cum date
                  <input
                    value={form.cumDate}
                    onChange={(event) => setForm((current) => ({ ...current, cumDate: event.target.value }))}
                    type="date"
                  />
                </label>
                <label>
                  Payment date
                  <input
                    value={form.paymentDate}
                    onChange={(event) => setForm((current) => ({ ...current, paymentDate: event.target.value }))}
                    type="date"
                    required
                  />
                </label>
              </div>
              <div className="grid two">
                <label>
                  Dividend/share
                  <input
                    value={form.dividendPerShare}
                    onChange={(event) => setForm((current) => ({ ...current, dividendPerShare: event.target.value }))}
                    type="number"
                    min="0"
                    step="0.01"
                    required
                  />
                </label>
                <label>
                  Shares owned
                  <input
                    value={form.sharesOwned}
                    onChange={(event) => setForm((current) => ({ ...current, sharesOwned: event.target.value }))}
                    type="number"
                    min="0"
                    required
                  />
                </label>
              </div>
              <label>
                Tax rate
                <input
                  value={form.taxRate}
                  onChange={(event) => setForm((current) => ({ ...current, taxRate: event.target.value }))}
                  type="number"
                  min="0"
                  max="100"
                  step="0.01"
                  required
                />
              </label>
              <button type="submit" disabled={isPending || !form.stockId}>
                Save dividend
              </button>
            </form>
          </div>

          <div className="card">
            <h2>Dividend summary {summary?.year ?? currentYear}</h2>
            <p>Total gross: {formatCurrency(summary?.totalGrossDividend)}</p>
            <p>Total tax: {formatCurrency(summary?.totalTax)}</p>
            <p>Total net: {formatCurrency(summary?.totalNetDividend)}</p>
            <table className="table">
              <thead>
                <tr>
                  <th>Stock</th>
                  <th>Net</th>
                  <th>YoC</th>
                </tr>
              </thead>
              <tbody>
                {summary?.byStock.length ? (
                  summary.byStock.map((item) => (
                    <tr key={item.stockId}>
                      <td>{item.stockCode}</td>
                      <td>{formatCurrency(item.totalNetDividend)}</td>
                      <td>{formatPercent(item.yieldOnCostPercentage)}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={3} className="muted empty-cell">
                      No dividends recorded yet.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        <section className="card">
          <h2>Recent dividend records</h2>
          <table className="table">
            <thead>
              <tr>
                <th>Stock</th>
                <th>Payment</th>
                <th>Gross</th>
                <th>Tax</th>
                <th>Net</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {dividends.length ? (
                dividends.map((item) => (
                  <tr key={item.id}>
                    <td>{item.stockCode}</td>
                    <td>{item.paymentDate}</td>
                    <td>{formatCurrency(item.grossDividend)}</td>
                    <td>{formatCurrency(item.totalTax)}</td>
                    <td>{formatCurrency(item.netReceived)}</td>
                    <td>
                      <button type="button" className="danger-button" onClick={() => handleDelete(item.id)}>
                        Delete
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6} className="muted empty-cell">
                    No dividends recorded yet.
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
