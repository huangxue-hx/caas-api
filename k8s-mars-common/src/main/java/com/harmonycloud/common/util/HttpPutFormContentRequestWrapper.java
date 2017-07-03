package com.harmonycloud.common.util;

import java.io.*;
import java.util.*;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Created by czm on 2017/4/6.
 */
public class HttpPutFormContentRequestWrapper extends HttpServletRequestWrapper {

    private MultiValueMap<String, String> formParameters;

    private byte[] bytes;
    private WrappedServletInputStream wrappedServletInputStream;


    /**
     * 构造器.
     * @param request request
     * @param parameters 参数
     * @throws IOException IO异常
     */
    public HttpPutFormContentRequestWrapper(HttpServletRequest request, MultiValueMap<String, String> parameters) throws IOException {
        super(request);
        this.formParameters = (parameters != null) ? parameters : new LinkedMultiValueMap<String, String>();
        // 读取输入流里的请求参数，并保存到bytes里
        bytes = IOUtils.toByteArray(request.getInputStream());
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        this.wrappedServletInputStream = new WrappedServletInputStream(byteArrayInputStream);
        reWriteInputStream();
    }

    @Override
    public String getParameter(String name) {
        String queryStringValue = super.getParameter(name);
        String formValue = this.formParameters.getFirst(name);
        return (queryStringValue != null) ? queryStringValue : formValue;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> result = new LinkedHashMap<String, String[]>();
        Enumeration<String> names = this.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            result.put(name, this.getParameterValues(name));
        }
        return result;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        Set<String> names = new LinkedHashSet<String>();
        names.addAll(Collections.list(super.getParameterNames()));
        names.addAll(this.formParameters.keySet());
        return Collections.enumeration(names);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] queryStringValues = super.getParameterValues(name);
        List<String> formValues = this.formParameters.get(name);
        if (formValues == null) {
            return queryStringValues;
        } else if (queryStringValues == null) {
            return formValues.toArray(new String[formValues.size()]);
        } else {
            List<String> result = new ArrayList<String>();
            result.addAll(Arrays.asList(queryStringValues));
            result.addAll(formValues);
            return result.toArray(new String[result.size()]);
        }
    }

    /**
     * 把参数重新写进请求里.
     */
    public void reWriteInputStream() {
        wrappedServletInputStream.setStream(new ByteArrayInputStream(bytes != null ? bytes : new byte[0]));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return wrappedServletInputStream;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(wrappedServletInputStream));
    }

    /**
     * 获取post参数，可以自己再转为相应格式.
     */
    public String getRequestParams() throws IOException {
        return new String(bytes, "utf-8");
    }

    private class WrappedServletInputStream extends ServletInputStream {

        private InputStream stream;

        WrappedServletInputStream(InputStream stream) {
            this.stream = stream;
        }

        public void setStream(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public int read() throws IOException {
            return stream.read();
        }

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
    }
}
