package w301.xyz.excel_import.util;

public enum TableHeadLineForFilterEnum {
    SEQUENCE_P1("序号"," "),
    SEQUENCE_P2(" ","序号");

    private String filedName;
    private String fieldAnotherName;

    TableHeadLineForFilterEnum(String filedName, String fieldAnotherName){
        this.filedName=filedName;
        this.fieldAnotherName=fieldAnotherName;
    }

    public String getFiledName() {
        return filedName;
    }

    public String getFieldAnotherName() {
        return fieldAnotherName;
    }
}
