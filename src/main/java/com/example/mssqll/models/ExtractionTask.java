package com.example.mssqll.models;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import java.time.LocalDateTime;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Getter
public class ExtractionTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;

    private LocalDateTime sendDate;
    // statusis shecvla gadatanissas
    //send_date gadatanis tarigi
    private String fileName;


    @Enumerated(EnumType.STRING) // Store the enum as a string in the database
    @Column(name = "status")
    private FileStatus status;

    public ExtractionTask(LocalDateTime date, String name, FileStatus status) {
        this.date = date;
        this.fileName = name;
        this.status = status;
    }
}