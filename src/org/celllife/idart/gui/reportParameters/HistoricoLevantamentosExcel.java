package org.celllife.idart.gui.reportParameters;

import model.manager.reports.HistoricoLevantamentoXLS;
import model.manager.reports.LivroRegistoDiarioXLS;
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
import java.util.Date;
import java.util.List;

public class HistoricoLevantamentosExcel implements IRunnableWithProgress {

    private List<HistoricoLevantamentoXLS> historicoLevantamentoXLS;
    private final Shell parent;
    private FileOutputStream out = null;
    private SWTCalendar swtCal;
    private String reportFileName;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private Date theStartDate;
    private Date theEndDate;
    private boolean inicio;
    private boolean manter;
    private boolean alterar;
    private boolean transfereDe;
    private boolean reInicio;
    private boolean fim;
    private String diseaseType;

    SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");

    public HistoricoLevantamentosExcel(boolean inicio,boolean manter, boolean alterar, boolean transfereDe, boolean reInicio, boolean fim, Shell parent, String reportFileName, Date theStartDate, Date theEndDate, String diseaseType) {
        this.inicio = inicio;
        this.manter = manter;
        this.alterar = alterar;
        this.transfereDe = transfereDe;
        this.reInicio = reInicio;
        this.historicoLevantamentoXLS = historicoLevantamentoXLS;
        this.parent = parent;
        this.swtCal = swtCal;
        this.reportFileName = reportFileName;
        this.theStartDate = theStartDate;
        this.theEndDate = theEndDate;
        this.diseaseType= diseaseType;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {

            ConexaoJDBC con=new ConexaoJDBC();

            monitor.beginTask("Por Favor, aguarde ... ", 1);

            historicoLevantamentoXLS = con.getQueryHistoricoLevantamentosXLS(this.inicio, this.manter,this.alterar,this.transfereDe, this.reInicio, this.fim, sdf.format(theStartDate), sdf.format(theEndDate), diseaseType);

            if(historicoLevantamentoXLS.size() > 0) {
                // Tell the user what you are doing
                monitor.beginTask("Carregando a lista... ", historicoLevantamentoXLS.size());

                FileInputStream currentXls = new FileInputStream(reportFileName);

                HSSFWorkbook workbook = new HSSFWorkbook(currentXls);

                HSSFSheet sheet = workbook.getSheetAt(0);

                HSSFCellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setBorderBottom(BorderStyle.THIN);
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderLeft(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);
                cellStyle.setAlignment(HorizontalAlignment.CENTER);


                HSSFRow healthFacility = sheet.getRow(11);
                HSSFCell healthFacilityCell = healthFacility.createCell(2);
                healthFacilityCell.setCellValue(LocalObjects.currentClinic.getClinicName());
                healthFacilityCell.setCellStyle(cellStyle);

                HSSFRow reportPeriod = sheet.getRow(10);
                HSSFCell reportPeriodCell = reportPeriod.createCell(9);
                reportPeriodCell.setCellValue(sdf.format(theStartDate) + " à " + sdf.format(theEndDate));
                reportPeriodCell.setCellStyle(cellStyle);

                HSSFRow reportYear = sheet.getRow(12);
                HSSFCell reportYearCell = reportYear.createCell(9);
                reportYearCell.setCellValue(sdfYear.format(theStartDate));
                reportYearCell.setCellStyle(cellStyle);

                for (int i = 14; i <= sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    deleteRow(sheet, row);
                }

                out = new FileOutputStream(new File(reportFileName));
                workbook.write(out);

                int rowNum = 14;
                int i = 0;
                for (HistoricoLevantamentoXLS xls : historicoLevantamentoXLS) {
                    i++;
                    HSSFRow row = sheet.createRow(rowNum++);

                    HSSFCell createCellNid = row.createCell(1);
                    createCellNid.setCellValue(xls.getPatientIdentifier());
                    createCellNid.setCellStyle(cellStyle);

                    HSSFCell createCellNome = row.createCell(2);
                    createCellNome.setCellValue(xls.getNome() + " " + xls.getApelido());
                    createCellNome.setCellStyle(cellStyle);

                    HSSFCell createCellIdade = row.createCell(3);
                    createCellIdade.setCellValue(xls.getIdade());
                    createCellIdade.setCellStyle(cellStyle);

                    HSSFCell createCellContacto = row.createCell(4);
                    createCellContacto.setCellValue(xls.getTelefone());
                    createCellContacto.setCellStyle(cellStyle);

                    HSSFCell createCellTipoPaciente = row.createCell(5);
                    createCellTipoPaciente.setCellValue(xls.getTipoPaciente());
                    createCellTipoPaciente.setCellStyle(cellStyle);

                    HSSFCell createCellTipoTarv = row.createCell(6);
                    createCellTipoTarv.setCellValue(xls.getTipoTarv());
                    createCellTipoTarv.setCellStyle(cellStyle);

                    HSSFCell createCellRegimeTerapeutico = row.createCell(7);
                    createCellRegimeTerapeutico.setCellValue(xls.getRegimeTerapeutico());
                    createCellRegimeTerapeutico.setCellStyle(cellStyle);

                    HSSFCell createCellTipoDispensa = row.createCell(8);
                    createCellTipoDispensa.setCellValue(xls.getTipoDispensa());
                    createCellTipoDispensa.setCellStyle(cellStyle);


                    HSSFCell createCellProveniencia = row.createCell(9);
                    createCellProveniencia.setCellValue(xls.getProveniencia());
                    createCellProveniencia.setCellStyle(cellStyle);

                    HSSFCell createCellModoDispensa = row.createCell(10);
                    createCellModoDispensa.setCellValue(xls.getModoDispensa());
                    createCellModoDispensa.setCellStyle(cellStyle);

                    HSSFCell createCellDataLevantamento = row.createCell(11);
                    createCellDataLevantamento.setCellValue(xls.getDataLevantamento());
                    createCellDataLevantamento.setCellStyle(cellStyle);

                    HSSFCell createCellDataProximoLevantamento = row.createCell(12);
                    createCellDataProximoLevantamento.setCellValue(xls.getDataProximoLevantamento());
                    createCellDataProximoLevantamento.setCellStyle(cellStyle);


                    // Optionally add subtasks
                    monitor.subTask("Carregando : " + i + " de " + historicoLevantamentoXLS.size() + "...");

                    Thread.sleep(5);

                    // Tell the monitor that you successfully finished one item of "workload"-many
                    monitor.worked(1);
                    // Check if the user pressed "cancel"
                    if (monitor.isCanceled()) {
                        monitor.done();
                        return;
                    }
                }

                for (int i0 = 1; i0 < HistoricoLevantamentoXLS.class.getClass().getDeclaredFields().length; i0++) {
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

    public List<HistoricoLevantamentoXLS> getList(){
        return this.historicoLevantamentoXLS;
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
