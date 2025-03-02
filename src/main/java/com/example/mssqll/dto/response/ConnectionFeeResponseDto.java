package com.example.mssqll.dto.response;

import com.example.mssqll.models.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ConnectionFeeResponseDto {

    private Long id;

    private OrderStatus orderStatus;

    private Status status;

    private String orderN;

    private String region;

    private String serviceCenter;

    private String queueNumber;

    private String projectID;

    private String withdrawType;

    private ExtractionTask extractionTask;

    private LocalDateTime clarificationDate;

    private LocalDate treasuryRefundDate;

    private LocalDate paymentOrderSentDate;

    private List<String> canceledOrders;

    private List<String> canceledProject;

    private LocalDateTime changeDate;

    private LocalDateTime transferDate;

    private Long extractionId;

    private String note;

    private LocalDate extractionDate;

    private Double totalAmount;

    private String purpose;

    private String description;

    private String tax;

    private UserResponseDto transferPerson;

    private UserResponseDto changePerson;

    private List<ConnectionFeeResponseDto> children;

    private Long historyId;

}
