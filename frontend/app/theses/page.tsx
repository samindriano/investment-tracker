import { fetchJson } from "../../lib/api";

export default async function ThesesPage() {
  const theses = await fetchJson("/theses");
  const summary = await fetchJson("/theses/summary");

  return (
    <div className="grid two">
      <div className="card">
        <h2>Thesis Summary</h2>
        <p>Total theses: {summary?.totalTheses ?? "-"}</p>
        <p>Active theses: {summary?.activeTheses ?? "-"}</p>
        <p>Invalidated theses: {summary?.invalidatedTheses ?? "-"}</p>
        <p>Reviews last 30 days: {summary?.reviewsLast30Days ?? "-"}</p>
      </div>
      <div className="card">
        <h2>Thesis List</h2>
        <table className="table">
          <thead>
            <tr>
              <th>Stock</th>
              <th>Confidence</th>
              <th>Holding Period</th>
            </tr>
          </thead>
          <tbody>
            {theses?.map((item: any) => (
              <tr key={item.id}>
                <td>{item.stockCode}</td>
                <td>{item.confidenceScore ?? "-"}</td>
                <td>{item.holdingPeriod ?? "-"}</td>
              </tr>
            )) ?? null}
          </tbody>
        </table>
      </div>
    </div>
  );
}
