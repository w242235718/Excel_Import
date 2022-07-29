package w301.xyz.excel_import.util;

public enum CommonFieldMapping {
    MOBILE_P1("手机号码","手机号"),
    MOBILE_P2("手机号","手机号码"),
    SFZ_P1("证件号码","身份证号"),
    SFZ_P2("身份证号","证件号码"),
    DISTRICTS_COUNTIES_P1("市县区","县（市、区）"),
    DISTRICTS_COUNTIES_P2("县（市、区）","市县区"),
    DISTRICTS_COUNTIES_P3("县（市、区）","县市区");

    private String filedName;
    private String fieldAnotherName;
    CommonFieldMapping(String name,String anotherName){
        this.filedName=name;
        this.fieldAnotherName=anotherName;
    }
    public String getFiledName() {
        return filedName;
    }

    public String getAnotherName() {
        return fieldAnotherName;
    }
}
