package org.celllife.idart.gui.reportParameters;

import model.manager.AdministrationManager;
import model.manager.reports.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.celllife.idart.database.hibernate.Clinic;
import org.celllife.idart.database.hibernate.StockCenter;
import org.celllife.idart.database.hibernate.User;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.gnu.amSpacks.app.resources.COLOR;
import org.vafada.swtcalendar.SWTCalendar;

import javax.swing.plaf.basic.BasicLookAndFeel;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MmiaReportMISAUExcel implements IRunnableWithProgress {
    private List<MmiaStock> mmiaStocks;
    private List<MmiaRegimeTerapeutico> mmiaRegimens;
    private final Shell parent;
    private FileOutputStream out = null;
    private SWTCalendar swtCal;
    private String reportFileName;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    StockCenter pharm = null;
    Clinic c = null;
    private Date theStartDate;
    private Date theEndDate;

    int totalpacientestransito;
    int totalpacientesinicio;
    int totalpacientesmanter;
    int totalpacientesalterar;
    int totalpacientestransferidoDe;
    int mesesdispensadosparaDM;
    int mesesdispensadosparaDT;
    int mesesdispensadosparaDS;
    int totalpacientesmanterTransporte;
    int totalpacientesppe;
    int totallinhas1;
    int totallinhas2;
    int totallinhas3;
    int totalpacientesprep;
    int totalpacientesCE;
    int totalpacienteptv;
    int mesesdispensados;
    int pacientesEmTarv;
    int adultosEmTarv;
    int pediatrico04EmTARV;
    int pediatrico59EmTARV;
    int pediatrico1014EmTARV;

    SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");

    public MmiaReportMISAUExcel(Shell parent, String reportFileName, Date theStartDate, Date theEndDate, StockCenter pharm, Clinic c) {
        this.parent = parent;
        this.swtCal = swtCal;
        this.reportFileName = reportFileName;
        this.theStartDate = theStartDate;
        this.theEndDate = theEndDate;
        this.pharm = pharm;
        this.c = c;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

        try {

            ConexaoJDBC con = new ConexaoJDBC();
            Map<String, Object> map = new HashMap<String, Object>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            monitor.beginTask("Por Favor, aguarde ... ", 1);

            mmiaStocks = con.getStockMmmia(theStartDate, theEndDate, pharm);
            monitor.worked(1);
            mmiaRegimens = con.getRegimenMmmia(theStartDate, theEndDate);
            monitor.worked(1);
            Map mapaDoMMIA = con.MMIA(dateFormat.format(theStartDate), dateFormat.format(theEndDate), c.getId());

            totalpacientestransito = Integer.parseInt(mapaDoMMIA.get("totalpacientestransito").toString());
            totalpacientesinicio = Integer.parseInt(mapaDoMMIA.get("totalpacientesinicio").toString());
            totalpacientesmanter = Integer.parseInt(mapaDoMMIA.get("totalpacientesmanter").toString());
            totalpacientesmanterTransporte = Integer.parseInt(mapaDoMMIA.get("totalpacientesmanterTransporte").toString());
            totalpacientesalterar = Integer.parseInt(mapaDoMMIA.get("totalpacientesalterar").toString());
            totalpacientestransferidoDe = Integer.parseInt(mapaDoMMIA.get("totalpacientestransferidoDe").toString());

            mesesdispensadosparaDM = Integer.parseInt(mapaDoMMIA.get("mesesdispensadosparaDM").toString());
            mesesdispensadosparaDT = Integer.parseInt(mapaDoMMIA.get("mesesdispensadosparaDT").toString());
            mesesdispensadosparaDS = Integer.parseInt(mapaDoMMIA.get("mesesdispensadosparaDS").toString());
            mesesdispensados = Integer.parseInt(mapaDoMMIA.get("mesesdispensados").toString());

            adultosEmTarv = Integer.parseInt(mapaDoMMIA.get("adultosEmTarv").toString());
            pediatrico04EmTARV = Integer.parseInt(mapaDoMMIA.get("pediatrico04EmTARV").toString());
            pediatrico59EmTARV = Integer.parseInt(mapaDoMMIA.get("pediatrico59EmTARV").toString());
            pediatrico1014EmTARV = Integer.parseInt(mapaDoMMIA.get("pediatrico1014EmTARV").toString());

            totalpacientesppe = Integer.parseInt(mapaDoMMIA.get("totalpacientesppe").toString());
            totalpacientesprep = Integer.parseInt(mapaDoMMIA.get("totalpacientesprep").toString());
            totalpacientesCE = Integer.parseInt(mapaDoMMIA.get("totalpacientesCE").toString());
            totalpacienteptv = Integer.parseInt(mapaDoMMIA.get("totalpacienteptv").toString());

            totallinhas1 = Integer.parseInt(mapaDoMMIA.get("totallinhas1").toString());
            totallinhas2 = Integer.parseInt(mapaDoMMIA.get("totallinhas2").toString());
            totallinhas3 = Integer.parseInt(mapaDoMMIA.get("totallinhas3").toString());
            pacientesEmTarv = Integer.parseInt(mapaDoMMIA.get("pacientesEmTarv").toString());


            if (mmiaStocks.size() > 0) {
                // Tell the user what you are doing
                monitor.beginTask("Carregando a lista... ", mmiaStocks.size());

                FileInputStream currentXls = new FileInputStream(reportFileName);

                HSSFWorkbook workbook = new HSSFWorkbook(currentXls);

                HSSFSheet sheet = workbook.getSheetAt(0);

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

                HSSFRow healthFacility = sheet.getRow(3);
                HSSFCell healthFacilityCell = healthFacility.createCell(3);
                healthFacilityCell.setCellValue(LocalObjects.currentClinic.getClinicName());
                healthFacilityCell.setCellStyle(cellStyle);

                HSSFRow reportStartDate = sheet.getRow(1);
                HSSFCell reportStartDateCell = reportStartDate.createCell(9);
                reportStartDateCell.setCellValue(sdf.format(theStartDate));
                reportStartDateCell.setCellStyle(cellStyle);

                HSSFRow reportEndDate = sheet.getRow(2);
                HSSFCell reportEndDateCell = reportEndDate.createCell(9);
                reportEndDateCell.setCellValue(sdf.format(theEndDate));
                reportEndDateCell.setCellStyle(cellStyle);

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

                monitor.subTask("Carregando : " + i++ + " de " + 3 + "...");
                fillOutStatisticResume(sheet, cellStyle, cellStyleAlignRight, mmiaStocks.size() + 7);
                Thread.sleep(5);
                monitor.worked(1);

                monitor.subTask("Carregando : " + i++ + " de " + 3 + "...");
                fillOutRegimen(sheet, cellStyle, cellStyleAlignRight, mmiaStocks.size() + 7);
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

    public void fillOutRegimen(HSSFSheet sheet, HSSFCellStyle cellStyle, HSSFCellStyle cellStyleAlignRight, int rowNum) {
        int i = 0;
        int totalDoentes = 0;
        HSSFRow row = null;

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
            totalDoentes = totalDoentes + Integer.parseInt(xls.getTotalDoentes());
        }

        // Last Row

        int lastrow = ++i;

        for( int g = lastrow; g <= lastrow + 4; g++) {
            if (sheet.getRow(g) == null)
                row = sheet.createRow(g);
            else
                row = sheet.getRow(g);
            if(lastrow == g)
            fillOutTotals(row, cellStyle, cellStyle, "Total Doentes", String.valueOf(totalDoentes));
            else if(lastrow + 1 == g)
            fillOutTotals(row, cellStyleAlignRight, cellStyle, "1as Linhas", String.valueOf(totallinhas1));
            else if(lastrow + 2 == g)
            fillOutTotals(row, cellStyleAlignRight, cellStyle, "2as Linhas", String.valueOf(totallinhas2));
            else if(lastrow + 3 == g)
            fillOutTotals(row, cellStyleAlignRight, cellStyle, "3as Linhas", String.valueOf(totallinhas3));
            else if(lastrow + 4 == g)
            fillOutTotals(row, cellStyle, cellStyle, "Total Linhas", String.valueOf(totallinhas1+totallinhas2+totallinhas3));
        }

        for (int i0 = 1; i0 < MmiaRegimeTerapeutico.class.getClass().getDeclaredFields().length; i0++) {
            sheet.autoSizeColumn(i0);
        }
    }

    public void fillOutStatisticResume(HSSFSheet sheet, HSSFCellStyle cellStyle, HSSFCellStyle cellStyleAlignRight, int rowNum) {

        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Novos", String.valueOf(totalpacientesinicio));
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Manutenção", String.valueOf(totalpacientesmanter + totalpacientesmanterTransporte));
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Alteração", String.valueOf(totalpacientesalterar));
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Trânsito", String.valueOf(totalpacientestransito));
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Transferências", String.valueOf(totalpacientestransferidoDe));
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Meses de Dispensa", "");

        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Dispensa Mensal (DM)", String.valueOf(mesesdispensadosparaDM));
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Dispensa para 3 Meses (DT)", String.valueOf(mesesdispensadosparaDT));
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Dispensa para 6 Meses (DS)", String.valueOf(mesesdispensadosparaDS));
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "# Meses de terapêutica dispensados", String.valueOf(mesesdispensadosparaDS+mesesdispensadosparaDT+mesesdispensadosparaDM));
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Faixa Etária dos Pacientes TARV", "");

        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Adultos", String.valueOf(adultosEmTarv));
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Pediátricos 0 aos 4 anos", String.valueOf(pediatrico04EmTARV));
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Pediátricos 5 aos 9 anos", String.valueOf(pediatrico59EmTARV));
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Pediátricos 10 aos 15 anos", String.valueOf(pediatrico1014EmTARV));
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Profilaxia", "");

        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "PPE", String.valueOf(totalpacientesppe));
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "PrEP", String.valueOf(totalpacientesprep));
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Criança Exposta", String.valueOf(totalpacientesCE));
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, "Total de pacientes em TARV na US:", "Ver Resumo Mensal");
        fillOutOthers(sheet.createRow(rowNum++), cellStyle, cellStyleAlignRight, " Observações: ", " ");

    }

    public void fillOutOthers(HSSFRow row, HSSFCellStyle cellStyle, HSSFCellStyle cellStyleRight, String cellName, String cellValue) {

        HSSFCell createCell = row.createCell(5);
        createCell.setCellValue(cellName);
        createCell.setCellStyle(cellStyleRight);

        HSSFCell createCellValue = row.createCell(6);
        createCellValue.setCellValue(cellValue);
        createCellValue.setCellStyle(cellStyle);

        HSSFCell createCellEmpty = row.createCell(7);
        createCellEmpty.setCellStyle(cellStyle);

        HSSFCell createCellEmpty2 = row.createCell(8);
        createCellEmpty2.setCellStyle(cellStyle);

        HSSFCell createCellEmpty3 = row.createCell(9);
        createCellEmpty3.setCellStyle(cellStyle);

    }

    public void fillOutTotals(HSSFRow row, HSSFCellStyle cellStyle, HSSFCellStyle cellStyleRight, String cellName, String cellValue) {

        HSSFCell createCellEmpty1 = row.createCell(1);
        createCellEmpty1.setCellStyle(cellStyle);

        HSSFCell createCell = row.createCell(2);
        createCell.setCellValue(cellName);
        createCell.setCellStyle(cellStyle);

        HSSFCell createCellValue = row.createCell(3);
        createCellValue.setCellValue(cellValue);
        createCellValue.setCellStyle(cellStyleRight);

        HSSFCell createCellEmpty2 = row.createCell(4);
        createCellEmpty2.setCellStyle(cellStyle);

    }

}
