package w301.xyz.excel_import.util;

public enum HeadLineExclusiveFiled {
    SEQUENCE_P1("序号"),
    SEQUENCE_P2(" ");
    private String field;
    HeadLineExclusiveFiled(String field){
        this.field=field;
    }

    public String getField() {
        return field;
    }
}
