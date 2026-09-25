package com.resolveit.repository;

import com.resolveit.entity.Incident;
import com.resolveit.entity.IncidentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentHistoryRepository extends JpaRepository<IncidentHistory, Long> {

    List<IncidentHistory> findByIncidentOrderByTimestampDesc(Incident incident);
}
