package com.github.yanghf2000.queryobject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.query.dsl.*;
import org.hibernate.search.spatial.DistanceSortField;

import com.github.yanghf2000.bridge.DateTimeFieldBridge;

/**
 * 封装搜索查询对象<p>
 * 由于Dao的子类是单例的，所以这个对象的实例不能作为Dao的变量<p>
 * @author 杨会锋
 * 2018-1-12
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class QuerySearchObject<T>{
	
	private FullTextSession fullTextSession;
	private QueryBuilder qb;
	private Criteria criteria;
	private org.apache.lucene.search.Query query;
	private FullTextQuery hibQuery; 
	
	private Class<T> clazz;
	private List<org.apache.lucene.search.Query> queries = new ArrayList<>();
	private Set<String> joinFields = new HashSet<>();
	
	private List<SortField> sortFields = new ArrayList<>();
	
	// 和求距离相关
	private String distanceField;
	private Double centerLongitude = null, centerLatitude = null;
	
	public static <T>QuerySearchObject<T> getInstance(Session session, Class<T> clazz){
		return new QuerySearchObject(session, clazz);
	}
	
	@SuppressWarnings("deprecation")
	private QuerySearchObject(Session session, Class<T> clazz){
		this.fullTextSession = Search.getFullTextSession(session);
		this.qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(clazz).get();
		this.criteria = fullTextSession.createCriteria(clazz);
		this.clazz = clazz;
	}
	
	// ********************************** 以下为添加条件 ********************************
	
	/**
	 * sentence，能与所有的关键词匹配上才能查找到结果<br>
	 * 比如：字段是：你叫什么名字 value 是什么 什么名字都可以，但如果是什么的名字，就查找不到，只有要有一个不匹配就不行<br>
	 * 对于xx.xx这种级联查询，不能用sentence，要用match，否则报错，找不到字段
	 * So far we have been looking for words or sets of words, you can also search exact or approximate sentences. Use phrase() to do so<br>
	 * @param fieldName 字段名
	 * @param value 值
	 * @return {@link QuerySearchObject}
	 */
	public QuerySearchObject<T> sentence(String fieldName, Object value){
		needJoinTable(fieldName);
		
		String val = value + "";
		
		Field field = null;
		try {
			// TODO 这里有一个bug，如果是子类的话，field会获取不到
			field = clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		Annotation[] annotations = field.getAnnotations();
		for(Annotation a : annotations){
			if(a.annotationType() == FieldBridge.class){
				FieldBridge fb = (FieldBridge)a;
				if(fb.impl() == DateTimeFieldBridge.class)
					val = DateTimeFieldBridge.toKey(value);
			}
		}
		
		queries.add(qb.phrase().onField(fieldName).sentence(val).createQuery());
		return this;
	}

	/**
	 * 范围条件， 默认搜索String类型的，其他类型的搜索不到，要指明类型<br>
	 * @param fieldName
	 * @param min
	 * @param max
	 */
	@Deprecated
	public QuerySearchObject<T> range(String fieldName, Object min, Object max){
		return range(fieldName, min, max, null);
	}

	/**
	 * 范围条件， 默认搜索String类型的，其他类型的搜索不到，要指明类型<br>
	 * @param min
	 * @param max
	 * @param fieldNames
	 */
	public QuerySearchObject<T> range(Object min, Object max, String... fieldNames){
		return range(min, max, null, fieldNames);
	}

	/**
	 * 范围条件<br>
	 * @param fieldName
	 * @param min
	 * @param max
	 * @param type 指定要比较的类型，对于这种搜索，若类型不对是搜不出来结果的。比如，要搜索的字段是double，但传入的是Int, 则搜不到结果
	 */
	@Deprecated
	public QuerySearchObject<T> range(String fieldName, Object min, Object max, Class type){
		needJoinTable(fieldName);
		queries.add(qb.range().onField(fieldName).from(typeConver(min, type)).to(typeConver(max, type)).createQuery());
		return this;
	}

	/**
	 * 范围条件<br>
	 * @param fieldNames
	 * @param min
	 * @param max
	 * @param type 指定要比较的类型，对于这种搜索，若类型不对是搜不出来结果的。比如，要搜索的字段是double，但传入的是Int, 则搜不到结果
	 */
	public QuerySearchObject<T> range(Object min, Object max, Class type, String... fieldNames){
		RangeMatchingContext rangeMatchingContext = null;
		for(int i = 0; i < fieldNames.length; i++) {
			String fieldName = fieldNames[i];
			needJoinTable(fieldName);
			if(i == 0) {
				rangeMatchingContext = qb.range().onField(fieldName);
			} else {
				rangeMatchingContext.andField(fieldName);
			}

		}

		queries.add(rangeMatchingContext.from(typeConver(min, type)).to(typeConver(max, type)).createQuery());
		return this;
	}

	/**
	 * above
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public QuerySearchObject<T> above(String fieldName, Object value){
		return above(fieldName, value, null);
	}
	
	/**
	 * above，在搜索值之上，包含搜索的值
	 * @param fieldName		字段名
	 * @param value			值
	 * @param type 指定要比较的类型，对于这种搜索，若类型不对是搜不出来结果的。比如，要搜索的字段是double，但传入的是Int, 则搜不到结果
	 * @return
	 */
	public QuerySearchObject<T> above(String fieldName, Object value, Class type){
		needJoinTable(fieldName);
		queries.add(qb.range().onField(fieldName).above(typeConver(value, type)).createQuery());
		return this;
	}
	
	/**
	 * below
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public QuerySearchObject<T> below(String fieldName, Object value){
		return below(fieldName, value, null);
	}
	
	/**
	 * below
	 * @param fieldName
	 * @param value
	 * @param type 指定要比较的类型，对于这种搜索，若类型不对是搜不出来结果的。比如，要搜索的字段是double，但传入的是Int, 则搜不到结果
	 * @return
	 */
	public QuerySearchObject<T> below(String fieldName, Object value, Class type){
		needJoinTable(fieldName);
		queries.add(qb.range().onField(fieldName).below(typeConver(value, type)).createQuery());
		return this;
	}
	
	/**
	 * 类型转换
	 * @param value 要转换的值
	 * @param type 指定要比较的类型，对于这种搜索，若类型不对是搜不出来结果的。比如，要搜索的字段是double，但传入的是Int, 则搜不到结果
	 * @return {@link Object}
	 */
	private Object typeConver(Object value, Class type) {
		Object val = value;
		if(type == null) {
			// 什么也不做
		}else if(type == int.class || type == Integer.class) {
			val = Integer.valueOf(value + "");
		}else if(type == long.class || type == Long.class) {
			val = Long.valueOf(value + "");
		}else if(type == short.class || type == Short.class) {
			val = Short.valueOf(value + "");
		}else if(type == double.class || type == Double.class) {
			val = Double.valueOf(value + "");
		}
		
		return val;
	}
	
	/**
	 * 匹配，只要有一个字或一个词匹配上就能查到结果
	 * @param value
	 * @param fieldNames
	 * @return
	 */
	public QuerySearchObject<T> match(Object value, String... fieldNames){
		// 这个只是添加联表用的
    	for(String s : fieldNames) 
    		needJoinTable(s);
    	
    	if(value instanceof String && value.toString().contains(" ")) {
    		return match(value.toString().split(" "), fieldNames);
    	}
    	
    	queries.add(qb.keyword().onFields(fieldNames).matching(value).createQuery());
		return this;
	}
	
	/**
	 * 匹配，只要有一个字或一个词匹配上就能查到结果, 这种针对的是多个值的情况下，比如枚举或对象，不能像空格那样拼写的
	 * @param values
	 * @param fieldNames
	 * @return
	 */
	public QuerySearchObject<T> match(Object[] values, String... fieldNames){
		return match(Arrays.asList(values), fieldNames);
	}
	
	/**
	 * 匹配，只要有一个字或一个词匹配上就能查到结果, 这种针对的是多个值的情况下，比如枚举或对象，不能像空格那样拼写的
	 * @param values
	 * @param fieldNames
	 * @return
	 */
	public QuerySearchObject<T> match(Collection values, String... fieldNames){
		// 这个只是添加联表用的
		for(String s : Objects.requireNonNull(fieldNames)) 
			needJoinTable(s);
		
		BooleanJunction<BooleanJunction> bool = qb.bool();
		for(Object v : Objects.requireNonNull(values)) {
			bool.should(qb.keyword().onFields(fieldNames).matching(v).createQuery());
		}
		
		queries.add(bool.createQuery());
		return this;
	}
	
	/**
	 * 匹配，只要有一个字或一个词匹配上就能查到结果, 这个一般是匹配单个字符，加上*号
	 * @param value
	 * @param fieldNames
	 * @return
	 */
	public QuerySearchObject<T> wildcardMatch(Object value, String... fieldNames){
		// 这个只是添加联表用的
		for(String s : fieldNames) 
			needJoinTable(s);
		
		queries.add(qb.keyword().wildcard().onFields(fieldNames).matching(value + "*").createQuery());
		return this;
	}

	/**
	 * 关联表
	 * @param fields
	 * @return {@link QuerySearchObject}
	 */
	public QuerySearchObject<T> join(String... fields){
		// 这个是添加联表用的，和上面的代码不冲突
		for(String s : fields) {
			if(!s.contains(".")) {
				joinFields.add(s);
				continue;
			}
			needJoinTable(s, true);
		}
		
		return this;
	}
	
	/**
	 * 处理是否需要关联表
	 * @param fieldName
	 */
	private void needJoinTable(String fieldName) {
		needJoinTable(fieldName, false);
	}
	
	/**
	 * 处理是否需要关联表
	 * @param fieldName
	 */
	private void needJoinTable(String fieldName, boolean includeLastFieldName) {
		if(fieldName.contains(".")) {
			String[] arr = fieldName.split("\\.");
			StringBuffer buffer = new StringBuffer();
			int len = includeLastFieldName ? arr.length : arr.length - 1;
			for(int i = 0; i < len; i++) {
				buffer.append(arr[i]);
				joinFields.add(buffer.toString());
				buffer.append(".");
			}
		}
	}
	
	/**
	 * 排序, 默认按自然排序
	 * @param field 要排序的字段，要加上@SortableField注解
	 * @return {@link QuerySearchObject}
	 */
	public QuerySearchObject<T> sort(String field){
		return sort(field, false);
	}
	
	/**
	 * 排序
	 * @param field 要排序的字段，要加上@SortableField注解
	 * @param reverse 是否倒序
	 * @return {@link QuerySearchObject}
	 */
	public QuerySearchObject<T> sort(String field, boolean reverse){
		return sort(field, SortField.Type.STRING, reverse);
	}
	
	/**
	 * 排序
	 * @param field 要排序的字段，要加上@SortableField注解
	 * @param type 要排序的字段的类型，默认为STRING <br>
	 * 		SCORE 			Sort by document score (relevance).  Sort values are Float and higher values are at the front.<br>
	 * 		DOC 	 			Sort by document number (index order).  Sort values are Integer and lower values are at the front.<br>
	 * 		STRING			Sort using term values as Strings.  Sort values are String and lower values are at the front. <br>
	 * 		INT				Sort using term values as encoded Integers.  Sort values are Integer and lower values are at the front. <br>
	 * 		FLOAT			Sort using term values as encoded Floats.  Sort values are Float and lower values are at the front.<br>
	 * 		LONG			Sort using term values as encoded Longs.  Sort values are Long and lower values are at the front.<br>
	 * 		DOUBLE 		Sort using term values as encoded Doubles.  Sort values are Double and lower values are at the front.<br>
	 * 		CUSTOM		Sort using a custom Comparator.  Sort values are any Comparable and sorting is done according to natural order.<br>
	 * 		STRING_VAL Sort using term values as Strings, but comparing by value (using String.compareTo) for all comparisons. 
	 * 							This is typically slower than STRING, which uses ordinals to do the sorting. <br>
	 * 		BYTES			Sort use byte[] index values.<br>
	 * 		REWRITEABLE	Force rewriting of SortField using {@link SortField#rewrite(IndexSearcher)} before it can be used for sorting<br>
	 * @param reverse 是否倒序
	 * @return {@link QuerySearchObject}
	 */
	public QuerySearchObject<T> sort(String field, SortField.Type type, boolean reverse){
		if(field != null && !"".equals(field.trim())) 
			sortFields.add(new SortField(field, type, reverse));
		
		return this;
	}
	
	/**
	 *  排序, 默认为自然排序
	 * @param field 要排序的字段，要加上@SortableField注解
	 * @param type 要排序的字段的类型，默认为STRING <br>
	 * @return
	 */
	public QuerySearchObject<T> sort(String field, SortField.Type type){
		return sort(field, type, false);
	}
	
	/**
	 * 距离排序
	 * @param field Coordinates
	 * @return {@link QuerySearchObject}
	 */
	public QuerySearchObject<T> sortDistance(String field){
		return sortDistance(field, false);
	}
	
	/**
	 * 距离排序
	 * @param field 获取Coordinates
	 * @return {@link QuerySearchObject}
	 */
	public QuerySearchObject<T> sortDistance(String field, boolean reverse){
		if(centerLatitude == null && centerLongitude == null)
			throw new IllegalArgumentException("经纬度不能为null!");
		
		return sortDistance(field, null, null, reverse);
	}
	
	/**
	 * 距离排序
	 * @param field Coordinates
	 * @param centerLongitude 经度 
	 * @param centerLatitude 纬度
	 * @return {@link QuerySearchObject}
	 */
	public QuerySearchObject<T> sortDistance(String field, double centerLongitude, double centerLatitude){
		return sortDistance(field, centerLongitude, centerLatitude, false);
	}
	
	/**
	 * 距离排序
	 * @param field 获取Coordinates
	 * @param centerLongitude 经度 
	 * @param centerLatitude 纬度
	 * @param reverse 是否倒序
	 * @return {@link QuerySearchObject}
	 */
	public QuerySearchObject<T> sortDistance(String field, Double centerLongitude, Double centerLatitude, boolean reverse){
		sortFields.add(new DistanceSortField(centerLatitude, centerLongitude, field, reverse));
		
		setDistanceFields(field, centerLongitude, centerLatitude);
		return this;
	}

	/**
	 * 设置和距离相关的属性
	 * @param field Coordinates
	 * @param centerLongitude 经度
	 * @param centerLatitude 纬度
	 */
	private void setDistanceFields(String field, Double centerLongitude, Double centerLatitude) {
		if(field != null && !"".equals(field))
			this.distanceField = field;
		
		if(centerLatitude != null && centerLongitude != null){
			this.centerLatitude = centerLatitude;
			this.centerLongitude = centerLongitude;
		}
	}
	
	/**
	 * 搜索，结果不带分页，默认获取Integer的最大值个结果
	 * @return {@link List}
	 */
	public <E>List<E> list() {
		return list(0, Integer.MAX_VALUE);
	}
	
	/**
	 * 搜索，结果带分页
	 * @param pageNo 起始页，从0开始
	 * @param pageSize 每页获取数量
	 * @return {@link List}
	 */
    public <E>List<E> list(Integer pageNo, Integer pageSize) {
    	return list(null, null, null, pageNo, pageSize);
    }
	
	/**
	 * 搜索附近，单位：km
	 * @param distanceInKilometers 距离
	 * @param centerLongitude 经度	Longitude values must be in the range (-180, 180]. Positive values are east of the prime meridian.
	 * @param centerLatitude 纬度 Latitude values must be in the range [-90, 90]. Positive values are north of the equator.
	 * @return {@link QuerySearchObject}
	 */
	public QuerySearchObject<T> distance(double distanceInKilometers, double centerLongitude, double centerLatitude){
		if(distanceInKilometers < 0)
			throw new IllegalArgumentException("距离不能为负数!");
		
		if(centerLongitude <= -180 || centerLongitude > 180)
			throw new IllegalArgumentException("经度取值不正确!");
		
		if(centerLatitude < -90 || centerLatitude > 90)
			throw new IllegalArgumentException("纬度取值不正确!");
		
		org.apache.lucene.search.Query luceneQuery = qb.spatial().within(distanceInKilometers, Unit.KM)
																							   .ofLatitude( centerLatitude )
																							   .andLongitude( centerLongitude )
																							   .createQuery();
		queries.add(luceneQuery);
		setDistanceFields(null, centerLongitude, centerLatitude);
		return this;
	}
	
	/**
	 * 查找列表，带查询距离，查询的结果是数组，0 距离， 1 查询的类<br>
	 * 该方法是对于调了distance()方法的，已经将经纬度保存了，不需要再次传参，如果没有调用distance()方法，会抛出异常
	 * @param pageNo 第几页，从0开始
	 * @param pageSize 每页获取数量
	 * @return
	 */
	public List<Object[]> listWithDistance(Integer pageNo, Integer pageSize) {
		if(distanceField == null || "".equals(distanceField))
			throw new IllegalArgumentException("查询距离的字段field不能为空!");
		
		return listWithDistance(distanceField, pageNo, pageSize);
	}
	
	/**
	 * 查找列表，带查询距离，查询的结果是数组，0 距离， 1 查询的类<br>
	 * 该方法是对于调了distance()方法的，已经将经纬度保存了，不需要再次传参，如果没有调用distance()方法，会抛出异常
	 * @param field Coordinates，根据此字段来计算距离，具体见User类中的getLocation()方法，属性名就是location
	 * @param pageNo 第几页，从0开始
	 * @param pageSize 每页获取数量
	 * @return
	 */
	public List<Object[]> listWithDistance(String field, Integer pageNo, Integer pageSize) {
		if(centerLatitude == null && centerLongitude == null)
			throw new IllegalArgumentException("缺乏必要的经纬度参数!");
		
		return listWithDistance(field, centerLongitude, centerLatitude, pageNo, pageSize);
	}
	
    /**
     * 查找列表，带查询距离，查询的结果是数组，长度为2，0 距离， 1 查询的类
     * @param field Coordinates，根据此字段来计算距离，具体见User类中的getLocation()方法，属性名就是location
     * @param centerLongitude 经度 Longitude values must be in the range (-180, 180]. Positive values are east of the prime meridian.
     * @param centerLatitude 纬度 Latitude values must be in the range [-90, 90]. Positive values are north of the equator.
     * @param pageNo 第几页，从0开始
     * @param pageSize 每页获取数量
     * @return
     */
    public List<Object[]> listWithDistance(String field, double centerLongitude, double centerLatitude, Integer pageNo, Integer pageSize) {
    	if(field == null || "".equals(field))
    		throw new IllegalArgumentException("Coordinates不能为空!");
    	
    	if(centerLongitude <= -180 || centerLongitude > 180)
			throw new IllegalArgumentException("经度取值不正确!");
		
		if(centerLatitude < -90 || centerLatitude > 90)
			throw new IllegalArgumentException("纬度取值不正确!");
    	
    	return list(field, centerLongitude, centerLatitude, pageNo, pageSize);
    }
    
    /**
     * 查找列表，带查询距离，查询的结果是数组，长度为2，0 距离， 1 查询的类
     * @param field Coordinates，根据此字段来计算距离，具体见User类中的getLocation()方法，属性名就是location
     * @param centerLongitude 经度
     * @param centerLatitude 纬度
     * @param pageNo 第几页，从0开始
     * @param pageSize 每页获取数量
     * @return {@link List}
     */
    private <E>List<E> list(String field, Double centerLongitude, Double centerLatitude, Integer pageNo, Integer pageSize) {
    	Map<String, Object> map = search(field, centerLongitude, centerLatitude, pageNo, pageSize);
    	return (List<E>) map.get("list");
    }
	
    /**
     * 查找列表，带查询距离，查询的结果是数组，长度为2，0 距离， 1 查询的类
     * @param field Coordinates，根据此字段来计算距离，具体见User类中的getLocation()方法，属性名就是location
     * @param centerLongitude 经度
     * @param centerLatitude 纬度
     * @param pageNo 第几页，从0开始
     * @param pageSize 每页获取数量
     * @return {@link Map} list 结果集 count 总数量
     */
    private Map<String, Object> search(String field, Double centerLongitude, Double centerLatitude, Integer pageNo, Integer pageSize) {
    	BooleanJunction<BooleanJunction> bool = qb.bool();
    	if(!queries.isEmpty()) {
    		for(org.apache.lucene.search.Query q : queries) {
    			bool.must(q);
    		}
    	}else 
    		bool.must(qb.all().createQuery());
    	
    	this.query = bool.createQuery();
    	this.hibQuery  =fullTextSession.createFullTextQuery(query, clazz); 
    	
    	hibQuery.setTimeout(60);
    	
    	if(!joinFields.isEmpty()) {
    		joinFields.forEach(j -> criteria.setFetchMode(j, FetchMode.JOIN));
    		hibQuery.setCriteriaQuery(criteria);
    	}
    	
    	if(!sortFields.isEmpty()) {
    		hibQuery.setSort(new Sort(sortFields.toArray(new SortField[sortFields.size()])));
    	}
    	
    	if(centerLatitude != null && centerLongitude != null && field != null && !"".equals(field)) {
    		hibQuery.setProjection(FullTextQuery.SPATIAL_DISTANCE, FullTextQuery.THIS);
    		// 对于这种查询距离的类，要有getLocation方法，或者别的方法，此地方要传入属性名（实际不一定要有该属性，只要有get方法就行），具体见User类
    		hibQuery.setSpatialParameters(centerLatitude, centerLongitude, field);
    	}
    	
    	if(pageNo != null && pageSize != null) {
    		if(pageNo < 0)
    			throw new IllegalArgumentException("页号不正确!");
    		
    		if(pageSize < 1)
    			throw new IllegalArgumentException("每页数量不正确!");
    		
    		hibQuery.setFirstResult(pageNo * pageSize);
    		hibQuery.setMaxResults(pageSize);
    	}
    	
    	Map<String, Object> map = new HashMap<>();
    	this.count = hibQuery.getResultSize();
    	// 这一句只能话在前面，如果先获取了List，再获取数量则会报错
    	map.put("count", count);
    	
    	List list = hibQuery.list();
    	// 有些情况下返回的集合不是java.util.ArrayList.ArrayList, 比如获取到单个值，是Collections中的SingleList, 
    	// 在某些操作下会出异常，所以封装处理过
		map.put("list", list == null ? new ArrayList<>() : new ArrayList<>(list));
    	
    	return map;
    }
    
    private long count;
    
    /**
     * 这个要放在先获取list后再获取，一开始获取是获取不到的
     * @return
     */
    public long count(){
    	return count;
    }
    
}
