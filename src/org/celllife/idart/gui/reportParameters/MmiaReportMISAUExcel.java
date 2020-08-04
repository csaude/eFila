package org.celllife.idart.gui.reportParameters;

import model.manager.AdministrationManager;
import model.manager.reports.AbsenteeForSupportCall;
import model.manager.reports.LivroRegistoDiarioXLS;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.celllife.idart.database.hibernate.StockCenter;
import org.celllife.idart.database.hibernate.User;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.vafada.swtcalendar.SWTCalendar;

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

    private final Shell parent;
    private FileOutputStream out = null;
    private SWTCalendar swtCal;
    private String reportFileName;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    StockCenter pharm = null;
    private Date theStartDate;
    private Date theEndDate;

    SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");

    public MmiaReportMISAUExcel(Shell parent, String reportFileName, Date theStartDate, Date theEndDate, StockCenter pharm) {
        this.parent = parent;
        this.swtCal = swtCal;
        this.reportFileName = reportFileName;
        this.theStartDate = theStartDate;
        this.theEndDate = theEndDate;
        this.pharm = pharm;
    }

//    @Override
//    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//        try {
//
//            ConexaoJDBC con = new ConexaoJDBC();
//            Map<String, Object> map = new HashMap<String, Object>();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//            monitor.beginTask("Por Favor, aguarde ... ", 1);
//
//            Map mapaDoMMIA = con.MMIA(dateFormat.format(theStartDate), dateFormat.format(theEndDate));
//
//            int totalpacientestransito = Integer.parseInt(mapaDoMMIA.get("totalpacientestransito").toString());
//            int totalpacientesinicio = Integer.parseInt(mapaDoMMIA.get("totalpacientesinicio").toString());
//            int totalpacientesmanter = Integer.parseInt(mapaDoMMIA.get("totalpacientesmanter").toString());
//            int totalpacientesalterar = Integer.parseInt(mapaDoMMIA.get("totalpacientesalterar").toString());
//            int totalpacientestransferidoDe = Integer.parseInt(mapaDoMMIA.get("totalpacientestransferidoDe").toString());
//            int mesesdispensadosparaDM = Integer.parseInt(mapaDoMMIA.get("mesesdispensadosparaDM").toString());
//            int mesesdispensadosparaDT = Integer.parseInt(mapaDoMMIA.get("mesesdispensadosparaDT").toString());
//            int mesesdispensadosparaDS = Integer.parseInt(mapaDoMMIA.get("mesesdispensadosparaDS").toString());
//            int totalpacientesmanterTransporte = Integer.parseInt(mapaDoMMIA.get("totalpacientesmanterTransporte").toString());
//            int totalpacientesppe = Integer.parseInt(mapaDoMMIA.get("totalpacientesppe").toString());
//            int totallinhas1 = Integer.parseInt(mapaDoMMIA.get("totallinhas1").toString());
//            int totallinhas2 = Integer.parseInt(mapaDoMMIA.get("totallinhas2").toString());
//            int totallinhas3 = Integer.parseInt(mapaDoMMIA.get("totallinhas3").toString());
//            int totalpacientesprep = Integer.parseInt(mapaDoMMIA.get("totalpacientesprep").toString());
//            int totalpacientesCE = Integer.parseInt(mapaDoMMIA.get("totalpacientesCE").toString());
//            int totalpacienteptv = Integer.parseInt(mapaDoMMIA.get("totalpacienteptv").toString());
//            int mesesdispensados = Integer.parseInt(mapaDoMMIA.get("mesesdispensados").toString());
//            int pacientesEmTarv = Integer.parseInt(mapaDoMMIA.get("pacientesEmTarv").toString());
//            int adultosEmTarv = Integer.parseInt(mapaDoMMIA.get("adultosEmTarv").toString());
//            int pediatrico04EmTARV = Integer.parseInt(mapaDoMMIA.get("pediatrico04EmTARV").toString());
//            int pediatrico59EmTARV = Integer.parseInt(mapaDoMMIA.get("pediatrico59EmTARV").toString());
//            int pediatrico1014EmTARV = Integer.parseInt(mapaDoMMIA.get("pediatrico1014EmTARV").toString());
//
//            if (livroRegistoDiarios.size() > 0) {
//                // Tell the user what you are doing
//                monitor.beginTask("Carregando a lista... ", livroRegistoDiarios.size());
//
//                FileInputStream currentXls = new FileInputStream(reportFileName);
//
//                HSSFWorkbook workbook = new HSSFWorkbook(currentXls);
//
//                HSSFSheet sheet = workbook.getSheetAt(0);
//
//                HSSFCellStyle cellStyle = workbook.createCellStyle();
//                cellStyle.setBorderBottom(BorderStyle.THIN);
//                cellStyle.setBorderTop(BorderStyle.THIN);
//                cellStyle.setBorderLeft(BorderStyle.THIN);
//                cellStyle.setBorderRight(BorderStyle.THIN);
//                cellStyle.setAlignment(HorizontalAlignment.CENTER);
//                cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//
//
//                HSSFRow healthFacility = sheet.getRow(10);
//                HSSFCell healthFacilityCell = healthFacility.createCell(2);
//                healthFacilityCell.setCellValue(LocalObjects.currentClinic.getClinicName());
//                healthFacilityCell.setCellStyle(cellStyle);
//
//                HSSFRow reportPeriod = sheet.getRow(10);
//                HSSFCell reportPeriodCell = reportPeriod.createCell(16);
//                reportPeriodCell.setCellValue(sdf.format(theStartDate) + " à " + sdf.format(theEndDate));
//                reportPeriodCell.setCellStyle(cellStyle);
//
//                HSSFRow reportYear = sheet.getRow(11);
//                HSSFCell reportYearCell = reportYear.createCell(16);
//                reportYearCell.setCellValue(sdfYear.format(theStartDate));
//                reportYearCell.setCellStyle(cellStyle);
//
//                for (int i = 15; i <= sheet.getLastRowNum(); i++) {
//                    HSSFRow row = sheet.getRow(i);
//                    deleteRow(sheet, row);
//                }
//
//                out = new FileOutputStream(new File(reportFileName));
//                workbook.write(out);
//
//                int rowNum = 15;
//                int i = 0;
//                LivroRegistoDiarioXLS xlsLivroTemp = null;
//
//                for (LivroRegistoDiarioXLS xls : livroRegistoDiarios) {
//                    i++;
//                    HSSFRow row = sheet.createRow(rowNum++);
//
//                    HSSFCell createCellNid = row.createCell(1);
//                    createCellNid.setCellValue(xls.getPatientIdentifier());
//                    createCellNid.setCellStyle(cellStyle);
//
//                    HSSFCell createCellNome = row.createCell(2);
//                    createCellNome.setCellValue(xls.getNome() + " " + xls.getApelido());
//                    createCellNome.setCellStyle(cellStyle);
//
//                    HSSFCell zeroQuatro = row.createCell(3);
//                    zeroQuatro.setCellValue(xls.getZeroQuatro());
//                    zeroQuatro.setCellStyle(cellStyle);
//
//                    HSSFCell cincoNove = row.createCell(4);
//                    cincoNove.setCellValue(xls.getCincoNove());
//                    cincoNove.setCellStyle(cellStyle);
//
//                    HSSFCell dezCatorze = row.createCell(5);
//                    dezCatorze.setCellValue(xls.getDezCatorze());
//                    dezCatorze.setCellStyle(cellStyle);
//
//                    HSSFCell maiorQuinze = row.createCell(6);
//                    maiorQuinze.setCellValue(xls.getMaiorQuinze());
//                    maiorQuinze.setCellStyle(cellStyle);
//
//                    HSSFCell createCellTipoTarv = row.createCell(7);
//                    createCellTipoTarv.setCellValue(xls.getTipoTarv());
//                    createCellTipoTarv.setCellStyle(cellStyle);
//
//                    HSSFCell createCellRegimeTerapeutico = row.createCell(8);
//                    createCellRegimeTerapeutico.setCellValue(xls.getRegimeTerapeutico());
//                    createCellRegimeTerapeutico.setCellStyle(cellStyle);
//
//                    HSSFCell produtos = row.createCell(9);
//                    produtos.setCellValue(xls.getProdutos());
//                    produtos.setCellStyle(cellStyle);
//
//                    HSSFCell quantidade = row.createCell(10);
//                    quantidade.setCellValue(xls.getQuantidade());
//                    quantidade.setCellStyle(cellStyle);
//
//                    HSSFCell createCellTipoDispensa = row.createCell(11);
//                    createCellTipoDispensa.setCellValue(xls.getTipoDispensa());
//                    createCellTipoDispensa.setCellStyle(cellStyle);
//
//                    HSSFCell linhaNome = row.createCell(12);
//                    linhaNome.setCellValue(xls.getLinha());
//                    linhaNome.setCellStyle(cellStyle);
//
//                    HSSFCell createCellDataLevantamento = row.createCell(13);
//                    createCellDataLevantamento.setCellValue(xls.getDataLevantamento());
//                    createCellDataLevantamento.setCellStyle(cellStyle);
//
//                    HSSFCell createCellDataProximoLevantamento = row.createCell(14);
//                    createCellDataProximoLevantamento.setCellValue(xls.getDataProximoLevantamento());
//                    createCellDataProximoLevantamento.setCellStyle(cellStyle);
//
//                    HSSFCell ppe = row.createCell(15);
//                    ppe.setCellValue(xls.getPpe());
//                    ppe.setCellStyle(cellStyle);
//
//                    HSSFCell prep = row.createCell(16);
//                    prep.setCellValue(xls.getPrep());
//                    prep.setCellStyle(cellStyle);
//
//                    HSSFCell criancaExposta = row.createCell(17);
//                    criancaExposta.setCellValue("");
//                    criancaExposta.setCellStyle(cellStyle);
//
//                    xlsLivroTemp = xls;
//
//                    // Optionally add subtasks
//                    monitor.subTask("Carregando : " + i + " de " + livroRegistoDiarios.size() + "...");
//
//                    Thread.sleep(5);
//
//                    // Tell the monitor that you successfully finished one item of "workload"-many
//                    monitor.worked(1);
//                    // Check if the user pressed "cancel"
//                    if (monitor.isCanceled()) {
//                        monitor.done();
//                        return;
//                    }
//                }
//
//                for (int i0 = 1; i0 < LivroRegistoDiarioXLS.class.getClass().getDeclaredFields().length; i0++) {
//                    sheet.autoSizeColumn(i0);
//                }
//
//                monitor.done();
//                currentXls.close();
//
//                FileOutputStream outputStream = new FileOutputStream(new File(reportFileName));
//                workbook.write(outputStream);
//                workbook.close();
//
//                Desktop.getDesktop().open(new File(reportFileName));
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//
//
//    }
//
//    private void deleteRow(HSSFSheet sheet, Row row) {
//        int lastRowNum = sheet.getLastRowNum();
//        if (lastRowNum > 0) {
//            int rowIndex = row.getRowNum();
//            HSSFRow removingRow = sheet.getRow(rowIndex);
//            if (removingRow != null) {
//                sheet.removeRow(removingRow);
//            }
//        }
//    }

    @Override
    public void run(IProgressMonitor iProgressMonitor) throws InvocationTargetException, InterruptedException {
        
    }
}