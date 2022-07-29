package w301.xyz.excel_import.service;

import org.apache.poi.ss.formula.functions.T;
import w301.xyz.excel_import.po.KeyCrowdNucleicAcid;

import java.util.List;
import java.util.Map;

public interface KeyCrowdNucleicService extends BasicMapper<KeyCrowdNucleicAcid> {
    Integer batchInsertByExcelData(List<Map<String,Object>> excelData);
}
