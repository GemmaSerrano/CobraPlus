package com.gema.cobraplus.models;

public class Concept {

    private String id;
    private String name;
    private String unitCalculation;

    private String company;
    private double amount;
    private String userId;
    private boolean baseConcept;
    private String baseConceptId;

    public Concept() {
    }

    public Concept(String id, String name, String unitCalculation, String company,
                   double amount, String userId, boolean baseConcept, String baseConceptId) {
        this.id = id;
        this.name = name;
        this.unitCalculation = unitCalculation;
        this.company = company;
        this.amount = amount;
        this.userId = userId;
        this.baseConcept = baseConcept;
        this.baseConceptId = baseConceptId;
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

    public String getUnitCalculation() {
        return unitCalculation;
    }

    public void setUnitCalculation(String unitCalculation) {
        this.unitCalculation = unitCalculation;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isBaseConcept() {
        return baseConcept;
    }

    public void setBaseConcept(boolean baseConcept) {
        this.baseConcept = baseConcept;
    }

    public String getBaseConceptId() {
        return baseConceptId;
    }

    public void setBaseConceptId(String baseConceptId) {
        this.baseConceptId = baseConceptId;
    }
}