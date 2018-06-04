package com.harmonycloud.dao.application;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.plugin.Invocation;

import java.util.Properties;

public class Interceptor implements org.apache.ibatis.plugin.Interceptor{
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        RoutingStatementHandler routingStatementHandler = (RoutingStatementHandler)invocation.getTarget();
        ParameterHandler p = routingStatementHandler.getParameterHandler();
        p.getParameterObject();
        invocation.getArgs();
        return null;
    }

    @Override
    public Object plugin(Object o) {
        return null;
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
