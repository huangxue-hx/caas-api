package com.harmonycloud.listener;

import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.fileupload.ProgressListener;

import com.harmonycloud.dto.application.Progress;

public class FileUploadProgressListener implements ProgressListener {
	
	private HttpSession session;

	public FileUploadProgressListener() {  }  
	
    public FileUploadProgressListener(HttpSession session) {
        this.session=session;  
        Progress status = new Progress();
        session.setAttribute("upload_ps", JSONObject.toJSONString(status));
    }  
	
	/**
	 * pBytesRead 到目前为止读取文件的比特数 pContentLength 文件总大小 pItems 目前正在读取第几个文件
	 */
	public void update(long pBytesRead, long pContentLength, int pItems) {
		Object uploadStatus = session.getAttribute("upload_ps");
		if (uploadStatus == null) {
           return;
		}
		Progress status = JSONObject.parseObject(uploadStatus.toString(),Progress.class);
		status.setBytesRead(pBytesRead);
		status.setContentLength(pContentLength);
		status.setItems(pItems);
		session.setAttribute("upload_ps", JSONObject.toJSONString(status));
	}
}
