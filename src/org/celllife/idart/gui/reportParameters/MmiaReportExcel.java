package org.celllife.idart.gui.reportParameters;

import model.manager.reports.MmiaRegimeTerapeutico;
import model.manager.reports.MmiaStock;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.Font;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.celllife.idart.database.hibernate.Clinic;
import org.celllife.idart.database.hibernate.Prescription;
import org.celllife.idart.database.hibernate.StockCenter;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.gnu.amSpacks.app.resources.COLOR;
import org.vafada.swtcalendar.SWTCalendar;

import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class MmiaReportExcel implements IRunnableWithProgress {
    private List<MmiaStock> mmiaStocks;
    private List<MmiaRegimeTerapeutico> mmiaRegimens;
    private final Shell parent;
    private FileOutputStream out = null;
    private SWTCalendar swtCal;
    private String reportFileName;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    StockCenter pharm = null;
    Clinic clinic = null;

    private final String month;
    private final String year;

    int totalpacientestransito;
    int totalpacientesinicio;
    int totalpacientesmanter;
    int totalpacientesalterar;
    int totalpacientestransferidoDe;
    int pacientesdispensadosparaDM;
    int pacientesdispensadosparaDT;
    int pacientesdispensadosparaDS;
    int totalpacientesmanterTransporte;
    int totalpacientesppe;
    int totallinhas1;
    int totallinhas2;
    int totallinhas3;
    int totallinhasDC1;
    int totallinhasDC2;
    int totallinhasDC3;
    int totalpacientesprep;
    int totalpacientesCE;
    int totalpacientedc;
    int totalpacienteptv;
    int mesesdispensados;
    int pacientesEmTarv;
    int adultosEmTarv;
    int pediatrico04EmTARV;
    int pediatrico59EmTARV;
    int pediatrico1014EmTARV;
    int totalDSM5;
    int totalDSM4;
    int totalDSM3;
    int totalDSM2;
    int totalDSM1;
    int totalDTM2;
    int totalDTM1;
    int totalDS;
    int totalDT;
    int totalMes;
    double ajuste;
    double roundedAjuste;
    int totalGeral;

    SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");

    public MmiaReportExcel(Shell parent, String reportFileName, String month, String year, StockCenter pharm, Clinic clinic) {
        this.parent = parent;
        this.swtCal = swtCal;
        this.reportFileName = reportFileName;
        this.month = month;
        this.year = year;
        this.pharm = pharm;
        this.clinic = clinic;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        try {

            ConexaoJDBC conn = new ConexaoJDBC();
            Map<String, Object> map = new HashMap<String, Object>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            String startDayStr;
            String endDayStr;

            if (getMes(month) > 9)
                endDayStr = year + "-" + getMes(month) + "-20";
            else
                endDayStr = year + "-0" + getMes(month) + "-20";

            if (getMes(month) == 1)
                startDayStr = (Integer.parseInt(year) - 1) + "-12" + "-21";
            else if (getMes(month) < 11)
                startDayStr = year + "-0" + (getMes(month) - 1) + "-21";
            else
                startDayStr = year + "-" + (getMes(month) - 1) + "-21";

            java.sql.Date theStartDate = java.sql.Date.valueOf(startDayStr);
            java.sql.Date theEndDate = java.sql.Date.valueOf(endDayStr);

            Date startMonth1 = getRetrospectiveDate(theStartDate);
            Date endMonth1 = getRetrospectiveDate(theEndDate);

            Date startMonth2 = getRetrospectiveDate(startMonth1);
            Date endMonth2 = getRetrospectiveDate(endMonth1);

            Date startMonth3 = getRetrospectiveDate(startMonth2);
            Date endMonth3 = getRetrospectiveDate(endMonth2);

            Date startMonth4 = getRetrospectiveDate(startMonth3);
            Date endMonth4 = getRetrospectiveDate(endMonth3);

            Date startMonth5 = getRetrospectiveDate(startMonth4);
            Date endMonth5 = getRetrospectiveDate(endMonth4);

            monitor.beginTask("Por Favor, aguarde ... ", 1);

            mmiaStocks = conn.getStockMmmia(theStartDate, theEndDate, pharm);
            monitor.worked(1);
            mmiaRegimens = conn.getRegimenMmmiaActualizado(theStartDate, theEndDate);
            monitor.worked(1);

            //Total de pacientes que levantaram arv 20 a 21
            Map mapaDoMMIA = conn.MMIAACTUALIZADO(dateFormat.format(theStartDate),dateFormat.format(theEndDate), Prescription.TIPO_DOENCA_TARV, clinic);
            Map mapaDoMMIAMes5  = conn.MMIA_Actualizado_Dispensas(dateFormat.format(startMonth5), dateFormat.format(endMonth5), Prescription.TIPO_DOENCA_TARV);
            Map mapaDoMMIAMes4  = conn.MMIA_Actualizado_Dispensas(dateFormat.format(startMonth4), dateFormat.format(endMonth4), Prescription.TIPO_DOENCA_TARV);
            Map mapaDoMMIAMes3  = conn.MMIA_Actualizado_Dispensas(dateFormat.format(startMonth3), dateFormat.format(endMonth3), Prescription.TIPO_DOENCA_TARV);
            Map mapaDoMMIAMes2  = conn.MMIA_Actualizado_Dispensas(dateFormat.format(startMonth2), dateFormat.format(endMonth2), Prescription.TIPO_DOENCA_TARV);
            Map mapaDoMMIAMes1  = conn.MMIA_Actualizado_Dispensas(dateFormat.format(startMonth1), dateFormat.format(endMonth1), Prescription.TIPO_DOENCA_TARV);

             totalpacientestransito = Integer.parseInt(mapaDoMMIA.get("totalpacientestransito").toString());
             totalpacientesinicio = Integer.parseInt(mapaDoMMIA.get("totalpacientesinicio").toString());
             totalpacientesmanter = Integer.parseInt(mapaDoMMIA.get("totalpacientesmanter").toString());
             totalpacientesalterar = Integer.parseInt(mapaDoMMIA.get("totalpacientesalterar").toString());
             totalpacientestransferidoDe = Integer.parseInt(mapaDoMMIA.get("totalpacientestransferidoDe").toString());

             pacientesdispensadosparaDM = Integer.parseInt(mapaDoMMIA.get("pacientesdispensadosparaDM").toString());
             pacientesdispensadosparaDT = Integer.parseInt(mapaDoMMIA.get("pacientesdispensadosparaDT").toString());
             pacientesdispensadosparaDS = Integer.parseInt(mapaDoMMIA.get("pacientesdispensadosparaDS").toString());

             totallinhas1 = Integer.parseInt(mapaDoMMIA.get("totallinhas1").toString());
             totallinhas2 = Integer.parseInt(mapaDoMMIA.get("totallinhas2").toString());
             totallinhas3 = Integer.parseInt(mapaDoMMIA.get("totallinhas3").toString());

             totallinhasDC1 = Integer.parseInt(mapaDoMMIA.get("totallinhasDC1").toString());
             totallinhasDC2 = Integer.parseInt(mapaDoMMIA.get("totallinhasDC2").toString());
             totallinhasDC3 = Integer.parseInt(mapaDoMMIA.get("totallinhasDC3").toString());

             totalpacientesppe = Integer.parseInt(mapaDoMMIA.get("totalpacientesppe").toString());
             totalpacientesprep = Integer.parseInt(mapaDoMMIA.get("totalpacientesprep").toString());
             totalpacientesCE = Integer.parseInt(mapaDoMMIA.get("totalpacientesCE").toString());
             totalpacientedc = Integer.parseInt(mapaDoMMIA.get("totalpacientedc").toString());

             adultosEmTarv = Integer.parseInt(mapaDoMMIA.get("adultosEmTarv").toString());
             pediatrico04EmTARV = Integer.parseInt(mapaDoMMIA.get("pediatrico04EmTARV").toString());
             pediatrico59EmTARV = Integer.parseInt(mapaDoMMIA.get("pediatrico59EmTARV").toString());
             pediatrico1014EmTARV = Integer.parseInt(mapaDoMMIA.get("pediatrico1014EmTARV").toString());
             pacientesEmTarv = Integer.parseInt(mapaDoMMIA.get("pacientesEmTarv").toString());


             totalDSM5 = Integer.parseInt(mapaDoMMIAMes5.get("pacientesdispensadosparaDS").toString());
             totalDSM4 = Integer.parseInt(mapaDoMMIAMes4.get("pacientesdispensadosparaDS").toString());
             totalDSM3= Integer.parseInt(mapaDoMMIAMes3.get("pacientesdispensadosparaDS").toString());
             totalDSM2 = Integer.parseInt(mapaDoMMIAMes2.get("pacientesdispensadosparaDS").toString());
             totalDSM1 = Integer.parseInt(mapaDoMMIAMes1.get("pacientesdispensadosparaDS").toString());
             totalDTM2 = Integer.parseInt(mapaDoMMIAMes2.get("pacientesdispensadosparaDT").toString());
             totalDTM1 = Integer.parseInt(mapaDoMMIAMes1.get("pacientesdispensadosparaDT").toString());

             totalDS = totalDSM1 + totalDSM2 + totalDSM3 + totalDSM4 + totalDSM5 + pacientesdispensadosparaDS;
             totalDT = totalDTM1 + totalDTM2 + pacientesdispensadosparaDT;

             totalMes = pacientesdispensadosparaDM + pacientesdispensadosparaDT  + pacientesdispensadosparaDS;
             totalGeral = totalDS + totalDT + pacientesdispensadosparaDM;

             ajuste = 0;
             roundedAjuste = 0.0;

            if(totalMes > 0){
                ajuste = (double) totalGeral / totalMes * 100;
            }

            roundedAjuste = (double) Math.round(ajuste * 100) / 100;

            if (mmiaStocks.size() > 0) {
                // Tell the user what you are doing
                monitor.beginTask("Carregando a lista... ", mmiaStocks.size());

                FileInputStream currentXls = new FileInputStream(reportFileName);

                HSSFWorkbook workbook = new HSSFWorkbook(currentXls);

                HSSFSheet sheet = workbook.getSheetAt(0);

                HSSFFont font = workbook.createFont();
                font.setBold(true);

                HSSFCellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setBorderBottom(BorderStyle.THIN);
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderLeft(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                HSSFCellStyle cellStyleAlignRight = workbook.createCellStyle();
                cellStyleAlignRight.setBorderBottom(BorderStyle.THIN);
                cellStyleAlignRight.setBorderTop(BorderStyle.THIN);
                cellStyleAlignRight.setBorderLeft(BorderStyle.THIN);
                cellStyleAlignRight.setBorderRight(BorderStyle.THIN);
                cellStyleAlignRight.setAlignment(HorizontalAlignment.LEFT);
                cellStyleAlignRight.setVerticalAlignment(VerticalAlignment.CENTER);

                HSSFCellStyle cellStyleFixedTotals = workbook.createCellStyle();
                cellStyleFixedTotals.setBorderBottom(BorderStyle.THIN);
                cellStyleFixedTotals.setBorderTop(BorderStyle.THIN);
                cellStyleFixedTotals.setBorderLeft(BorderStyle.THIN);
                cellStyleFixedTotals.setBorderRight(BorderStyle.THIN);
                cellStyleFixedTotals.setAlignment(HorizontalAlignment.CENTER);
                cellStyleFixedTotals.setVerticalAlignment(VerticalAlignment.CENTER);
                cellStyleFixedTotals.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
                cellStyleFixedTotals.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                cellStyleFixedTotals.setFont(font);

                HSSFRow reportStartDate = sheet.getRow(1);
                HSSFCell reportStartDateCell = reportStartDate.createCell(9);
                reportStartDateCell.setCellValue(sdf.format(theStartDate));
                reportStartDateCell.setCellStyle(cellStyle);

                HSSFRow reportEndDate = sheet.getRow(2);
                HSSFCell reportEndDateCell = reportEndDate.createCell(9);
                reportEndDateCell.setCellValue(sdf.format(theEndDate));
                reportEndDateCell.setCellStyle(cellStyle);

                HSSFRow healthFacility = sheet.getRow(3);
                HSSFCell healthFacilityCell = healthFacility.createCell(3);
                healthFacilityCell.setCellValue(LocalObjects.currentClinic.getClinicName());
                healthFacilityCell.setCellStyle(cellStyle);

                HSSFRow reportYear = sheet.getRow(3);
                HSSFCell reportYearCell = reportYear.createCell(9);
                reportYearCell.setCellValue(sdfYear.format(theEndDate));
                reportYearCell.setCellStyle(cellStyle);

                for (int i = 6; i < findRow(sheet, "REGIME TERAP"); i++) {
                    HSSFRow row = sheet.getRow(i);
                    deleteRow(sheet, row);
                }

                for (int i = findRow(sheet, "REGIME TERAP") + 1; i < sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    if (row != null) {
                        deleteRow(sheet, row);
                    }
                }

                out = new FileOutputStream(new File(reportFileName));
                workbook.write(out);

                int i = 1;
                monitor.subTask("Carregando : " + i + " de " + 3 + "...");
                fillOutStrocks(sheet, cellStyle, cellStyleAlignRight, 6);
                Thread.sleep(5);
                monitor.worked(1);

                int rowNumber = mmiaStocks.size() + 6;
                HSSFRow centerRow = null;

                if (sheet.getRow(rowNumber) == null)
                    centerRow = sheet.createRow(rowNumber);
                else
                    centerRow = sheet.getRow(rowNumber);

                HSSFCell createCellCodeTiltle = centerRow.createCell(1);
                createCellCodeTiltle.setCellValue("Código");
                createCellCodeTiltle.setCellStyle(cellStyleFixedTotals);

                HSSFCell createCellRegimeTerapeuticoTiltle = centerRow.createCell(2);
                createCellRegimeTerapeuticoTiltle.setCellValue("REGIME TERAPÊUTICO");
                createCellRegimeTerapeuticoTiltle.setCellStyle(cellStyleFixedTotals);

                HSSFCell createCelltotalPacienteTiltle = centerRow.createCell(3);
                createCelltotalPacienteTiltle.setCellValue("Total Doentes");
                createCelltotalPacienteTiltle.setCellStyle(cellStyleFixedTotals);

                HSSFCell createCellTotalFarmaciaComunitariaTiltle = centerRow.createCell(4);
                createCellTotalFarmaciaComunitariaTiltle.setCellValue("Farmacia Comunitária");
                createCellTotalFarmaciaComunitariaTiltle.setCellStyle(cellStyleFixedTotals);

                HSSFCell createCellTotalFarmaciaUnidadeSanitariaTiltle = centerRow.createCell(5);
                createCellTotalFarmaciaUnidadeSanitariaTiltle.setCellValue("Farmácia da Unidade Sanitária");
                createCellTotalFarmaciaUnidadeSanitariaTiltle.setCellStyle(cellStyleFixedTotals);

                HSSFCell createCellPacienntesTARVTiltle = centerRow.createCell(6);
                createCellPacienntesTARVTiltle.setCellValue("Tipo de doentes em TARV");
                createCellPacienntesTARVTiltle.setCellStyle(cellStyleFixedTotals);


                monitor.subTask("Carregando : " + i++ + " de " + 3 + "...");
                fillOutStatisticResume(sheet, cellStyle, cellStyleFixedTotals, cellStyleAlignRight, mmiaStocks.size() + 7);
                Thread.sleep(5);
                monitor.worked(1);

                monitor.subTask("Carregando : " + i++ + " de " + 3 + "...");
                fillOutRegimen(sheet, workbook, cellStyle, cellStyleAlignRight, mmiaStocks.size() + 7);
                Thread.sleep(5);
                // Optionally add subtasks

                // Tell the monitor that you successfully finished one item of "workload"-many
                monitor.worked(1);
                // Check if the user pressed "cancel"
                if (monitor.isCanceled()) {
                    monitor.done();
                    return;
                }

                monitor.done();
                currentXls.close();

                FileOutputStream outputStream = new FileOutputStream(new File(reportFileName));
                workbook.write(outputStream);
                workbook.close();

                Desktop.getDesktop().open(new File(reportFileName));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public List<MmiaStock> getList() {
        return this.mmiaStocks;
    }

    private void deleteRow(HSSFSheet sheet, Row row) {
        int lastRowNum = sheet.getLastRowNum();
        if (lastRowNum > 0) {
            int rowIndex = row.getRowNum();
            HSSFRow removingRow = sheet.getRow(rowIndex);
            if (removingRow != null) {
                sheet.removeRow(removingRow);
            }
        }
    }

    private static int findRow(HSSFSheet sheet, String cellContent) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    if (cell.getRichStringCellValue().getString().trim().contains(cellContent)) {
                        return row.getRowNum();
                    }
                }
            }
        }
        return 0;
    }

    public void fillOutStrocks(HSSFSheet sheet, HSSFCellStyle cellStyle, HSSFCellStyle cellStyleAlignRight, int rowNum) {
        int i = 0;
        for (MmiaStock xls : mmiaStocks) {
            i++;

            HSSFRow row = sheet.createRow(rowNum++);

            HSSFCell createCellFNMCode = row.createCell(1);
            createCellFNMCode.setCellValue(xls.getFnm());
            createCellFNMCode.setCellStyle(cellStyleAlignRight);

            HSSFCell createCellMedicamento = row.createCell(2);
            createCellMedicamento.setCellValue(xls.getMedicamento());
            createCellMedicamento.setCellStyle(cellStyleAlignRight);

            HSSFCell createCellQuantidadeFrasco = row.createCell(3);
            createCellQuantidadeFrasco.setCellValue(xls.getQuantidadeFrasco());
            createCellQuantidadeFrasco.setCellStyle(cellStyle);

            HSSFCell createCellSaldo = row.createCell(4);
            createCellSaldo.setCellValue(xls.getSaldo());
            createCellSaldo.setCellStyle(cellStyle);

            HSSFCell createCellEntradas = row.createCell(5);
            createCellEntradas.setCellValue(xls.getEntrdas());
            createCellEntradas.setCellStyle(cellStyle);

            HSSFCell createCellSaidas = row.createCell(6);
            createCellSaidas.setCellValue(xls.getSaidas());
            createCellSaidas.setCellStyle(cellStyle);

            HSSFCell createCellAjuste = row.createCell(7);
            createCellAjuste.setCellValue(xls.getPerdasAjustes());
            createCellAjuste.setCellStyle(cellStyle);

            HSSFCell createCellIventario = row.createCell(8);
            createCellIventario.setCellValue(xls.getInventario());
            createCellIventario.setCellStyle(cellStyle);

            HSSFCell createCellValidade = row.createCell(9);
            createCellValidade.setCellValue(xls.getValidade());
            createCellValidade.setCellStyle(cellStyle);

        }

        for (int i0 = 1; i0 < MmiaStock.class.getClass().getDeclaredFields().length; i0++) {
            sheet.autoSizeColumn(i0);
        }
    }

    public void fillOutRegimen(HSSFSheet sheet, HSSFWorkbook workbook, HSSFCellStyle cellStyle, HSSFCellStyle cellStyleAlignRight, int rowNum) {
        int i = 0;
        int totalDoentes = 0;
        int totalDoentesDC = 0;
        int totalDoentesUS =0;
        HSSFRow row = null;

        HSSFFont font = workbook.createFont();
        font.setBold(true);

        HSSFCellStyle cellStyleAlignRLeft = workbook.createCellStyle();
        cellStyleAlignRLeft.setBorderBottom(BorderStyle.THIN);
        cellStyleAlignRLeft.setBorderTop(BorderStyle.THIN);
        cellStyleAlignRLeft.setBorderLeft(BorderStyle.THIN);
        cellStyleAlignRLeft.setBorderRight(BorderStyle.THIN);
        cellStyleAlignRLeft.setAlignment(HorizontalAlignment.RIGHT);
        cellStyleAlignRLeft.setVerticalAlignment(VerticalAlignment.CENTER);

        HSSFCellStyle cellStyleTotals = workbook.createCellStyle();
        cellStyleTotals.setBorderBottom(BorderStyle.THIN);
        cellStyleTotals.setBorderTop(BorderStyle.THIN);
        cellStyleTotals.setBorderLeft(BorderStyle.THIN);
        cellStyleTotals.setBorderRight(BorderStyle.THIN);
        cellStyleTotals.setAlignment(HorizontalAlignment.CENTER);
        cellStyleTotals.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyleTotals.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
        cellStyleTotals.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyleTotals.setFont(font);


        for (MmiaRegimeTerapeutico xls : mmiaRegimens) {
            i = rowNum++;

            if (sheet.getRow(i) == null)
                row = sheet.createRow(i);
            else
                row = sheet.getRow(i);

            HSSFCell createCellCode = row.createCell(1);
            createCellCode.setCellValue(xls.getCodigo());
            createCellCode.setCellStyle(cellStyleAlignRight);

            HSSFCell createCellRegimeTerapeutico = row.createCell(2);
            createCellRegimeTerapeutico.setCellValue(xls.getRegimeTerapeutico());
            createCellRegimeTerapeutico.setCellStyle(cellStyleAlignRight);

            HSSFCell createCelltotalPaciente = row.createCell(3);
            createCelltotalPaciente.setCellValue(xls.getTotalDoentes());
            createCelltotalPaciente.setCellStyle(cellStyle);

            HSSFCell createCellTotalFarmaciaComunitaria = row.createCell(4);
            createCellTotalFarmaciaComunitaria.setCellValue(xls.getTotalDoentesFarmaciaComunitaria());
            createCellTotalFarmaciaComunitaria.setCellStyle(cellStyle);

            HSSFCell createCellTotalFarmaciaUnidadeSaniaria = row.createCell(5);
            createCellTotalFarmaciaUnidadeSaniaria.setCellValue(xls.getTotalDoentesUnidadeSanitaria());
            createCellTotalFarmaciaUnidadeSaniaria.setCellStyle(cellStyle);

            totalDoentes = totalDoentes + Integer.parseInt(xls.getTotalDoentes()) - Integer.parseInt(xls.getTotalDoentesPREP());
            totalDoentesDC = totalDoentesDC + Integer.parseInt(xls.getTotalDoentesFarmaciaComunitaria());
            totalDoentesUS = totalDoentesUS + Integer.parseInt(xls.getTotalDoentesUnidadeSanitaria());
        }

        // Last Row
        int lastrow = ++i;

        for (int g = lastrow; g <= lastrow + 4; g++) {
            if (sheet.getRow(g) == null)
                row = sheet.createRow(g);
            else
                row = sheet.getRow(g);
            if (lastrow == g)
                fillOutTotals(row, cellStyleTotals, cellStyle, "Total Doentes", String.valueOf(totalDoentes), String.valueOf(totalDoentesDC));
            else if (lastrow + 1 == g)
                fillOutTotals(row, cellStyleAlignRLeft, cellStyle, "1as Linhas", String.valueOf(totallinhas1), String.valueOf(totallinhasDC1));
            else if (lastrow + 2 == g)
                fillOutTotals(row, cellStyleAlignRLeft, cellStyle, "2as Linhas", String.valueOf(totallinhas2), String.valueOf(totallinhasDC2));
            else if (lastrow + 3 == g)
                fillOutTotals(row, cellStyleAlignRLeft, cellStyle, "3as Linhas", String.valueOf(totallinhas3), String.valueOf(totallinhasDC3));
            else if (lastrow + 4 == g)
                fillOutTotals(row, cellStyleTotals, cellStyle, "Total Linhas", String.valueOf(totallinhas1 + totallinhas2 + totallinhas3), String.valueOf(totallinhasDC1 + totallinhasDC2 + totallinhasDC3));
        }

        for (int i0 = 1; i0 < MmiaRegimeTerapeutico.class.getClass().getDeclaredFields().length; i0++) {
            sheet.autoSizeColumn(i0);
        }
    }

    public void fillOutStatisticResume(HSSFSheet sheet, HSSFCellStyle cellStyle, HSSFCellStyle cellStyleLabel, HSSFCellStyle cellStyleAlignRight, int rowNum) {
        int a;

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyleAlignRight, "Novos");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(totalpacientesinicio));

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyleAlignRight, "Manutenção");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(totalpacientesmanter));

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyleAlignRight, "Alteração");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(totalpacientesalterar));

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyleAlignRight, "Trânsito");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(totalpacientestransito));

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyleAlignRight, "Transferências");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(totalpacientestransferidoDe));

        fillOutOthers(sheet.createRow(rowNum++).createCell(5), cellStyleLabel, "Faixa Etária dos Pacientes TARV");

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyleAlignRight, "Adultos");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(adultosEmTarv));

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyleAlignRight, "Pediátricos 0 aos 4 anos");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(pediatrico04EmTARV));

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyleAlignRight, "Pediátricos 5 aos 9 anos");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(pediatrico59EmTARV));

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyleAlignRight, "Pediátricos 10 aos 14 anos");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(pediatrico1014EmTARV));

        fillOutOthers(sheet.createRow(rowNum++).createCell(5), cellStyleLabel, "Profilaxia");

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyleAlignRight, "PPE");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(totalpacientesppe));

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyleAlignRight, "PrEP");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(totalpacientesprep));

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyleAlignRight, "Criança Exposta");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(totalpacientesCE));

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyleAlignRight, "Total de pacientes em TARV na US:");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, "Ver Resumo Mensal");

        fillOutOthers(sheet.createRow(rowNum++).createCell(5), cellStyleLabel, "Tipo de Dispensa");

        fillOutOthers(sheet.createRow(rowNum++).createCell(6), cellStyleLabel, "Dispensa Semestral (DS)");

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyle, "Mês - 5");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(totalDSM5));
        fillOutOthers(sheet.getRow(a).createCell(8), cellStyleLabel, "Ajuste");
        fillOutOthers(sheet.getRow(a).createCell(9), cellStyle, roundedAjuste+" %");

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyle, "Mês - 4");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(totalDSM4));

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyle, "Mês - 3");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(totalDSM3));
        fillOutOthers(sheet.getRow(a).createCell(7), cellStyleLabel, "Dispensa Trimestral (DT)");

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyle, "Mês - 2");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(totalDSM2));
        fillOutOthers(sheet.getRow(a).createCell(7), cellStyle, String.valueOf(totalDTM2));

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyle, "Mês - 1");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(totalDSM1));
        fillOutOthers(sheet.getRow(a).createCell(7), cellStyle, String.valueOf(totalDTM1));
        fillOutOthers(sheet.getRow(a).createCell(8), cellStyleLabel, "Dispensa Mensal (DM)");
        fillOutOthers(sheet.getRow(a).createCell(9), cellStyleLabel, "Total");

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyle, "No Mês");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(pacientesdispensadosparaDS));
        fillOutOthers(sheet.getRow(a).createCell(7), cellStyle, String.valueOf(pacientesdispensadosparaDT));
        fillOutOthers(sheet.getRow(a).createCell(8), cellStyle, String.valueOf(pacientesdispensadosparaDM));
        fillOutOthers(sheet.getRow(a).createCell(9), cellStyle, String.valueOf(totalMes));

        fillOutOthers(sheet.createRow(a = rowNum++).createCell(5), cellStyleLabel, "Total de Pacientes c/ Tratamento");
        fillOutOthers(sheet.getRow(a).createCell(6), cellStyle, String.valueOf(totalDS));
        fillOutOthers(sheet.getRow(a).createCell(7), cellStyle, String.valueOf(totalDT));
        fillOutOthers(sheet.getRow(a).createCell(8), cellStyle, String.valueOf(pacientesdispensadosparaDM));
        fillOutOthers(sheet.getRow(a).createCell(9), cellStyle, String.valueOf(totalGeral));

        fillOutOthers(sheet.createRow(rowNum++).createCell(5), cellStyleAlignRight, " Observações: ");

        sheet.autoSizeColumn(5);

    }

    public void fillOutOthers(HSSFCell row, HSSFCellStyle cellStyleRight, String cellName) {
        row.setCellValue(cellName);
        row.setCellStyle(cellStyleRight);
    }

    public void fillOutTotals(HSSFRow row, HSSFCellStyle cellStyle, HSSFCellStyle cellStyleRight, String cellName, String cellValue1, String cellValue2) {

        HSSFCell createCellEmpty1 = row.createCell(1);
        createCellEmpty1.setCellStyle(cellStyle);

        HSSFCell createCell = row.createCell(2);
        createCell.setCellValue(cellName);
        createCell.setCellStyle(cellStyle);

        HSSFCell createCellValue = row.createCell(3);
        createCellValue.setCellValue(cellValue1);
        createCellValue.setCellStyle(cellStyleRight);

        HSSFCell createCellEmpty2 = row.createCell(4);
        createCellEmpty2.setCellValue(cellValue2);
        createCellEmpty2.setCellStyle(cellStyleRight);

    }

    private int getMes(String mesString) {
        int mes = 0;

        switch (mesString) {
            case "Janeiro":
                mes = 1;
                break;
            case "Fevereiro":
                mes = 2;
                break;
            case "Março":
                mes = 3;
                break;
            case "Abril":
                mes = 4;
                break;
            case "Maio":
                mes = 5;
                break;
            case "Junho":
                mes = 6;
                break;
            case "Julho":
                mes = 7;
                break;
            case "Agosto":
                mes = 8;
                break;
            case "Setembro":
                mes = 9;
                break;
            case "Outubro":
                mes = 10;
                break;
            case "Novembro":
                mes = 11;
                break;
            case "Dezembro":
                mes = 12;
                break;

            default:
                mes = 0;
                break;

        }

        return mes;
    }

    private Date getRetrospectiveDate(Date date){

        Calendar retroDate = Calendar.getInstance();
        retroDate.setTime(date);
        retroDate.add(Calendar.MONTH, -1);

        return retroDate.getTime();

    }

}
