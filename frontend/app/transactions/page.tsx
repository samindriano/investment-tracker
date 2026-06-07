"use client";

import { FormEvent, useEffect, useState, useTransition } from "react";

import { AuthGate } from "../../components/auth-gate";
import { useAuth } from "../../components/auth-provider";
import {
  apiRequest,
  getErrorMessage,
  HoldingResponse,
  isUnauthorizedError,
  PageResponse,
  PriceResponse,
  StockResponse,
  TransactionResponse,
  TransactionType
} from "../../lib/api";
import { formatCurrency, formatDateTime, formatNumber } from "../../lib/format";

interface StockFormState {
  code: string;
  name: string;
  sector: string;
}

interface TransactionFormState {
  stockId: string;
  type: TransactionType;
  transactionDate: string;
  quantityLot: string;
  price: string;
  fee: string;
  notes: string;
}

interface PriceFormState {
  stockId: string;
  price: string;
}

const emptyStockForm: StockFormState = { code: "", name: "", sector: "" };
const emptyTransactionForm: TransactionFormState = {
  stockId: "",
  type: "BUY",
  transactionDate: new Date().toISOString().slice(0, 10),
  quantityLot: "",
  price: "",
  fee: "0",
  notes: ""
};
const emptyPriceForm: PriceFormState = { stockId: "", price: "" };

export default function TransactionsPage() {
  const { session, logout } = useAuth();
  const [stocks, setStocks] = useState<StockResponse[]>([]);
  const [transactions, setTransactions] = useState<TransactionResponse[]>([]);
  const [holdings, setHoldings] = useState<HoldingResponse[]>([]);
  const [prices, setPrices] = useState<PriceResponse[]>([]);
  const [stockForm, setStockForm] = useState<StockFormState>(emptyStockForm);
  const [transactionForm, setTransactionForm] = useState<TransactionFormState>(emptyTransactionForm);
  const [priceForm, setPriceForm] = useState<PriceFormState>(emptyPriceForm);
  const [editingTransactionId, setEditingTransactionId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
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
      setIsLoading(true);
      setError(null);

      const [stocksResponse, transactionsResponse, holdingsResponse, pricesResponse] = await Promise.all([
        apiRequest<StockResponse[]>("/stocks", { token: accessToken }),
        apiRequest<PageResponse<TransactionResponse>>("/transactions?page=0&size=20", { token: accessToken }),
        apiRequest<HoldingResponse[]>("/portfolio/holdings", { token: accessToken }),
        apiRequest<PriceResponse[]>("/prices", { token: accessToken })
      ]);

      setStocks(stocksResponse);
      setTransactions(transactionsResponse.items);
      setHoldings(holdingsResponse);
      setPrices(pricesResponse);

      if (stocksResponse.length > 0) {
        setTransactionForm((current) => ({
          ...current,
          stockId: current.stockId || String(stocksResponse[0].id)
        }));
        setPriceForm((current) => ({
          ...current,
          stockId: current.stockId || String(stocksResponse[0].id)
        }));
      }
    } catch (requestError) {
      handleRequestError(requestError);
    } finally {
      setIsLoading(false);
    }
  }

  function handleRequestError(requestError: unknown) {
    if (isUnauthorizedError(requestError)) {
      logout();
    }
    setError(getErrorMessage(requestError));
  }

  function handleCreateStock(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const accessToken = session?.accessToken;
    if (!accessToken) {
      return;
    }

    startTransition(() => {
      void apiRequest<StockResponse>("/stocks", {
        method: "POST",
        token: accessToken,
        body: {
          code: stockForm.code,
          name: stockForm.name,
          sector: stockForm.sector || null
        }
      })
        .then(async (createdStock) => {
          setStatusMessage(`Stock ${createdStock.code} created.`);
          setStockForm(emptyStockForm);
          setTransactionForm((current) => ({ ...current, stockId: String(createdStock.id) }));
          setPriceForm((current) => ({ ...current, stockId: String(createdStock.id) }));
          await loadData();
        })
        .catch(handleRequestError);
    });
  }

  function handleSaveTransaction(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const accessToken = session?.accessToken;
    if (!accessToken) {
      return;
    }

    const path = editingTransactionId === null ? "/transactions" : `/transactions/${editingTransactionId}`;
    const method = editingTransactionId === null ? "POST" : "PUT";

    startTransition(() => {
      void apiRequest<TransactionResponse>(path, {
        method,
        token: accessToken,
        body: {
          stockId: Number(transactionForm.stockId),
          type: transactionForm.type,
          transactionDate: transactionForm.transactionDate,
          quantityLot: Number(transactionForm.quantityLot),
          price: Number(transactionForm.price),
          fee: Number(transactionForm.fee || "0"),
          notes: transactionForm.notes || null
        }
      })
        .then(async () => {
          setStatusMessage(editingTransactionId === null ? "Transaction created." : "Transaction updated.");
          resetTransactionForm();
          await loadData();
        })
        .catch(handleRequestError);
    });
  }

  function handleDeleteTransaction(transactionId: number) {
    const accessToken = session?.accessToken;
    if (!accessToken || !window.confirm("Delete this transaction?")) {
      return;
    }

    startTransition(() => {
      void apiRequest<null>(`/transactions/${transactionId}`, {
        method: "DELETE",
        token: accessToken
      })
        .then(async () => {
          setStatusMessage("Transaction deleted.");
          if (editingTransactionId === transactionId) {
            resetTransactionForm();
          }
          await loadData();
        })
        .catch(handleRequestError);
    });
  }

  function handleUpsertPrice(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const accessToken = session?.accessToken;
    if (!accessToken) {
      return;
    }

    startTransition(() => {
      void apiRequest<PriceResponse>(`/prices/${priceForm.stockId}`, {
        method: "PUT",
        token: accessToken,
        body: {
          price: Number(priceForm.price)
        }
      })
        .then(async () => {
          setStatusMessage("Price snapshot saved.");
          setPriceForm((current) => ({ ...emptyPriceForm, stockId: current.stockId }));
          await loadData();
        })
        .catch(handleRequestError);
    });
  }

  function startEditingTransaction(transaction: TransactionResponse) {
    setEditingTransactionId(transaction.id);
    setTransactionForm({
      stockId: String(transaction.stockId),
      type: transaction.type,
      transactionDate: transaction.transactionDate,
      quantityLot: String(transaction.quantityLot),
      price: String(transaction.price),
      fee: String(transaction.fee),
      notes: transaction.notes ?? ""
    });
  }

  function resetTransactionForm() {
    setEditingTransactionId(null);
    setTransactionForm((current) => ({
      ...emptyTransactionForm,
      stockId: current.stockId || (stocks[0] ? String(stocks[0].id) : "")
    }));
  }

  return (
    <AuthGate description="Gunakan halaman ini untuk bikin stock, transaksi buy-sell, dan manual price tanpa Postman.">
      <div className="grid">
        {statusMessage ? <p className="status success">{statusMessage}</p> : null}
        {error ? <p className="status error">{error}</p> : null}

        <section className="grid two">
          <div className="card">
            <div className="section-heading">
              <h2>Stock master</h2>
              <span className="muted">{stocks.length} stock(s)</span>
            </div>
            <form onSubmit={handleCreateStock}>
              <label>
                Code
                <input
                  value={stockForm.code}
                  onChange={(event) => setStockForm((current) => ({ ...current, code: event.target.value.toUpperCase() }))}
                  placeholder="BBCA"
                  required
                />
              </label>
              <label>
                Name
                <input
                  value={stockForm.name}
                  onChange={(event) => setStockForm((current) => ({ ...current, name: event.target.value }))}
                  placeholder="Bank Central Asia"
                  required
                />
              </label>
              <label>
                Sector
                <input
                  value={stockForm.sector}
                  onChange={(event) => setStockForm((current) => ({ ...current, sector: event.target.value }))}
                  placeholder="Banking"
                />
              </label>
              <button type="submit" disabled={isPending}>
                Add stock
              </button>
            </form>
          </div>

          <div className="card">
            <div className="section-heading">
              <h2>Manual price</h2>
              <span className="muted">{prices.length} snapshot(s)</span>
            </div>
            <form onSubmit={handleUpsertPrice}>
              <label>
                Stock
                <select
                  value={priceForm.stockId}
                  onChange={(event) => setPriceForm((current) => ({ ...current, stockId: event.target.value }))}
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
              <label>
                Current price
                <input
                  value={priceForm.price}
                  onChange={(event) => setPriceForm((current) => ({ ...current, price: event.target.value }))}
                  placeholder="9150"
                  type="number"
                  min="0"
                  step="0.01"
                  required
                />
              </label>
              <button type="submit" disabled={isPending || !priceForm.stockId}>
                Save price
              </button>
            </form>
          </div>
        </section>

        <section className="grid two">
          <div className="card">
            <div className="section-heading">
              <h2>{editingTransactionId === null ? "New transaction" : "Edit transaction"}</h2>
              <span className="muted">{isLoading ? "Refreshing..." : `${transactions.length} recent transaction(s)`}</span>
            </div>
            <form onSubmit={handleSaveTransaction}>
              <label>
                Stock
                <select
                  value={transactionForm.stockId}
                  onChange={(event) => setTransactionForm((current) => ({ ...current, stockId: event.target.value }))}
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
              <label>
                Type
                <select
                  value={transactionForm.type}
                  onChange={(event) =>
                    setTransactionForm((current) => ({ ...current, type: event.target.value as TransactionType }))
                  }
                >
                  <option value="BUY">BUY</option>
                  <option value="SELL">SELL</option>
                </select>
              </label>
              <label>
                Transaction date
                <input
                  value={transactionForm.transactionDate}
                  onChange={(event) => setTransactionForm((current) => ({ ...current, transactionDate: event.target.value }))}
                  type="date"
                  required
                />
              </label>
              <div className="grid two">
                <label>
                  Quantity lot
                  <input
                    value={transactionForm.quantityLot}
                    onChange={(event) => setTransactionForm((current) => ({ ...current, quantityLot: event.target.value }))}
                    type="number"
                    min="1"
                    required
                  />
                </label>
                <label>
                  Price
                  <input
                    value={transactionForm.price}
                    onChange={(event) => setTransactionForm((current) => ({ ...current, price: event.target.value }))}
                    type="number"
                    min="0"
                    step="0.01"
                    required
                  />
                </label>
              </div>
              <label>
                Fee
                <input
                  value={transactionForm.fee}
                  onChange={(event) => setTransactionForm((current) => ({ ...current, fee: event.target.value }))}
                  type="number"
                  min="0"
                  step="0.01"
                  required
                />
              </label>
              <label>
                Notes
                <textarea
                  value={transactionForm.notes}
                  onChange={(event) => setTransactionForm((current) => ({ ...current, notes: event.target.value }))}
                  rows={3}
                  placeholder="Optional note"
                />
              </label>
              <div className="inline-actions">
                <button type="submit" disabled={isPending || !transactionForm.stockId}>
                  {editingTransactionId === null ? "Create transaction" : "Save changes"}
                </button>
                {editingTransactionId !== null ? (
                  <button type="button" className="secondary-button" onClick={resetTransactionForm}>
                    Cancel edit
                  </button>
                ) : null}
              </div>
            </form>
          </div>

          <div className="card">
            <h2>Current holdings</h2>
            <table className="table">
              <thead>
                <tr>
                  <th>Stock</th>
                  <th>Lot</th>
                  <th>Avg price</th>
                  <th>Cost basis</th>
                </tr>
              </thead>
              <tbody>
                {holdings.length ? (
                  holdings.map((item) => (
                    <tr key={item.stockId}>
                      <td>{item.stockCode}</td>
                      <td>{formatNumber(item.totalLot)}</td>
                      <td>{formatCurrency(item.averagePrice)}</td>
                      <td>{formatCurrency(item.totalCostBasis)}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={4} className="muted empty-cell">
                      No positions yet.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        <section className="card">
          <h2>Recent transactions</h2>
          <table className="table">
            <thead>
              <tr>
                <th>Stock</th>
                <th>Type</th>
                <th>Date</th>
                <th>Lot</th>
                <th>Price</th>
                <th>Fee</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {transactions.length ? (
                transactions.map((item) => (
                  <tr key={item.id}>
                    <td>{item.stockCode}</td>
                    <td>{item.type}</td>
                    <td>{item.transactionDate}</td>
                    <td>{formatNumber(item.quantityLot)}</td>
                    <td>{formatCurrency(item.price)}</td>
                    <td>{formatCurrency(item.fee)}</td>
                    <td>
                      <div className="inline-actions compact">
                        <button type="button" className="secondary-button" onClick={() => startEditingTransaction(item)}>
                          Edit
                        </button>
                        <button type="button" className="danger-button" onClick={() => handleDeleteTransaction(item.id)}>
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={7} className="muted empty-cell">
                    No transactions yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </section>

        <section className="card">
          <h2>Latest prices</h2>
          <table className="table">
            <thead>
              <tr>
                <th>Stock</th>
                <th>Price</th>
                <th>Updated</th>
              </tr>
            </thead>
            <tbody>
              {prices.length ? (
                prices.map((item) => (
                  <tr key={item.stockId}>
                    <td>{item.stockCode}</td>
                    <td>{formatCurrency(item.price)}</td>
                    <td>{formatDateTime(item.pricedAt)}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={3} className="muted empty-cell">
                    No price snapshots yet.
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
