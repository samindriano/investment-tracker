"use client";

import { FormEvent, useEffect, useState, useTransition } from "react";

import { AuthGate } from "../../components/auth-gate";
import { useAuth } from "../../components/auth-provider";
import {
  apiRequest,
  getErrorMessage,
  isUnauthorizedError,
  PageResponse,
  StockResponse,
  ThesisResponse,
  ThesisReviewResponse,
  ThesisSummaryResponse
} from "../../lib/api";

interface ThesisFormState {
  stockId: string;
  thesis: string;
  risks: string;
  invalidationCondition: string;
  holdingPeriod: string;
  confidenceScore: string;
  emotionTag: string;
}

interface ReviewFormState {
  thesisId: string;
  reviewDate: string;
  stillValid: "true" | "false";
  action: string;
  lesson: string;
}

const emptyThesisForm: ThesisFormState = {
  stockId: "",
  thesis: "",
  risks: "",
  invalidationCondition: "",
  holdingPeriod: "",
  confidenceScore: "",
  emotionTag: ""
};

const emptyReviewForm: ReviewFormState = {
  thesisId: "",
  reviewDate: new Date().toISOString().slice(0, 10),
  stillValid: "true",
  action: "HOLD",
  lesson: ""
};

export default function ThesesPage() {
  const { session, logout } = useAuth();
  const [stocks, setStocks] = useState<StockResponse[]>([]);
  const [theses, setTheses] = useState<ThesisResponse[]>([]);
  const [summary, setSummary] = useState<ThesisSummaryResponse | null>(null);
  const [reviews, setReviews] = useState<ThesisReviewResponse[]>([]);
  const [thesisForm, setThesisForm] = useState<ThesisFormState>(emptyThesisForm);
  const [reviewForm, setReviewForm] = useState<ReviewFormState>(emptyReviewForm);
  const [editingThesisId, setEditingThesisId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [isPending, startTransition] = useTransition();

  useEffect(() => {
    if (!session?.accessToken) {
      return;
    }

    void loadData();
  }, [session?.accessToken]);

  useEffect(() => {
    if (!session?.accessToken || !reviewForm.thesisId) {
      setReviews([]);
      return;
    }

    void loadReviews(reviewForm.thesisId);
  }, [reviewForm.thesisId, session?.accessToken]);

  async function loadData() {
    const accessToken = session?.accessToken;
    if (!accessToken) {
      return;
    }

    try {
      setError(null);
      const [stocksResponse, thesesResponse, summaryResponse] = await Promise.all([
        apiRequest<StockResponse[]>("/stocks", { token: accessToken }),
        apiRequest<ThesisResponse[]>("/theses", { token: accessToken }),
        apiRequest<ThesisSummaryResponse>("/theses/summary", { token: accessToken })
      ]);

      setStocks(stocksResponse);
      setTheses(thesesResponse);
      setSummary(summaryResponse);

      const selectedThesisId = thesesResponse[0] ? String(thesesResponse[0].id) : "";
      setThesisForm((current) => ({
        ...current,
        stockId: current.stockId || (stocksResponse[0] ? String(stocksResponse[0].id) : "")
      }));
      setReviewForm((current) => ({
        ...current,
        thesisId: thesesResponse.some((item) => String(item.id) === current.thesisId) ? current.thesisId : selectedThesisId
      }));
    } catch (requestError) {
      if (isUnauthorizedError(requestError)) {
        logout();
      }
      setError(getErrorMessage(requestError));
    }
  }

  async function loadReviews(thesisId: string) {
    const accessToken = session?.accessToken;
    if (!accessToken) {
      return;
    }

    try {
      const response = await apiRequest<PageResponse<ThesisReviewResponse>>(
        `/theses/${thesisId}/reviews?page=0&size=20`,
        { token: accessToken }
      );
      setReviews(response.items);
    } catch (requestError) {
      if (isUnauthorizedError(requestError)) {
        logout();
      }
      setError(getErrorMessage(requestError));
    }
  }

  function handleSaveThesis(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const accessToken = session?.accessToken;
    if (!accessToken) {
      return;
    }

    const path = editingThesisId === null ? "/theses" : `/theses/${editingThesisId}`;
    const method = editingThesisId === null ? "POST" : "PUT";

    startTransition(() => {
      void apiRequest<ThesisResponse>(path, {
        method,
        token: accessToken,
        body: {
          stockId: Number(thesisForm.stockId),
          thesis: thesisForm.thesis,
          risks: thesisForm.risks || null,
          invalidationCondition: thesisForm.invalidationCondition || null,
          holdingPeriod: thesisForm.holdingPeriod || null,
          confidenceScore: thesisForm.confidenceScore ? Number(thesisForm.confidenceScore) : null,
          emotionTag: thesisForm.emotionTag || null
        }
      })
        .then(async () => {
          setStatusMessage(editingThesisId === null ? "Thesis saved." : "Thesis updated.");
          resetThesisForm();
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

  function handleDeleteThesis(thesisId: number) {
    const accessToken = session?.accessToken;
    if (!accessToken || !window.confirm("Delete this thesis?")) {
      return;
    }

    startTransition(() => {
      void apiRequest<null>(`/theses/${thesisId}`, {
        method: "DELETE",
        token: accessToken
      })
        .then(async () => {
          setStatusMessage("Thesis deleted.");
          if (editingThesisId === thesisId) {
            resetThesisForm();
          }
          if (reviewForm.thesisId === String(thesisId)) {
            setReviewForm((current) => ({ ...current, thesisId: "" }));
            setReviews([]);
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

  function handleCreateReview(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const accessToken = session?.accessToken;
    if (!accessToken || !reviewForm.thesisId) {
      return;
    }

    startTransition(() => {
      void apiRequest<ThesisReviewResponse>(`/theses/${reviewForm.thesisId}/reviews`, {
        method: "POST",
        token: accessToken,
        body: {
          reviewDate: reviewForm.reviewDate,
          stillValid: reviewForm.stillValid === "true",
          action: reviewForm.action,
          lesson: reviewForm.lesson
        }
      })
        .then(async () => {
          setStatusMessage("Review added.");
          setReviewForm((current) => ({
            ...emptyReviewForm,
            thesisId: current.thesisId,
            reviewDate: new Date().toISOString().slice(0, 10)
          }));
          await loadReviews(reviewForm.thesisId);
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

  function handleDeleteReview(reviewId: number) {
    const accessToken = session?.accessToken;
    if (!accessToken || !reviewForm.thesisId || !window.confirm("Delete this review?")) {
      return;
    }

    startTransition(() => {
      void apiRequest<null>(`/theses/${reviewForm.thesisId}/reviews/${reviewId}`, {
        method: "DELETE",
        token: accessToken
      })
        .then(async () => {
          setStatusMessage("Review deleted.");
          await loadReviews(reviewForm.thesisId);
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

  function startEditingThesis(thesis: ThesisResponse) {
    setEditingThesisId(thesis.id);
    setThesisForm({
      stockId: String(thesis.stockId),
      thesis: thesis.thesis,
      risks: thesis.risks ?? "",
      invalidationCondition: thesis.invalidationCondition ?? "",
      holdingPeriod: thesis.holdingPeriod ?? "",
      confidenceScore: thesis.confidenceScore === null ? "" : String(thesis.confidenceScore),
      emotionTag: thesis.emotionTag ?? ""
    });
  }

  function resetThesisForm() {
    setEditingThesisId(null);
    setThesisForm((current) => ({
      ...emptyThesisForm,
      stockId: current.stockId || (stocks[0] ? String(stocks[0].id) : "")
    }));
  }

  return (
    <AuthGate description="Record thesis dan review keputusan investasi langsung dari browser.">
      <div className="grid">
        {statusMessage ? <p className="status success">{statusMessage}</p> : null}
        {error ? <p className="status error">{error}</p> : null}

        <section className="grid stats-grid">
          <div className="card">
            <p className="muted">Total theses</p>
            <div className="stat">{summary?.totalTheses ?? "-"}</div>
          </div>
          <div className="card">
            <p className="muted">Active theses</p>
            <div className="stat">{summary?.activeTheses ?? "-"}</div>
          </div>
          <div className="card">
            <p className="muted">Invalidated</p>
            <div className="stat">{summary?.invalidatedTheses ?? "-"}</div>
          </div>
          <div className="card">
            <p className="muted">Reviews last 30 days</p>
            <div className="stat">{summary?.reviewsLast30Days ?? "-"}</div>
          </div>
        </section>

        <section className="grid two">
          <div className="card">
            <h2>{editingThesisId === null ? "Create thesis" : "Edit thesis"}</h2>
            <form onSubmit={handleSaveThesis}>
              <label>
                Stock
                <select
                  value={thesisForm.stockId}
                  onChange={(event) => setThesisForm((current) => ({ ...current, stockId: event.target.value }))}
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
                Thesis
                <textarea
                  value={thesisForm.thesis}
                  onChange={(event) => setThesisForm((current) => ({ ...current, thesis: event.target.value }))}
                  rows={4}
                  required
                />
              </label>
              <label>
                Risks
                <textarea
                  value={thesisForm.risks}
                  onChange={(event) => setThesisForm((current) => ({ ...current, risks: event.target.value }))}
                  rows={3}
                />
              </label>
              <label>
                Invalidation condition
                <textarea
                  value={thesisForm.invalidationCondition}
                  onChange={(event) =>
                    setThesisForm((current) => ({ ...current, invalidationCondition: event.target.value }))
                  }
                  rows={3}
                />
              </label>
              <div className="grid two">
                <label>
                  Holding period
                  <input
                    value={thesisForm.holdingPeriod}
                    onChange={(event) => setThesisForm((current) => ({ ...current, holdingPeriod: event.target.value }))}
                    placeholder="3-5 years"
                  />
                </label>
                <label>
                  Confidence score
                  <input
                    value={thesisForm.confidenceScore}
                    onChange={(event) =>
                      setThesisForm((current) => ({ ...current, confidenceScore: event.target.value }))
                    }
                    type="number"
                    min="0"
                    max="10"
                  />
                </label>
              </div>
              <label>
                Emotion tag
                <input
                  value={thesisForm.emotionTag}
                  onChange={(event) => setThesisForm((current) => ({ ...current, emotionTag: event.target.value }))}
                  placeholder="calm"
                />
              </label>
              <div className="inline-actions">
                <button type="submit" disabled={isPending || !thesisForm.stockId}>
                  {editingThesisId === null ? "Save thesis" : "Update thesis"}
                </button>
                {editingThesisId !== null ? (
                  <button type="button" className="secondary-button" onClick={resetThesisForm}>
                    Cancel edit
                  </button>
                ) : null}
              </div>
            </form>
          </div>

          <div className="card">
            <h2>Add review</h2>
            <form onSubmit={handleCreateReview}>
              <label>
                Thesis
                <select
                  value={reviewForm.thesisId}
                  onChange={(event) => setReviewForm((current) => ({ ...current, thesisId: event.target.value }))}
                  required
                >
                  <option value="">Select thesis</option>
                  {theses.map((thesis) => (
                    <option key={thesis.id} value={thesis.id}>
                      {thesis.stockCode} - {thesis.holdingPeriod ?? "No period"}
                    </option>
                  ))}
                </select>
              </label>
              <div className="grid two">
                <label>
                  Review date
                  <input
                    value={reviewForm.reviewDate}
                    onChange={(event) => setReviewForm((current) => ({ ...current, reviewDate: event.target.value }))}
                    type="date"
                    required
                  />
                </label>
                <label>
                  Still valid
                  <select
                    value={reviewForm.stillValid}
                    onChange={(event) =>
                      setReviewForm((current) => ({ ...current, stillValid: event.target.value as "true" | "false" }))
                    }
                  >
                    <option value="true">Yes</option>
                    <option value="false">No</option>
                  </select>
                </label>
              </div>
              <label>
                Action
                <input
                  value={reviewForm.action}
                  onChange={(event) => setReviewForm((current) => ({ ...current, action: event.target.value }))}
                  placeholder="HOLD"
                  required
                />
              </label>
              <label>
                Lesson
                <textarea
                  value={reviewForm.lesson}
                  onChange={(event) => setReviewForm((current) => ({ ...current, lesson: event.target.value }))}
                  rows={4}
                  required
                />
              </label>
              <button type="submit" disabled={isPending || !reviewForm.thesisId}>
                Save review
              </button>
            </form>
          </div>
        </section>

        <section className="card">
          <h2>Thesis list</h2>
          <table className="table">
            <thead>
              <tr>
                <th>Stock</th>
                <th>Confidence</th>
                <th>Holding period</th>
                <th>Emotion</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {theses.length ? (
                theses.map((thesis) => (
                  <tr key={thesis.id}>
                    <td>
                      <strong>{thesis.stockCode}</strong>
                      <div className="muted small-text">{thesis.thesis}</div>
                    </td>
                    <td>{thesis.confidenceScore ?? "-"}</td>
                    <td>{thesis.holdingPeriod ?? "-"}</td>
                    <td>{thesis.emotionTag ?? "-"}</td>
                    <td>
                      <div className="inline-actions compact">
                        <button type="button" className="secondary-button" onClick={() => startEditingThesis(thesis)}>
                          Edit
                        </button>
                        <button type="button" className="danger-button" onClick={() => handleDeleteThesis(thesis.id)}>
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={5} className="muted empty-cell">
                    No theses recorded yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </section>

        <section className="card">
          <h2>Selected thesis reviews</h2>
          <table className="table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Still valid</th>
                <th>Action</th>
                <th>Lesson</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {reviews.length ? (
                reviews.map((review) => (
                  <tr key={review.id}>
                    <td>{review.reviewDate}</td>
                    <td>{review.stillValid ? "Yes" : "No"}</td>
                    <td>{review.action}</td>
                    <td>{review.lesson}</td>
                    <td>
                      <button type="button" className="danger-button" onClick={() => handleDeleteReview(review.id)}>
                        Delete
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={5} className="muted empty-cell">
                    No reviews for the selected thesis yet.
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
