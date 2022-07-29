package w301.xyz.excel_import;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import w301.xyz.excel_import.service.JourneyInfoService;
import w301.xyz.excel_import.service.KeyCrowdNucleicService;
import w301.xyz.excel_import.util.ImportExcelUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class TableImportDev {
    @Autowired
    private KeyCrowdNucleicService crowdNucleicService;
    @Autowired
    @Qualifier(value = "journeyInfoServiceImpl")
    private JourneyInfoService journeyInfoService;

    @Test
    public void testKeyCrowdNucleicAcidBatchImport(){
        List excelDates = ImportExcelUtil.readExcel("E:\\WorkData\\重点人员核算对比\\7.28风险职业和重点人群底数45204.xlsx", 0);
        crowdNucleicService.batchInsertByExcelData(excelDates);
    }

    @Test
    public void testJourneyExcelBatchImport(){
        //添加多个Excel文件
        List<String> filePaths=new ArrayList<>();
        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.27\\移动7.27上午.csv");
        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.27\\移动7.27下午.csv");
        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.27\\移动7.27晚上.csv");
//        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.27\\联通7.27上午.xls");
//        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.27\\联通7.27下午.xls");
//        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.27\\联通7.27晚上.xls");
        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.27\\电信7.27上午.xls");
        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.27\\电信7.27下午.xls");
        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.27\\电信7.27晚上.xls");
        //读取多个excel文件
        List<List<Map<String, Object>>> excelDates=ImportExcelUtil.readExcelFileList(filePaths,0);

        //分批插入数据库
        journeyInfoService.batchInsertByExcelData(excelDates);

    }
}
