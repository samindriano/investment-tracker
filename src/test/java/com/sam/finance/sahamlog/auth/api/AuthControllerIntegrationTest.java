package com.sam.finance.sahamlog.auth.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class AuthControllerIntegrationTest {

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
    void shouldRegisterAndLogin() throws Exception {
        String payload = """
            {
              "email": "sam@example.com",
              "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.user.email").value("sam@example.com"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.user.email").value("sam@example.com"));
    }

    @Test
    void shouldReturnCurrentUserProfile() throws Exception {
        String payload = """
            {
              "email": "me@example.com",
              "password": "password123"
            }
            """;

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andReturn();

        String token = (String) objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<java.util.Map<String, Object>>() {})
            .get("accessToken");

        mockMvc.perform(get("/api/v1/auth/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("me@example.com"));
    }

    @Test
    void shouldRejectDuplicateRegistration() throws Exception {
        String payload = """
            {
              "email": "sam@example.com",
              "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Email is already registered"));
    }
}
