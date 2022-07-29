package w301.xyz.excel_import;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import w301.xyz.excel_import.mapper.KeyCrowdNucleicTableMapper;
import w301.xyz.excel_import.po.KeyCrowdNucleicAcid;

import java.util.List;

@SpringBootTest
class ExcelImportToolApplicationTests {
    @Autowired
    private KeyCrowdNucleicTableMapper keyCrowdMapper;
    @Test
    void contextLoads() {
        List<KeyCrowdNucleicAcid> latestByDate = keyCrowdMapper.getLatestByDate("20220713");
        System.out.println(latestByDate);
    }

}
