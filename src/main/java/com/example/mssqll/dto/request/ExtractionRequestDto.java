package com.example.mssqll.dto.request;

import com.example.mssqll.models.ExtractionTask;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ExtractionRequestDto {

    private LocalDate date;

    private int totalAmount;

    private String purpose;

    private String description;

    private int status;

    private ExtractionTask extractionTask;

}
