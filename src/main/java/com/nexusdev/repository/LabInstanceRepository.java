package com.nexusdev.repository;

import com.nexusdev.model.LabInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabInstanceRepository extends JpaRepository<LabInstance, Integer> {
    // Spring generates: SELECT * FROM lab_instances WHERE user_id = ?
    List<LabInstance> findByUserId(Integer userId);

    // Spring generates: SELECT * FROM lab_instances WHERE template_id = ?
    List<LabInstance> findByTemplateId(Integer templateId);
}