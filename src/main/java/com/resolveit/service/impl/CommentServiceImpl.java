package com.resolveit.service.impl;

import com.resolveit.entity.Incident;
import com.resolveit.entity.IncidentComment;
import com.resolveit.entity.IncidentHistory;
import com.resolveit.entity.User;
import com.resolveit.enums.HistoryAction;
import com.resolveit.exception.ResourceNotFoundException;
import com.resolveit.repository.IncidentCommentRepository;
import com.resolveit.repository.IncidentHistoryRepository;
import com.resolveit.repository.IncidentRepository;
import com.resolveit.service.CommentService;
import com.resolveit.service.IncidentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final IncidentRepository incidentRepository;
    private final IncidentCommentRepository commentRepository;
    private final IncidentHistoryRepository historyRepository;
    private final IncidentService incidentService;

    public CommentServiceImpl(IncidentRepository incidentRepository,
                              IncidentCommentRepository commentRepository,
                              IncidentHistoryRepository historyRepository,
                              IncidentService incidentService) {
        this.incidentRepository = incidentRepository;
        this.commentRepository = commentRepository;
        this.historyRepository = historyRepository;
        this.incidentService = incidentService;
    }

    @Override
    public IncidentComment addComment(Long incidentId, String commentText, User author) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + incidentId));

        // Validate access
        incidentService.validateAccess(incident, author);

        if (commentText == null || commentText.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text cannot be empty");
        }

        IncidentComment comment = new IncidentComment(incident, author, commentText.trim());
        IncidentComment saved = commentRepository.save(comment);

        // Record history
        String snippet = commentText.trim();
        if (snippet.length() > 60) {
            snippet = snippet.substring(0, 57) + "...";
        }
        IncidentHistory history = new IncidentHistory(
                incident,
                HistoryAction.COMMENT_ADDED,
                null,
                snippet,
                author
        );
        historyRepository.save(history);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentComment> getCommentsForIncident(Long incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + incidentId));
        return commentRepository.findByIncidentOrderByCreatedAtAsc(incident);
    }
}
