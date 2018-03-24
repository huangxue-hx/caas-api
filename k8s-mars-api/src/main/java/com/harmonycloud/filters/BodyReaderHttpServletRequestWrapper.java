package com.harmonycloud.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;

/**
 * @Author jiangmi
 * @Description 获取post application/json请求方式的body参数
 * @Date created in 2018-1-22
 * @Modified
 */
public class BodyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper{

    private static Logger log = LoggerFactory.getLogger(BodyReaderHttpServletRequestWrapper.class);

    private final String body;

    public BodyReaderHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        StringBuffer sb = new StringBuffer();
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = request.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            if (inputStream == null) {
                sb.append("");
            }
        } catch (IOException e) {
            log.error("get request inputStream error", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("close request inputStream error", e);
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    log.error("close input reader error", e);
                }
            }
        }
        body = sb.toString();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes(Charset.forName("utf-8")));
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }

    public String getBody() {
        return this.body;
    }
}
