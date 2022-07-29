package w301.xyz.excel_import.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.csv.*;
import cn.hutool.poi.excel.sax.Excel03SaxReader;
import cn.hutool.poi.excel.sax.Excel07SaxReader;
import cn.hutool.poi.excel.sax.handler.RowHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import w301.xyz.excel_import.po.JourneyInfo;
import w301.xyz.excel_import.po.TableEntity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ImportExcelUtil {

    private static List<Map<String, Object>> tableDatas = new ArrayList<>();
    private static List<Object> headLine = new ArrayList<>();
    public static char QUOTE=34;
    private static SimpleDateFormat sd=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    private static Integer sequenceId=1;

    //=================生成sqlMap===================================

    /**
     * 读取excel文件
     * @param filePath
     * @param sheetIndex
     * @return
     */
    public static List<Map<String, Object>> readExcel(String filePath,Integer sheetIndex){
        clean();
        log.info("开始读取{}表中数据.........",filePath.substring(filePath.lastIndexOf("\\")));
        if (StringUtils.isEmpty(filePath)){
            throw new RuntimeException("文件路径为空");
        }
        if (sheetIndex<0){
            throw new RuntimeException("sheet不能小于0");
        }
        handlerReadByExt(filePath.substring(filePath.lastIndexOf(".")),filePath,sheetIndex);

        List<Map<String, Object>> tDatas = tableDatas.stream().collect(Collectors.toList());
        tableDatas.clear();
        log.info("读取完成...............");
        return tDatas;
    }

    /**
     * 根据excelMap生成
     */


    /**
     *
     * @param tableEnumClazz 映射关系枚举类
     * @param targetTable  目标表
     * @param tDates 数据源
     * @return
     * @param <T>
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <T> List<T> transferToSqlEntity(Class tableEnumClazz,Class<T> targetTable,List<List<TableEntity>> tDates) throws InstantiationException, IllegalAccessException {
        int tDataCounts=tDates.size();
        List<T> sqlEntities=new ArrayList<>(tDates.size());

        for (int i = 0; i < tDataCounts; i++) {
            List<TableEntity> te = tDates.remove(0);
            if (CollectionUtils.isEmpty(te)){
                continue;
            }else{
                //获取反射赋值后的表实体对象
                T instance = getMappedTableEntity(tableEnumClazz, targetTable, te);
                sqlEntities.add(instance);
            }
        }
        return sqlEntities;
    }

    /**
     * 获取通过反射完成赋值的表实体对象
     * @param tableEnumClazz 字段与表关系映射
     * @param targetTable 目标表类
     * @param te
     * @return
     * @param <T>
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private static <T> T getMappedTableEntity(Class tableEnumClazz, Class<T> targetTable, List<TableEntity> te) throws InstantiationException, IllegalAccessException {
        T instance = targetTable.newInstance();
        te.stream().forEach(tEntity->{
            boolean isCompatibleField=false;
            //枚举获取到字段名称
            if (!tableEnumClazz.isEnum()) {
                throw new RuntimeException(tableEnumClazz.getName()+",不是枚举类型!");
            }
            Object[] enumConstants = tableEnumClazz.getEnumConstants();
            for (Object enumObj : enumConstants) {
                //获取到字段
                try {
                    Class<?> enumClazz = enumObj.getClass();
                    Method enumFieldName = enumClazz.getDeclaredMethod("getFiledName");
                    Method enumField = enumClazz.getDeclaredMethod("getFiled");
                    String enumFieldNameStr = (String) enumFieldName.invoke(enumObj, null);
                    String enumFieldStr = (String) enumField.invoke(enumObj, null);
                    String tableFieldNameStr="";
                    //匹配字段
                    if (tEntity.getFiledName().equalsIgnoreCase(enumFieldNameStr)) {
                        isCompatibleField=true;
                        //驼峰转换
                        tableFieldNameStr=getCamelCaseFieldName(enumFieldStr);
                        //反射赋值
                        setMethodInvoke(targetTable, instance, tEntity, tableFieldNameStr);

                    }
                    if (!isCompatibleField){
                        //兼容性字段
                        for (CommonFieldMapping cfm : CommonFieldMapping.values()) {
                            if (cfm.getFiledName().equals(tEntity.getFiledName()) && cfm.getAnotherName().equals(enumFieldNameStr)) {
                                String anotherName = cfm.getAnotherName();
                                //获取tableFieldNameStr
                                tableFieldNameStr=getCamelCaseFieldName(enumFieldStr);
                                setMethodInvoke(targetTable, instance, tEntity, tableFieldNameStr);
                            }else{
                                continue;
                            }
                        }
                    }
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }


            }
        });
        return instance;
    }

    private static <T> void setMethodInvoke(Class<T> targetTable, T instance, TableEntity tEntity, String tableFieldNameStr) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method tableFieldMethod = getFieldSetMethod(targetTable, tableFieldNameStr);
        tableFieldMethod.invoke(instance, tEntity.getFiledValue());
    }

    private static <T> Method getFieldSetMethod(Class<T> targetTable, String tableFieldNameStr) throws NoSuchFieldException, NoSuchMethodException {
        Field tableField = targetTable.getDeclaredField(tableFieldNameStr);
        Class<?> filedType = tableField.getType();
        Method tableFieldMethod = targetTable.getDeclaredMethod(
                "set" + tableFieldNameStr.substring(0,1).toUpperCase()+ tableFieldNameStr.substring(1),
                filedType);
        return tableFieldMethod;
    }

    private static String getCamelCaseFieldName(String enumFieldStr){
        StringBuilder tableFieldName=new StringBuilder();

        String[] tbFieldNames = enumFieldStr.split("_");
        if (tbFieldNames.length>1){
            tableFieldName.append(tbFieldNames[0]);
            for (int i1 = 1; i1 < tbFieldNames.length; i1++) {
                tableFieldName.append(tbFieldNames[i1].substring(0,1).toUpperCase());
                tableFieldName.append(tbFieldNames[i1].substring(1));
            }
        }else{
            tableFieldName.append(enumFieldStr);
        }
        return tableFieldName.toString();
    }


    public static void clean(){
        tableDatas.clear();
        headLine.clear();
        sequenceId=0;
    }

    private static void handlerReadByExt(String extType, String filePath, Integer sheetIndex) {
        switch (extType) {
            case ".xlsx":
                Sax07Read(filePath,new ExcelSqlHandler(), sheetIndex);
                break;
            case ".xls":
                Sax03Read(filePath,new ExcelSqlHandler(), sheetIndex);
                break;
            case ".csv":
                CsvReadToSqlMap(filePath,sheetIndex);
                break;
            default:
                throw new RuntimeException("出错了~!该格式不支持~");
        }
    }

    private static void CsvReadToSqlMap(String filePath, Integer sheetIndex) {
        try (CsvReader reader=CsvUtil.getReader()){
            CsvData data = reader.read(FileUtil.file(filePath), Charset.forName("GBK"));
            List<CsvRow> rows = data.getRows();
            handleCsvDataToSqlMap(rows,true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param rows 数据
     * @param sciNotationToNormal 是否将科学计数数字 转换成普通数字
     */
    private static void handleCsvDataToSqlMap(List<CsvRow> rows,Boolean sciNotationToNormal) throws InstantiationException, IllegalAccessException {
        int totalRows = rows.size();
        boolean isTableHead=false;
        //数据处理
        for (int i = 0; i < totalRows; i++) {
            CsvRow row = rows.remove(0);
            if (row==null){
                continue;
            }else {
                List<String> rawList = row.getRawList();
                //判定csv表头
                List<String> finalRawList = rawList;
                if (!isTableHead && Arrays.stream(TableHeadLineForFilterEnum.values()).anyMatch(title->{
                    return finalRawList.stream().anyMatch(raw->title.getFiledName().equals(raw));
                })) {
                    headLine= rawList.stream().map(item->item).collect(Collectors.toList());
                    isTableHead=true;
                    continue;
                }

                //科学计数转换
                if (sciNotationToNormal){
                    rawList=toNormalNumber(rawList);
                }
                addRawToTableMap(rawList);
                //todo:身份证字段去除'
                sequenceId++;
            }
        }
    }

    /**
     * 将原始数据转换为map
     * @param rawList
     */
    private static void addRawToTableMap(List<String> rawList) {
        //将数据转换为tableEntity
        Map<String,Object> tMap=new HashMap<>();

        for (int h = 0; h < headLine.size(); h++) {
            String rawVal = rawList.get(h).trim();
            String title = (String) headLine.get(h);
            //value为空
            if (StringUtils.isEmpty(rawVal) && !StringUtils.isEmpty(title)){
                tMap.put(title,"");
                continue;
            } else if (StringUtils.isEmpty(rawVal) && StringUtils.isEmpty(title)) {
                //value和字段都为空
                continue;
            }
            //替换序号
            if (title.equals("序号")||title.equals(" ")){
                tMap.put("序号",sequenceId);
                continue;
            }
            tMap.put(title,rawVal);
        }
        tableDatas.add(tMap);
    }

    /**
     * 根据映射关系创建实体
     * @param rowMap 数据源
     * @param mappingClass 目标类
     * @param mappingClass 映射关系
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <T> T CreateTableEntity(Map<String, Object> rowMap, Class<T> targetClazz, Class mappingClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
        T instance;
        try {
            if (!mappingClass.isEnum()){
                log.error("{}不是映射类",mappingClass.getName());
                throw new RuntimeException(mappingClass.getName()+"不是映射类");
            }
            instance =targetClazz.newInstance();
            Object[] mappingFields = mappingClass.getEnumConstants();
            //
            for (Object mf : mappingFields) {
                //获取枚举类 映射关系
                Class<?> mfClass = mf.getClass();
                Method mfGetFieldName = mfClass.getDeclaredMethod("getFiledName");
                Method mfGetField = mfClass.getDeclaredMethod("getFiled");
                String fieldNameVal = (String) mfGetFieldName.invoke(mf, null);
                String fieldVal = (String) mfGetField.invoke(mf, null);
                String mapVal="";
                //获取字段值
                if (!StringUtils.isEmpty(fieldNameVal)){
                     mapVal=toStr(rowMap.get(fieldNameVal));
                }
                //尝试兼容性字段
                if (StringUtils.isEmpty(mapVal)){
                    for (CommonFieldMapping field : CommonFieldMapping.values()) {
                        if (field.getFiledName().equals(fieldNameVal)) {
                           mapVal=toStr(rowMap.get(field.getAnotherName()));
                        }
                        if (mapVal.length()>0){
                            break;
                        }
                        continue;
                    }
                }
                if (mapVal.length()==0){
                    continue;
                }
                //拼接方法名(驼峰)
                StringBuilder fieldName=new StringBuilder();
                String setFieldMethodName="";
                if (!StringUtils.isEmpty(fieldVal)){
                    String[] sp = fieldVal.split("_");
                    if (sp.length>1){
                        fieldName.append(sp[0]);
                        Arrays.stream(sp).skip(1).forEach((field)->fieldName.append(field.substring(0,1).toUpperCase().concat(field.substring(1))));

                        setFieldMethodName=new String("set"+ fieldName.substring(0,1).toUpperCase().concat(fieldName.substring(1)));
                    }else{
                        fieldName.append(sp[0]);
                        setFieldMethodName=new String("set"+sp[0].substring(0,1).toUpperCase().concat(sp[0].substring(1)));
                    }
                }
                //参数类型
                Field clazzField = targetClazz.getDeclaredField(fieldName.toString());
                Class<?> fieldType = clazzField.getType();

                //获取字段Set方法
                Method setFieldMethod = targetClazz.getDeclaredMethod(setFieldMethodName, fieldType);
                setFieldMethod.invoke(instance,mapVal);

            }

        } catch (Exception e) {
            log.error("error:{}",e.getMessage(),e);
            throw e;
        }

        return instance;
    }

    public static List<List<Map<String, Object>>> readExcelFileList(List<String> filePaths, int sheetIndex) {
        List<List<Map<String, Object>>> tList=new ArrayList<>();
        int filePathSize = filePaths.size();
        for (int i = 0; i < filePathSize; i++) {
            String filePath = filePaths.remove(0);
            if (StringUtils.isEmpty(filePath)){
                continue;
            }
            List<Map<String, Object>> tMap = readExcel(filePath, sheetIndex);
            if (!CollectionUtils.isEmpty(tMap)){
                tList.add(tMap);
            }
        }
        return tList;
    }


    private static class ExcelSqlHandler implements RowHandler{
        @Override
        public void handle(int sheetIndex, long rowIndex, List<Object> rowCells) {
            Map<String,Object> tMap=new HashMap<>();
            boolean isContainExclusiveField=false;
            //表头
            if (rowIndex==0L){
                headLine=rowCells;
                //是否包含序号字段
                HeadLineExclusiveFiled[] hef = HeadLineExclusiveFiled.values();
                for (HeadLineExclusiveFiled field : hef) {
                    String fieldStr = field.getField();
                    if (fieldStr.equals(headLine.get(0).toString())) {
                        isContainExclusiveField=true;
                    }
                }
            }
            //数据
            if (rowIndex>0L && isContainExclusiveField) {
                excelReadHandle(rowCells, tMap,1);
            } else if (rowIndex > 0L && !isContainExclusiveField) {
                excelReadHandle(rowCells,tMap,0);
            }
            sequenceId++;
        }

        private void excelReadHandle(List<Object> rowCells, Map<String, Object> tMap,Integer startIndex) {
            tMap.put("序号",sequenceId.toString());
            for (int i = startIndex; i < rowCells.size(); i++) {
                String title = toStr(headLine.get(i));
                String value = formatOfCommonFiledNumber(title,toStr(rowCells.get(i)).trim());
                tMap.put(title,value);
            }
            tableDatas.add(tMap);
        }

        private String formatOfCommonFiledNumber(String title, String value) {
            String val;
            switch (title){
                case "证件号码":
                case "身份证号":
                    val=formatIdCard(value);
                    return val;
                default:
                    return value;
            }
        }

        private String formatIdCard(String value) {
            int index=-1;
            index=value.indexOf(39);
            index=value.indexOf(145);
            index=value.indexOf(222);
            index=value.indexOf('‘');
            if (index!=-1){
                return value.substring(index+1);
            }else{
                return value;
            }
        }
    }
    private static String toStr(Object obj){
        return obj==null?"":obj.toString().trim();
    }
    //==================写入到磁盘txt格式==================================
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

    /**
     * 读取excel数据 支持.csv .xls .xlsx格式
     * @param path
     * @param sheetIndex
     * @return
     */
    public static List<Map<String, Object>> readBigExcelToWrite(String path, Integer sheetIndex) {
        tableDatas.clear();
        String extType = path.substring(path.lastIndexOf("."));
        handlerByExt(extType, path, sheetIndex);
        return tableDatas;
    }

    /**
     * 将读取到的excel写出到硬盘
     * @param file
     * @param data
     */
    public  void WriteToDisk(File file, List<Map<String,Object>> data){
        try(BufferedWriter bw=new BufferedWriter(new FileWriter(file))) {
            writeTableTitle(bw,null);
            writeTableData(bw,data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeTableData(BufferedWriter bw, List<Map<String, Object>> data) throws IOException, InterruptedException {
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> tMap = data.get(i);
            bw.write(ImportExcelUtil.QUOTE+String.valueOf(i+1)+ImportExcelUtil.QUOTE+"\t");
            bw.write(tMap.get("数据报送时间")==null?"":tMap.get("数据报送时间").toString()+"\t");
            bw.write(tMap.get("运营商")==null?"":tMap.get("运营商").toString()+"\t");
            bw.write(tMap.get("区县")==null?"":tMap.get("区县").toString()+"\t");
            bw.write(tMap.get("乡镇")==null?"":tMap.get("乡镇").toString()+"\t");
            bw.write(tMap.get("姓名")==null?"":tMap.get("姓名").toString()+"\t");
            String phoneNumber;
            phoneNumber=tMap.get("手机号")==null?"":tMap.get("手机号").toString();
            if (phoneNumber==null || phoneNumber.length()<=0){
                phoneNumber=tMap.get("手机号码")==null?"":tMap.get("手机号码").toString();
            }
            bw.write(phoneNumber+"\t");
            String idCardNo;
            if ((idCardNo=tMap.get("证件号码").toString()).indexOf("‘")!=-1){
                idCardNo=QUOTE+idCardNo.substring(idCardNo.indexOf("‘")+1);
            }
            bw.write(idCardNo==null?"":idCardNo+"\t");
            bw.write(tMap.get("入新时间")==null?"":tMap.get("入新时间").toString()+"\t");
            bw.write(tMap.get("来源地")==null?"":tMap.get("来源地").toString()+"\t");
            bw.write(tMap.get("基站位置")==null?"":tMap.get("基站位置").toString()+"\t");
            bw.write(tMap.get("IMSI")==null?"":tMap.get("IMSI").toString()+"\t");
            bw.write(QUOTE+sd.format(new Date())+QUOTE+"\t");
            bw.write(QUOTE+sd.format(new Date())+QUOTE+"\t");
            bw.write("\n");
        }
    }

    private void writeTableTitle(BufferedWriter bw,Class tableTitleEnum) throws IOException {
        List<String> tableEnumTitle = getTableEnumTitle(JourneyInfoTableEnumMapping.class);
        for (String title : tableEnumTitle) {
            bw.write(title+"\t");
        }
        bw.write("\n");
    }

    /**
     * @param extType
     * @param path
     * @param sheetIndex
     * @return Handler ClassName
     */
    private static void handlerByExt(String extType, String path, Integer sheetIndex) {
        switch (extType) {
            case ".xlsx":
                Sax07Read(path,new ExcelRowHandler(),sheetIndex);
                break;
            case ".xls":
                Sax03Read(path,new ExcelRowHandler(), sheetIndex);
                break;
            case ".csv":
                //todo:重写
                //CsvRead(path, sheetIndex, "utf8");
                break;
            default:
                throw new RuntimeException("出错了~!该格式不支持~");
        }
    }

    private static void CsvRead(String path, Integer sheetIndex, String charset) {
        try (CsvReader reader=CsvUtil.getReader()){
            CsvData data = reader.read(FileUtil.file(path), Charset.forName(charset));
            List<CsvRow> rows = data.getRows();
            handleCsvData(rows);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleCsvData(List<CsvRow> rows) {
        for (CsvRow row : rows) {
            List<String> rawList = row.getRawList();
            //csv表头
            if (rawList.get(0).equalsIgnoreCase("序号") || rawList.get(0).equalsIgnoreCase(" ")) {
                headLine = Collections.singletonList(rawList);
                continue;
            }
            //科学计数转环
            if (rawList.get(11)!=null && rawList.get(11).length()>0){
                rawList=toNormalNumber(rawList);
            }
            //添加数据
            HashMap<String, Object> tMap = new HashMap<>();
            for (int i = 0; i < rawList.size(); i++) {
                Object title = ((ArrayList) headLine.get(0)).get(i);
                tMap.put(title.toString(),QUOTE+rawList.get(i)+QUOTE);
            }
            tableDatas.add(tMap);
        }
    }



    private static List<String> toNormalNumber(List<String> rawList) {

        return rawList.stream().map(raw->{
//            System.out.println(raw);
            if (raw!=null && raw.contains("E+")){
                raw=new BigDecimal(raw).toBigInteger().toString(10);
                return raw;
            }else{
                return raw;
            }
        }).collect(Collectors.toList());

    }

    private static void Sax03Read(String path, RowHandler rowHandler,Integer sheetIndex) {
        Excel03SaxReader reader = new Excel03SaxReader(rowHandler);
        reader.read(path, sheetIndex);
    }

    private static void Sax07Read(String path, RowHandler rowHandler,Integer sheetIndex) {
        Excel07SaxReader reader = new Excel07SaxReader(rowHandler);
        reader.read(path, sheetIndex);
    }

    /**
     * todo:数据重复
     */
    private static class ExcelRowHandler implements RowHandler {
        @Override
        public void handle(int sheetIndex, long rowIndex, List<Object> rowList) {
            //表头
            boolean isContainSequence = false;
            if (rowIndex == 0) {
                headLine = rowList;
                if (headLine.get(0).equals(HeadLineConst.SEQUENCE_CN) || headLine.get(0).equals(" ")) {
                    isContainSequence = true;
                }
            }

            //读入数据
            if (isContainSequence) {
                handleExcelRow(rowIndex, rowList, 1);
            } else {
                handleExcelRow(rowIndex, rowList, 0);
            }

        }

        //先暂存map
        private void handleExcelRow(long rowIndex, List<Object> rowList, int index) {
            //数据非空判断
            if (rowList.stream().allMatch(row->Objects.isNull(row)||toStr(row).length()==0)){
                return;
            }
            Map<String, Object> tMap = new HashMap<>();
            tMap.put("序号", String.valueOf(rowIndex));
            //跳过表头
            if (!headLine.get(index).equals(rowList.get(index))) {
                for (int i = index; i < rowList.size(); i++) {
                    tMap.put(toStr(headLine.get(i)), QUOTE+toStr(rowList.get(i))+QUOTE);
                }
                tableDatas.add(tMap);
            }
        }

        public static String toStr(Object item) {
            return item == null ? "" : item.toString().trim();
        }
    }


}
