package com.nexusdev.repository;

import com.nexusdev.model.LabTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LabTemplateRepository extends JpaRepository<LabTemplate, Integer> {
    // Spring generates: SELECT * FROM lab_templates WHERE share_code = ?
    Optional<LabTemplate> findByShareCode(String shareCode);
}