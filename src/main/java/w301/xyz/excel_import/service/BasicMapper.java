package w301.xyz.excel_import.service;

import w301.xyz.excel_import.po.KeyCrowdNucleicAcid;

import java.util.List;

public interface BasicMapper<T>{
    /**
     * 批量插入
     * @return
     */
    Integer batchInsert(List<T> datas);
    /**
     * 查询最新日期记录
     */
    List<T> getLatestData(String date);


}
