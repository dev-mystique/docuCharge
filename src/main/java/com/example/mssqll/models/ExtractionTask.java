package com.example.mssqll.models;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import java.time.LocalDateTime;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Nationalized;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Getter
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class ExtractionTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;

    private LocalDateTime sendDate;

    @Nationalized
    private String fileName;


    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FileStatus status;

    public ExtractionTask(LocalDateTime date, String name, FileStatus status) {
        this.date = date;
        this.fileName = name;
        this.status = status;
    }
}