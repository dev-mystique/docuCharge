package com.example.mssqll.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ConnectionFeeChildrenDTO {
    private Long id;
    private String orderN;
    private String region;
    private String serviceCenter;
    private String projectID;
    private String withdrawType;
    private LocalDateTime clarificationDate;
    private LocalDateTime changeDate;
    private LocalDateTime transferDate;
    private Long extractionId;
    private String note;
    private LocalDate extractionDate;
    private Double totalAmount;
    private String purpose;
    private String description;
    private String tax;
    private List<ConnectionFeeChildrenDTO> children;
    private Long historyId;
}
