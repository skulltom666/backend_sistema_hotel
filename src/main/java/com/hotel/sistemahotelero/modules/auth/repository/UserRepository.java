package com.hotel.sistemahotelero.modules.auth.repository;

import com.hotel.sistemahotelero.shared.enums.SubscriptionPlan;
import com.hotel.sistemahotelero.shared.persistence.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByRuc(String ruc);
    Optional<User> findByEmail(String email);
    Optional<User> findByRucAndEmail(String ruc, String email);

    boolean existsByRuc(String ruc);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId")
    Optional<User> findByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT u FROM User u WHERE u.subscriptionPlan = :plan AND u.enabled = true")
    List<User> findBySubscriptionPlan(@Param("plan") SubscriptionPlan plan);

    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findAllEnabled();

    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.ruc = :ruc")
    void updateEnabledByRuc(@Param("ruc") String ruc, @Param("enabled") boolean enabled);

    @Query("SELECT COUNT(u) FROM User u WHERE u.subscriptionPlan = :plan AND u.enabled = true")
    long countBySubscriptionPlan(@Param("plan") SubscriptionPlan plan);
}