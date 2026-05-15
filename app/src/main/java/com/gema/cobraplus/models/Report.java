package com.gema.cobraplus.models;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;

public class Report {

    private String id;
    private String name;
    private String company;
    private Date startDate;
    private Date endDate;
    private String groupBy;
    private boolean baseReport;
    private String baseReportId;
    private String userId;
    private Date createdAt;

    public Report() {
    }

    public Report(String id, String name, String company, Date startDate, Date endDate, String groupBy,
                  boolean baseReport, String baseReportId, String userId, Date createdAt) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.startDate = startDate;
        this.endDate = endDate;
        this.groupBy = groupBy;
        this.baseReport = baseReport;
        this.baseReportId = baseReportId;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public boolean isBaseReport() {
        return baseReport;
    }

    public void setBaseReport(boolean baseReport) {
        this.baseReport = baseReport;
    }

    public String getBaseReportId() {
        return baseReportId;
    }

    public void setBaseReportId(String baseReportId) {
        this.baseReportId = baseReportId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
