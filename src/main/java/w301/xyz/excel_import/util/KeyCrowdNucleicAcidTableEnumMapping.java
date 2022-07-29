package w301.xyz.excel_import.util;

public enum KeyCrowdNucleicAcidTableEnumMapping {
    INDEX_ID("id","主键"),
    SEQUENCE("number","序号"),
    DISTRICTS_COUNTIES("districts_counties","县（市、区）"),
    CROWD_TYPE("crowd_type","人员类别"),
    NAME("name","姓名"),
    ID_CARD_NO("id_card_no","证件号码"),
    MOBILE("mobile","手机号"),
    CREATE_TIME("create_time","创建时间"),
    UPDATE_TIME("update_time","更新时间");

    private String filed;
    private String filedName;

    KeyCrowdNucleicAcidTableEnumMapping(String filed, String filedName) {
        this.filed = filed;
        this.filedName=filedName;
    }

    public String getFiled() {
        return filed;
    }

    public String getFiledName() {
        return filedName;
    }
}
