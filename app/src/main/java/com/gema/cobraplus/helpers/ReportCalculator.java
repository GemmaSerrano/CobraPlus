package com.gema.cobraplus.helpers;

import com.gema.cobraplus.models.Concept;
import com.gema.cobraplus.models.Record;
import com.gema.cobraplus.models.Report;
import com.gema.cobraplus.models.ReportLine;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class ReportCalculator {

    private Report currentReport;
    private ArrayList<Record> recordList;
    private ArrayList<Concept> conceptList;

    public ReportCalculator(Report currentReport, ArrayList<Record> recordList, ArrayList<Concept> conceptList) {
        this.currentReport = currentReport;
        this.recordList = recordList;
        this.conceptList = conceptList;
    }

    // Decide qué tipo de agrupación usar
    public ArrayList<ReportLine> generateLines() {
        String groupBy = currentReport.getGroupBy();

        if (groupBy == null || groupBy.trim().isEmpty() || groupBy.equals("Concepto")) {
            return createLinesByConcept();
        }

        if (groupBy.equals("Empresa")) {
            return createLinesByCompany();
        }

        if (groupBy.equals("Fecha")) {
            return createLinesByDate();
        }

        return createLinesWithoutGrouping();
    }

    // Agrupa por concepto
    private ArrayList<ReportLine> createLinesByConcept() {
        ArrayList<ReportLine> lines = new ArrayList<>();

        for (Record record : recordList) {
            if (!recordBelongsToReport(record)) continue;

            Concept concept = findConceptById(record.getConceptId());
            if (concept == null) continue;

            addOrUpdateLine(lines, concept, record);
        }

        return lines;
    }

    // Agrupa por empresa
    private ArrayList<ReportLine> createLinesByCompany() {
        ArrayList<ReportLine> finalLines = new ArrayList<>();
        ArrayList<String> companyList = new ArrayList<>();

        // Obtener empresas únicas
        for (Record record : recordList) {
            if (!recordBelongsToReport(record)) continue;

            Concept concept = findConceptById(record.getConceptId());
            if (concept == null) continue;

            String company = concept.getCompany();

            if (company != null && !company.trim().isEmpty() && !companyList.contains(company)) {
                companyList.add(company);
            }
        }

        Collections.sort(companyList);

        // Crear bloque por cada empresa
        for (String company : companyList) {
            addSectionHeader(finalLines, "Empresa: " + company);

            ArrayList<ReportLine> tempLines = new ArrayList<>();

            for (Record record : recordList) {
                if (!recordBelongsToReport(record)) continue;

                Concept concept = findConceptById(record.getConceptId());
                if (concept == null) continue;

                if (concept.getCompany() == null) continue;
                if (!company.equals(concept.getCompany())) continue;

                addOrUpdateLine(tempLines, concept, record);
            }

            finalLines.addAll(tempLines);
        }

        return finalLines;
    }

    // Agrupa por fecha
    private ArrayList<ReportLine> createLinesByDate() {
        ArrayList<ReportLine> finalLines = new ArrayList<>();
        ArrayList<Date> dateList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Obtener fechas únicas
        for (Record record : recordList) {
            if (!recordBelongsToReport(record)) continue;
            if (record.getDate() == null) continue;

            Date recordDate = record.getDate();

            boolean exists = false;
            for (Date date : dateList) {
                if (sameDay(date, recordDate)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                dateList.add(recordDate);
            }
        }

        Collections.sort(dateList);

        // Crear bloque por cada fecha
        for (Date date : dateList) {
            String dateText = sdf.format(date);
            addSectionHeader(finalLines, "Fecha: " + dateText);

            ArrayList<ReportLine> tempLines = new ArrayList<>();

            for (Record record : recordList) {
                if (!recordBelongsToReport(record)) continue;
                if (record.getDate() == null) continue;
                if (!sameDay(record.getDate(), date)) continue;

                Concept concept = findConceptById(record.getConceptId());
                if (concept == null) continue;

                addOrUpdateLine(tempLines, concept, record);
            }

            finalLines.addAll(tempLines);
        }

        return finalLines;
    }

    // Sin agrupar: una línea por cada registro
    private ArrayList<ReportLine> createLinesWithoutGrouping() {
        ArrayList<ReportLine> lines = new ArrayList<>();

        for (Record record : recordList) {
            if (!recordBelongsToReport(record)) continue;

            Concept concept = findConceptById(record.getConceptId());
            if (concept == null) continue;

            double quantity = record.getQuantity() != null ? record.getQuantity() : 0;
            double subtotal = quantity * concept.getAmount();

            ReportLine line = new ReportLine();
            line.setConceptName(concept.getName());
            line.setQuantity(quantity);
            line.setSubtotal(subtotal);
            line.setUnitCalculation(concept.getUnitCalculation());

            lines.add(line);
        }

        return lines;
    }

    // Comprueba si el registro entra dentro del informe
    private boolean recordBelongsToReport(Record record) {
        if (record == null || record.getDate() == null) return false;
        if (currentReport == null) return false;
        if (currentReport.getStartDate() == null || currentReport.getEndDate() == null)
            return false;

        Date recordDate = record.getDate();

        boolean insidePeriod =
                !recordDate.before(currentReport.getStartDate()) &&
                        !recordDate.after(currentReport.getEndDate());

        if (!insidePeriod) return false;

        // Si el informe filtra por una empresa concreta
        if (currentReport.getCompany() != null && !currentReport.getCompany().trim().isEmpty()) {
            Concept concept = findConceptById(record.getConceptId());

            if (concept == null || concept.getCompany() == null) return false;

            return currentReport.getCompany().equals(concept.getCompany());
        }

        return true;
    }

    // Busca un concepto por su id
    private Concept findConceptById(String conceptId) {
        for (Concept concept : conceptList) {
            if (concept.getId() != null && concept.getId().equals(conceptId)) {
                return concept;
            }
        }
        return null;
    }

    // Añade una sección tipo "Empresa: X" o "Fecha: X"
    private void addSectionHeader(ArrayList<ReportLine> list, String title) {
        ReportLine section = new ReportLine();
        section.setSectionHeader(true);
        section.setSectionTitle(title);
        list.add(section);
    }

    // Añade una línea nueva o suma a una existente
    private void addOrUpdateLine(ArrayList<ReportLine> list, Concept concept, Record record) {
        double quantity = record.getQuantity() != null ? record.getQuantity() : 0;
        double subtotal = quantity * concept.getAmount();

        for (ReportLine line : list) {
            if (!line.isSectionHeader() && line.getConceptName().equals(concept.getName())) {
                line.setQuantity(line.getQuantity() + quantity);
                line.setSubtotal(line.getSubtotal() + subtotal);
                return;
            }
        }

        ReportLine newLine = new ReportLine();
        newLine.setConceptName(concept.getName());
        newLine.setQuantity(quantity);
        newLine.setSubtotal(subtotal);
        newLine.setUnitCalculation(concept.getUnitCalculation());

        list.add(newLine);
    }

    // Calcula el total final del informe
    public double calculateTotal(ArrayList<ReportLine> reportLineList) {
        double total = 0;

        for (ReportLine line : reportLineList) {
            if (!line.isSectionHeader()) {
                total += line.getSubtotal();
            }
        }

        return total;
    }

    // Comprueba si dos fechas son el mismo día
    private boolean sameDay(Date d1, Date d2) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(d1).equals(sdf.format(d2));
    }
}