package org.celllife.idart.gui.reportParameters;

import model.manager.reports.LivroRegistoDiarioXLS;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
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

public class LivroRegistoDiarioExcel implements IRunnableWithProgress {

    private List<LivroRegistoDiarioXLS> livroRegistoDiarios;
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
    private String diseaseType;

    SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");

    public LivroRegistoDiarioExcel(boolean inicio, boolean manter, boolean alterar, boolean transfereDe, boolean reInicio, Shell parent, String reportFileName, Date theStartDate, Date theEndDate, String diseaseType) {
        this.inicio = inicio;
        this.manter = manter;
        this.alterar = alterar;
        this.transfereDe = transfereDe;
        this.reInicio = reInicio;
        this.livroRegistoDiarios = livroRegistoDiarios;
        this.parent = parent;
        this.swtCal = swtCal;
        this.reportFileName = reportFileName;
        this.theStartDate = theStartDate;
        this.theEndDate = theEndDate;
        this.diseaseType =diseaseType;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {

            ConexaoJDBC con = new ConexaoJDBC();

            monitor.beginTask("Por Favor, aguarde ... ", 1);

            livroRegistoDiarios = con.getLivroRegistoDiarioXLS(this.inicio, this.manter, this.alterar, this.transfereDe, this.reInicio, sdf.format(theStartDate), sdf.format(theEndDate), this.diseaseType);

            if (livroRegistoDiarios.size() > 0) {
                // Tell the user what you are doing
                monitor.beginTask("Carregando a lista... ", livroRegistoDiarios.size());

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


                HSSFRow healthFacility = sheet.getRow(10);
                HSSFCell healthFacilityCell = healthFacility.createCell(2);
                healthFacilityCell.setCellValue(LocalObjects.currentClinic.getClinicName());
                healthFacilityCell.setCellStyle(cellStyle);

                HSSFRow reportPeriod = sheet.getRow(10);
                HSSFCell reportPeriodCell = reportPeriod.createCell(15);
                reportPeriodCell.setCellValue(sdf.format(theStartDate) + " à " + sdf.format(theEndDate));
                reportPeriodCell.setCellStyle(cellStyle);

                HSSFRow reportYear = sheet.getRow(11);
                HSSFCell reportYearCell = reportYear.createCell(15);
                reportYearCell.setCellValue(sdfYear.format(theStartDate));
                reportYearCell.setCellStyle(cellStyle);

                for (int i = 15; i <= sheet.getLastRowNum(); i++) {
                    HSSFRow row = sheet.getRow(i);
                    deleteRow(sheet, row);

                    for(int dm=0; dm < sheet.getNumMergedRegions(); dm++)
                    {
                        // Delete the region
                        sheet.removeMergedRegion(dm);
                    }
                }

                out = new FileOutputStream(new File(reportFileName));
                workbook.write(out);

                int rowNum = 15;
                int i = 0;
                LivroRegistoDiarioXLS xlsLivroTemp = null;
                int total04 = 0;
                int total59 = 0;
                int total1014 = 0;
                int total15 = 0;
                int totalppe = 0;
                int totalprep= 0;

                for (LivroRegistoDiarioXLS xls : livroRegistoDiarios) {
                    i++;

                    if(xls.getZeroQuatro().equalsIgnoreCase("Sim"))
                        total04++;
                    if(xls.getCincoNove().equalsIgnoreCase("Sim"))
                        total59++;
                    if(xls.getDezCatorze().equalsIgnoreCase("Sim"))
                        total1014++;
                    if(xls.getMaiorQuinze().equalsIgnoreCase("Sim"))
                        total15++;
                    if(xls.getPpe().equalsIgnoreCase("Sim"))
                        totalppe++;
                    if(xls.getPrep().equalsIgnoreCase("Sim"))
                        totalprep++;

                    HSSFRow row = sheet.createRow(rowNum++);

                    if (xlsLivroTemp != null)
                        if (xlsLivroTemp.getPatientIdentifier().equalsIgnoreCase(xls.getPatientIdentifier()) &&
                                xlsLivroTemp.getNome().equalsIgnoreCase(xls.getNome())) {

                            for(int r = 1; r <= 18; r++){
                                if(!(r == 10 || r==11)) {
                                    sheet.addMergedRegion(new CellRangeAddress(
                                            rowNum - 2,
                                            rowNum - 1,
                                            r,
                                            r
                                    ));
                                }
                            }
                        }

                    HSSFCell createCellNid = row.createCell(1);
                    createCellNid.setCellValue(xls.getPatientIdentifier());
                    createCellNid.setCellStyle(cellStyle);

                    HSSFCell createCellNome = row.createCell(2);
                    createCellNome.setCellValue(xls.getNome() + " " + xls.getApelido());
                    createCellNome.setCellStyle(cellStyle);

                    HSSFCell tipoPaciente= row.createCell(3);
                    tipoPaciente.setCellValue(xls.getTipoPaciente());
                    tipoPaciente.setCellStyle(cellStyle);

                    HSSFCell zeroQuatro = row.createCell(4);
                    zeroQuatro.setCellValue(xls.getZeroQuatro());
                    zeroQuatro.setCellStyle(cellStyle);

                    HSSFCell cincoNove = row.createCell(5);
                    cincoNove.setCellValue(xls.getCincoNove());
                    cincoNove.setCellStyle(cellStyle);

                    HSSFCell dezCatorze = row.createCell(6);
                    dezCatorze.setCellValue(xls.getDezCatorze());
                    dezCatorze.setCellStyle(cellStyle);

                    HSSFCell maiorQuinze = row.createCell(7);
                    maiorQuinze.setCellValue(xls.getMaiorQuinze());
                    maiorQuinze.setCellStyle(cellStyle);

                    HSSFCell createCellTipoTarv = row.createCell(8);
                    createCellTipoTarv.setCellValue(xls.getTipoTarv());
                    createCellTipoTarv.setCellStyle(cellStyle);

                    HSSFCell createCellRegimeTerapeutico = row.createCell(9);
                    createCellRegimeTerapeutico.setCellValue(xls.getRegimeTerapeutico());
                    createCellRegimeTerapeutico.setCellStyle(cellStyle);

                    HSSFCell produtos = row.createCell(10);
                    produtos.setCellValue(xls.getProdutos());
                    produtos.setCellStyle(cellStyle);

                    HSSFCell quantidade = row.createCell(11);
                    quantidade.setCellValue(xls.getQuantidade());
                    quantidade.setCellStyle(cellStyle);

                    HSSFCell createCellTipoDispensa = row.createCell(12);
                    createCellTipoDispensa.setCellValue(xls.getTipoDispensa());
                    createCellTipoDispensa.setCellStyle(cellStyle);

                    HSSFCell linhaNome = row.createCell(13);
                    linhaNome.setCellValue(xls.getLinha());
                    linhaNome.setCellStyle(cellStyle);

                    HSSFCell createCellDataLevantamento = row.createCell(14);
                    createCellDataLevantamento.setCellValue(xls.getDataLevantamento());
                    createCellDataLevantamento.setCellStyle(cellStyle);

                    HSSFCell createCellDataProximoLevantamento = row.createCell(15);
                    createCellDataProximoLevantamento.setCellValue(xls.getDataProximoLevantamento());
                    createCellDataProximoLevantamento.setCellStyle(cellStyle);

                    HSSFCell ppe = row.createCell(16);
                    ppe.setCellValue(xls.getPpe());
                    ppe.setCellStyle(cellStyle);

                    HSSFCell prep = row.createCell(17);
                    prep.setCellValue(xls.getPrep());
                    prep.setCellStyle(cellStyle);

                    HSSFCell criancaExposta = row.createCell(18);
                    criancaExposta.setCellValue("");
                    criancaExposta.setCellStyle(cellStyle);

                    xlsLivroTemp = xls;

                    // Optionally add subtasks
                    monitor.subTask("Carregando : " + i + " de " + livroRegistoDiarios.size() + "...");

                    Thread.sleep(5);

                    // Tell the monitor that you successfully finished one item of "workload"-many
                    monitor.worked(1);
                    // Check if the user pressed "cancel"
                    if (monitor.isCanceled()) {
                        monitor.done();
                        return;
                    }
                }

                totalRow(sheet.createRow(rowNum++), cellStyle, total04, total59, total1014, total15, totalppe, totalprep);

                for (int i0 = 1; i0 < LivroRegistoDiarioXLS.class.getClass().getDeclaredFields().length; i0++) {
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

    public List<LivroRegistoDiarioXLS> getList() {
        return this.livroRegistoDiarios;
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

    public void totalRow(HSSFRow row, HSSFCellStyle cellStyle,int total04,int total59,int total1014,int total15,int totalppe,int totalprep){

        HSSFCell createCellNid = row.createCell(1);
        createCellNid.setCellValue("Totais");
        createCellNid.setCellStyle(cellStyle);

        HSSFCell createCellNome = row.createCell(2);
        createCellNome.setCellValue("");
        createCellNome.setCellStyle(cellStyle);

        HSSFCell tipoPaciente= row.createCell(3);
        tipoPaciente.setCellValue("");
        tipoPaciente.setCellStyle(cellStyle);

        HSSFCell zeroQuatro = row.createCell(4);
        zeroQuatro.setCellValue(total04);
        zeroQuatro.setCellStyle(cellStyle);

        HSSFCell cincoNove = row.createCell(5);
        cincoNove.setCellValue(total59);
        cincoNove.setCellStyle(cellStyle);

        HSSFCell dezCatorze = row.createCell(6);
        dezCatorze.setCellValue(total1014);
        dezCatorze.setCellStyle(cellStyle);

        HSSFCell maiorQuinze = row.createCell(7);
        maiorQuinze.setCellValue(total15);
        maiorQuinze.setCellStyle(cellStyle);

        HSSFCell createCellTipoTarv = row.createCell(8);
        createCellTipoTarv.setCellValue("");
        createCellTipoTarv.setCellStyle(cellStyle);

        HSSFCell createCellRegimeTerapeutico = row.createCell(9);
        createCellRegimeTerapeutico.setCellValue("");
        createCellRegimeTerapeutico.setCellStyle(cellStyle);

        HSSFCell produtos = row.createCell(10);
        produtos.setCellValue("");
        produtos.setCellStyle(cellStyle);

        HSSFCell quantidade = row.createCell(11);
        quantidade.setCellValue("");
        quantidade.setCellStyle(cellStyle);

        HSSFCell createCellTipoDispensa = row.createCell(12);
        createCellTipoDispensa.setCellValue("");
        createCellTipoDispensa.setCellStyle(cellStyle);

        HSSFCell linhaNome = row.createCell(13);
        linhaNome.setCellValue("");
        linhaNome.setCellStyle(cellStyle);

        HSSFCell createCellDataLevantamento = row.createCell(14);
        createCellDataLevantamento.setCellValue("");
        createCellDataLevantamento.setCellStyle(cellStyle);

        HSSFCell createCellDataProximoLevantamento = row.createCell(15);
        createCellDataProximoLevantamento.setCellValue("");
        createCellDataProximoLevantamento.setCellStyle(cellStyle);

        HSSFCell ppe = row.createCell(16);
        ppe.setCellValue(totalppe);
        ppe.setCellStyle(cellStyle);

        HSSFCell prep = row.createCell(17);
        prep.setCellValue(totalprep);
        prep.setCellStyle(cellStyle);

        HSSFCell criancaExposta = row.createCell(18);
        criancaExposta.setCellValue("");
        criancaExposta.setCellStyle(cellStyle);


    }

}
