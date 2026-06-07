import { fetchJson } from "../../lib/api";

export default async function DividendsPage() {
  const year = new Date().getFullYear();
  const summary = await fetchJson(`/dividends/summary?year=${year}`);

  return (
    <div className="grid two">
      <div className="card">
        <h2>Dividend Summary {year}</h2>
        <p>Total gross: {summary?.totalGrossDividend ?? "-"}</p>
        <p>Total tax: {summary?.totalTax ?? "-"}</p>
        <p>Total net: {summary?.totalNetDividend ?? "-"}</p>
      </div>
      <div className="card">
        <h2>Per Stock</h2>
        <table className="table">
          <thead>
            <tr>
              <th>Stock</th>
              <th>Net</th>
              <th>Yield on Cost</th>
            </tr>
          </thead>
          <tbody>
            {summary?.byStock?.map((item: any) => (
              <tr key={item.stockId}>
                <td>{item.stockCode}</td>
                <td>{item.totalNetDividend}</td>
                <td>{item.yieldOnCostPercentage}%</td>
              </tr>
            )) ?? null}
          </tbody>
        </table>
      </div>
    </div>
  );
}
