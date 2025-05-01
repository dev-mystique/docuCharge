package com.example.mssqll.controller;

import com.example.mssqll.dto.response.ConnectionFeeChildrenDTO;
import com.example.mssqll.dto.response.TokenValidationResult;
import com.example.mssqll.models.ConnectionFee;
import com.example.mssqll.service.ConnectionFeeService;
import com.example.mssqll.service.impl.JwtService;
import com.example.mssqll.specifications.ConnectionFeeSpecification;
import com.example.mssqll.utiles.exceptions.DivideException;
import com.example.mssqll.utiles.exceptions.ResourceNotFoundException;
import com.example.mssqll.utiles.exceptions.TokenValidationException;
import com.example.mssqll.utiles.resonse.ApiResponse;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            @RequestParam(required = false) List<String> withdrawType,
            @RequestParam(required = false) List<String> orderN,
            @RequestParam(required = false) List<String> orderStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transferDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        int adjustedPage = (page < 1) ? 0 : page - 1;

        Map<String, Object> updatedFilters = new HashMap<>(filters);
        if (withdrawType != null) {
            updatedFilters.put("withdrawType", withdrawType);
        }
        if (orderN != null) {
            updatedFilters.put("orderN", orderN);
        }
        if (orderStatus != null) {
            updatedFilters.put("status", orderStatus);
        }
        Specification<ConnectionFee> spec = ConnectionFeeSpecification.getSpecifications(updatedFilters);

        PagedModel<?> resPage = connectionFeeService.letDoFilter(spec, adjustedPage, size, sortBy, sortDir);
        return ResponseEntity.ok().body(resPage);
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
    public ResponseEntity<?> downloadExcel(
            @RequestParam Map<String, String> filters,
            @RequestParam String accessToken) throws IOException {
        TokenValidationResult res = jwtService.validateTokenWithoutUserName(accessToken);
        if (!res.isValid()) {
            throw new TokenValidationException(res.getMessage());
        }
        filters.put("download", "REMINDER");
        List<ConnectionFee> resti = connectionFeeService.getFeeCustom(filters);
        ByteArrayInputStream excelStream = connectionFeeService.createExcel(resti);
        HttpHeaders headers = new HttpHeaders();
        String time = LocalDateTime.now(ZoneId.of("Asia/Tbilisi"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        headers.add("Content-Disposition",
                "attachment; filename=" +
                        time +
                        " connection_fees.csv");
//        headers.add("Content-Disposition", "attachment; filename=connection_fees.csv");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(excelStream));
    }
//
//    @GetMapping("/download")
//    public ResponseEntity<byte[]> downloadExcel(
//            @RequestParam Map<String, String> filters,
//            @RequestParam String accessToken) throws IOException {
//        TokenValidationResult res = jwtService.validateTokenWithoutUserName(accessToken);
//        if (!res.isValid()) {
//            throw new TokenValidationException(res.getMessage());
//        }
//        filters.put("download", "REMINDER");
//        List<ConnectionFee> resti = connectionFeeService.getFeeCustom(filters);
//        ByteArrayInputStream excelStream = connectionFeeService.createExcel(resti);
//
//        HttpHeaders headers = new HttpHeaders();
//        String time = LocalDateTime.now(ZoneId.of("Asia/Tbilisi"))
//                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
//        headers.add("Content-Disposition",
//                "attachment; filename=" +
//                        time +
//                        " connection_fees.xlsx");
//        return ResponseEntity.ok()
//                .headers(headers)
//                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
//                .body(excelStream.readAllBytes());
//    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    @PostMapping("/divide-fee/{id}")
    public ResponseEntity<?> divideFee(@PathVariable Long id, @RequestBody Double[] arr) {
        try {
            connectionFeeService.divideFee(id, arr);
            return ResponseEntity.ok().body(Collections.singletonMap("message", "Divide Successfully"));
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


    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/upload-history")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file) {
        Integer count = 0;
        if (file.isEmpty()) {
            throw new ResourceNotFoundException("Please select a file to upload");
        }
        IOUtils.setByteArrayMaxOverride(141582236);
        try {
            count = connectionFeeService.uploadHistory(file);
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Successfully uploaded",
                            "count", count
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process the file: " + e.getMessage());
        }
    }

    @GetMapping("/download-ext")
    public ResponseEntity<Resource> downloadFile(@RequestParam String fileName, @RequestParam String accessToken) {
        TokenValidationResult res = jwtService.validateTokenWithoutUserName(accessToken);
        if (!res.isValid()) {
            throw new TokenValidationException(res.getMessage());
        }
        try {
            System.out.println("sxva ram xdeba");
            // Decode the filename in case it's URL encoded
            String decodedFileName = java.net.URLDecoder.decode(fileName, StandardCharsets.UTF_8);

            // Construct the file path
            Path filePath = Paths.get("C:/uploads/").resolve(decodedFileName).normalize();
            File file = filePath.toFile();

            // Check if file exists
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Load file as a resource
            Resource resource = new FileSystemResource(file);

            // Encode filename for safe download
            String encodedFileName = URLEncoder.encode(decodedFileName, StandardCharsets.UTF_8)
                    .replace("+", "%20"); // Fix spaces

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM) // Generic content type for download
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}