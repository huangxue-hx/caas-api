package com.harmonycloud.common.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtil {
	private static final ObjectMapper mapper = new ObjectMapper();

	private static Logger logger = LoggerFactory.getLogger(JsonUtil.class);
	
	/**
	 * 将对象转换成json字符串
	 * @param object
	 * @return
	 */
	public static String objectToJson(Object object){
		try {
			return mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 将json结果集转化为对象
	 * @param json
	 * @param beanType
	 * @return
	 */
	public static <T> T jsonToPojo(String json,Class<T> beanType){
		if(json == null || json.equals("")){
			return null;
		}
		try {
			T t = mapper.readValue(json,beanType);
			return t;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 将json数据转换成pojo对象list
	 * @param jsonData
	 * @param beanType
	 * @return
	 */
	 public static <T>List<T> jsonToList(String jsonData, Class<T> beanType) {
	    	JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, beanType);
	    	try {
	    		List<T> list = mapper.readValue(jsonData, javaType);
	    		return list;
			} catch (Exception e) {
				e.printStackTrace();
			} 	
	    	return null;
	    }
	 /**
	  * 将json数据转换成map
	  * @param json
	  * @return
	  */
	
	public static Map<String,Object> jsonToMap(String json){
		 Map<String,Object> map = null;
		try {
			map = mapper.readValue(json, new TypeReference<Map<String,Object>>() {});
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	 }
	
	/**
	 * 将json数据转换成list(map<String,object>)
	 * @param json
	 * @return
	 */
	public static List<Map<String, Object>> JsonToMapList(String json) {
        List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
        if (json != null) {
            try {
                retList = mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return retList;
    }

	public static String convertToJson(Object value) {
		if (mapper.canSerialize(value.getClass())) {
			try {
				return mapper.writeValueAsString(value);
			} catch (IOException e) {
				logger.warn("Error while serializing " + value + " to JSON", e);
				return null;
			}
		}
		else {
			throw new IllegalArgumentException("Value of type " + value.getClass().getName() +
					" can not be serialized to JSON.");
		}
	}
	
	public static String convertToJsonNonNull(Object value) {
		mapper.setSerializationInclusion(Include.NON_NULL);
		if (mapper.canSerialize(value.getClass())) {
			try {
				return mapper.writeValueAsString(value);
			} catch (IOException e) {
				logger.warn("Error while serializing " + value + " to JSON", e);
				return null;
			}
		}
		else {
			throw new IllegalArgumentException("Value of type " + value.getClass().getName() +
					" can not be serialized to JSON.");
		}
	}

	public static Map<String, Object> convertJsonToMap(String json) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		if (json != null) {
			try {
				retMap = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
			} catch (IOException e) {
				logger.warn("Error while reading Java Map from JSON response: " + json, e);
			}
		}
		return retMap;
	}
}
