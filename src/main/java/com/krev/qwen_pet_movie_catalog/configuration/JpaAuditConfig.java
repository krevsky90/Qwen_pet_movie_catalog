package com.krev.qwen_pet_movie_catalog.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//needs to fill auditable fields like created_at and updated_at
@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {
}
