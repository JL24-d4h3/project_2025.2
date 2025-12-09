package org.project.project.model.dto;

/**
 * DTO para información básica de equipos en el dashboard
 * Adaptado a la nueva BD official_dev_portal con la tabla equipo
 */
public class TeamInfoDTO {
    
    private Long teamId;
    private String teamName;
    private String teamDescription;
    private int memberCount;
    private String createdBy;
    
    // Constructores
    public TeamInfoDTO() {}
    
    public TeamInfoDTO(Long teamId, String teamName, int memberCount) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.memberCount = memberCount;
    }
    
    public TeamInfoDTO(Long teamId, String teamName, String teamDescription, int memberCount, String createdBy) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamDescription = teamDescription;
        this.memberCount = memberCount;
        this.createdBy = createdBy;
    }
    
    // Getters y Setters
    public Long getTeamId() {
        return teamId;
    }
    
    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
    
    public String getTeamName() {
        return teamName;
    }
    
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
    
    public String getTeamDescription() {
        return teamDescription;
    }
    
    public void setTeamDescription(String teamDescription) {
        this.teamDescription = teamDescription;
    }
    
    public int getMemberCount() {
        return memberCount;
    }
    
    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}