package com.resolveit.service;

import com.resolveit.entity.IncidentComment;
import com.resolveit.entity.User;

import java.util.List;

public interface CommentService {

    IncidentComment addComment(Long incidentId, String commentText, User author);

    List<IncidentComment> getCommentsForIncident(Long incidentId);
}
