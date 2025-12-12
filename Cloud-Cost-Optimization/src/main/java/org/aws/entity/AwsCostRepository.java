package org.aws.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface AwsCostRepository extends JpaRepository<AwsCost, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE AwsCost a SET a.isAnomaly = 1 " +
            "WHERE a.date = :date AND a.service = :service")
    void updateIsAnomaly(@Param("date") LocalDate date,
                         @Param("service") String service);
}

