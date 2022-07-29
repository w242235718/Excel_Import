package w301.xyz.excel_import;

import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PoolRelatedTest {
    @Test
    public void coreNum(){
        //核心数量
        int coreSize = Runtime.getRuntime().availableProcessors()/4;
        System.out.println("核心数:"+coreSize);
        //线程池
        ExecutorService dbInsertPool = Executors.newFixedThreadPool(coreSize);
        //数量
        int listSize=54051;
        int listSplice=listSize/coreSize;
        List<Integer> testList=new ArrayList<>(listSize);

        for (int i = 0; i < listSize; i++) {
            testList.add(i+1);
        }

        //切分
        for (int i = 0; i < coreSize; i++) {
            List<Integer> collect = testList.stream().skip((i) * listSplice).limit(listSplice).collect(Collectors.toList());
            System.out.println(collect);
            //提交任务
            Future<?> f1 = dbInsertPool.submit(() -> {
                Integer remove = collect.remove(0);
            });
        }
        //残余
        int left = listSize % coreSize;
        System.out.println("left:"+left);
        List<Integer> leftList = testList.stream().skip(testList.size() - left).collect(Collectors.toList());
        System.out.println(leftList);

        //提交任务
        Future<?> f1 = dbInsertPool.submit(() -> {

        });
        try {
            //等待任务完成
            f1.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
