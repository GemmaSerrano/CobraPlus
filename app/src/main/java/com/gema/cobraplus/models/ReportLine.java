package com.gema.cobraplus.models;

//Representa cada línea del informe que se muestra en SeeReport incluye cabecera y títulos
public class ReportLine {

    private String conceptName;
    private double quantity;
    private double subtotal;
    private String unitCalculation;
    private boolean sectionHeader;
    private String sectionTitle;

    public ReportLine() {
    }

    public ReportLine(String conceptName, double quantity, double subtotal, String unitCalculation) {
        this.conceptName = conceptName;
        this.quantity = quantity;
        this.subtotal = subtotal;
        this.unitCalculation = unitCalculation;
        this.sectionHeader = false;
        this.sectionTitle = "";
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public String getUnitCalculation() {
        return unitCalculation;
    }

    public void setUnitCalculation(String unitCalculation) {
        this.unitCalculation = unitCalculation;
    }

    public boolean isSectionHeader() {
        return sectionHeader;
    }

    public void setSectionHeader(boolean sectionHeader) {
        this.sectionHeader = sectionHeader;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }
}
