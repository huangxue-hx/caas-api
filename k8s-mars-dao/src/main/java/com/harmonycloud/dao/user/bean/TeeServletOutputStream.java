package com.harmonycloud.dao.user.bean;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import org.apache.commons.io.output.TeeOutputStream;

/**
 * Created by czm on 2017/4/1.
 */
public class TeeServletOutputStream extends ServletOutputStream {
    private final TeeOutputStream teeOutputStream;

    public TeeServletOutputStream(OutputStream one, OutputStream two) {
        this.teeOutputStream = new TeeOutputStream(one, two);
    }


    @Override
    public void write(int byteStream) throws IOException {
        this.teeOutputStream.write(byteStream);
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        this.teeOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.teeOutputStream.close();
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }
}
