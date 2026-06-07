"use client";

import { FormEvent, useEffect, useState, useTransition } from "react";

import { AuthGate } from "../../components/auth-gate";
import { useAuth } from "../../components/auth-provider";
import { apiRequest, getErrorMessage, isUnauthorizedError, StockResponse, WatchlistResponse } from "../../lib/api";
import { formatCurrency, formatPercent } from "../../lib/format";

interface WatchlistFormState {
  stockId: string;
  fairPrice: string;
  cheapPrice: string;
  veryCheapPrice: string;
  expensivePrice: string;
  notes: string;
}

const emptyForm: WatchlistFormState = {
  stockId: "",
  fairPrice: "",
  cheapPrice: "",
  veryCheapPrice: "",
  expensivePrice: "",
  notes: ""
};

export default function WatchlistPage() {
  const { session, logout } = useAuth();
  const [stocks, setStocks] = useState<StockResponse[]>([]);
  const [watchlist, setWatchlist] = useState<WatchlistResponse[]>([]);
  const [form, setForm] = useState<WatchlistFormState>(emptyForm);
  const [editingWatchlistId, setEditingWatchlistId] = useState<number | null>(null);
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
      const [stocksResponse, watchlistResponse] = await Promise.all([
        apiRequest<StockResponse[]>("/stocks", { token: accessToken }),
        apiRequest<WatchlistResponse[]>("/watchlist", { token: accessToken })
      ]);

      setStocks(stocksResponse);
      setWatchlist(watchlistResponse);
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

    const path = editingWatchlistId === null ? "/watchlist" : `/watchlist/${editingWatchlistId}`;
    const method = editingWatchlistId === null ? "POST" : "PUT";

    startTransition(() => {
      void apiRequest<WatchlistResponse>(path, {
        method,
        token: accessToken,
        body: {
          stockId: Number(form.stockId),
          fairPrice: Number(form.fairPrice),
          cheapPrice: Number(form.cheapPrice),
          veryCheapPrice: Number(form.veryCheapPrice),
          expensivePrice: Number(form.expensivePrice),
          notes: form.notes || null
        }
      })
        .then(async () => {
          setStatusMessage(editingWatchlistId === null ? "Watchlist item created." : "Watchlist item updated.");
          resetForm();
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

  function handleDelete(watchlistId: number) {
    const accessToken = session?.accessToken;
    if (!accessToken || !window.confirm("Delete this watchlist item?")) {
      return;
    }

    startTransition(() => {
      void apiRequest<null>(`/watchlist/${watchlistId}`, {
        method: "DELETE",
        token: accessToken
      })
        .then(async () => {
          setStatusMessage("Watchlist item deleted.");
          if (editingWatchlistId === watchlistId) {
            resetForm();
          }
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

  function startEditing(item: WatchlistResponse) {
    setEditingWatchlistId(item.id);
    setForm({
      stockId: String(item.stockId),
      fairPrice: String(item.fairPrice),
      cheapPrice: String(item.cheapPrice),
      veryCheapPrice: String(item.veryCheapPrice),
      expensivePrice: String(item.expensivePrice),
      notes: item.notes ?? ""
    });
  }

  function resetForm() {
    setEditingWatchlistId(null);
    setForm((current) => ({
      ...emptyForm,
      stockId: current.stockId || (stocks[0] ? String(stocks[0].id) : "")
    }));
  }

  return (
    <AuthGate description="Set valuation band per stock, lalu zone-nya akan ikut berubah dari manual current price yang sudah kamu input.">
      <div className="grid">
        {statusMessage ? <p className="status success">{statusMessage}</p> : null}
        {error ? <p className="status error">{error}</p> : null}

        <section className="grid two">
          <div className="card">
            <h2>{editingWatchlistId === null ? "Add watchlist item" : "Edit watchlist item"}</h2>
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
                  Fair price
                  <input
                    value={form.fairPrice}
                    onChange={(event) => setForm((current) => ({ ...current, fairPrice: event.target.value }))}
                    type="number"
                    min="0"
                    step="0.01"
                    required
                  />
                </label>
                <label>
                  Cheap price
                  <input
                    value={form.cheapPrice}
                    onChange={(event) => setForm((current) => ({ ...current, cheapPrice: event.target.value }))}
                    type="number"
                    min="0"
                    step="0.01"
                    required
                  />
                </label>
              </div>
              <div className="grid two">
                <label>
                  Very cheap price
                  <input
                    value={form.veryCheapPrice}
                    onChange={(event) => setForm((current) => ({ ...current, veryCheapPrice: event.target.value }))}
                    type="number"
                    min="0"
                    step="0.01"
                    required
                  />
                </label>
                <label>
                  Expensive price
                  <input
                    value={form.expensivePrice}
                    onChange={(event) => setForm((current) => ({ ...current, expensivePrice: event.target.value }))}
                    type="number"
                    min="0"
                    step="0.01"
                    required
                  />
                </label>
              </div>
              <label>
                Notes
                <textarea
                  value={form.notes}
                  onChange={(event) => setForm((current) => ({ ...current, notes: event.target.value }))}
                  rows={3}
                />
              </label>
              <div className="inline-actions">
                <button type="submit" disabled={isPending || !form.stockId}>
                  {editingWatchlistId === null ? "Save watchlist item" : "Update watchlist item"}
                </button>
                {editingWatchlistId !== null ? (
                  <button type="button" className="secondary-button" onClick={resetForm}>
                    Cancel edit
                  </button>
                ) : null}
              </div>
            </form>
          </div>

          <div className="card">
            <h2>Valuation snapshot</h2>
            <p className="muted">Current zone is resolved from the latest manual price snapshot for each stock.</p>
            <table className="table">
              <thead>
                <tr>
                  <th>Stock</th>
                  <th>Current</th>
                  <th>Zone</th>
                  <th>Premium/Discount</th>
                  <th>MoS</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {watchlist.length ? (
                  watchlist.map((item) => (
                    <tr key={item.id}>
                      <td>{item.stockCode}</td>
                      <td>{formatCurrency(item.currentPrice)}</td>
                      <td>
                        <span className="pill">{item.valuationZone}</span>
                      </td>
                      <td>{formatPercent(item.premiumDiscountPercentage)}</td>
                      <td>{formatPercent(item.marginOfSafetyPercentage)}</td>
                      <td>
                        <div className="inline-actions compact">
                          <button type="button" className="secondary-button" onClick={() => startEditing(item)}>
                            Edit
                          </button>
                          <button type="button" className="danger-button" onClick={() => handleDelete(item.id)}>
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={6} className="muted empty-cell">
                      No watchlist items yet.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </AuthGate>
  );
}
