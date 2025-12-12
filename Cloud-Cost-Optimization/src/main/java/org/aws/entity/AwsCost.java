package org.aws.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "aws_cost")
public class AwsCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private BigDecimal amount;

    private String currency;

    // 🔹 New: Service name (e.g., AmazonEC2, AmazonS3)
    private String service;

    // 🔹 New: Anomaly flag (0 = normal, 1 = anomaly)
    @Column(name = "is_anomaly", columnDefinition = "int default 0")
    private Integer isAnomaly = 0;

    // Explicit setters (optional with Lombok)
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setIsAnomaly(Integer isAnomaly) {
        this.isAnomaly = isAnomaly;
    }
}
