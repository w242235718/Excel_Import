package w301.xyz.excel_import.util;

public enum CompatibleFieldMapping {
    SFZ_NO_1("id_card_no","身份证号"),
    SFZ_NO_2("id_card_no","证件号码"),
    PHONE_NO_1("id_card_no","手机号"),
    PHONE_NO_2("id_card_no","手机号码");

    private String filed;
    private String filedName;

    CompatibleFieldMapping(String filed, String filedName) {
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
