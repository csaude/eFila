package org.celllife.idart.gui.reportParameters;

import model.manager.reports.AbsenteeForSupportCall;
import model.manager.reports.DispensaTrimestralSemestral;
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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class DispensaTrimestralExcel implements IRunnableWithProgress {

    public List<DispensaTrimestralSemestral> dispensaTrimestralXLS;
    private final Shell parent;
    private FileOutputStream out = null;
    private String reportFileName;
    private SWTCalendar calendarStart;
    private SWTCalendar calendarEnd;



    public DispensaTrimestralExcel(SWTCalendar calendarStart,SWTCalendar calendarEnd,
                                                 Shell parent, String reportFileName) {
        this.parent = parent;
        this.reportFileName = reportFileName;
        this.calendarStart = calendarStart;
        this.calendarEnd = calendarEnd;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            ConexaoJDBC con = new ConexaoJDBC();

            monitor.beginTask("Por Favor, aguarde ... ", 1);

            Map<String, Object> map = new HashMap<>();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Date theStartDate = calendarStart.getCalendar().getTime();

            Calendar c = Calendar.getInstance(Locale.US);
            c.setLenient(true);
            c.setTime(theStartDate);

            if(Calendar.MONDAY == c.get(Calendar.DAY_OF_WEEK)){
                c.add(Calendar.DAY_OF_WEEK, -2);
                theStartDate = c.getTime();
            }

            Map mapaDispensaTrimestral = con.DispensaTrimestral(dateFormat.format(theStartDate),dateFormat.format(calendarEnd.getCalendar().getTime()));

            int totalpacientesManter = Integer.parseInt(mapaDispensaTrimestral.get("totalpacientesmanter").toString());
            int totalpacientesNovos = Integer.parseInt(mapaDispensaTrimestral.get("totalpacientesnovos").toString());
            int totalpacienteManuntencaoTransporte = Integer.parseInt(mapaDispensaTrimestral.get("totalpacienteManuntencaoTransporte").toString());
            int totalpacienteCumulativo = Integer.parseInt(mapaDispensaTrimestral.get("totalpacienteCumulativo").toString());

            dispensaTrimestralXLS = con.dispensaTrimestral(dateFormat.format(theStartDate), dateFormat.format(calendarEnd.getCalendar().getTime()));

            if (dispensaTrimestralXLS.size() > 0) {

                monitor.beginTask("Carregando a lista... ", dispensaTrimestralXLS.size());
                FileInputStream currentXls = new FileInputStream(reportFileName);

                HSSFWorkbook workbook = new HSSFWorkbook(currentXls);

                HSSFSheet sheet = workbook.getSheetAt(0);

                HSSFCellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setBorderBottom(BorderStyle.THIN);
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderLeft(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);
                cellStyle.setAlignment(HorizontalAlignment.CENTER);


                HSSFRow healthFacility = sheet.getRow(9);
                HSSFCell healthFacilityCell = healthFacility.createCell(2);
                healthFacilityCell.setCellValue(LocalObjects.currentClinic.getClinicName());
                healthFacilityCell.setCellStyle(cellStyle);

                HSSFRow reportPeriod = sheet.getRow(10);
                HSSFCell reportPeriodCell = reportPeriod.createCell(2);
                reportPeriodCell.setCellValue(dateFormat.format(theStartDate) +" à "+ dateFormat.format(calendarEnd.getCalendar().getTime()));
                reportPeriodCell.setCellStyle(cellStyle);

                HSSFRow _totalpacientesNovos = sheet.getRow(14);
                HSSFCell totalpacientesNovosCell = _totalpacientesNovos.createCell(2);
                totalpacientesNovosCell.setCellValue(totalpacientesNovos);
                totalpacientesNovosCell.setCellStyle(cellStyle);

                HSSFRow _totalpacientesManter = sheet.getRow(15);
                HSSFCell totalpacientesManterCell = _totalpacientesManter.createCell(2);
                totalpacientesManterCell.setCellValue(totalpacientesManter);
                totalpacientesManterCell.setCellStyle(cellStyle);

                HSSFRow _totalpacienteManuntencaoTransporte = sheet.getRow(16);
                HSSFCell totalpacienteManuntencaoTransporteCell = _totalpacienteManuntencaoTransporte.createCell(2);
                totalpacienteManuntencaoTransporteCell.setCellValue(totalpacienteManuntencaoTransporte);
                totalpacienteManuntencaoTransporteCell.setCellStyle(cellStyle);

                HSSFRow _totalpacienteCumulativo = sheet.getRow(17);
                HSSFCell totalpacienteCumulativoCell = _totalpacienteCumulativo.createCell(2);
                totalpacienteCumulativoCell.setCellValue(totalpacienteCumulativo);
                totalpacienteCumulativoCell.setCellStyle(cellStyle);

                for(int i=21; i<= sheet.getLastRowNum(); i++)
                {
                    HSSFRow row = sheet.getRow(i);
                    deleteRow(sheet,row);
                }

                out = new FileOutputStream(new File(reportFileName));
                workbook.write(out);

                int rowNum = 21;

                int i = 0;
                for (DispensaTrimestralSemestral xls : dispensaTrimestralXLS) {
                    i++;
                    HSSFRow row = sheet.createRow(rowNum++);

                    HSSFCell createCellNid = row.createCell(1);
                    createCellNid.setCellValue(xls.getPatientIdentifier());
                    createCellNid.setCellStyle(cellStyle);

                    HSSFCell createCellNome = row.createCell(2);
                    createCellNome.setCellValue(xls.getNome());
                    createCellNome.setCellStyle(cellStyle);

                    HSSFCell createCellIdade = row.createCell(3);
                    createCellIdade.setCellValue(xls.getIdade());
                    createCellIdade.setCellStyle(cellStyle);

                    HSSFCell createCellRegimeTerapeutico = row.createCell(4);
                    createCellRegimeTerapeutico.setCellValue(xls.getRegimeTerapeutico());
                    createCellRegimeTerapeutico.setCellStyle(cellStyle);

                    HSSFCell createCellTipoTarv = row.createCell(5);
                    createCellTipoTarv.setCellValue(xls.getTipoPaciente());
                    createCellTipoTarv.setCellStyle(cellStyle);


                    HSSFCell createCellDataPrescricao = row.createCell(6);
                    createCellDataPrescricao.setCellValue(xls.getDataPrescricao());
                    createCellDataPrescricao.setCellStyle(cellStyle);

                    HSSFCell createCellDataLevantamento = row.createCell(7);
                    createCellDataLevantamento.setCellValue(xls.getDataLevantamento());
                    createCellDataLevantamento.setCellStyle(cellStyle);

                    HSSFCell createCellDataProximoLevantamento = row.createCell(8);
                    createCellDataProximoLevantamento.setCellValue(xls.getDataProximoLevantamento());
                    createCellDataProximoLevantamento.setCellStyle(cellStyle);

                    monitor.subTask("Carregando : " + i + " de " + dispensaTrimestralXLS.size() + "...");

                    Thread.sleep(5);

                    monitor.worked(1);
                    if (monitor.isCanceled()) {
                        monitor.done();
                        return;
                    }
                }

                for(int i0 = 1; i0 < DispensaTrimestralSemestral.class.getClass().getDeclaredFields().length; i0++) {
                    sheet.autoSizeColumn(i0);
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

    public List<DispensaTrimestralSemestral> getList(){
        return this.dispensaTrimestralXLS;
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
