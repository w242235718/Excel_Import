package w301.xyz.excel_import.mapper;

import org.apache.ibatis.annotations.Param;
import w301.xyz.excel_import.po.KeyCrowdNucleicAcid;

import java.util.List;

public interface KeyCrowdNucleicTableMapper {
    public List<KeyCrowdNucleicAcid> getLatestByDate(@Param("date") String date);

    Integer insertExcelDataByBatch(@Param("item") KeyCrowdNucleicAcid item);


    void deleteRepeatableDate(@Param("criterion") KeyCrowdNucleicAcid criterion,@Param("now") String nowDateStr);


    Integer countBeforeOrAfterInsert(@Param("criterion") KeyCrowdNucleicAcid criterion, @Param("now") String nowDateStr);
}
