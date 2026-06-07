package com.sam.finance.sahamlog.portfolio.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sam.finance.sahamlog.auth.repository.AppUserRepository;
import com.sam.finance.sahamlog.dividend.repository.DividendRepository;
import com.sam.finance.sahamlog.journal.repository.InvestmentThesisRepository;
import com.sam.finance.sahamlog.journal.repository.ThesisReviewRepository;
import com.sam.finance.sahamlog.portfolio.repository.StockPriceSnapshotRepository;
import com.sam.finance.sahamlog.portfolio.repository.StockRepository;
import com.sam.finance.sahamlog.portfolio.repository.TransactionEntryRepository;
import com.sam.finance.sahamlog.reporting.repository.DividendMonthlySnapshotRepository;
import com.sam.finance.sahamlog.reporting.repository.PortfolioDailySnapshotRepository;
import com.sam.finance.sahamlog.reporting.repository.ThesisStatusSnapshotRepository;
import com.sam.finance.sahamlog.watchlist.repository.WatchlistItemRepository;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class PortfolioApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StockPriceSnapshotRepository stockPriceSnapshotRepository;

    @Autowired
    private TransactionEntryRepository transactionEntryRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private DividendRepository dividendRepository;

    @Autowired
    private WatchlistItemRepository watchlistItemRepository;

    @Autowired
    private InvestmentThesisRepository investmentThesisRepository;

    @Autowired
    private ThesisReviewRepository thesisReviewRepository;

    @Autowired
    private PortfolioDailySnapshotRepository portfolioDailySnapshotRepository;

    @Autowired
    private DividendMonthlySnapshotRepository dividendMonthlySnapshotRepository;

    @Autowired
    private ThesisStatusSnapshotRepository thesisStatusSnapshotRepository;

    @BeforeEach
    void cleanDatabase() {
        thesisReviewRepository.deleteAll();
        investmentThesisRepository.deleteAll();
        watchlistItemRepository.deleteAll();
        dividendRepository.deleteAll();
        stockPriceSnapshotRepository.deleteAll();
        transactionEntryRepository.deleteAll();
        portfolioDailySnapshotRepository.deleteAll();
        dividendMonthlySnapshotRepository.deleteAll();
        thesisStatusSnapshotRepository.deleteAll();
        stockRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void shouldRequireAuthenticationForPortfolioEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/stocks"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateStockAndRejectDuplicateCode() throws Exception {
        String token = registerAndGetToken("stocker@example.com");

        mockMvc.perform(post("/api/v1/stocks")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "bbca",
                      "name": "Bank Central Asia",
                      "sector": "Banking"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("BBCA"));

        mockMvc.perform(post("/api/v1/stocks")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "BBCA",
                      "name": "Duplicate",
                      "sector": "Banking"
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Stock code is already registered"));
    }

    @Test
    void shouldHandlePortfolioFlowAndDashboardSummary() throws Exception {
        String token = registerAndGetToken("flow@example.com");
        long stockId = createStock(token, "BBCA", "Bank Central Asia", "Banking");

        createTransaction(token, stockId, "BUY", "2026-01-01", 10, "100.00", "0.00");
        createTransaction(token, stockId, "BUY", "2026-01-02", 10, "200.00", "10.00");
        createPrice(token, stockId, "250.00");

        mockMvc.perform(get("/api/v1/portfolio/holdings")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].stockCode").value("BBCA"))
            .andExpect(jsonPath("$[0].totalLot").value(20))
            .andExpect(jsonPath("$[0].totalShares").value(2000))
            .andExpect(jsonPath("$[0].averagePrice").value(150.01))
            .andExpect(jsonPath("$[0].totalCostBasis").value(300010.00));

        mockMvc.perform(get("/api/v1/dashboard/summary")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalModal").value(300010.00))
            .andExpect(jsonPath("$.totalMarketValue").value(500000.00))
            .andExpect(jsonPath("$.totalUnrealizedGainLoss").value(199990.00))
            .andExpect(jsonPath("$.holdings[0].allocationPercentage").value(100.00))
            .andExpect(jsonPath("$.holdings[0].currentPrice").value(250.00));
    }

    @Test
    void shouldRejectSellThatExceedsHoldings() throws Exception {
        String token = registerAndGetToken("sell@example.com");
        long stockId = createStock(token, "BBRI", "Bank Rakyat Indonesia", "Banking");
        createTransaction(token, stockId, "BUY", "2026-01-01", 1, "100.00", "0.00");

        mockMvc.perform(post("/api/v1/transactions")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "stockId": %d,
                      "type": "SELL",
                      "transactionDate": "2026-01-02",
                      "quantityLot": 2,
                      "price": 100.00,
                      "fee": 0.00,
                      "notes": "too much"
                    }
                    """.formatted(stockId)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Sell quantity exceeds current holdings"));
    }

    @Test
    void shouldResetAveragePriceAfterFullExitAndReentry() throws Exception {
        String token = registerAndGetToken("reset@example.com");
        long stockId = createStock(token, "BMRI", "Bank Mandiri", "Banking");

        createTransaction(token, stockId, "BUY", "2026-01-01", 10, "100.00", "0.00");
        createTransaction(token, stockId, "SELL", "2026-01-02", 10, "120.00", "0.00");
        createTransaction(token, stockId, "BUY", "2026-01-03", 5, "300.00", "0.00");

        mockMvc.perform(get("/api/v1/portfolio/holdings")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].totalLot").value(5))
            .andExpect(jsonPath("$[0].averagePrice").value(300.00))
            .andExpect(jsonPath("$[0].totalCostBasis").value(150000.00));
    }

    @Test
    void shouldFilterTransactionsAndIsolateUsers() throws Exception {
        String tokenOne = registerAndGetToken("user1@example.com");
        String tokenTwo = registerAndGetToken("user2@example.com");
        long bbcaId = createStock(tokenOne, "BBCA", "Bank Central Asia", "Banking");
        long bbriId = createStock(tokenOne, "BBRI", "Bank Rakyat Indonesia", "Banking");

        createTransaction(tokenOne, bbcaId, "BUY", "2026-01-01", 2, "100.00", "0.00");
        createTransaction(tokenOne, bbriId, "BUY", "2026-01-02", 3, "200.00", "0.00");
        createTransaction(tokenTwo, bbcaId, "BUY", "2026-01-03", 7, "300.00", "0.00");
        createPrice(tokenOne, bbcaId, "150.00");
        createPrice(tokenTwo, bbcaId, "999.00");

        mockMvc.perform(get("/api/v1/transactions")
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenOne))
                .param("stockCode", "BBCA")
                .param("type", "BUY")
                .param("dateFrom", "2026-01-01")
                .param("dateTo", "2026-01-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].stockCode").value("BBCA"))
            .andExpect(jsonPath("$.items[0].quantityLot").value(2));

        mockMvc.perform(get("/api/v1/prices/" + bbcaId)
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenOne)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.price").value(150.00));

        mockMvc.perform(get("/api/v1/dashboard/summary")
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenTwo)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.holdings.length()").value(1))
            .andExpect(jsonPath("$.holdings[0].totalLot").value(7))
            .andExpect(jsonPath("$.holdings[0].currentPrice").value(999.00));
    }

    @Test
    void shouldOverwriteManualPrice() throws Exception {
        String token = registerAndGetToken("price@example.com");
        long stockId = createStock(token, "ASII", "Astra", "Industrial");

        createPrice(token, stockId, "100.00");
        createPrice(token, stockId, "123.45");

        mockMvc.perform(get("/api/v1/prices/" + stockId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.price").value(123.45));
    }

    @Test
    void shouldUpdateAndDeleteTransaction() throws Exception {
        String token = registerAndGetToken("mutate@example.com");
        long stockId = createStock(token, "ADRO", "Adaro", "Mining");

        long txId = createTransactionReturningId(token, stockId, "BUY", "2026-01-01", 2, "100.00", "0.00");

        mockMvc.perform(put("/api/v1/transactions/" + txId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "stockId": %d,
                      "type": "BUY",
                      "transactionDate": "2026-01-01",
                      "quantityLot": 3,
                      "price": 150.00,
                      "fee": 0.00,
                      "notes": "edited"
                    }
                    """.formatted(stockId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantityLot").value(3));

        mockMvc.perform(get("/api/v1/portfolio/holdings")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].totalLot").value(3))
            .andExpect(jsonPath("$[0].averagePrice").value(150.00));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/transactions/" + txId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/portfolio/holdings")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldRejectStockDeletionWhenReferenced() throws Exception {
        String token = registerAndGetToken("stock-delete@example.com");
        long stockId = createStock(token, "PGAS", "Perusahaan Gas Negara", "Energy");
        createTransaction(token, stockId, "BUY", "2026-01-01", 1, "100.00", "0.00");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/stocks/" + stockId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Stock cannot be deleted because it is already referenced by portfolio data"));
    }

    private String registerAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "%s",
                      "password": "password123"
                    }
                    """.formatted(email)))
            .andExpect(status().isCreated())
            .andReturn();

        Map<String, Object> response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<>() {});

        return (String) response.get("accessToken");
    }

    private long createStock(String token, String code, String name, String sector) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/stocks")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "%s",
                      "name": "%s",
                      "sector": "%s"
                    }
                    """.formatted(code, name, sector)))
            .andExpect(status().isCreated())
            .andReturn();

        Map<String, Object> response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<>() {});

        return ((Number) response.get("id")).longValue();
    }

    private void createTransaction(
        String token,
        long stockId,
        String type,
        String date,
        int quantityLot,
        String price,
        String fee) throws Exception {

        mockMvc.perform(post("/api/v1/transactions")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "stockId": %d,
                      "type": "%s",
                      "transactionDate": "%s",
                      "quantityLot": %d,
                      "price": %s,
                      "fee": %s,
                      "notes": "test"
                    }
                    """.formatted(stockId, type, date, quantityLot, price, fee)))
            .andExpect(status().isCreated());
    }

    private long createTransactionReturningId(
        String token,
        long stockId,
        String type,
        String date,
        int quantityLot,
        String price,
        String fee) throws Exception {

        MvcResult result = mockMvc.perform(post("/api/v1/transactions")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "stockId": %d,
                      "type": "%s",
                      "transactionDate": "%s",
                      "quantityLot": %d,
                      "price": %s,
                      "fee": %s,
                      "notes": "test"
                    }
                    """.formatted(stockId, type, date, quantityLot, price, fee)))
            .andExpect(status().isCreated())
            .andReturn();

        Map<String, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        return ((Number) response.get("id")).longValue();
    }

    private void createPrice(String token, long stockId, String price) throws Exception {
        mockMvc.perform(put("/api/v1/prices/" + stockId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "price": %s
                    }
                    """.formatted(price)))
            .andExpect(status().isOk());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
