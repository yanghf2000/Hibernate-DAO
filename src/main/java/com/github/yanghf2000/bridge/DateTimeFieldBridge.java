package com.github.yanghf2000.bridge;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.TwoWayFieldBridge;
import org.hibernate.search.bridge.spi.IgnoreAnalyzerBridge;

/**
 * DateTimeFieldBridge<br>
 * 将 {@link LocalDate} {@link LocalTime} {@link LocalDateTime} 转换为字符串索引<br>
 * <hr>
 * 在只加以下注解时，不加DateTimeFieldBridge：
 * 	@Field(index = org.hibernate.search.annotations.Index.YES, analyze = Analyze.NO, store = Store.NO)<br>
 * 分析的结果为 +0000019990909(加号不能少)，对于sentence语句，是不做分析的，为toString()方法<br/>
 * 此时，若将sentence的key也转换成此格式，此时都可以搜索到，range方法也能查到结果<br/><br/>
 * 将analyze改为 analyze = Analyze.YES后，range sentence能查到结果，但match查不到<br/><br/>
 * <hr>
 * 在加上@FieldBridge(impl = DateTimeFieldBridge.class)注解后，处理后，不管分析加不加上，因为此时key值都
 * 转换成了自定义key值，所以都可以通过
 * @author 杨会锋
 * 2018-4-30
 */
public class DateTimeFieldBridge implements TwoWayFieldBridge, IgnoreAnalyzerBridge{

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		luceneOptions.addFieldToDocument(name, objectToString(value), document);
	}
	
	/**
	 * 要保存的索引值
	 */
	@Override
	public String objectToString(Object object) {
		return toKey(object);
	}

	@Override
	public Object get(String name, Document document) {
		return null;
	}
	
	/**
	 * 将值转换成字符串key
	 * @param value
	 * @return
	 */
	public static String toKey(Object value){
		String s = null;
		if (value != null) {
			if(value instanceof LocalDate){
				LocalDate date = (LocalDate)value;
				s = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
			}else if(value instanceof LocalTime){
				LocalTime time = (LocalTime)value;
				s = time.format(DateTimeFormatter.ofPattern("HHmmss"));
			}else if(value instanceof LocalDateTime){
				LocalDateTime dateTime = (LocalDateTime)value;
				s = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
			}
		}
		return s;
	}

}
