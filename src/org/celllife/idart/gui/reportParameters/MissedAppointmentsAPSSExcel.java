package org.celllife.idart.gui.reportParameters;

import model.manager.reports.FollowupFaulty;
import model.manager.reports.RegistoChamadaTelefonicaXLS;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.vafada.swtcalendar.SWTCalendar;

import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.List;

public class MissedAppointmentsAPSSExcel implements IRunnableWithProgress {

    private List<RegistoChamadaTelefonicaXLS> chamadaTelefonicaXLSs;
    private final Shell parent;
    private FileOutputStream out = null;
    private SWTCalendar swtCal;
    private String reportFileName;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String txtMinimumDaysLate;
    String txtMaximumDaysLate;
    private boolean chkBtnALL;
    private boolean chkBtnTB;


    SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");

    public MissedAppointmentsAPSSExcel(SWTCalendar swtCal,
                                     Shell parent, String reportFileName, String min, String max,boolean chkBtnALL,boolean chkBtnTB) {
        this.parent = parent;
        this.swtCal = swtCal;
        this.reportFileName = reportFileName;
        this.txtMinimumDaysLate = min;
        this.txtMaximumDaysLate = max;
        this.chkBtnALL = chkBtnALL;
        this.chkBtnTB = chkBtnTB;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {

            ConexaoJDBC con=new ConexaoJDBC();

            monitor.beginTask("Por Favor, aguarde ... ", 1);

            if (chkBtnALL) {

                chamadaTelefonicaXLSs = con.getMissedAppointmentsReport(txtMinimumDaysLate,txtMaximumDaysLate,
                        swtCal.getCalendar().getTime(),String.valueOf(LocalObjects.mainClinic.getId()));
            } else if (chkBtnTB) {

                chamadaTelefonicaXLSs = con.getMissedAppointmentsPTV(txtMinimumDaysLate,txtMaximumDaysLate,
                        swtCal.getCalendar().getTime(),String.valueOf(LocalObjects.mainClinic.getId()));
            } else {

                chamadaTelefonicaXLSs = con.getMissedAppointmentsSMI(txtMinimumDaysLate,txtMaximumDaysLate,
                        swtCal.getCalendar().getTime(),String.valueOf(LocalObjects.mainClinic.getId()));
            }


            if (chamadaTelefonicaXLSs.size() > 0) {

                monitor.beginTask("Carregando a lista... ", chamadaTelefonicaXLSs.size());

                FileInputStream currentXls = new FileInputStream(reportFileName);

                HSSFWorkbook workbook = new HSSFWorkbook(currentXls);

                HSSFSheet sheet = workbook.getSheetAt(0);

                HSSFCellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setBorderBottom(BorderStyle.THIN);
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderLeft(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);
                cellStyle.setAlignment(HorizontalAlignment.CENTER);


                HSSFRow healthFacility = sheet.getRow(10);
                HSSFCell healthFacilityCell = healthFacility.createCell(2);
                healthFacilityCell.setCellValue(LocalObjects.currentClinic.getClinicName());
                healthFacilityCell.setCellStyle(cellStyle);

                HSSFRow reportPeriod = sheet.getRow(10);
                HSSFCell reportPeriodCell = reportPeriod.createCell(20);
                reportPeriodCell.setCellValue(sdf.format(DateUtils.addDays(swtCal.getCalendar().getTime(), -(Integer.parseInt(txtMaximumDaysLate)))) +" à "+
                        sdf.format(DateUtils.addDays(swtCal.getCalendar().getTime(), -(Integer.parseInt(txtMinimumDaysLate)))));
                reportPeriodCell.setCellStyle(cellStyle);

                HSSFRow reportYear = sheet.getRow(11);
                HSSFCell reportYearCell = reportYear.createCell(20);
                reportYearCell.setCellValue(sdfYear.format(swtCal.getCalendar().getTime()));
                reportYearCell.setCellStyle(cellStyle);

                HSSFRow daysPeriod = sheet.getRow(11);
                HSSFCell daysCell = daysPeriod.createCell(5);
                daysCell.setCellValue("Este relatório mostra os pacientes que têm entre " + txtMinimumDaysLate + " e " + txtMaximumDaysLate+ "dias de falta.");
                daysCell.setCellStyle(cellStyle);

                for(int i=15; i<= sheet.getLastRowNum(); i++)
                {
                    HSSFRow row = sheet.getRow(i);
                    deleteRow(sheet,row);
                }

                out = new FileOutputStream(new File(reportFileName));
                workbook.write(out);

                int rowNum = 15;
                int i = 0;
                for (RegistoChamadaTelefonicaXLS xls : chamadaTelefonicaXLSs) {
                    i++;
                    HSSFRow row = sheet.createRow(rowNum++);

                    HSSFCell createCellNome = row.createCell(1);
                    createCellNome.setCellValue(xls.getNome());
                    createCellNome.setCellStyle(cellStyle);

                    HSSFCell createCellNid = row.createCell(2);
                    createCellNid.setCellValue(xls.getNid());
                    createCellNid.setCellStyle(cellStyle);

                    HSSFCell createCellIdade = row.createCell(3);
                    createCellIdade.setCellValue(xls.getIdade());
                    createCellIdade.setCellStyle(cellStyle);

                    HSSFCell createCellContacto = row.createCell(4);
                    createCellContacto.setCellValue(xls.getContacto());
                    createCellContacto.setCellStyle(cellStyle);

                    HSSFCell createCellEndereco = row.createCell(5);
                    createCellEndereco.setCellValue(xls.getEndereco());
                    createCellEndereco.setCellStyle(cellStyle);

                    HSSFCell createCellTarv = row.createCell(6);
                    createCellTarv.setCellValue(xls.getTarv());
                    createCellTarv.setCellStyle(cellStyle);

                    HSSFCell createCellTb = row.createCell(7);
                    createCellTb.setCellValue(xls.getTb());
                    createCellTb.setCellStyle(cellStyle);

                    HSSFCell createCellSmi = row.createCell(8);
                    createCellSmi.setCellValue(xls.getSmi());
                    createCellSmi.setCellStyle(cellStyle);

                    HSSFCell apoio = row.createCell(9);
                    apoio.setCellValue("");
                    apoio.setCellStyle(cellStyle);

                    HSSFCell reintegracao = row.createCell(10);
                    reintegracao.setCellValue("");
                    reintegracao.setCellStyle(cellStyle);

                    HSSFCell incontactavel = row.createCell(11);
                    incontactavel.setCellValue("");
                    incontactavel.setCellStyle(cellStyle);

                    HSSFCell esqueceuData = row.createCell(12);
                    esqueceuData.setCellValue("");
                    esqueceuData.setCellStyle(cellStyle);

                    HSSFCell estaDoente = row.createCell(13);
                    estaDoente.setCellValue("");
                    estaDoente.setCellStyle(cellStyle);

                    HSSFCell transporte = row.createCell(14);
                    transporte.setCellValue("");
                    transporte.setCellStyle(cellStyle);

                    HSSFCell viagem = row.createCell(15);
                    viagem.setCellValue("");
                    viagem.setCellStyle(cellStyle);

                    HSSFCell obito = row.createCell(16);
                    obito.setCellValue("");
                    obito.setCellStyle(cellStyle);

                    HSSFCell retornou = row.createCell(17);
                    retornou.setCellValue("");
                    retornou.setCellStyle(cellStyle);

                    HSSFCell visitado = row.createCell(18);
                    visitado.setCellValue("");
                    visitado.setCellStyle(cellStyle);

                    HSSFCell observacao = row.createCell(19);
                    observacao.setCellValue("");
                    observacao.setCellStyle(cellStyle);

                    HSSFCell responsavel = row.createCell(20);
                    responsavel.setCellValue("");
                    responsavel.setCellStyle(cellStyle);

                    monitor.subTask("Carregando : " + i + " de " + chamadaTelefonicaXLSs.size() + "...");

                    Thread.sleep(5);

                    monitor.worked(1);
                    if (monitor.isCanceled()) {
                        monitor.done();
                        return;
                    }
                }

                for(int i0 = 1; i0 < RegistoChamadaTelefonicaXLS.class.getClass().getDeclaredFields().length; i0++) {
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
        }

    }

    public List<RegistoChamadaTelefonicaXLS> getList(){
        return this.chamadaTelefonicaXLSs;
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
