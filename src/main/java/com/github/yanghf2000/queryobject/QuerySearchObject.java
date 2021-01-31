package com.github.yanghf2000.queryobject;

import org.hibernate.Session;
import org.hibernate.graph.GraphSemantic;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep;
import org.hibernate.search.engine.search.sort.dsl.SortOrder;
import org.hibernate.search.engine.search.sort.dsl.SortThenStep;
import org.hibernate.search.engine.spatial.DistanceUnit;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.search.loading.dsl.SearchLoadingOptionsStep;
import org.hibernate.search.mapper.orm.session.SearchSession;

import javax.persistence.AttributeNode;
import javax.persistence.EntityGraph;
import javax.persistence.Subgraph;
import java.util.*;

/**
 * 封装搜索查询对象<p>
 * 由于Dao的子类是单例的，所以这个对象的实例不能作为Dao的变量<p>
 * @author 杨会锋
 * 2018-1-12
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class QuerySearchObject<T>{
	
	private SearchSession searchSession;
	private Class<T> clazz;
	private Set<String> joinFields = new HashSet<>();
	private SearchScope<T> scope;

	/**
	 * 查询条件
	 */
	private List<PredicateFinalStep> predicateSteps = new ArrayList<>();

	/**
	 * 排序
	 */
	private SortThenStep sortStep;

	/**
	 * 关联表用
	 */
	private EntityGraph graph;

	// 和求距离相关
	private Double centerLongitude = null, centerLatitude = null;

	public static <T>QuerySearchObject<T> getInstance(Session session, Class<T> clazz){
		return new QuerySearchObject(session, clazz);
	}
	
	@SuppressWarnings("deprecation")
	private QuerySearchObject(Session session, Class<T> clazz){
		this.searchSession = Search.session(session);
		this.clazz = clazz;
		scope = searchSession.scope(clazz);
		graph = searchSession.toEntityManager().createEntityGraph(clazz);
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
	@Deprecated
	public QuerySearchObject<T> sentence(String fieldName, Object value){
//		queries.add(scope.predicate().phrase().fields(fieldName).matching(null));
		return this;
	}

	/**
	 * phrase，字段要包含（给定顺序的）分词，须是加了@FullTextField注解的
	 * 比如：字段是：小米手机 value 小米 手机，小米手机，小米，手机都能查到，但手机小米查不到，顺序不对
	 * The phrase predicate matches documents for which a given field contains a given sequence of words, in the given order.
	 * @param value 值
	 * @param fieldNames 字段名
	 * @return {@link QuerySearchObject}
	 */
	public QuerySearchObject<T> phrase(String value, String... fieldNames){
		// slop：中间可包含的词，如：a b, a x x b都是可以查到的
		predicateSteps.add(scope.predicate().phrase().fields(fieldNames).matching(value)/*.slop(2)*/);
		return this;
	}

	/**
	 * 范围条件， 默认搜索String类型的，其他类型的搜索不到，要指明类型<br>
	 * @param min
	 * @param max
	 * @param fieldNames
	 */
	public QuerySearchObject<T> range(Object min, Object max, String... fieldNames){
		predicateSteps.add(scope.predicate().range().fields(fieldNames).between(min, max));
		return this;
	}

	/**
	 * above
	 * @param value
	 * @param fieldNames
	 * @return
	 */
	public QuerySearchObject<T> above(Object value, String... fieldNames) {
		return range(value, null, fieldNames);
	}

	/**
	 * below
	 * @param value
	 * @param fieldNames
	 * @return
	 */
	public QuerySearchObject<T> below(Object value, String... fieldNames) {
		return range((Object)null, value, fieldNames);
	}

	/**
	 * 匹配，只要有一个字或一个词匹配上就能查到结果
	 * @param value
	 * @param fieldNames
	 * @see QuerySearchObject#matchId(Object) 查询id的情况下用此方法 
	 * @return
	 */
	public QuerySearchObject<T> match(Object value, String... fieldNames){
		Objects.requireNonNull(fieldNames);
		List<String> list = new ArrayList<>(Arrays.asList(fieldNames));
		if(list.contains("id")) {
			matchId(value);
			list.remove("id");
		}

		if(!list.isEmpty()) {
			String[] fields = list.toArray(new String[0]);
			if(value == null) {
				for (String field : fields) {
					predicateSteps.add(scope.predicate().bool().mustNot(scope.predicate().exists().field(field)));
				}
			} else {
				if(list.size() > 0) {
					if(value instanceof Collection) {
						BooleanPredicateClausesStep<?> bool = scope.predicate().bool();
						for (Object v : ((Collection) value)) {
							bool.should(scope.predicate().match().fields(fields).matching(v));
						}
						predicateSteps.add(bool);
					} else if(value instanceof Object[]) {
						BooleanPredicateClausesStep<?> bool = scope.predicate().bool();
						for (Object v : ((Object[]) value)) {
							bool.should(scope.predicate().match().fields(fields).matching(v));
						}
						predicateSteps.add(bool);
					} else {
						predicateSteps.add(scope.predicate().match().fields(fields).matching(value));
					}
				}
			}
		}
		return this;
	}

	/**
	 * id查询
	 * @param value 值，not null，单个值或Collection
	 * @return
	 */
	public QuerySearchObject<T> matchId(Object value){
		Objects.requireNonNull(value);
		if(value instanceof Collection) {
			predicateSteps.add(scope.predicate().id().matchingAny((Collection) value));
		} else {
			predicateSteps.add(scope.predicate().id().matching(value));
		}
		return this;
	}

	/**
	 * 匹配，只要有一个字或一个词匹配上就能查到结果, 这个一般是匹配单个字符，加上*号
	 * @param value
	 * @param fieldNames
	 * @return
	 */
	public QuerySearchObject<T> wildcardMatch(String value, String... fieldNames){
		predicateSteps.add(scope.predicate().wildcard().fields(fieldNames).matching(value));
		return this;
	}

	/**
	 * 关联表
	 * @param fields
	 * @return {@link QuerySearchObject}
	 */
	public QuerySearchObject<T> join(String... fields){
		for (String field : fields) {
			if(!field.contains(".")) {
				graph.addSubgraph(field);
				continue;
			}

			String[] arr = field.split("\\.");
			Subgraph subgraph = null;
			for (int i = 0; i < arr.length; i++) {
				String s = arr[i];

				List<AttributeNode<?>> attributeNodes = subgraph == null ? graph.getAttributeNodes()
						: subgraph.getAttributeNodes();
				if(attributeNodes != null && !attributeNodes.isEmpty()) {
					AttributeNode<?> attr = attributeNodes.stream()
							.filter(e -> e.getAttributeName().equals(s)).findFirst().orElse(null);
					if(attr != null) {
						Map<Class, Subgraph> subgraphs = attr.getSubgraphs();
						subgraph = subgraphs.values().iterator().next();
						continue;
					}
				}

				if(subgraph == null) {
					subgraph = graph.addSubgraph(s);
				} else {
					subgraph = subgraph.addSubgraph(s);
				}
			}
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
		if(sortStep == null) {
			sortStep = scope.sort().field(field).order(reverse ? SortOrder.DESC : SortOrder.ASC);
		} else {
			sortStep = sortStep.then().field(field).order(reverse ? SortOrder.DESC : SortOrder.ASC);
		}
		return this;
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
		return sortDistance(field, centerLongitude, centerLatitude, reverse);
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
		setDistanceFields(centerLongitude, centerLatitude);
		if(sortStep == null) {
			sortStep = scope.sort().distance(field, centerLatitude, centerLongitude)
					.order(reverse ? SortOrder.DESC : SortOrder.ASC);
		} else {
			sortStep = sortStep.then().field(field).order(reverse ? SortOrder.DESC : SortOrder.ASC);
		}
		return this;
	}

	/**
	 * 设置和距离相关的属性
	 * @param field Coordinates
	 * @param centerLongitude 经度
	 * @param centerLatitude 纬度
	 */
	@Deprecated
	private void setDistanceFields(String field, Double centerLongitude, Double centerLatitude) {
		if(centerLongitude <= -180 || centerLongitude > 180) {
			throw new IllegalArgumentException("经度取值不正确!");
		}

		if(centerLatitude < -90 || centerLatitude > 90) {
			throw new IllegalArgumentException("纬度取值不正确!");
		}

		this.centerLatitude = centerLatitude;
		this.centerLongitude = centerLongitude;
	}

	/**
	 * 设置和距离相关的属性
	 * @param centerLongitude 经度
	 * @param centerLatitude 纬度
	 */
	private void setDistanceFields(Double centerLongitude, Double centerLatitude) {
		if(centerLatitude == null && centerLongitude == null) {
			throw new IllegalArgumentException("经纬度不能为null!");
		}

		if(centerLongitude <= -180 || centerLongitude > 180) {
			throw new IllegalArgumentException("经度取值不正确!");
		}

		if(centerLatitude < -90 || centerLatitude > 90) {
			throw new IllegalArgumentException("纬度取值不正确!");
		}

		this.centerLatitude = centerLatitude;
		this.centerLongitude = centerLongitude;
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
	@Deprecated
	public QuerySearchObject<T> distance(double distanceInKilometers, double centerLongitude, double centerLatitude){
		if(distanceInKilometers < 0) {
			throw new IllegalArgumentException("距离不能为负数!");
		}
		
//		org.apache.lucene.search.Query luceneQuery = qb.spatial().within(distanceInKilometers, Unit.KM)
//																							   .ofLatitude( centerLatitude )
//																							   .andLongitude( centerLongitude )
//																							   .createQuery();
//		queries.add(luceneQuery);
		setDistanceFields(null, centerLongitude, centerLatitude);
		return this;
	}

	/**
	 * 搜索附近，单位：km
	 * @param distanceInKilometers 距离
	 * @param centerLongitude 经度	Longitude values must be in the range (-180, 180].
	 *                           Positive values are east of the prime meridian.
	 * @param centerLatitude 纬度 Latitude values must be in the range [-90, 90].
	 *                       Positive values are north of the equator.
	 * @param fieldNames	要查询的字段
	 * @return {@link QuerySearchObject}
	 */
	public QuerySearchObject<T> distance(double distanceInKilometers, double centerLongitude,
										 double centerLatitude, String... fieldNames){
		if(distanceInKilometers < 0) {
			throw new IllegalArgumentException("距离不能为负数!");
		}

		setDistanceFields(centerLongitude, centerLatitude);

		predicateSteps.add(scope.predicate().spatial().within().fields(fieldNames)
				.circle(centerLatitude, centerLongitude, distanceInKilometers, DistanceUnit.KILOMETERS));
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
//		if(distanceField == null || "".equals(distanceField)) {
//			throw new IllegalArgumentException("查询距离的字段field不能为空!");
//		}
//
//		return listWithDistance(distanceField, pageNo, pageSize);

		return null;
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
		if(centerLatitude == null && centerLongitude == null) {
			throw new IllegalArgumentException("缺乏必要的经纬度参数!");
		}
		
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
    	if(field == null || "".equals(field)) {
			throw new IllegalArgumentException("Coordinates不能为空!");
		}
    	
    	if(centerLongitude <= -180 || centerLongitude > 180) {
			throw new IllegalArgumentException("经度取值不正确!");
		}
		
		if(centerLatitude < -90 || centerLatitude > 90) {
			throw new IllegalArgumentException("纬度取值不正确!");
		}
    	
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
    private <E>List<E> list(String field, Double centerLongitude, Double centerLatitude,
							Integer pageNo, Integer pageSize) {
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
    private Map<String, Object> search(String field, Double centerLongitude,
									   Double centerLatitude, Integer pageNo, Integer pageSize) {

		pageNo = pageNo == null ? 0 : pageNo;
		pageSize = pageSize == null ? Integer.MAX_VALUE : pageSize;
		pageNo = pageNo * pageSize;

		SearchQueryOptionsStep search = searchSession.search(clazz)
			.where(f -> {
				if (predicateSteps.isEmpty()) {
					return f.matchAll();
				}

				BooleanPredicateClausesStep<?> bool = f.bool();
				for (PredicateFinalStep step : predicateSteps) {
					bool = bool.must(step);
				}
				return bool;
			});

		if(graph != null) {
			((SearchQueryOptionsStep<?, T, SearchLoadingOptionsStep, ?, ?>)search)
					.loading( o -> o.graph(graph, GraphSemantic.FETCH));
		}

		if(sortStep != null) {
			search.sort(sortStep.toSort());
		}

		SearchResult<T> searchResult = search.fetch(pageNo, pageSize);

		this.count = searchResult.total().hitCount();
		Map<String, Object> map = new HashMap<>();
		map.put("count", count);

		List<T> results = searchResult.hits();
		map.put("list", results == null ? new ArrayList<>() : new ArrayList<>(results));
    	
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
