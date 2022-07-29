package w301.xyz.excel_import;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("w301.xyz.excel_import.mapper")
@EnableAsync
public class ExcelImportApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExcelImportApplication.class, args);
    }

}
