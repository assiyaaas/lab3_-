package com.example.bankcase;

import com.example.bankcase.config.BankCaseManagementApplication;
import com.example.bankcase.domain.CaseNumber;
import com.example.bankcase.persistence.CaseJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full context on PostgreSQL: controller -> service -> JPA -> table.
 * No @Transactional: the 409 must leave the committed first row in place.
 */
@SpringBootTest(classes = BankCaseManagementApplication.class)
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CaseJpa cases;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void empty() {
        jdbc.update("DELETE FROM bank_case");
    }

    @Test
    void createThenReadThenDuplicateKeepsOneRow() throws Exception {
        String location = mvc.perform(post("/api/v1/bank-cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessKey":"CASE-001","title":"Card blocked"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("New"))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessKey").value("CASE-001"))
                .andExpect(jsonPath("$.title").value("Card blocked"));

        mvc.perform(post("/api/v1/bank-cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessKey":"CASE-001","title":"Card blocked again"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE"));

        assertEquals(1, cases.count(new CaseNumber("CASE-001")));
    }

    @Test
    void unknownUuidIs404() throws Exception {
        mvc.perform(get("/api/v1/bank-cases/00000000-0000-0000-0000-000000000099"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void healthIsUpAndInfoNamesTheApp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());

        mvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("bank-case-management"));
    }

    @Test
    void dangerousActuatorEndpointsAreNotExposed() throws Exception {
        for (String path : new String[]{"/actuator/env", "/actuator/beans", "/actuator/heapdump", "/actuator/shutdown"}) {
            mvc.perform(get(path)).andExpect(status().isNotFound());
        }
    }

    @Test
    void openApiListsTheContract() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/bank-cases'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/bank-cases'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/bank-cases'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/bank-cases/{id}'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/bank-cases/{id}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.components.schemas.CreateCase.required").value(
                        org.hamcrest.Matchers.containsInAnyOrder("businessKey", "title")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("CaseEntity"))));
    }
}
