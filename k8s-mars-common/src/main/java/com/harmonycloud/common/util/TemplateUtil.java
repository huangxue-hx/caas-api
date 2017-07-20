package com.harmonycloud.common.util;

import freemarker.template.*;

import java.io.*;
import java.util.Map;

/**
 * Created by anson on 17/6/1.
 */
public class TemplateUtil {
    private static Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);

    static {
        cfg.setDefaultEncoding("UTF-8");
        cfg.setClassForTemplateLoading(TemplateUtil.class, "/template");
    }


    public static String generate(String templateName, Map dataModel) throws IOException,TemplateException {
        Template t = cfg.getTemplate(templateName);
        StringWriter stringWriter = new StringWriter();
        BufferedWriter writer = new BufferedWriter(stringWriter);
        try {
            t.process(dataModel,writer);
            return stringWriter.toString();
        } catch (Exception e) {
            throw e;
        }finally{
            writer.close();
        }
    }




}
