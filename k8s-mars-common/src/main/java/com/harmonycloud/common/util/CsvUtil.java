package com.harmonycloud.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvUtil {

	private String[] content;

	private String[] head;

	public CsvUtil() {

	}

	public CsvUtil(String[] content) {
		this.content = content;
		if (content.length > 0) {
			String firstRow = content[0];
			if (firstRow.contains(",")) {
				this.head = content[0].split(",");
			}
		}
	}

	/**
	 * 获取列数
	 * 
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public int getColNum() throws Exception {
		if (content.length > 0) {
			// csv为逗号分隔文件
			if (content[0].contains(",")) {
				return content[0].split(",").length;
			} else if (content[0].trim().length() != 0) {
				return 1;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	/**
	 * 获取行数
	 * 
	 * @return
	 */
	public int getRowNum() {
		return content.length;
	}

	/**
	 * 获取指定列
	 * 
	 * @param index
	 * @return
	 */
	public String getCol(int index, int totalCol) throws Exception {
		if (totalCol == 0) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		if (totalCol > 1) {
			for (String data : content) {
				sb = sb.append(data.split(",")[index] + ",");
			}
		} else {
			for (String data : content) {
				sb = sb.append(data + ",");
			}
		}
		String str = new String(sb.toString());
		str = str.substring(0, str.length() - 1);
		return str;
	}

	/**
	 * 获取指定行
	 * 
	 * @param index
	 * @return
	 */
	public String getRow(int index) {
		if (content.length != 0) {
			return content[index];
		} else {
			return null;
		}
	}

	/**
	 * 获取某个单元格
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public String getString(int row, int col, int totalCol) {
		String temp = null;
		if (totalCol > 1) {
			temp = content[row].toString().split(",")[col];
		} else if (totalCol == 1) {
			temp = content[row].toString();
		} else {
			temp = null;
		}
		return temp;
	}

	public String[] getHead(int totalCol) throws Exception {
		if (totalCol == 0 || content.length <= 0) {
			return null;
		}
		String firstRow = content[0];
		String[] rowCon = null;
		if (firstRow.contains(",")) {
			rowCon = content[0].split(",");
		}
		return rowCon;
	}

	public Map<String, Object> rowToJson(int row) throws Exception {
		Map<String, Object> data = new HashMap<>();
		String[] rowData = content[row].split(",");
		List<String> newRowData = Arrays.asList(rowData);
		List<String> realData = new ArrayList<String>(newRowData);
		if (rowData.length < head.length) {
			for (int i = 0; i < head.length - rowData.length; i++) {
				realData.add("");
			}
		}
		if (head != null && head.length > 0) {
			for (int i = 0; i < head.length ; i++) {
				data.put(head[i], realData.get(i));
			}
		}
		return data;
	}

	public String[] getContent() {
		return content;
	}

	public void setContent(String[] content) {
		this.content = content;
	}

}