package com.harmonycloud.service.user.auth;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class AuthManagerCrowd {
    public static final String DOMAIN = "http://crowd.harmonycloud.com:8095/crowd/rest/usermanagement/latest/";

    public static HttpURLConnection crowdPost(URL url, String contenttype, String data) throws Exception{
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", contenttype);
        connection.setRequestProperty("Charset", "UTF-8");
//		http基本认证
        String base64encodedString = Base64.getEncoder().encodeToString("mars:123456".getBytes("utf-8"));
        connection.setRequestProperty("Authorization", "Basic " + base64encodedString);
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());

        out.write(new String(data.getBytes("UTF-8")));
        out.flush();
        out.close();
        return connection;
    }

    public static HttpURLConnection crowdGet(URL url) throws Exception{
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("connection", "Keep-Alive");
        //http基本认证
        String base64encodedString = Base64.getEncoder().encodeToString("mars:123456".getBytes("utf-8"));
        connection.setRequestProperty("Authorization", "Basic " + base64encodedString);
        connection.connect();
        return connection;
    }

    public static HttpURLConnection crowdDelete(URL url) throws Exception{
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("connection", "Keep-Alive");
        //http基本认证
        String base64encodedString = Base64.getEncoder().encodeToString("mars:123456".getBytes("utf-8"));
        connection.setRequestProperty("Authorization", "Basic " + base64encodedString);
        connection.connect();
        return connection;
    }


    public static String getMessageBody(HttpURLConnection connection)throws Exception{
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuffer result = new StringBuffer();
        for (line = br.readLine(); line != null; line = br.readLine()) {
            result.append(line);
        }
        System.out.println(result.toString());
        return result.toString();
    }
}
