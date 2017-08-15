/**
 * 
 */
package xyz.javanew.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import xyz.javanew.BaseTest;
import xyz.javanew.constant.Method;

/**
 * @Desc
 * @author wewenge.yan
 * @Date 2017年3月9日
 * @ClassName HttpServiceTest
 */
public class HttpServiceTest extends BaseTest {

    @Autowired
    private HttpService httpService;

    private String url = "http://www.baidu.com/s";
    
    @Test
    public void testHttpServiceTest() throws UnsupportedEncodingException {
        String url1 = "https://activity.waimai.meituan.com:443/";
        String url2 = "https://s0.meituan.com:443/";
        String url3 = "https://log.waimai.meituan.com:443/";
        String actUrl = "https://activity.waimai.meituan.com/coupon/sharechannel/B2EA8E1ABA8B47EA82DB475BA17B517D?urlKey=01F0425030544EF9A206C4AA1E429603";
        Map<String,String> headers = new HashMap<String, String>();
        headers.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
        headers.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        headers.put("Accept-Encoding","gzip, deflate, br");
        headers.put("Accept-Language","zh-CN,zh;q=0.8");
        headers.put("Connection","keep-alive");
        headers.put("Upgrade-Insecure-Requests","1");
        headers.put("Cache-Control","max-age=0");
        headers.put("Host","activity.waimai.meituan.com");
        Map<String, List<String>> result = httpService.requestByUrl(actUrl, Method.GET, headers, null);
        try {
            String res = URLDecoder.decode(result.get("res").get(0), "UTF-8");
            System.out.println(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(result);
    }

    @Test
    public void testRequestByUrl() {
        url = "http://caicaikan.win/api/query";
        Map<String,String> headers = new HashMap<String, String>();
        headers.put("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2");
//        headers.put("Cookie","BD_LAST_QID=13244742210928356861; PSTM=1500953267; BIDUPSID=ACB70921F3A75F0BB2E2233CF0529787; BAIDUID=ACB70921F3A75F0BB2E2233CF0529787:FG=1");
        Map<String, List<String>> requestByUrl = httpService.requestByUrl(url, Method.GET, headers, null);
        System.out.println(requestByUrl.get("res"));
        System.out.println(requestByUrl);
    }
    
    /**
     * Test method for
     * {@link xyz.javanew.service.HttpService#requestByRestTemplate(java.lang.String, xyz.javanew.constant.Method, java.lang.Object, java.lang.Class, java.util.Map)}
     * .
     */
    @Test
    public void testRequestByRestTemplate() {
        String response = httpService.requestByRestTemplate(url, Method.POST, null, String.class);
        System.out.println(response);
        response = httpService.requestByRestTemplate(url, Method.GET, null, String.class);
        System.out.println(response);
    }

    /**
     * Test method for
     * {@link xyz.javanew.service.HttpService#requestByJsoup(java.lang.String, xyz.javanew.constant.Method, java.lang.Object)}
     * .
     */
    @Test
    public void testRequestByJsoup() {
        Document response = httpService.requestByJsoup(url, Method.POST, null);
        System.out.println(response);
        response = httpService.requestByJsoup(url, Method.GET, null);
        System.out.println(response);
    }

    @Test
    public void testRequestByJsoup2() {
        url = "http://caicaikan.win/api/query";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json;charset=UTF-8");
        Map<String, String> body = new HashMap<String, String>();
        body.put("start", "2017070");
        body.put("end", "2017080");
        Document response = httpService.requestByJsoup(url, Method.POST, headers, body);
        System.out.println(response);
        response = httpService.requestByJsoup(url, Method.GET, null, body);
        System.out.println(response);
    }

    /**
     * Test method for
     * {@link xyz.javanew.service.HttpService#request(java.lang.String, xyz.javanew.constant.Method, java.lang.Object)}
     * .
     */
    @Test
    public void testRequest() {
        String response = httpService.request(url, Method.POST, null);
        System.out.println(response);
        response = httpService.request(url, Method.GET, null);
        System.out.println(response);
    }

}
