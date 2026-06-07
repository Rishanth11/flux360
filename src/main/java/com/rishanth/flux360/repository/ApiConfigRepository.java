package com.rishanth.flux360.repository;

import com.rishanth.flux360.entity.ApiConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiConfigRepository
        extends JpaRepository<ApiConfigEntity, Long> {

    Optional<ApiConfigEntity> findByConfigKey(
            String configKey
    );

    List<ApiConfigEntity> findByCategoryOrderByConfigKeyAsc(
            String category
    );

    boolean existsByConfigKey(
            String configKey
    );
}