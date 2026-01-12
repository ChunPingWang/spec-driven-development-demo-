package com.example.inventory.infrastructure.adapter.outbound.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, Long> {

    Optional<ProductJpaEntity> findByProductId(String productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductJpaEntity p WHERE p.productId = :productId")
    Optional<ProductJpaEntity> findByProductIdForUpdate(@Param("productId") String productId);
}
