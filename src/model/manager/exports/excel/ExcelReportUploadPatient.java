package model.manager.exports.excel;

import model.manager.exports.PackageExportObject;

import java.util.Date;
import java.util.List;

public class ExcelReportUploadPatient {
    private Date startDate;

    private Date endDate;

    private boolean showBatchInfo;

    private String pharmacy;

    private List<PackageExportObject> columns;

    private String path;

    private List<PackageExportObject> endColumns;
}
