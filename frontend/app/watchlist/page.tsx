import { fetchJson } from "../../lib/api";

export default async function WatchlistPage() {
  const watchlist = await fetchJson("/watchlist");

  return (
    <div className="card">
      <h2>Watchlist</h2>
      <table className="table">
        <thead>
          <tr>
            <th>Stock</th>
            <th>Current</th>
            <th>Zone</th>
            <th>MoS</th>
          </tr>
        </thead>
        <tbody>
          {watchlist?.map((item: any) => (
            <tr key={item.id}>
              <td>{item.stockCode}</td>
              <td>{item.currentPrice ?? "-"}</td>
              <td>
                <span className="pill">{item.valuationZone}</span>
              </td>
              <td>{item.marginOfSafetyPercentage}%</td>
            </tr>
          )) ?? null}
        </tbody>
      </table>
    </div>
  );
}
