export const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1";

export type TransactionType = "BUY" | "SELL";

export interface UserSummary {
  id: number;
  email: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserSummary;
}

export interface AuthMeResponse {
  id: number;
  email: string;
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface StockResponse {
  id: number;
  code: string;
  name: string;
  sector: string | null;
}

export interface TransactionResponse {
  id: number;
  stockId: number;
  stockCode: string;
  stockName: string;
  type: TransactionType;
  transactionDate: string;
  quantityLot: number;
  quantityShare: number;
  price: number;
  fee: number;
  notes: string | null;
}

export interface HoldingResponse {
  stockId: number;
  stockCode: string;
  stockName: string;
  totalLot: number;
  totalShares: number;
  averagePrice: number;
  totalCostBasis: number;
}

export interface PriceResponse {
  stockId: number;
  stockCode: string;
  stockName: string;
  price: number;
  pricedAt: string;
}

export interface DashboardHoldingResponse {
  stockId: number;
  stockCode: string;
  stockName: string;
  totalLot: number;
  totalShares: number;
  averagePrice: number;
  totalCostBasis: number;
  currentPrice: number | null;
  marketValue: number;
  unrealizedGainLoss: number;
  unrealizedGainLossPercentage: number;
  allocationPercentage: number;
}

export interface DashboardSummaryResponse {
  totalModal: number;
  totalMarketValue: number;
  totalUnrealizedGainLoss: number;
  totalUnrealizedGainLossPercentage: number;
  holdings: DashboardHoldingResponse[];
}

export interface DividendResponse {
  id: number;
  stockId: number;
  stockCode: string;
  stockName: string;
  cumDate: string | null;
  paymentDate: string;
  dividendPerShare: number;
  sharesOwned: number;
  taxRate: number;
  grossDividend: number;
  totalTax: number;
  netReceived: number;
}

export interface DividendStockSummaryResponse {
  stockId: number;
  stockCode: string;
  stockName: string;
  totalGrossDividend: number;
  totalTax: number;
  totalNetDividend: number;
  yieldOnCostPercentage: number;
}

export interface DividendMonthSummaryResponse {
  month: number;
  totalGrossDividend: number;
  totalTax: number;
  totalNetDividend: number;
}

export interface DividendSummaryResponse {
  year: number;
  totalGrossDividend: number;
  totalTax: number;
  totalNetDividend: number;
  byStock: DividendStockSummaryResponse[];
  byMonth: DividendMonthSummaryResponse[];
}

export interface WatchlistResponse {
  id: number;
  stockId: number;
  stockCode: string;
  stockName: string;
  fairPrice: number;
  cheapPrice: number;
  veryCheapPrice: number;
  expensivePrice: number;
  notes: string | null;
  currentPrice: number | null;
  valuationZone: string;
  premiumDiscountPercentage: number;
  marginOfSafetyPercentage: number;
}

export interface ThesisResponse {
  id: number;
  stockId: number;
  stockCode: string;
  stockName: string;
  thesis: string;
  risks: string | null;
  invalidationCondition: string | null;
  holdingPeriod: string | null;
  confidenceScore: number | null;
  emotionTag: string | null;
}

export interface ThesisReviewResponse {
  id: number;
  reviewDate: string;
  stillValid: boolean;
  action: string;
  lesson: string;
}

export interface ThesisSummaryResponse {
  totalTheses: number;
  activeTheses: number;
  invalidatedTheses: number;
  reviewsLast30Days: number;
}

export class ApiError extends Error {
  status: number;
  details: string[];

  constructor(message: string, status: number, details: string[] = []) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.details = details;
  }
}

interface RequestOptions {
  method?: string;
  token?: string | null;
  body?: unknown;
}

interface ErrorPayload {
  message?: string;
  details?: string[];
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers();
  headers.set("Accept", "application/json");

  if (options.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }
  if (options.token) {
    headers.set("Authorization", `Bearer ${options.token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method ?? "GET",
    headers,
    cache: "no-store",
    body: options.body === undefined ? undefined : JSON.stringify(options.body)
  });

  if (response.status === 204) {
    return null as T;
  }

  const contentType = response.headers.get("content-type") ?? "";
  const payload = contentType.includes("application/json")
    ? ((await response.json()) as T | ErrorPayload)
    : null;

  if (!response.ok) {
    const errorPayload = (payload ?? {}) as ErrorPayload;
    throw new ApiError(
      errorPayload.message ?? `Request failed with status ${response.status}`,
      response.status,
      errorPayload.details ?? []
    );
  }

  return payload as T;
}

export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError;
}

export function isUnauthorizedError(error: unknown): boolean {
  return isApiError(error) && error.status === 401;
}

export function getErrorMessage(error: unknown): string {
  if (isApiError(error)) {
    return error.details.length > 0
      ? `${error.message}: ${error.details.join(", ")}`
      : error.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return "Unexpected error";
}
