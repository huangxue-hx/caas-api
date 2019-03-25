package com.harmonycloud.common.util;


import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

/**
 * Created by anson on 17/6/2.
 */
public class XmlUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtil.class);

    public static Map<String, Object> parseXmlStringToMap(String xmlString){
        Map<String, Object> map = new HashMap<String, Object>();

        Document doc = null;
        try {
            doc = DocumentHelper.parseText(xmlString);
        } catch (DocumentException e) {
            LOGGER.warn("parseXmlStringToMap失败", e);
        }
        if (doc == null)
            return map;
        Element rootElement = doc.getRootElement();
        element2map(rootElement,map);
        return map;

    }

    private static void element2map (Element elmt,Map map) {
        if (null == elmt) {
            return;
        }
        String name = elmt.getName();
        if (elmt.isTextOnly()) {
            map.put(name, elmt.getText());
        } else {
            Map<Object, Object> mapSub = new HashMap<Object, Object>();
            List<Element> elements = (List<Element>) elmt.elements();
            for (Element elmtSub : elements) {
                element2map(elmtSub, mapSub);
            }
            Object first = map.get(name);
            if (null == first) {
                map.put(name, mapSub);
            } else {
                if (first instanceof List<?>) {
                    ((List) first).add(mapSub);
                } else {
                    List<Object> listSub = new ArrayList<Object>();
                    listSub.add(first);
                    listSub.add(mapSub);
                    map.put(name, listSub);
                }
            }
        }
    }

}
