package w301.xyz.excel_import.service.impl;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;
import w301.xyz.excel_import.mapper.JourneyTableMapper;
import w301.xyz.excel_import.mapper.KeyCrowdNucleicTableMapper;
import w301.xyz.excel_import.po.JourneyInfo;
import w301.xyz.excel_import.po.KeyCrowdNucleicAcid;
import w301.xyz.excel_import.service.KeyCrowdNucleicService;

import w301.xyz.excel_import.util.DateUtils;
import w301.xyz.excel_import.util.KeyCrowdNucleicAcidTableEnumMapping;
import w301.xyz.excel_import.util.SqlEntityUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class KeyCrowdNucleicServiceImpl implements KeyCrowdNucleicService {
    @Autowired
    private ThreadPoolExecutor dbThreadPool;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private KeyCrowdNucleicTableMapper keyCrowdNucleicTableMapper;
    private static final int BATCH_SIZE=5000;

    /**
     * 不完善
     *  插入完成以后 发送查询count affectRow是否和count结果一致
     * @param datas
     * @return
     */
    @Override
    public Integer batchInsert(List<KeyCrowdNucleicAcid> datas) {
        return -1;
    }

    /**
     * 查询最新一条日期数据记录
     *
     * @param date
     * @return
     */
    @Override
    public List<KeyCrowdNucleicAcid> getLatestData(String date) {
        return keyCrowdNucleicTableMapper.getLatestByDate(date);
    }

    @Override
    public Integer batchInsertByExcelData(List<Map<String, Object>> excelData) {
        int totalRow = excelData.size();
        List<KeyCrowdNucleicAcid> tempList=new ArrayList<>();
        int count=1;
        //拆分数组
        log.info("开始对excel数据进行转换......");
        CompletableFuture<List<KeyCrowdNucleicAcid>> t1 = CompletableFuture.supplyAsync(() -> {
            List<Map<String, Object>> tempExcelDate = excelData.stream().limit(totalRow / 2).parallel().collect(Collectors.toList());
            return SqlEntityUtil.transferMapDateToEntityList(tempExcelDate,KeyCrowdNucleicAcid.class, KeyCrowdNucleicAcidTableEnumMapping.class);
        }, dbThreadPool);

        CompletableFuture<List<KeyCrowdNucleicAcid>> t2 = CompletableFuture.supplyAsync(() -> {
            List<Map<String, Object>> tempExcelDate = excelData.stream().skip(totalRow / 2).parallel().collect(Collectors.toList());
            return SqlEntityUtil.transferMapDateToEntityList(tempExcelDate,KeyCrowdNucleicAcid.class, KeyCrowdNucleicAcidTableEnumMapping.class);
        }, dbThreadPool);
        log.info("转换完成...............");

        try {
            CompletableFuture.allOf(t1,t2).get();
            SqlEntityUtil.addEntityToTempList(tempList, t1,t2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        //排序
        if (CollectionUtils.isEmpty(tempList)){
            log.warn("tempList数据为空,无法进行后续操作~!");
            return -1;
        }
        log.info("开始排序...............");
        tempList=tempList.stream().sorted((j1,j2)->{
            return Integer.valueOf(j1.getNumber())-Integer.valueOf(j2.getNumber());
        }).collect(Collectors.toList());
        log.info("排序完成...............");
        log.info("检测是否存在重复插入...............");

        KeyCrowdNucleicAcid criterion = tempList.get(0);
        Integer totalRowBeforeInsert = keyCrowdNucleicTableMapper.countBeforeOrAfterInsert(criterion,DateUtils.getNowDateStr());
        if (totalRowBeforeInsert>0){
            //删除
            log.info("删除重复记录...............");
            keyCrowdNucleicTableMapper.deleteRepeatableDate(criterion,DateUtils.getNowDateStr());
            log.info("删除完成..................");
        }
        //批处理插入
        log.info("开始执行批量插入...............");
        SqlSession batchSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        try{
            KeyCrowdNucleicTableMapper crowdNucleicTableMapper = batchSession.getMapper(KeyCrowdNucleicTableMapper.class);
            //批量插入
            int tempSize = tempList.size();
            for (int i = 0; i < tempSize; i++) {
                KeyCrowdNucleicAcid insertEntity = tempList.remove(0);
                if (insertEntity==null){
                    continue;
                }
                //todo:插入
                crowdNucleicTableMapper.insertExcelDataByBatch(insertEntity);
                count++;
                if ((count % BATCH_SIZE)==0 || count==tempSize){
                    batchSession.flushStatements();
                }
            }
            //事务
            batchSession.commit(TransactionSynchronizationManager.isSynchronizationActive());

            log.info("校验插入数量是否匹配...............");

            Integer totalRowAfterInsert=crowdNucleicTableMapper.countBeforeOrAfterInsert(criterion, DateUtils.getNowDateStr());
            if (totalRowAfterInsert<(count-1)){
                log.error("批量插入失败,校验数量不匹配");
                throw new RuntimeException("批量插入失败,校验数量不匹配");
            }
            log.info("校验完成...............");
        } catch (Exception e) {
            batchSession.rollback();
            log.error("插入时出错,错误信息:{}\n",e.getMessage(),e);
            throw new RuntimeException(e);
        }finally {
            if (batchSession!=null){
                batchSession.close();
            }
        }
        log.info("执行批量插入完成...............");
        return count>0?1:-1;
    }
}

