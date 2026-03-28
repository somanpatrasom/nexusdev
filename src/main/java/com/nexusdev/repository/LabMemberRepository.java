package com.nexusdev.repository;

import com.nexusdev.model.LabMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabMemberRepository extends JpaRepository<LabMember, Integer> {
    List<LabMember> findByInstanceId(Integer instanceId);
    List<LabMember> findByUserId(Integer userId);
}