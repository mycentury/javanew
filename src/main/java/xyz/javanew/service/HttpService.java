/**
 * 
 */
package xyz.javanew.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.ContentType;
import org.bytedeco.javacpp.opencv_legacy.lsh_hash;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import xyz.javanew.constant.Method;
import xyz.javanew.util.TypeConverterUtil;

/**
 * @Desc
 * @author wewenge.yan
 * @Date 2017年3月9日
 * @ClassName HttpService
 */
@Service
public class HttpService {
    @Value("${http.connect.timeout}")
    private Integer connectTimeout;
    @Value("${http.socket.timeout}")
    private Integer socketTimeout;

    @Autowired
    private RestTemplate restTemplate;
    
    public Map<String,List<String>> requestByUrl(String urlString, Method method, Map<String, String> headers, String body) {
        Map<String,List<String>> result = new HashMap<String, List<String>>();
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method.name());
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(socketTimeout);
            conn.setUseCaches(false);
            conn.setDoInput(true);
            boolean isGet = Method.GET.equals(method);
            if (isGet) {
                conn.setDoOutput(false);
                conn.setInstanceFollowRedirects(false);
            } else {
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(true);
            }
            String accept = null;
            String charset = null;
            if (!CollectionUtils.isEmpty(headers)) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    if ("charset".equalsIgnoreCase(entry.getKey().toString())) {
                        charset = entry.getValue().toString();
                    }
                    if ("accept".equalsIgnoreCase(entry.getKey().toString())) {
                        accept = entry.getValue().toString();
                    }
                    conn.setRequestProperty(entry.getKey().toString(),entry.getValue().toString());
                }
            }
            if (charset==null) {
                charset = Charset.defaultCharset().name();
                accept = Charset.defaultCharset().name();
            }
            conn.connect();
            if (!StringUtils.isEmpty(body)) {
                conn.getOutputStream().write(body.getBytes(charset));
                conn.getOutputStream().flush();
            }
            result.putAll(conn.getHeaderFields());
            InputStream inputStream = conn.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024*10];
            int length = 0;
            while ((length=inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer,0,length);
            }
            length = 0;
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            if (result.get("Transfer-Encoding").contains("chunked")) {
                int start = 0;
                for (int i = 0; i < byteArray.length; i++) {
                    if (length == 0 && (byteArray[i] == 10||byteArray[i] == 13 && byteArray[i+1] == 10)) {
                        System.out.println(new String(new byte[]{0,0,0},"UTF-8"));
                        String string = new String(Arrays.copyOfRange(byteArray, start, i));
                        length = Integer.valueOf(string, 16);
                    }
                }
            }
            String res = new String(byteArray, accept);
            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(res);
            result.put("res", arrayList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public <T> T requestByRestTemplate(String url, Method method, Object request, Class<T> responseType) {
        if (Method.POST.equals(method)) {
            return restTemplate.postForObject(url, request, responseType);
        } else {
            url += request == null ? "" : this.generateParamUrlByRequest(request);
            return restTemplate.getForObject(url, responseType);
        }
    }

    public Document requestByJsoup(String url, Method method, Map<String, String> headers, Map<String, String> body) {
        Document result = null;
        try {
            if (Method.POST.equals(method)) {
                Connection connect = Jsoup.connect(url).ignoreContentType(true);
                if (!CollectionUtils.isEmpty(headers)) {
                    for (Entry<String, String> header : headers.entrySet()) {
                        connect.header(header.getKey(), header.getValue());
                    }
                }
                connect.data(body);
                result = connect.post();
            } else {
                url += headers == null ? "" : this.generateParamUrlByRequest(body);
                Connection connect = Jsoup.connect(url).ignoreContentType(true);
                result = connect.get();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Document requestByJsoup(String url, Method method, Object request) {
        Document result = null;
        try {
            if (Method.POST.equals(method)) {
                Connection connect = Jsoup.connect(url);
            } else {
                url += request == null ? "" : this.generateParamUrlByRequest(request);
                Connection connect = Jsoup.connect(url);
                result = connect.get();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String request(String url, Method method, Object request) {
        Request req = null;
        if (Method.POST.equals(method)) {
            req = Request.Post(url);
            req.bodyString(this.generateParamUrlByRequest(request), ContentType.APPLICATION_JSON);
        } else {
            req = Request.Get(url).addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
        }
        req.connectTimeout(connectTimeout).socketTimeout(socketTimeout);
        try {
            return req.execute().returnContent().asString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String generateParamUrlByRequest(Object request) {
        Map<String, Object> map = TypeConverterUtil.changeSourceToMap(request);
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (Entry<String, Object> entry : map.entrySet()) {
            result.append(i == 0 ? "?" : "&");
            result.append(entry.getKey()).append("=").append(entry.getValue());
            i++;
        }
        return result.toString();
    }
}
