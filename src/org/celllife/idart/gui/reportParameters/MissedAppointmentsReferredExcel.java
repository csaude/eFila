package org.celllife.idart.gui.reportParameters;

import model.manager.reports.MissedAppointmentsReportReferredXLS;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.vafada.swtcalendar.SWTCalendar;

import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.List;

public class MissedAppointmentsReferredExcel implements IRunnableWithProgress {

    public List<MissedAppointmentsReportReferredXLS> missedAppointmentsReportReferredXLS;
    private final Shell parent;
    private FileOutputStream out = null;
    private SWTCalendar swtCal;
    private String reportFileName;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String txtMinimumDaysLate;
    String txtMaximumDaysLate;


    SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");

    public MissedAppointmentsReferredExcel(SWTCalendar swtCal,
                                      Shell parent, String reportFileName, String min, String max) {
        this.parent = parent;
        this.swtCal = swtCal;
        this.reportFileName = reportFileName;
        this.txtMinimumDaysLate = min;
        this.txtMaximumDaysLate = max;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            ConexaoJDBC con = new ConexaoJDBC();

            monitor.beginTask("Por Favor, aguarde ... ", 1);

            missedAppointmentsReportReferredXLS = con.getReferredMissedAppointment(txtMinimumDaysLate,
                    txtMaximumDaysLate, swtCal.getCalendar().getTime(), String.valueOf(LocalObjects.mainClinic.getId()));

            if (missedAppointmentsReportReferredXLS.size() > 0) {

                monitor.beginTask("Gerando a Lista ... ", missedAppointmentsReportReferredXLS.size());

                FileInputStream currentXls = new FileInputStream(reportFileName);
                HSSFWorkbook workbook = new HSSFWorkbook(currentXls);
                HSSFSheet sheet = workbook.getSheetAt(0);

                HSSFCellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setBorderBottom(BorderStyle.THIN);
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderLeft(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);
                cellStyle.setAlignment(HorizontalAlignment.CENTER);

                HSSFCellStyle cellFontStyle = workbook.createCellStyle();
                HSSFFont font = workbook.createFont();
                font.setFontHeightInPoints((short) 14);
                cellFontStyle.setFont(font);

                HSSFRow healthFacility = sheet.getRow(10);
                HSSFCell healthFacilityCell = healthFacility.createCell(2);
                healthFacilityCell.setCellValue(LocalObjects.currentClinic.getClinicName());
                healthFacilityCell.setCellStyle(cellStyle);

                HSSFRow reportPeriod = sheet.getRow(10);
                HSSFCell reportPeriodCell = reportPeriod.createCell(7);
                reportPeriodCell.setCellValue(sdf.format(swtCal.getCalendar().getTime()));
                reportPeriodCell.setCellStyle(cellStyle);

                HSSFRow reportYear = sheet.getRow(11);
                HSSFCell reportYearCell = reportYear.createCell(7);
                reportYearCell.setCellValue(sdfYear.format(swtCal.getCalendar().getTime()));
                reportYearCell.setCellStyle(cellStyle);

                HSSFRow minMax = sheet.getRow(8);
                HSSFCell minMaxCell = minMax.createCell(4);
                minMaxCell.setCellValue("Este relatório mostra os pacientes referidos que faltaram entre " + txtMinimumDaysLate + " e " + txtMaximumDaysLate + " dias");
                minMaxCell.setCellStyle(cellFontStyle);

                for (int i = 14; i <= sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    deleteRow(sheet, row);
                }

                out = new FileOutputStream(new File(reportFileName));
                workbook.write(out);

                int rowNum = 14;
                int i = 0;
                for (MissedAppointmentsReportReferredXLS xls : missedAppointmentsReportReferredXLS) {
                    i++;
                    HSSFRow row = sheet.createRow(rowNum++);

                    HSSFCell createCellNid = row.createCell(1);
                    createCellNid.setCellValue(xls.getPatientIdentifier());
                    createCellNid.setCellStyle(cellStyle);

                    HSSFCell createCellNome = row.createCell(2);
                    createCellNome.setCellValue(xls.getNome());
                    createCellNome.setCellStyle(cellStyle);

                    HSSFCell createCellDataQueFaltouLevantamento = row.createCell(3);
                    createCellDataQueFaltouLevantamento.setCellValue(xls.getDataQueFaltouLevantamento());
                    createCellDataQueFaltouLevantamento.setCellStyle(cellStyle);

                    HSSFCell createCellDataIdentificouAbandonoTarv = row.createCell(4);
                    createCellDataIdentificouAbandonoTarv.setCellValue(xls.getDataIdentificouAbandonoTarv());
                    createCellDataIdentificouAbandonoTarv.setCellStyle(cellStyle);

                    HSSFCell createCellEfectuouLigacao = row.createCell(5);
                    createCellEfectuouLigacao.setCellValue(xls.getDataRegressoUnidadeSanitaria());
                    createCellEfectuouLigacao.setCellStyle(cellStyle);

                    HSSFCell createCellDataRegressoUnidadeSanitaria = row.createCell(6);
                    createCellDataRegressoUnidadeSanitaria.setCellValue(xls.getFarmaciaReferencia());
                    createCellDataRegressoUnidadeSanitaria.setCellStyle(cellStyle);

                    HSSFCell createCellChamadaEfectuada = row.createCell(7);
                    createCellChamadaEfectuada.setCellValue(xls.getContacto());
                    createCellChamadaEfectuada.setCellStyle(cellStyle);

                    monitor.subTask("Carregando : " + i + " de " + missedAppointmentsReportReferredXLS.size() + "...");

                    Thread.sleep(5);

                    monitor.worked(1);
                    if (monitor.isCanceled()) {
                        monitor.done();
                        return;
                    }
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
        }

    }

    public List<MissedAppointmentsReportReferredXLS> getList() {
        return this.missedAppointmentsReportReferredXLS;
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
}