package com.sky.test;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class HttpClientTest {

    /**
     * 测试通过HttpClient发送GET请求
     */
    @Test
    public void testGET() {
        // 1. 创建 HttpClient 对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // 2. 创建 HttpGet 请求对象，并指定目标 URL
        HttpGet httpGet = new HttpGet("http://localhost:8080/user/shop/status");

        CloseableHttpResponse response = null;
        try {
            // 3. 执行请求，获取响应
            response = httpClient.execute(httpGet);

            // 4. 解析响应结果
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("GET 请求状态码：" + statusCode);

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // 将响应实体转换为字符串打印
                String result = EntityUtils.toString(entity, "UTF-8");
                System.out.println("GET 请求响应数据：" + result);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 5. 释放资源
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 测试通过HttpClient发送POST请求
     */
    @Test
    public void testPOST() throws JSONException {
        // 1. 创建 HttpClient 对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // 2. 创建 HttpPost 请求对象
        HttpPost httpPost = new HttpPost("http://localhost:8080/admin/employee/login");

        // 3. 构造请求参数 (这里模拟登录，构造 JSON 格式的数据)
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", "admin");
        jsonObject.put("password", "123456");

        StringEntity entity = new StringEntity(jsonObject.toString(), "UTF-8");
        // 指定请求数据的内容类型为 JSON
        entity.setContentType("application/json");
        httpPost.setEntity(entity);

        CloseableHttpResponse response = null;
        try {
            // 4. 执行请求，获取响应
            response = httpClient.execute(httpPost);

            // 5. 解析响应结果
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("POST 请求状态码：" + statusCode);

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String result = EntityUtils.toString(responseEntity, "UTF-8");
                System.out.println("POST 请求响应数据：" + result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 6. 释放资源
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}