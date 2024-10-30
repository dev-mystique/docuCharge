package com.example.mssqll.controller;

import com.example.mssqll.dto.response.ConnectionFeeChildrenDTO;
import com.example.mssqll.dto.response.TokenValidationResult;
import com.example.mssqll.models.ConnectionFee;
import com.example.mssqll.service.ConnectionFeeService;
import com.example.mssqll.service.impl.JwtService;
import com.example.mssqll.specifications.ConnectionFeeSpecification;
import com.example.mssqll.utiles.exceptions.DivideException;
import com.example.mssqll.utiles.exceptions.TokenValidationException;
import com.example.mssqll.utiles.resonse.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/v1/connection-fees")
public class ConnectionFeeController {
    private final JwtService jwtService;

    private final ConnectionFeeService connectionFeeService;

    @Autowired
    public ConnectionFeeController(JwtService jwtService, ConnectionFeeService connectionFeeService) {
        this.jwtService = jwtService;
        this.connectionFeeService = connectionFeeService;
    }

    @GetMapping
    public ResponseEntity<PagedModel<ConnectionFee>> getExtractions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int adjustedPage = (page < 1) ? 0 : page - 1;
        PagedModel<ConnectionFee> fees = connectionFeeService.getAllFee(adjustedPage, size);
        return ResponseEntity.ok().body(fees);
    }

    @GetMapping("/{id}")
    public ApiResponse<ConnectionFee> getConnectionFee(@PathVariable Long id) {
        Optional<ConnectionFee> fee = connectionFeeService.getFee(id);
        if (fee.isPresent()) {
            return ApiResponse.<ConnectionFee>builder()
                    .success(true)
                    .message("Operation successful")
                    .data(fee.get())
                    .build();
        } else {
            return ApiResponse.<ConnectionFee>builder()
                    .success(false)
                    .message("Connection Fee not found")
                    .build();
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/{extractionTaskId}")
    public ResponseEntity<List<ConnectionFee>> createConnectionFee(@PathVariable Long extractionTaskId) {
        List<ConnectionFee> createdConnectionFee = connectionFeeService.saveFee(extractionTaskId);
        return ResponseEntity.ok().body(createdConnectionFee);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping()
    public ResponseEntity<ConnectionFee> createConnectionFee(@RequestBody ConnectionFee connectionFee) {
        ConnectionFee fee = connectionFeeService.save(connectionFee);
        return ResponseEntity.ok().body(fee);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    @PutMapping("/{connectionFeeId}")
    public ResponseEntity<ConnectionFee> updateConnectionFee(
            @PathVariable Long connectionFeeId,
            @RequestBody ConnectionFee connectionFeeDetails) {
        ConnectionFee updatedConnectionFee = connectionFeeService.updateFee(connectionFeeId, connectionFeeDetails);
        return ResponseEntity.ok().body(updatedConnectionFee);
    }

    @GetMapping("/filter")
    public ResponseEntity<PagedModel<?>> filterConnectionFees(
            @RequestParam Map<String, String> filters,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transferDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        int adjustedPage = (page < 1) ? 0 : page - 1;
        Specification<ConnectionFee> spec = ConnectionFeeSpecification.getSpecifications((Map) filters);
        return ResponseEntity.ok().body(connectionFeeService.letDoFilter(spec, adjustedPage, size, sortBy, sortDir));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    @DeleteMapping("/delete-by-task/{extractionTaskId}")
    public ResponseEntity<?> deleteConnectionFeeByTaskId(@PathVariable Long extractionTaskId) {
        connectionFeeService.deleteByTaskId(extractionTaskId);
        return ResponseEntity.ok().body(Collections.singletonMap("message", "Connection fee deleted successfully"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    @DeleteMapping("/soft-delete/{fee}")
    public ResponseEntity<?> softDeleteConnectionFee(@PathVariable Long fee) {
        Optional<ConnectionFee> optionalConnectionFee = connectionFeeService.findById(fee);
        if (optionalConnectionFee.isPresent()) {
            connectionFeeService.softDeleteById(fee);
            return ResponseEntity.ok().body(Collections.singletonMap("message", "Connection fee deleted successfully"));
        } else {
            return ResponseEntity.ok().body(Collections.singletonMap("message", "Connection fee not found"));
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadExcel(
            @RequestParam String accessToken) throws IOException {
        TokenValidationResult res = jwtService.validateTokenWithoutUserName(accessToken);
        if (!res.isValid()) {
            throw new TokenValidationException(res.getMessage());
        }
        List<ConnectionFee> connectionFees = connectionFeeService.getAllConnectionFees();
        ByteArrayInputStream excelStream = connectionFeeService.createExcel(connectionFees);

        HttpHeaders headers = new HttpHeaders();
        String time = LocalDateTime.now(ZoneId.of("Asia/Tbilisi"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        headers.add("Content-Disposition",
                "attachment; filename=" +
                        time +
                        " connection_fees.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelStream.readAllBytes());
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    @PostMapping("/divide-fee/{id}")
    public ResponseEntity<?> divideFee(@PathVariable Long id, @RequestBody Double[] arr) {
        try {
            connectionFeeService.divideFee(id, arr);
            return ResponseEntity.ok().body("Divide Successfully");
        } catch (Exception e) {
            throw new DivideException(e.getMessage());
        }
    }

    @GetMapping("/find-by-parent/{id}")
    public ResponseEntity<List<ConnectionFeeChildrenDTO>> findByParent(@PathVariable Long id) {
        try {
            return ResponseEntity.ok().body(
                    connectionFeeService.getFeesByParent(id)
            );
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }

}