package w301.xyz.excel_import.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 配置db相关操作的线程池(主要是对数据进行处理)
 *  cpu密集型
 */
@Configuration
public class DbThreadPool {

    @Bean
    public ThreadPoolExecutor threadPool(){
       return new ThreadPoolExecutor(
               2,
               4,
               36000,
               TimeUnit.MILLISECONDS,
               new ArrayBlockingQueue<>(10000)
       );
    }
}
