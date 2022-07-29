package w301.xyz.excel_import;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import w301.xyz.excel_import.service.JourneyInfoService;
import w301.xyz.excel_import.service.KeyCrowdNucleicService;
import w301.xyz.excel_import.util.DateUtils;
import w301.xyz.excel_import.util.ImportExcelUtil;
import w301.xyz.excel_import.util.JourneyInfoTableEnumMapping;
import w301.xyz.excel_import.util.KeyCrowdNucleicAcidTableEnumMapping;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@SpringBootTest
public class EnumReflectionTest {
    @Autowired
    private KeyCrowdNucleicService crowdNucleicService;
    @Autowired
    @Qualifier(value = "journeyInfoServiceImpl")
    private JourneyInfoService journeyInfoService;
    @Test
    public void test(){
        getTableEnumTitle(JourneyInfoTableEnumMapping.class);
    }

    public List<String> getTableEnumTitle(Class enumTableClazz){
        List<String> tableTitle=new ArrayList<>();
        if (enumTableClazz.isEnum()){
            Object[] enumConstants = enumTableClazz.getEnumConstants();
            if (enumConstants.length>0){
                try {
                    for (Object enumObj : enumConstants) {
                        if (!ObjectUtils.isEmpty(enumObj)) {
                            Method getFiled = enumObj.getClass().getDeclaredMethod("getFiled", null);
                            String result = (String) getFiled.invoke(enumObj, null);
                            tableTitle.add(result);
                        }
                    }
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return tableTitle;
    }


    @Test
    public void testKeyCrowdNucleicAcidBatchImport(){
        List excelDates = ImportExcelUtil.readExcel("E:\\WorkData\\重点人员核算对比\\7.20风险职业和重点人群底数43387.xlsx", 0);
        crowdNucleicService.batchInsertByExcelData(excelDates);
    }

    @Test
    public void testJourneyExcelBatchImport(){
        //添加多个Excel文件
        List<String> filePaths=new ArrayList<>();
        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.20\\移动7.20上午.csv");
        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.20\\移动7.20下午.csv");
        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.20\\移动7.20晚上.csv");
//        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.20\\联通7.20上午.xlsx");
//        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.20\\联通7.20下午.xlsx");
//        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.20\\联通7.20晚上.xlsx");
//        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.20\\电信7.20上午.xls");
//        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.20\\电信7.20下午.xls");
//        filePaths.add("E:\\WorkData\\三大运营商明漫入数据\\202207\\7.20\\电信7.20晚上.xls");
        //读取多个excel文件
        List<List<Map<String, Object>>> excelDates=ImportExcelUtil.readExcelFileList(filePaths,0);

        //分批插入数据库
        journeyInfoService.batchInsertByExcelData(excelDates);

    }





    //excel解析成 List<Map<String,String>>
    //mybatis批处理阶段 构建对象和插入


}
