import { fetchJson } from "../lib/api";

export default async function DashboardPage() {
  const dashboard = await fetchJson("/reports/portfolio");
  const dividends = await fetchJson(`/reports/dividends?year=${new Date().getFullYear()}`);

  return (
    <div className="grid">
      <section className="hero">
        <div className="card">
          <p className="muted">Manual-first IDX portfolio companion</p>
          <h1>SahamLog</h1>
          <p className="muted">
            The frontend is scaffolded for the backend APIs in this repo. Use it as the starting point for the
            authenticated workflow after wiring JWT persistence.
          </p>
        </div>
      </section>

      <section className="grid two">
        <div className="card">
          <p className="muted">Total modal</p>
          <div className="stat">{dashboard?.totalModal ?? "-"}</div>
        </div>
        <div className="card">
          <p className="muted">Market value</p>
          <div className="stat">{dashboard?.totalMarketValue ?? "-"}</div>
        </div>
        <div className="card">
          <p className="muted">Unrealized P&L</p>
          <div className="stat">{dashboard?.totalUnrealizedGainLoss ?? "-"}</div>
        </div>
        <div className="card">
          <p className="muted">Dividend this year</p>
          <div className="stat">{dividends?.totalNetDividend ?? "-"}</div>
        </div>
      </section>
    </div>
  );
}
