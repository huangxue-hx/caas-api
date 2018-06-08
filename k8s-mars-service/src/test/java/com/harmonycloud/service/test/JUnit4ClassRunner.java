package com.harmonycloud.service.test;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.junit.runners.model.InitializationError;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class JUnit4ClassRunner extends SpringJUnit4ClassRunner {
  
    static {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        try {
            configurator.doConfigure("src/test/resources/logback.xml");
        } catch (JoranException e) {
            e.printStackTrace();
        }
    }  
      
    public JUnit4ClassRunner(Class<?> clazz) throws InitializationError {
        super(clazz);  
    }  
  
}  