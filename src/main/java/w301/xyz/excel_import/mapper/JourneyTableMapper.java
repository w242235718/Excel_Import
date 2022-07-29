package w301.xyz.excel_import.mapper;

import org.apache.ibatis.annotations.Param;
import w301.xyz.excel_import.po.JourneyInfo;
import w301.xyz.excel_import.service.BasicMapper;

import java.util.List;

public interface JourneyTableMapper extends BasicMapper<JourneyInfo> {
    public List<JourneyInfo> getLatestByDate(@Param("date") String date);

    int insertExcelDateByBatch(@Param("entity") JourneyInfo entity);


    JourneyInfo countBeforeInsert(@Param("info") JourneyInfo journeyInfo);

    void deleteRepeatableDate(@Param("entity") JourneyInfo journeyInfo);

    Integer countAfterInsert(@Param("criterion") JourneyInfo criterion);
}
