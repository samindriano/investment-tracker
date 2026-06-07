import { fetchJson } from "../../lib/api";

export default async function TransactionsPage() {
  const transactions = await fetchJson("/transactions?page=0&size=20");
  const holdings = await fetchJson("/portfolio/holdings");

  return (
    <div className="grid two">
      <div className="card">
        <h2>Recent Transactions</h2>
        <table className="table">
          <thead>
            <tr>
              <th>Stock</th>
              <th>Type</th>
              <th>Date</th>
              <th>Lot</th>
            </tr>
          </thead>
          <tbody>
            {transactions?.items?.map((item: any) => (
              <tr key={item.id}>
                <td>{item.stockCode}</td>
                <td>{item.type}</td>
                <td>{item.transactionDate}</td>
                <td>{item.quantityLot}</td>
              </tr>
            )) ?? null}
          </tbody>
        </table>
      </div>
      <div className="card">
        <h2>Holdings</h2>
        <table className="table">
          <thead>
            <tr>
              <th>Stock</th>
              <th>Lot</th>
              <th>Avg Price</th>
            </tr>
          </thead>
          <tbody>
            {holdings?.map((item: any) => (
              <tr key={item.stockId}>
                <td>{item.stockCode}</td>
                <td>{item.totalLot}</td>
                <td>{item.averagePrice}</td>
              </tr>
            )) ?? null}
          </tbody>
        </table>
      </div>
    </div>
  );
}
