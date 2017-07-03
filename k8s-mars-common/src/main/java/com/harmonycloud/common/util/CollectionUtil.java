package com.harmonycloud.common.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 2017-01-17 created
 * 
 * @author jmi
 *
 */
public class CollectionUtil {

	/**
	 * List<Map<String, Object>>去重
	 * 
	 * @param list
	 * @return
	 */
	public static List<Map<String, Object>> rmDuplicate(List<Map<String, Object>> list) {
		if (list != null) {
			Set<Map<String, Object>> setMap = new HashSet<Map<String, Object>>();
			List<Map<String, Object>> listMap2 = new ArrayList<Map<String, Object>>();
			for (Map<String, Object> map1 : list) {
				if (setMap.add(map1)) {
					listMap2.add(map1);
				}
			}
			return listMap2;
		}
		return list;
	}

	public static Map<String, Object> transBean2Map(Object obj) throws Exception {

		if (obj == null) {
			return null;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for (PropertyDescriptor property : propertyDescriptors) {
			String key = property.getName();

			// 过滤class属性
			if (!key.equals("class")) {
				// 得到property对应的getter方法
				Method getter = property.getReadMethod();
				Object value = getter.invoke(obj);

				map.put(key, value);
			}

		}
		return map;
	}
}
