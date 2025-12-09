package org.project.project.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * DTO para solicitar invitación de usuarios a proyecto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InviteUserRequest {
    private List<String> emails;  // one or multiple
    private List<Long> rolIds;    // roles a asignar
    private String permission;    // LECTOR, COMENTADOR, EDITOR
    private List<Long> equipoIds; // equipos a asignar
    private Map<Long, String> teamNamesMap; // Map: negative ID -> team name (for temporary teams)
    private Boolean sendEmail = true;

    @Override
    public String toString() {
        return "InviteUserRequest{" +
                "emails=" + emails +
                ", rolIds=" + rolIds +
                ", permission='" + permission + '\'' +
                ", equipoIds=" + equipoIds +
                ", teamNamesMap=" + teamNamesMap +
                ", sendEmail=" + sendEmail +
                '}';
    }
}
