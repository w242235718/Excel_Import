package w301.xyz.excel_import.service;

import w301.xyz.excel_import.po.JourneyInfo;

import java.util.List;
import java.util.Map;

public interface JourneyInfoService extends BasicMapper<JourneyInfo>{
    /**
     * 根据excelMap插入数据
     * @param excelData
     * @return
     */
    Integer batchInsertByExcelData(List<List<Map<String, Object>>> excelData);
}
