package fctg.profile.transform.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class HttpClientUtil {
	@Autowired
    private CloseableHttpClient httpClient;
 
    @Autowired
    private RequestConfig config;
 
    // 编码格式。发送编码格式统一用UTF-8
    private static final String ENCODING = "UTF-8";
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    /**
     * Get请求
     * @param url 地址
     * @param headers 请求头
     * @param params 请求参数
     * @return
     */
    public String doGet(String url, Map<String, String> headers, Map<String, String> params) {
        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        // 创建访问的地址
        HttpGet httpGet=getUrlParam(url, params);
        httpGet.setConfig(config);
        packageHeader(headers, httpGet);
 
        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 由客户端执行(发送)Get请求
            response = httpClient.execute(httpGet);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
//            System.out.println("响应状态为:" + response.getStatusLine());
//            if (responseEntity != null) {
//                System.out.println("响应内容长度为:" + responseEntity.getContentLength());
//                System.out.println("响应内容为:" + EntityUtils.toString(responseEntity));
//            }
            return EntityUtils.toString(responseEntity);
        } catch (ClientProtocolException e) {
            logger.info(e.getMessage());
        } catch (ParseException e) {
        	logger.info(e.getMessage());
        } catch (IOException e) {
        	logger.info(e.getMessage());
        } finally {
            //5.回收链接到连接池
            if (null != response) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                	logger.info(e.getMessage());
                }
            }
        }
        return null;
    }
 
    /**
     * Get请求
     * @param url 地址
     * @param params 请求参数
     * @return
     */
    public String doGet(String url, Map<String, String> params){
        return doGet(url,null,params);
    }
 
    /**
     * Get请求
     * @param url 地址
     * @return
     */
    public String doGet(String url){
        return doGet(url,null,null);
    }
 
    /**
     * Post请求
     * @param url 地址
     * @param headers  请求头
     * @param params  请求参数
     * @param params  url请求参数
     * @return
     */
    public String doPost(String url, Map<String, String> headers, Map<String, String> params,Map<String, String> UrlParams){
        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        // 创建访问的地址
        HttpPost httpPost = postUrlParam(url,UrlParams);
        httpPost.setConfig(config);
 
        packageHeader(headers, httpPost);
        packageParam(headers, httpPost);
 
        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 由客户端执行(发送)Get请求
            response = httpClient.execute(httpPost);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            return EntityUtils.toString(responseEntity);
        } catch (ClientProtocolException e) {
        	logger.info(e.getMessage());
        } catch (ParseException e) {
        	logger.info(e.getMessage());
        } catch (IOException e) {
        	logger.info(e.getMessage());
        } finally {
            //5.回收链接到连接池
            if (null != response) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                	logger.info(e.getMessage());
                }
            }
        }
        return null;
    }
 
    /**
     * Post请求
     * @param url 地址
     * @param headers 请求头
     * @param urlParams url请求参数
     * @return
     */
    public String doPost(String url, Map<String,String> headers,Map<String, String> urlParams){
        return doPost(url,headers,null,urlParams);
    }
 
    /**
     * Post请求
     * @param url 地址
     * @param urlParams url请求参数
     * @return
     */
    public String doPost(String url, Map<String, String> urlParams){
        return doPost(url,null,null,urlParams);
    }
 
    /**
     * Post请求
     * @param url 地址
     * @return
     */
    public String doPost(String url){
        return doPost(url,new HashMap<>());
    }
 
    /**
     * Post请求
     * @param url 地址
     * @param headers  请求头
     * @param params  url请求参数
     * @param json  json请求体
     * @return
     */
    public String doPost(String url, Map<String, String> headers, Map<String, String> params,String json){
        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        // 创建访问的地址
        HttpPost httpPost = postUrlParam(url,params);
        httpPost.setConfig(config);
        if(headers==null){
            headers=new HashMap<>();
        }
        headers.put("Content-Type", "application/json;charset=utf-8");
        packageHeader(headers, httpPost);
        httpPost.setEntity(new StringEntity(json, "UTF-8"));
 
        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 由客户端执行(发送)Get请求
            response = httpClient.execute(httpPost);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
//            logger.info("http "+EntityUtils.toString(responseEntity));
            return EntityUtils.toString(responseEntity);
        } catch (ClientProtocolException e) {
            logger.info(e.getMessage());
        } catch (ParseException e) {
            logger.info(e.getMessage());
        } catch (IOException e) {
            logger.info(e.getMessage());
        } finally {
            //5.回收链接到连接池
            if (null != response) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    logger.info(e.getMessage());
                }
            }
        }
        return null;
    }
 
    /**
     * 简单json请求
     * @param url 地址
     * @param json  json请求体
     * @return
     */
    public String doPost(String url, String json){
        Map<String, String> headers=new HashMap<>();
        headers.put("Content-Type", "application/json;charset=utf8");
        return doPost(url,headers,null,json);
    }
    
    public String doPostXML(String url, String XML){
        Map<String, String> headers=new HashMap<>();
        headers.put("Content-Type", "application/xml;charset=utf8");
        return doPostXML(url,headers,null,XML);
    }
 
    public String doPostXML(String url, Map<String, String> headers, Map<String, String> params,String XML){
        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        // 创建访问的地址
        HttpPost httpPost = postUrlParam(url,params);
        httpPost.setConfig(config);
        if(headers==null){
            headers=new HashMap<>();
        }
        headers.put("Content-Type", "application/xml;charset=utf8");
        packageHeader(headers, httpPost);
        httpPost.setEntity(new StringEntity(XML, "UTF-8"));
 
        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 由客户端执行(发送)Get请求
            response = httpClient.execute(httpPost);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            return EntityUtils.toString(responseEntity, "utf-8");
        } catch (ClientProtocolException e) {
            logger.info(e.getMessage());
        } catch (ParseException e) {
            logger.info(e.getMessage());
        } catch (IOException e) {
            logger.info(e.getMessage());
        } finally {
            //5.回收链接到连接池
            if (null != response) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    logger.info(e.getMessage());
                }
            }
        }
        return null;
    }
    
    private HttpGet getUrlParam(String url, Map<String, String> params) {
        try{
            URIBuilder uriBuilder = new URIBuilder(url);
            if (params != null) {
                Set<Map.Entry<String, String>> entrySet = params.entrySet();
                for (Map.Entry<String, String> entry : entrySet) {
                    uriBuilder.setParameter(entry.getKey(), entry.getValue());
                }
            }
            // 创建Get请求
            return new HttpGet(uriBuilder.build());
        }catch (URISyntaxException e){
            throw new RuntimeException("url语法错误！");
        }
    }
 
    private HttpPost postUrlParam(String url, Map<String, String> params) {
        try{
            URIBuilder uriBuilder = new URIBuilder(url);
            if (params != null) {
                Set<Map.Entry<String, String>> entrySet = params.entrySet();
                for (Map.Entry<String, String> entry : entrySet) {
                    uriBuilder.setParameter(entry.getKey(), entry.getValue());
                }
            }
            // 创建Get请求
            return new HttpPost(uriBuilder.build());
        }catch (URISyntaxException e){
            throw new RuntimeException("url语法错误！");
        }
    }
 
    /**
     * Description: 封装请求头
     * @param params
     * @param httpMethod
     */
    private void packageHeader(Map<String, String> params, HttpRequestBase httpMethod) {
        // 封装请求头
        if (params != null) {
            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                // 设置到请求头到HttpRequestBase对象中
                httpMethod.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }
 
    /**
     * Description: 封装请求参数
     *
     * @param params
     * @param httpMethod
     * @throws UnsupportedEncodingException
     */
    private void packageParam(Map<String, String> params, HttpEntityEnclosingRequestBase httpMethod) {
        // 封装请求参数
        if (params != null) {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
 
            // 设置到请求的http对象中
            try {
                httpMethod.setEntity(new UrlEncodedFormEntity(nvps, ENCODING));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("不支持的地址格式！");
            }
        }
    }

}
