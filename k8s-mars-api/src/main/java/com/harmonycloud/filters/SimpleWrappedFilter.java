package com.harmonycloud.filters;

import com.alibaba.fastjson.JSON;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.user.bean.OperationResult;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

public class SimpleWrappedFilter implements Filter {
    private static Logger log = LoggerFactory.getLogger(SimpleWrappedFilter.class);


    public void init(FilterConfig filterConfig) throws ServletException {

    }

    /**
     * 包装Response，将操作日志存储到ES.
     *
     * @param request  请求
     * @param response 响应
     * @param chain    拦截器链
     * @throws IOException      IO异常
     * @throws ServletException Servlet异常
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;


        String opType = "";
        String opStatus = "false";
        String opDate = "";
        String username = "";
        String opFun = "";

        String uri = req.getRequestURI();


        String moduleKey = DicUtil.parseModelName(uri, 3);
        final String moduleName = DicUtil.get(moduleKey);

        //模块名称不为空
        if (StringUtils.isNotBlank(moduleName)) {
//            opFun = DicUtil.get(DicUtil.parseModelName(model, model.length));
            opFun = DicUtil.get(DicUtil.parseDicKey(uri, req.getMethod()));
            if (StringUtils.isNotBlank(opFun)) {
                opType = req.getMethod();
//                opDate = DateUtil.timeFormat.format(new Date());
                  Date date = DateUtil.getCurrentUtcTime();
                date = new Date(date.getTime() +  8 * 60 * 60 * 1000L);
//                System.out.println("审计时间data.getTime():加上8之后"+date);

                opDate = DateUtil.timeFormat.format(date.getTime());

                HttpSession session = req.getSession();
                username = (String) session.getAttribute("username");
            }
        }

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {

            HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper((HttpServletResponse) response) {
                @Override
                public ServletOutputStream getOutputStream() throws IOException {
                    OutputStream os = super.getOutputStream();
                    return new TeeServletOutputStream(os, byteArrayOutputStream);
                }
            };
            chain.doFilter(request, responseWrapper);
            opStatus = "true";

        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        } finally {
            if (StringUtils.isNotBlank(moduleName) && StringUtils.isNotBlank(opFun)) {
                String resParam = byteArrayOutputStream.toString("utf-8");
                String reqParam = DicUtil.parseParams((HttpServletRequest) request);
                StringBuffer opDetails = new StringBuffer();
                String url = req.getRequestURI();
                if (StringUtils.isNotBlank(reqParam)) {
                    opDetails.append(reqParam);
                    if (url.contains("login") || url.indexOf("validation") >=0 || url.indexOf("getToken") >= 0|| url.contains("changePwd")|| url.contains("adminReset")|| url.contains("userReset")) {
                        opDetails.replace(0,opDetails.toString().length(),"请求参数:*****");
                    }
                }
                if (StringUtils.isNotBlank(resParam)) {
                    opDetails.append("返回结果：" + resParam);
                    //解析结果
                    opStatus = JSON.parseObject(resParam, OperationResult.class).getSuccess();
                }



                if (url.indexOf("login") >= 0 || url.indexOf("validation") >=0 || url.indexOf("getToken") >= 0) {

                    username = req.getParameter("username");
                }

                log.info("操作详情" + opDetails.toString());

                final SearchResult sr = new SearchResult();
                sr.setOpTime(opDate);
                sr.setUser(username);
                sr.setOpStatus(opStatus);
                sr.setModule(moduleName);
                //sr.setOpDetails(opDetails.toString());
                //sr.setOpType(opType);
                sr.setOpFun(opFun);


                Runnable worker = new Runnable() {
                    @Override
                    public void run() {
                        ActionReturnUtil flag = new ActionReturnUtil();
                        try {

                            flag = ESFactory.insertToIndexBySR(sr);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        log.info("插入是否成功" + flag.get("success"));
                    }
                };

                if(StringUtils.isNotBlank(username)&&StringUtils.isNotBlank(opStatus)){
                    ESFactory.executor.execute(worker);
                }




            }

        }
    }

    public void destroy() {

    }

}
