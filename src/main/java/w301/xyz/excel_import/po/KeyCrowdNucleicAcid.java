package w301.xyz.excel_import.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyCrowdNucleicAcid {
    private Long id;
    private String number;
    private String districtsCounties;
    private String crowdType;
    private String name;
    private String idCardNo;
    private String mobile;
    private Date createTime;
    private Date updateTime;
}
