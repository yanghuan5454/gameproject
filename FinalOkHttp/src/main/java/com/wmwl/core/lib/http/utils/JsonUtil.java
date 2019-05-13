/**
 * Copyright 2015 DevStore
 *
 * All right reserved
 *
 * Created on 2015-5-26 下午1:59:54 
 */
package com.wmwl.core.lib.http.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 
 * @author RilkeZhu
 * @version 1.0
 * @param <T>
 * @date 2015-5-26下午1:59:54
 */
public class JsonUtil {

	private static JsonUtil instance;

	public static final JsonUtil getInstance() {
		if (instance == null) {
			synchronized (JsonUtil.class) {
				if (instance == null) {
					instance = new JsonUtil();
				}
			}
		}
		return instance;
	}
	
	/**
	 * 将JsonObject转化成HashMap
	 * @param job
	 * @return
	 */
	public HashMap<String,String> Json2HashMap(JSONObject job){
		HashMap<String, String> data = new HashMap<String, String>();
	    // 将json字符串转换成jsonObject
	    Iterator<String> ite = job.keys();
	    // 遍历jsonObject数据,添加到Map对象
	    while (ite.hasNext()) {
	        String key = ite.next().toString();
	        String value = null;
			try {
				value = job.get(key).toString();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        data.put(key, value);
	    }
	    // 或者直接将 jsonObject赋值给Map
	    // data = jsonObject;
	    return data;
	}

	/**
	 * 解析JSON
	 * 
	 * @param json
	 * @param trf
	 * @return
	 */
	public <T> T fromJson(String json, TypeReference<T> trf) {
		T t = null;
		try {
			t = JSON.parseObject(json, trf);
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		return t;
	}
	
	/**
	 * 解析JSON
	 * 
	 * @param json
	 * @param clzz
	 * @return
	 */
	public <T> List<T> fromJsonArray(String json, Class<T> clzz) {
		List<T> t = null;
		try {
			t = JSON.parseArray(json, clzz);
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		return t;
	}

	/**
	 * 
	 * 解析Json
	 * 
	 * @date 2015-5-26下午2:03:13
	 * @param json
	 * @param clzz
	 * @return
	 */
	public <T> T fromJson(String json, Class<T> clzz) {
		T t = null;
		try {
			t = JSON.parseObject(json, clzz);
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		return t;
	}

	/**
	 * 转换成JSON
	 * 
	 * @param obj
	 * @return
	 */
	public String toJson(Object obj) {
		return JSON.toJSONString(obj);
	}

}
