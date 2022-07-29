package w301.xyz.excel_import.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    private static SimpleDateFormat sd=new SimpleDateFormat("yyyy-MM-dd");

    public static String getNowDateStr(){
        return sd.format(new Date()).toString();
    }
}
