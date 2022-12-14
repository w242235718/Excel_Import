package w301.xyz.excel_import.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;
import w301.xyz.excel_import.mapper.JourneyTableMapper;
import w301.xyz.excel_import.po.JourneyInfo;
import w301.xyz.excel_import.service.JourneyInfoService;
import w301.xyz.excel_import.util.JourneyInfoTableEnumMapping;
import w301.xyz.excel_import.util.SqlEntityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class JourneyInfoServiceStreamImpl implements JourneyInfoService {
    @Autowired
    private ThreadPoolExecutor dbThreadPool;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private JourneyTableMapper journeyTableMapper;

    private ThreadLocal<List<Future>> futureStore=new ThreadLocal<>();

    private static final int BATCH_SIZE=5000;

    @Override
    public Integer batchInsert(List<JourneyInfo> datas) {
        return null;
    }



    @Override
    public List<JourneyInfo> getLatestData(String date) {
        return null;
    }


    @Override
    public Integer batchInsertByExcelData(List<List<Map<String, Object>>> excelData) {
        int excelDataSize = excelData.size();

        for (int i = 0; i < excelDataSize; i++) {
            List<Map<String, Object>> tExcelDate = excelData.remove(0);
            if (CollectionUtils.isEmpty(tExcelDate)){
                continue;
            }
            //todo:??????Stream???
            try {
                TimeUnit.SECONDS.sleep(1);
                doBatchExcelInsert(tExcelDate);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }



        return 1;
    }


    public Integer batchInsertByExcelDataList(List<List<Map<String, Object>>> excelData) {
        int excelDataSize = excelData.size();
        CompletableFuture[] futureStore=new CompletableFuture[excelDataSize];

        for (int i = 0; i < excelDataSize; i++) {
            List<Map<String, Object>> tExcelDate = excelData.remove(0);
            if (CollectionUtils.isEmpty(tExcelDate)){
                continue;
            }
            //??????????????????
            CompletableFuture<Integer> t = CompletableFuture.supplyAsync(() -> {
                return doBatchExcelInsert(tExcelDate);
            }, dbThreadPool);
            futureStore[i]=t;
        }

        try {
            CompletableFuture.allOf(futureStore).get();
            for (int i = 0; i < futureStore.length; i++) {
                int isSuccess = (int) futureStore[i].get();
                if (isSuccess<0){
                    log.error("??????????????????");
                    throw new RuntimeException("??????????????????");
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        return 1;
    }
    @Async
    @Transactional
    public int doBatchExcelInsert(List<Map<String, Object>> excelData) {
        int totalRow = excelData.size();
        List<JourneyInfo> tempList=new ArrayList<>();
        int count=0;

        //????????????
        log.info("?????????excel??????????????????......");
        CompletableFuture<List<JourneyInfo>> t1 = CompletableFuture.supplyAsync(() -> {
            List<Map<String, Object>> tempExcelDate = excelData.stream().limit(totalRow / 2).parallel().collect(Collectors.toList());
            return SqlEntityUtil.transferMapDateToEntityList(tempExcelDate,JourneyInfo.class,JourneyInfoTableEnumMapping.class);
        }, dbThreadPool);

        CompletableFuture<List<JourneyInfo>> t2 = CompletableFuture.supplyAsync(() -> {
            List<Map<String, Object>> tempExcelDate = excelData.stream().skip(totalRow / 2).parallel().collect(Collectors.toList());
            return SqlEntityUtil.transferMapDateToEntityList(tempExcelDate,JourneyInfo.class,JourneyInfoTableEnumMapping.class);
        }, dbThreadPool);
        log.info("????????????...............");

        try {
            CompletableFuture.allOf(t1,t2).get();
            SqlEntityUtil.addEntityToTempList(tempList, t1,t2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        //??????
        if (CollectionUtils.isEmpty(tempList)){
            log.warn("tempList????????????,????????????????????????~!");
            return -1;
        }
        log.info("????????????...............");
        tempList=tempList.stream().sorted((j1,j2)->{
           return Integer.valueOf(j1.getNumber())-Integer.valueOf(j2.getNumber());
        }).collect(Collectors.toList());
        log.info("????????????...............");

        log.info("??????????????????????????????...............");
        JourneyInfo criterion = tempList.get(0);
        JourneyInfo tCriterion = journeyTableMapper.countBeforeInsert(criterion);
        if (tCriterion!=null){
            //??????
            log.info("??????????????????...............");
            journeyTableMapper.deleteRepeatableDate(tCriterion);
            log.info("????????????..................");
        }
        //???????????????
        log.info("????????????????????????...............");

        SqlSession batchSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        try{
            JourneyTableMapper mapper = batchSession.getMapper(JourneyTableMapper.class);
            //????????????
            int tempSize = tempList.size();
            for (int i = 0; i < tempSize; i++) {
                JourneyInfo insertEntity = tempList.remove(0);
                if (insertEntity==null){
                    continue;
                }
                ++count;
                mapper.insertExcelDateByBatch(insertEntity);
                if ((count % BATCH_SIZE)==0 || count>=tempSize){
                    batchSession.flushStatements();
                }
            }
            //??????
            batchSession.commit(TransactionSynchronizationManager.isSynchronizationActive());
            log.info("????????????????????????...............");
        } catch (Exception e) {
            batchSession.rollback();
            log.error("???????????????,????????????:{}\n",e.getMessage(),e);
            throw new RuntimeException(e);
        }finally {
            if (batchSession!=null){
                batchSession.close();
            }
        }
        log.info("??????????????????~~!...............");

        return count > 0 ? 1 : -1;
    }
    /**
     * ?????????
     */
    private void doBatchInsertWithPool(List<JourneyInfo> tempList) {
        int coreSize=Runtime.getRuntime().availableProcessors()>4?
                Runtime.getRuntime().availableProcessors()/4:Runtime.getRuntime().availableProcessors()/2;
        int listSize=tempList.size();
        int listSplice=listSize/coreSize;
        ExecutorService dbPool = Executors.newFixedThreadPool(coreSize);

        //??????
        for (int i = 0; i < coreSize; i++) {
            List<JourneyInfo> tList = tempList.stream().skip(i * listSplice).limit(listSize).collect(Collectors.toList());
            Future<?> f = dbPool.submit(() -> {
                createBatchInsertTask(tList);
            });
            //todo:??????????????????1
            keepInsertFuture(f);
        }
        //??????
        List<JourneyInfo> leftElement = tempList.stream().skip(tempList.size() - (listSize % coreSize)).collect(Collectors.toList());
        Future<?> f = dbPool.submit(() -> {
            createBatchInsertTask(leftElement);
        });
        keepInsertFuture(f);
        //todo:clear tempList

        //????????????????????????
        futureStore.get().stream().forEach((future)->{
            try {
                future.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void keepInsertFuture(Future<?> f) {
        List<Future> fList = futureStore.get();
        if (fList==null || CollectionUtils.isEmpty(fList)){
            ArrayList<Future> tFlist = new ArrayList<>();
            tFlist.add(f);
            futureStore.set(tFlist);
        }else{
            fList.add(f);
        }
    }

    private void createBatchInsertTask(List<JourneyInfo> tList){
        SqlSession batchSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        try {
            JourneyTableMapper mapper = batchSession.getMapper(JourneyTableMapper.class);
            //????????????
            int tempSize = tList.size();
            int count = 0;
            for (int k = 0; k < tempSize; k++) {
                JourneyInfo insertEntity = tList.remove(0);
                if (insertEntity == null) {
                    continue;
                }
                //todo:
                ++count;
                mapper.insertExcelDateByBatch(insertEntity);
                if ((count % BATCH_SIZE) == 0 || count >= tempSize) {
                    batchSession.flushStatements();
                }
            }
            //??????
            batchSession.commit(TransactionSynchronizationManager.isSynchronizationActive());
        } catch (Exception e) {
            batchSession.rollback();
            log.error("???????????????,????????????:{}\n", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (batchSession != null) {
                batchSession.close();
            }
        }
    }

}
