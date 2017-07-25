package com.harmonycloud.common.util;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.JSch;

/**
 * Created by zgl on 2017/7/28.
 */
@Component
public class JSchClient {
	private static Logger logger = LoggerFactory.getLogger(JSchClient.class);

	public static JSch JSchClient;
	public static synchronized JSch createJSch() {
		if (JSchClient == null) {
				try {
				    JSchClient = new JSch();
					return JSchClient;
				} catch (Exception e) {
					logger.error("创建JSch Client 失败", e);
				}
		}
		return JSchClient;
	}

	

}