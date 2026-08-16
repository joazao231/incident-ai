package dev.incidentai.backend.controller;

import dev.incidentai.backend.repository.UserAccountRepository;
import dev.incidentai.backend.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    TokenService tokenService;

    @Autowired
    UserAccountRepository users;

    @Test
    void validatesAndCreatesApplication() throws Exception {
        mvc.perform(post("/api/applications")
                        .header(HttpHeaders.AUTHORIZATION, bearerAdminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Portfolio API\",\"url\":\"https://example.com/health\",\"environment\":\"PRODUCTION\",\"monitoringEnabled\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Portfolio API"))
                .andExpect(jsonPath("$.status").value("UNKNOWN"));
    }

    @Test
    void rejectsInvalidUrl() throws Exception {
        mvc.perform(post("/api/applications")
                        .header(HttpHeaders.AUTHORIZATION, bearerAdminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"API\",\"url\":\"invalid\",\"environment\":\"PRODUCTION\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.url").exists());
    }

    @Test
    void blocksAnonymousApiAccess() throws Exception {
        mvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized());
    }

    private String bearerAdminToken() {
        var admin = users.findByUsernameIgnoreCase("admin").orElseThrow();
        return "Bearer " + tokenService.generate(admin);
    }
}
