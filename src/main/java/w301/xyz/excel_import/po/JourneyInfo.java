package w301.xyz.excel_import.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JourneyInfo {
    private Long id;
    private String number;
    private String dataApplyTime;
    private String operator;
    private String districtsCounties;
    private String township;
    private String name;
    private String mobile;
    private String idCardNo;
    private String comeXinTime;
    private String source;
    private String baseStationLocation;
    private String imsi;
    private Date createTime;
    private Date updateTime;
}
