package org.project.project.model.dto;

/**
 * DTO para las estadísticas del dashboard
 * Adaptado a la nueva BD official_dev_portal con estructura de equipos, proyectos y repositorios
 */
public class DashboardStatsDTO {
    
    // Estadísticas principales
    private long totalTeams;
    private long totalProjects;
    private long totalRepositories;
    private long totalTickets;
    private long totalApis;
    private long totalDocumentations;
    
    // Estadísticas de proyectos por propietario
    private long personalProjects;
    private long groupProjects;
    private long companyProjects;
    
    // Estadísticas de proyectos por estado
    private long plannedProjects;
    private long developmentProjects;
    private long maintenanceProjects;
    private long closedProjects;
    
    // Estadísticas de repositorios
    private long personalRepositories;
    private long collaborativeRepositories;
    private long publicRepositories;
    private long privateRepositories;
    private long activeRepositories;
    private long archivedRepositories;
    
    // Estadísticas de tickets por estado
    private long sentTickets;
    private long receivedTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long closedTickets;
    private long rejectedTickets;
    
    // Estadísticas de tickets por prioridad
    private long lowPriorityTickets;
    private long mediumPriorityTickets;
    private long highPriorityTickets;
    
    // Constructores
    public DashboardStatsDTO() {}
    
    // Getters y Setters
    public long getTotalTeams() {
        return totalTeams;
    }
    
    public void setTotalTeams(long totalTeams) {
        this.totalTeams = totalTeams;
    }
    
    public long getTotalProjects() {
        return totalProjects;
    }
    
    public void setTotalProjects(long totalProjects) {
        this.totalProjects = totalProjects;
    }
    
    public long getTotalRepositories() {
        return totalRepositories;
    }
    
    public void setTotalRepositories(long totalRepositories) {
        this.totalRepositories = totalRepositories;
    }
    
    public long getTotalTickets() {
        return totalTickets;
    }
    
    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }
    
    public long getTotalApis() {
        return totalApis;
    }
    
    public void setTotalApis(long totalApis) {
        this.totalApis = totalApis;
    }
    
    public long getTotalDocumentations() {
        return totalDocumentations;
    }
    
    public void setTotalDocumentations(long totalDocumentations) {
        this.totalDocumentations = totalDocumentations;
    }
    
    public long getPersonalProjects() {
        return personalProjects;
    }
    
    public void setPersonalProjects(long personalProjects) {
        this.personalProjects = personalProjects;
    }
    
    public long getGroupProjects() {
        return groupProjects;
    }
    
    public void setGroupProjects(long groupProjects) {
        this.groupProjects = groupProjects;
    }
    
    public long getCompanyProjects() {
        return companyProjects;
    }
    
    public void setCompanyProjects(long companyProjects) {
        this.companyProjects = companyProjects;
    }
    
    public long getPlannedProjects() {
        return plannedProjects;
    }
    
    public void setPlannedProjects(long plannedProjects) {
        this.plannedProjects = plannedProjects;
    }
    
    public long getDevelopmentProjects() {
        return developmentProjects;
    }
    
    public void setDevelopmentProjects(long developmentProjects) {
        this.developmentProjects = developmentProjects;
    }
    
    public long getMaintenanceProjects() {
        return maintenanceProjects;
    }
    
    public void setMaintenanceProjects(long maintenanceProjects) {
        this.maintenanceProjects = maintenanceProjects;
    }
    
    public long getClosedProjects() {
        return closedProjects;
    }
    
    public void setClosedProjects(long closedProjects) {
        this.closedProjects = closedProjects;
    }
    
    public long getPersonalRepositories() {
        return personalRepositories;
    }
    
    public void setPersonalRepositories(long personalRepositories) {
        this.personalRepositories = personalRepositories;
    }
    
    public long getCollaborativeRepositories() {
        return collaborativeRepositories;
    }
    
    public void setCollaborativeRepositories(long collaborativeRepositories) {
        this.collaborativeRepositories = collaborativeRepositories;
    }
    
    public long getPublicRepositories() {
        return publicRepositories;
    }
    
    public void setPublicRepositories(long publicRepositories) {
        this.publicRepositories = publicRepositories;
    }
    
    public long getPrivateRepositories() {
        return privateRepositories;
    }
    
    public void setPrivateRepositories(long privateRepositories) {
        this.privateRepositories = privateRepositories;
    }
    
    public long getActiveRepositories() {
        return activeRepositories;
    }

    public void setActiveRepositories(long activeRepositories) {
        this.activeRepositories = activeRepositories;
    }

    public long getArchivedRepositories() {
        return archivedRepositories;
    }

    public void setArchivedRepositories(long archivedRepositories) {
        this.archivedRepositories = archivedRepositories;
    }

    public long getSentTickets() {
        return sentTickets;
    }
    
    public void setSentTickets(long sentTickets) {
        this.sentTickets = sentTickets;
    }
    
    public long getReceivedTickets() {
        return receivedTickets;
    }
    
    public void setReceivedTickets(long receivedTickets) {
        this.receivedTickets = receivedTickets;
    }
    
    public long getInProgressTickets() {
        return inProgressTickets;
    }
    
    public void setInProgressTickets(long inProgressTickets) {
        this.inProgressTickets = inProgressTickets;
    }
    
    public long getResolvedTickets() {
        return resolvedTickets;
    }
    
    public void setResolvedTickets(long resolvedTickets) {
        this.resolvedTickets = resolvedTickets;
    }
    
    public long getClosedTickets() {
        return closedTickets;
    }
    
    public void setClosedTickets(long closedTickets) {
        this.closedTickets = closedTickets;
    }
    
    public long getRejectedTickets() {
        return rejectedTickets;
    }

    public void setRejectedTickets(long rejectedTickets) {
        this.rejectedTickets = rejectedTickets;
    }

    public long getLowPriorityTickets() {
        return lowPriorityTickets;
    }
    
    public void setLowPriorityTickets(long lowPriorityTickets) {
        this.lowPriorityTickets = lowPriorityTickets;
    }
    
    public long getMediumPriorityTickets() {
        return mediumPriorityTickets;
    }
    
    public void setMediumPriorityTickets(long mediumPriorityTickets) {
        this.mediumPriorityTickets = mediumPriorityTickets;
    }
    
    public long getHighPriorityTickets() {
        return highPriorityTickets;
    }
    
    public void setHighPriorityTickets(long highPriorityTickets) {
        this.highPriorityTickets = highPriorityTickets;
    }
}