package com.example.mssqll.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "connection_fees",
        indexes = {
                @Index(name = "idx_change_person", columnList = "change_person"),
                @Index(name = "idx_parent_id", columnList = "parent_id"),
                @Index(name = "idx_transfer_person", columnList = "transfer_person"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_transfer_date", columnList = "transfer_date"),
                @Index(name = "idx_extraction_date", columnList = "extraction_date"),
                @Index(name = "idx_history_id", columnList = "history_id")
        })
@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
//@Cacheable
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entityCache")
public class ConnectionFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Nullable
    private Long historyId;

    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "orderN")
    @Nationalized
    private String orderN;

    @Column(name = "region")
    @Nationalized
    private String region;

    @Column(name = "sevice_center")
    @Nationalized
    private String serviceCenter;

    @Column(name = "queueNumber")
    private String queueNumber;

    @Column(name = "project_id")
    @Nationalized
    private String projectID;

    @Column(name = "withdraw_type")
    @Nationalized
    private String withdrawType;

    @ManyToOne
    @JoinColumn(name = "extraction_task_id", nullable = false)
    private ExtractionTask extractionTask;

    @Column(name = "clarification_date")
    private LocalDateTime clarificationDate;

    @Column(name = "treasury_refund_date")
    private LocalDate treasuryRefundDate;

    @Column(name = "payment_order_sent_date")
    private LocalDate paymentOrderSentDate;

    @ElementCollection
    @Nationalized
    private List<String> canceledOrders;

    @ElementCollection
    @Nationalized
    private List<String> canceledProject;

    @Column(name = "change_date")
    private LocalDateTime changeDate;

    @Column(name = "transfer_date")
    private LocalDateTime transferDate;

    @Column(name = "exextraction_id")
    @Nullable
    private Long extractionId;

    @Column(name = "note")
    @Nationalized
    private String note;

    @Column(name = "extraction_date")
    private LocalDate extractionDate;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "purpose", length = 1024, nullable = false)
    @Nationalized
    private String purpose;

    @Column(name = "description", length = 1024)
    @Nationalized
    private String description;

    @Column(name = "tax_id")
    @Nationalized
    @Nullable
    private String tax;

    @ManyToOne
    @JoinColumn(name = "transfer_person", nullable = false)
    private User transferPerson;

    @ManyToOne
    @JoinColumn(name = "change_person", nullable = false)
    @Nullable
    private User changePerson;

    @ManyToOne // Lazy loading to improve performance
    @JsonIgnore
    @JoinColumn(name = "parent_id", nullable = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private ConnectionFee parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Where(clause = "status != 'SOFT_DELETED'") // Filter out soft-deleted children
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE) // Cache parent entity
    private List<ConnectionFee> children ;

    @Nullable
    @Column(name = "first_withdraw_type", length = 255)
    @Nationalized
    private String firstWithdrawType;

    public ConnectionFee() {

    }

    public ConnectionFee(String purpose, Double totalAmount, LocalDate extractionDate, Status status, LocalDateTime transferDate, ExtractionTask extractionTask, String description, Long extractionId) {
        this.purpose = purpose;
        this.totalAmount = totalAmount;
        this.extractionDate = extractionDate;
        this.status = status;
        this.transferDate = transferDate;
        this.extractionTask = extractionTask;
        this.description = description;
        this.extractionId = extractionId;
    }


    public ConnectionFee(ConnectionFee connectionFee) {
        this.status = connectionFee.getStatus();
        this.orderN = connectionFee.getOrderN();
        this.region = connectionFee.getRegion();
        this.serviceCenter = connectionFee.getServiceCenter();
        this.projectID = connectionFee.getProjectID();
        this.withdrawType = connectionFee.getWithdrawType();
        this.extractionTask = connectionFee.getExtractionTask();
        this.clarificationDate = connectionFee.getClarificationDate();
        this.changeDate = connectionFee.getChangeDate();
        this.transferDate = connectionFee.getTransferDate();
        this.extractionId = connectionFee.getExtractionId();
        this.note = connectionFee.getNote();
        this.extractionDate = connectionFee.getExtractionDate();
        this.totalAmount = connectionFee.getTotalAmount();
        this.purpose = connectionFee.getPurpose();
        this.description = connectionFee.getDescription();
        this.tax = connectionFee.getTax();
        this.parent = connectionFee.getParent();
    }

}