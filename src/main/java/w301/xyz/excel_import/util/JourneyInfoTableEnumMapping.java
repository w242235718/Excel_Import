package w301.xyz.excel_import.util;
public enum JourneyInfoTableEnumMapping {
    INDEX_ID("id","主键"),
    SEQUENCE("number","序号"),
    DATA_APPLY_TIME("data_apply_time","数据报送时间"),
    OPERATOR("operator","运营商"),
    DISTRICTS_COUNTIES("districts_counties","区县"),
    TOWNSHIP("township","乡镇"),
    NAME("name","姓名"),
    MOBILE("mobile","手机号"),
    ID_CARD_NO("id_card_no","证件号码"),
    COME_XIN_TIME("come_xin_time","入新时间"),
    SOURCE("source","来源地"),
    BASE_STATION_LOCATION("base_station_location","基站位置"),
    IMSI("imsi","IMSI"),
    CREATE_TIME("create_time","创建时间"),
    UPDATE_TIME("update_time","更新时间");
    private String filed;
    private String filedName;
    JourneyInfoTableEnumMapping(String filed, String name){
        this.filed=filed;
        this.filedName=name;
    }

    public String getFiled() {
        return filed;
    }

    public String getFiledName() {
        return filedName;
    }
}
