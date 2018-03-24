package com.harmonycloud.common.util;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.Charset;
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

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
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

	public static String runCommand(String host, String user, String password, String cmd) throws Exception {
		Session session = connect(host,user,password);
		String result =  runCommand(session, cmd, CommonConstant.DEFAULT_CHARSET_UTF_8);
		disconnect(session);
		return result;
	}

	public static String runCommand(String host, String user, String password, String cmd, String charset) throws Exception {
		Session session = connect(host,user,password);
		String result =  runCommand(session, cmd, charset);
		disconnect(session);
		return result;
	}

	public static String runCommand(Session session, String cmd, String charset) throws MarsRuntimeException {
		try {
			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
			channelExec.setCommand(cmd);
			channelExec.setInputStream(null);
			channelExec.setErrStream(System.err);
			channelExec.connect();
			InputStream in = channelExec.getInputStream();
			charset = StringUtils.isBlank(charset) ? CommonConstant.DEFAULT_CHARSET_UTF_8 : charset;
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName(charset)));
			String line = null;
			StringBuffer result = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				result.append(line + "\n");
			}
			reader.close();
			channelExec.disconnect();
			return result.toString();
		} catch (Exception e) {
			throw new MarsRuntimeException(ErrorCodeMessage.NODE_CONNECT_ERROR.value());
		}

	}

	public static Session connect(String host, String user, String password) throws MarsRuntimeException {
		JSch jsch = new JSch();
		Session session = null;
		try {
			session = jsch.getSession(user, host, 22);
			// 如果服务器连接不上，则抛出异常
			if (session == null) {
				throw new MarsRuntimeException(ErrorCodeMessage.NODE_CONNECT_ERROR.value());
			}
			// 设置登陆主机的密码
			session.setPassword(password);
			// 设置第一次登陆的时候提示，可选值：(ask | yes | no)
			session.setConfig("StrictHostKeyChecking", "no");
			// 设置登陆超时时间
			session.connect(5000);
		} catch (Exception e) {
			throw new MarsRuntimeException(ErrorCodeMessage.NODE_CONNECT_ERROR.value());
		}
		return session;
	}

	public static void disconnect(Session session){
		session.disconnect();
	}



}