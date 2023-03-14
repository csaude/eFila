package org.celllife.idart.gui.reportParameters;

import model.manager.reports.PacienteReferidoXLS;
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
import java.util.Date;
import java.util.List;

public class PacientesReferidosSectorClinicoExcel
        implements IRunnableWithProgress {

    public List<PacienteReferidoXLS> pacienteReferidoXLSList ;
    private final Shell parent;
    private FileOutputStream out = null;
    private String reportFileName;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date theStartDate;
    Date theEndDate;


    SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");

    public PacientesReferidosSectorClinicoExcel(Shell parent, String reportFileName, Date theStartDate,
                                   Date theEndDate) {
        this.parent = parent;
        this.reportFileName = reportFileName;
        this.theStartDate = theStartDate;
        this.theEndDate = theEndDate;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            ConexaoJDBC con = new ConexaoJDBC();

            monitor.setTaskName("Por Favor, aguarde ... ");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            pacienteReferidoXLSList = con.getClinicSectorReferedPatients(sdf.format(theStartDate),sdf.format(theEndDate));

            if (pacienteReferidoXLSList.size() > 0) {

                monitor.beginTask("Gerando a Lista ... ", pacienteReferidoXLSList.size());

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
                HSSFCell reportPeriodCell = reportPeriod.createCell(9);
                reportPeriodCell.setCellValue(sdf.format(theStartDate) + " - " + sdf.format(theEndDate));
                reportPeriodCell.setCellStyle(cellStyle);

                HSSFRow reportYear = sheet.getRow(11);
                HSSFCell reportYearCell = reportYear.createCell(9);
                reportYearCell.setCellValue(sdfYear.format(theEndDate));
                reportYearCell.setCellStyle(cellStyle);

                for (int i = 14; i <= sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    deleteRow(sheet, row);
                }

                out = new FileOutputStream(new File(reportFileName));
                workbook.write(out);

                int rowNum = 14;
                int i = 0;
                for (PacienteReferidoXLS xls : pacienteReferidoXLSList) {
                    i++;
                    HSSFRow row = sheet.createRow(rowNum++);

                    HSSFCell createCellNid = row.createCell(1);
                    createCellNid.setCellValue(xls.getNid());
                    createCellNid.setCellStyle(cellStyle);

                    HSSFCell createCellNome = row.createCell(2);
                    createCellNome.setCellValue(xls.getNome());
                    createCellNome.setCellStyle(cellStyle);

                    HSSFCell createCellIdade = row.createCell(3);
                    createCellIdade.setCellValue(xls.getIdade());
                    createCellIdade.setCellStyle(cellStyle);

                    HSSFCell createCellDataUltimaPrescricao = row.createCell(4);
                    createCellDataUltimaPrescricao.setCellValue(xls.getDataultimaPrescricao());
                    createCellDataUltimaPrescricao.setCellStyle(cellStyle);

                    HSSFCell createCellRegimaTerapeutico = row.createCell(5);
                    createCellRegimaTerapeutico.setCellValue(xls.getRegimaterapeutico());
                    createCellRegimaTerapeutico.setCellStyle(cellStyle);

                    HSSFCell createCellTipoDispensa = row.createCell(6);
                    createCellTipoDispensa.setCellValue(xls.getTipoDispensa());
                    createCellTipoDispensa.setCellStyle(cellStyle);

                    HSSFCell createCellDataProxLev = row.createCell(7);
                    createCellDataProxLev.setCellValue(xls.getDataProximoLevantamento());
                    createCellDataProxLev.setCellStyle(cellStyle);

                    HSSFCell createCellDataReferencia = row.createCell(8);
                    createCellDataReferencia.setCellValue(xls.getDatareferencia());
                    createCellDataReferencia.setCellStyle(cellStyle);

                    HSSFCell createCellFarmaciaReferencia = row.createCell(9);
                    createCellFarmaciaReferencia.setCellValue(xls.getFarmaciaReferencia());
                    createCellFarmaciaReferencia.setCellStyle(cellStyle);

                    monitor.subTask("Carregando : " + i + " de " + pacienteReferidoXLSList.size() + "...");

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

    public List<PacienteReferidoXLS> getList() {
        return this.pacienteReferidoXLSList;
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
