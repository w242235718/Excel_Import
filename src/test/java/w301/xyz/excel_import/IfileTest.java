package w301.xyz.excel_import;


import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class IfileTest {

    @Test
    public void testGetWebBody(){
//        getWebBody("http://172.21.1.2:3030/");
    }

    @Test
    public void testWeb(){
        getWebBody1("http://172.21.1.2:3030/");
//        getWebBody1("https://www.bilibili.com/");
    }

    public static void getWebBody1(String url){
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setActiveXNative(false);//ActiveX关闭
        webClient.getOptions().setCssEnabled(false);//不需要加载css
//        webClient.getOptions().setUseInsecureSSL(true);//启用任何连接,不检查ssl
        webClient.getOptions().setJavaScriptEnabled(true);//开启js
        webClient.getOptions().setDownloadImages(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);//js异常不抛出
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);//非200是否抛出异常,否
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        System.out.println("aaaa");
//        webClient.getOptions().setTimeout(10*1000);//等待10s
//        webClient.getOptions().setConnectionTimeToLive(15*1000);

//
        HtmlPage page=null;
        try {
            page = webClient.getPage(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            webClient.close();
        }
        webClient.waitForBackgroundJavaScript(25 * 1000);// 异步JS执行耗时

        String pageXml = page.asXml();
        System.out.println(pageXml);
    }
}
