package com.gema.cobraplus.models;


//Representa cada registro individual del informe
public class ConfirmReportItem {

    private String recordId;
    private String date;
    private String company;
    private String concept;
    private String quantity;
    private String commentary;

    public ConfirmReportItem() {
    }

    public ConfirmReportItem(String recordId, String date, String company, String concept, String quantity, String commentary) {
        this.recordId = recordId;
        this.date = date;
        this.company = company;
        this.concept = concept;
        this.quantity = quantity;
        this.commentary = commentary;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }
}