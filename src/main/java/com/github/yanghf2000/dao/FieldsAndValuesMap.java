package com.github.yanghf2000.dao;

import java.util.HashMap;
import java.util.Map;

/**
 * 键值对集合，由字段和值组成
 * @author 杨会锋
 * 2017-12-27
 */
public class FieldsAndValuesMap{

	private Map<String, Object> map = new HashMap<>();
	
	private FieldsAndValuesMap() {}
	
	/**
	 * 初始化
	 * @return
	 */
	public static FieldsAndValuesMap init() {
		return new FieldsAndValuesMap();
	}
	
	/**
	 * 追加键值对
	 * @param field
	 * @param value
	 * @return
	 */
	public FieldsAndValuesMap add(String field, Object value){
		map.put(field, value);
		return this;
	}
	
	/**
	 * 获取键值对集合
	 * @return
	 */
	public Map<String, Object> getMap(){
		return this.map;
	}
	
}
