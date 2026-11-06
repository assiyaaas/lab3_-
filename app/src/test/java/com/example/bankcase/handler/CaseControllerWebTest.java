package com.example.bankcase.handler;

import com.example.bankcase.config.BankCaseManagementApplication;
import com.example.bankcase.config.CaseService;
import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseNumber;
import com.example.bankcase.domain.CaseView;
import com.example.bankcase.domain.DuplicateCase;
import com.example.bankcase.domain.MissingCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice: only the controller, the advice and Jackson. CaseService is a mock,
 * so no PostgreSQL and no real HTTP server. Locks 201, 400, 404, 409 and the correlation header.
 */
@WebMvcTest(CaseController.class)
@ContextConfiguration(classes = BankCaseManagementApplication.class)
class CaseControllerWebTest {

    private static final String CASES = "/api/v1/bank-cases";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CaseService service;

    @Test
    void validBodyIsCreatedWithLocation() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.register(any(CaseId.class), eq(new CaseNumber("CASE-001")), eq("Card blocked")))
                .thenReturn(new CaseView(new CaseId(id), new CaseNumber("CASE-001"), "New", "Card blocked"));

        mvc.perform(post(CASES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessKey":"CASE-001","title":"Card blocked"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/bank-cases/" + id)))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.businessKey").value("CASE-001"))
                .andExpect(jsonPath("$.status").value("New"))
                .andExpect(jsonPath("$.title").value("Card blocked"));
    }

    @Test
    void blankTitleIs400AndServiceNeverRuns() throws Exception {
        mvc.perform(post(CASES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessKey":"CASE-001","title":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("title must not be blank"))
                .andExpect(jsonPath("$.correlationId", not(emptyOrNullString())));

        verifyNoInteractions(service);
    }

    @Test
    void blankBusinessKeyIs400() throws Exception {
        mvc.perform(post(CASES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessKey":"","title":"Card blocked"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("businessKey must not be blank"));

        verifyNoInteractions(service);
    }

    @Test
    void titleOf121CharactersIs400() throws Exception {
        String title = "x".repeat(121);

        mvc.perform(post(CASES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessKey\":\"CASE-001\",\"title\":\"" + title + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value(containsString("size must be between 0 and 120")));
    }

    @Test
    void bodyThatIsNotJsonIs400() throws Exception {
        mvc.perform(post(CASES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("body must be JSON"));
    }

    @Test
    void wrongContentTypeIs415() throws Exception {
        mvc.perform(post(CASES)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("CASE-001"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED"));
    }

    @Test
    void missingCaseIs404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.require(new CaseId(id))).thenThrow(new MissingCase(new CaseId(id)));

        mvc.perform(get(CASES + "/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void duplicateBusinessKeyIs409() throws Exception {
        when(service.register(any(CaseId.class), eq(new CaseNumber("CASE-001")), eq("Card blocked again")))
                .thenThrow(new DuplicateCase(new CaseNumber("CASE-001")));

        mvc.perform(post(CASES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessKey":"CASE-001","title":"Card blocked again"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE"))
                .andExpect(jsonPath("$.message").value(containsString("CASE-001")));
    }

    @Test
    void businessKeyInThePathIs400NotFound() throws Exception {
        mvc.perform(get(CASES + "/CASE-001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("id must be a UUID"));

        verifyNoInteractions(service);
    }

    @Test
    void correlationHeaderComesBackInTheErrorBody() throws Exception {
        mvc.perform(post(CASES)
                        .header("X-Correlation-Id", "req-42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"businessKey":"CASE-001","title":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.correlationId").value("req-42"));
    }

    @Test
    void unexpectedErrorIs500WithoutInternals() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.require(new CaseId(id))).thenThrow(new IllegalStateException("Unknown status in database: X"));

        mvc.perform(get(CASES + "/" + id))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL"))
                .andExpect(jsonPath("$.message").value("unexpected error"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());
    }
}
