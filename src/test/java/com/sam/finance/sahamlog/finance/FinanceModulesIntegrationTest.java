package com.sam.finance.sahamlog.finance;

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
class FinanceModulesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ThesisReviewRepository thesisReviewRepository;

    @Autowired
    private InvestmentThesisRepository investmentThesisRepository;

    @Autowired
    private WatchlistItemRepository watchlistItemRepository;

    @Autowired
    private DividendRepository dividendRepository;

    @Autowired
    private StockPriceSnapshotRepository stockPriceSnapshotRepository;

    @Autowired
    private TransactionEntryRepository transactionEntryRepository;

    @Autowired
    private PortfolioDailySnapshotRepository portfolioDailySnapshotRepository;

    @Autowired
    private DividendMonthlySnapshotRepository dividendMonthlySnapshotRepository;

    @Autowired
    private ThesisStatusSnapshotRepository thesisStatusSnapshotRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private AppUserRepository appUserRepository;

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
    void shouldSupportDividendWatchlistAndThesisFlows() throws Exception {
        String token = registerAndGetToken("modules@example.com");
        long stockId = createStock(token, "BBCA", "Bank Central Asia", "Banking");
        createTransaction(token, stockId, "BUY", "2026-01-01", 10, "100.00", "0.00");
        createPrice(token, stockId, "80.00");

        mockMvc.perform(post("/api/v1/dividends")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "stockId": %d,
                      "cumDate": "2026-03-20",
                      "paymentDate": "2026-04-10",
                      "dividendPerShare": 10.00,
                      "sharesOwned": 1000,
                      "taxRate": 10.00
                    }
                    """.formatted(stockId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.netReceived").value(9000.00));

        mockMvc.perform(get("/api/v1/dividends/summary")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .param("year", "2026"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalGrossDividend").value(10000.00))
            .andExpect(jsonPath("$.totalNetDividend").value(9000.00))
            .andExpect(jsonPath("$.byStock[0].yieldOnCostPercentage").value(9.00));

        mockMvc.perform(post("/api/v1/watchlist")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "stockId": %d,
                      "fairPrice": 100.00,
                      "cheapPrice": 90.00,
                      "veryCheapPrice": 70.00,
                      "expensivePrice": 120.00,
                      "notes": "value zone"
                    }
                    """.formatted(stockId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.valuationZone").value("CHEAP"));

        MvcResult thesisResult = mockMvc.perform(post("/api/v1/theses")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "stockId": %d,
                      "thesis": "Strong franchise",
                      "risks": "Margin compression",
                      "invalidationCondition": "Two bad quarters",
                      "holdingPeriod": "3-5 years",
                      "confidenceScore": 8,
                      "emotionTag": "calm"
                    }
                    """.formatted(stockId)))
            .andExpect(status().isCreated())
            .andReturn();

        Map<String, Object> thesis = objectMapper.readValue(thesisResult.getResponse().getContentAsString(), new TypeReference<>() {});
        long thesisId = ((Number) thesis.get("id")).longValue();

        mockMvc.perform(post("/api/v1/theses/" + thesisId + "/reviews")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reviewDate": "2026-05-01",
                      "stillValid": true,
                      "action": "HOLD",
                      "lesson": "Stay patient"
                    }
                    """))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/theses/summary")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTheses").value(1))
            .andExpect(jsonPath("$.activeTheses").value(1));

        mockMvc.perform(get("/api/v1/reports/dividends")
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .param("year", "2026"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalNetDividend").value(9000.00));

        mockMvc.perform(get("/api/v1/watchlist/export.csv")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
            .andExpect(status().isOk());
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

        Map<String, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
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

        Map<String, Object> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        return ((Number) response.get("id")).longValue();
    }

    private void createTransaction(String token, long stockId, String type, String date, int quantityLot, String price, String fee) throws Exception {
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
                      "notes": "seed"
                    }
                    """.formatted(stockId, type, date, quantityLot, price, fee)))
            .andExpect(status().isCreated());
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
