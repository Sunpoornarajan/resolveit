package com.resolveit.dto;

import java.util.HashMap;
import java.util.Map;

public class DashboardStatsDto {

    // Global / Admin metrics
    private long totalIncidents;
    private long openIncidents;
    private long assignedIncidents;
    private long inProgressIncidents;
    private long resolvedIncidents;
    private long closedIncidents;
    private long criticalIncidents;
    private long totalEmployees;
    private long totalSupportEngineers;

    // Support Engineer / Employee specific metrics
    private long myTotalIncidents;
    private long myOpenIncidents;
    private long myInProgressIncidents;
    private long myResolvedIncidents;
    private long myClosedIncidents;
    private long myHighPriorityIncidents;
    private long myCriticalPriorityIncidents;

    public DashboardStatsDto() {
    }

    // Getters and Setters
    public long getTotalIncidents() {
        return totalIncidents;
    }

    public void setTotalIncidents(long totalIncidents) {
        this.totalIncidents = totalIncidents;
    }

    public long getOpenIncidents() {
        return openIncidents;
    }

    public void setOpenIncidents(long openIncidents) {
        this.openIncidents = openIncidents;
    }

    public long getAssignedIncidents() {
        return assignedIncidents;
    }

    public void setAssignedIncidents(long assignedIncidents) {
        this.assignedIncidents = assignedIncidents;
    }

    public long getInProgressIncidents() {
        return inProgressIncidents;
    }

    public void setInProgressIncidents(long inProgressIncidents) {
        this.inProgressIncidents = inProgressIncidents;
    }

    public long getResolvedIncidents() {
        return resolvedIncidents;
    }

    public void setResolvedIncidents(long resolvedIncidents) {
        this.resolvedIncidents = resolvedIncidents;
    }

    public long getClosedIncidents() {
        return closedIncidents;
    }

    public void setClosedIncidents(long closedIncidents) {
        this.closedIncidents = closedIncidents;
    }

    public long getCriticalIncidents() {
        return criticalIncidents;
    }

    public void setCriticalIncidents(long criticalIncidents) {
        this.criticalIncidents = criticalIncidents;
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public long getTotalSupportEngineers() {
        return totalSupportEngineers;
    }

    public void setTotalSupportEngineers(long totalSupportEngineers) {
        this.totalSupportEngineers = totalSupportEngineers;
    }

    public long getMyTotalIncidents() {
        return myTotalIncidents;
    }

    public void setMyTotalIncidents(long myTotalIncidents) {
        this.myTotalIncidents = myTotalIncidents;
    }

    public long getMyOpenIncidents() {
        return myOpenIncidents;
    }

    public void setMyOpenIncidents(long myOpenIncidents) {
        this.myOpenIncidents = myOpenIncidents;
    }

    public long getMyInProgressIncidents() {
        return myInProgressIncidents;
    }

    public void setMyInProgressIncidents(long myInProgressIncidents) {
        this.myInProgressIncidents = myInProgressIncidents;
    }

    public long getMyResolvedIncidents() {
        return myResolvedIncidents;
    }

    public void setMyResolvedIncidents(long myResolvedIncidents) {
        this.myResolvedIncidents = myResolvedIncidents;
    }

    public long getMyClosedIncidents() {
        return myClosedIncidents;
    }

    public void setMyClosedIncidents(long myClosedIncidents) {
        this.myClosedIncidents = myClosedIncidents;
    }

    public long getMyHighPriorityIncidents() {
        return myHighPriorityIncidents;
    }

    public void setMyHighPriorityIncidents(long myHighPriorityIncidents) {
        this.myHighPriorityIncidents = myHighPriorityIncidents;
    }

    public long getMyCriticalPriorityIncidents() {
        return myCriticalPriorityIncidents;
    }

    public void setMyCriticalPriorityIncidents(long myCriticalPriorityIncidents) {
        this.myCriticalPriorityIncidents = myCriticalPriorityIncidents;
    }
}
