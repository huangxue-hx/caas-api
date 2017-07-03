package com.harmonycloud.common.util;

import java.io.*;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;

public class WrappedHttpServletRequest extends HttpServletRequestWrapper {

    private byte[] bytes;
    private WrappedServletInputStream wrappedServletInputStream;

    /**
     * 构造器.
     * @param request request
     * @throws IOException IO异常
     */
    public WrappedHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        // 读取输入流里的请求参数，并保存到bytes里
        bytes = IOUtils.toByteArray(request.getInputStream());
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        this.wrappedServletInputStream = new WrappedServletInputStream(byteArrayInputStream);
        reWriteInputStream();

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
        return new String(bytes, this.getCharacterEncoding());
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
