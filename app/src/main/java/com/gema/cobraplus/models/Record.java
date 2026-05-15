package com.gema.cobraplus.models;

import java.util.Date;

public class Record {

    private String id;
    private String userId;
    private String conceptId;
    private Double quantity;
    private Date date;
    private String description;

    public Record() {
    }

    public Record(String id, String userId, String conceptId,Double quantity, Date date,
                  String description) {
        this.id = id;
        this.userId = userId;
        this.conceptId = conceptId;
        this.quantity = quantity;
        this.date = date;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConceptId() {
        return conceptId;
    }

    public void setConceptId(String conceptId) {
        this.conceptId = conceptId;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}