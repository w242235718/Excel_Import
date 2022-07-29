package w301.xyz.excel_import.util;

import org.springframework.util.CollectionUtils;
import w301.xyz.excel_import.po.JourneyInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SqlEntityUtil {
    public static<T> List<T> transferMapDateToEntityList(List<Map<String, Object>> tempExcelDate,Class<T> targetClass,Class mappingClass) {
        int tempRow = tempExcelDate.size();
        return transferMapToEntities(tempRow,tempExcelDate,targetClass,mappingClass);
    }

    /**
     * Map转换为Entity 实现
     *
     * @param tempExcelDate
     * @param targetClass
     * @param mappingClass
     * @return
     */
    private static <T> List<T> transferMapToEntities(Integer tempRow,List<Map<String, Object>> tempExcelDate, Class<T> targetClass, Class mappingClass) {
        List<T> tempList=new ArrayList<>(tempRow);
        for (int i = 0; i < tempRow; i++) {
            Map<String, Object> rowMap = tempExcelDate.remove(0);
            if (CollectionUtils.isEmpty(rowMap)){
                continue;
            }
            //利用反射创建对象
            JourneyInfo journeyInfo= null;
             T tEntity=null;
            try {
                tEntity = ImportExcelUtil.CreateTableEntity(rowMap, targetClass, mappingClass);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            //
            tempList.add(tEntity);
        }
        return tempList;
    }

    public static<T> void addEntityToTempList(List<T> tempList, CompletableFuture<List<T>>... ts) throws InterruptedException, ExecutionException {
        for (CompletableFuture<List<T>> t : ts) {
            List<T> tList = t.get();
            int listCount = tList.size();
            for (int i = 0; i < listCount; i++) {
                T entity = tList.remove(0);
                if (entity==null){
                    continue;
                }
                tempList.add(entity);
            }
        }
    }
}
