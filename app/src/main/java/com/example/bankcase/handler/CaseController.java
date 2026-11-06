package com.example.bankcase.handler;

import com.example.bankcase.config.CaseService;
import com.example.bankcase.domain.CaseId;
import com.example.bankcase.domain.CaseNumber;
import com.example.bankcase.domain.CaseView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

/** HTTP adapter for bank cases. Chooses the status code; the service still inserts. */
@RestController
@RequestMapping("/api/v1/bank-cases")
public class CaseController {

    private final CaseService cases;

    public CaseController(CaseService cases) {
        this.cases = cases;
    }

    @Operation(summary = "Open a bank case")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created. Location is the GET url."),
            @ApiResponse(responseCode = "400", description = "businessKey or title blank, title over 120, body not JSON",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "business key already stored",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<CaseResponse> create(@Valid @RequestBody CreateCase body) {
        CaseView created = cases.register(CaseId.newId(), new CaseNumber(body.businessKey()), body.title());
        URI location = URI.create("/api/v1/bank-cases/" + created.id().value());
        return ResponseEntity.created(location).body(CaseResponse.from(created));
    }

    @Operation(summary = "Read one bank case by its UUID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The case"),
            @ApiResponse(responseCode = "400", description = "id is not a UUID",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "no case with this id",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public CaseResponse get(@PathVariable UUID id) {
        return CaseResponse.from(cases.require(new CaseId(id)));
    }
}
