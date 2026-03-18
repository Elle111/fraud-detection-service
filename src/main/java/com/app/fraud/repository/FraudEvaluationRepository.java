package com.app.fraud.repository;

import com.app.fraud.entity.FraudEvaluation;
import com.app.fraud.dto.FraudDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface FraudEvaluationRepository extends JpaRepository<FraudEvaluation, UUID> {
    
    List<FraudEvaluation> findByTransactionId(UUID transactionId);
    
    List<FraudEvaluation> findByAccountId(UUID accountId);
    
    List<FraudEvaluation> findByCustomerId(UUID customerId);
    
    List<FraudEvaluation> findByDecision(FraudDecision decision);
    
    List<FraudEvaluation> findByTimestampBetween(Instant start, Instant end);
    
    @Query("SELECT f FROM FraudEvaluation f WHERE f.accountId = :accountId AND f.timestamp >= :since")
    List<FraudEvaluation> findByAccountIdSince(@Param("accountId") UUID accountId, @Param("since") Instant since);
    
    @Query("SELECT COUNT(f) FROM FraudEvaluation f WHERE f.accountId = :accountId AND f.timestamp >= :since")
    long countByAccountIdSince(@Param("accountId") UUID accountId, @Param("since") Instant since);
}
