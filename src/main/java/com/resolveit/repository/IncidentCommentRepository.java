package com.resolveit.repository;

import com.resolveit.entity.Incident;
import com.resolveit.entity.IncidentComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentCommentRepository extends JpaRepository<IncidentComment, Long> {

    List<IncidentComment> findByIncidentOrderByCreatedAtAsc(Incident incident);
}
