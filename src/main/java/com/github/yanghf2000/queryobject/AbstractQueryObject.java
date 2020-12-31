package com.github.yanghf2000.queryobject;

import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.*;
import java.util.*;


/**
 * 封装查询对象<p>
 * 由于Dao的子类是单例的，所以这个对象的实例不能作为Dao的变量<p>
 * @author 杨会锋
 * 2017-12-10
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractQueryObject<O extends AbstractQueryObject, T>{
	
	public final static String FETCH = "fetch";
	
	public final static String JOIN = "join";
	
	public final static String REGEXP_DOT = "\\.";
	
	protected Session session;
	protected CriteriaBuilder builder;
	protected Root root;
	
	protected Class clazz;
	
	// 能否连表
	protected boolean canJoin = true;
	
	/**
	 * and条件集合
	 */
	protected List<Predicate> andPres = new ArrayList<>();
	
	/**
	 * or条件集合
	 */
	protected List<Predicate> orPres = new ArrayList<>();
	
	protected AbstractQueryObject(Session session, Class<T> clazz){
		this.session = session;
		this.builder = session.getCriteriaBuilder();
		this.clazz = clazz;
	}
	
	/**
	 * 获取查询条件
	 * @return
	 */
	protected Predicate getPredicate() {
		Predicate and = builder.and(andPres.toArray(new Predicate[andPres.size()]));
		Predicate or = builder.or(orPres.toArray(new Predicate[orPres.size()]));
		return builder.or(and, or);
	}
	
	// ********************************** 以下为添加条件 ********************************
	
	/**
	 * 等于，如果value是null，用is null
	 * @param fieldName 字段名
	 * @param value 值
	 * @return {@link AbstractQueryObject}
	 */
	public O andEqual(String fieldName, Object value){
		if(value == null) {
			return isNull(fieldName);
		}
		return addAndCondition(builder.equal(extractPath(fieldName), value));
	}
	
	/**
	 * 不等于，如果value是null，用is not null
	 * @param fieldName 字段名
	 * @param value 值
	 * @return {@link AbstractQueryObject}
	 */
	public O andNotEqual(String fieldName, Object value){
		if(value == null) {
			return isNotNull(fieldName);
		}
		return addAndCondition(builder.notEqual(extractPath(fieldName), value));
	}
	
	private O addAndCondition(Predicate e) {
		andPres.add(e);
		return (O)this;
	}
	
	/**
	 * isNull
	 * @param fieldName
	 * @return
	 */
	public O isNull(String fieldName){
		return addAndCondition(builder.isNull(extractPath(fieldName)));
	}
	
	/**
	 * isNotNull
	 * @param fieldName
	 * @return
	 */
	public O isNotNull(String fieldName){
		return addAndCondition(builder.isNotNull(extractPath(fieldName)));
	}
	
	/**
	 * isTrue
	 * @param fieldName
	 * @return
	 */
	public O isTrue(String fieldName){
		return addAndCondition(builder.isTrue(extractPath(fieldName)));
	}
	
	/**
	 * isFalse
	 * @param fieldName
	 * @return
	 */
	public O isFalse(String fieldName){
		return addAndCondition(builder.isFalse(extractPath(fieldName)));
	}
	
	/**
	 * 大于等于
	 * @param fieldName 字段名
	 * @param value 值
	 * @return {@link AbstractQueryObject}
	 */
	public O andGe(String fieldName, Comparable value){
		if(value instanceof Number) {
			return addAndCondition(builder.ge(extractPath(fieldName), (Number)value));
		} else {
			// 由于放入参数时要指定类型，但此处没有办法加上Object的泛型，所以暂时先指定Comparable，比较大多数参与比较的类型都实现了Comparable接口
			Path<Comparable> path = extractPath(fieldName);
			return addAndCondition(builder.greaterThanOrEqualTo(path, (Comparable)value));
		}
	}
	
	/**
	 * 大于
	 * @param fieldName 字段名
	 * @param value 值
	 * @return {@link AbstractQueryObject}
	 */
	public O andGt(String fieldName, Comparable value){
		if(value instanceof Number) {
			return addAndCondition(builder.gt(extractPath(fieldName), (Number)value));
		} else {
			Path<Comparable> path = extractPath(fieldName);
			return addAndCondition(builder.greaterThan(path, (Comparable)value));
		}
	}

	/**
	 * 小于等于
	 * @param fieldName 字段名
	 * @param value 值
	 * @return {@link AbstractQueryObject}
	 */
	public O andLe(String fieldName, Comparable value){
		if(value instanceof Number) {
			return addAndCondition(builder.le(extractPath(fieldName), (Number)value));
		} else {
			Path<Comparable> path = extractPath(fieldName);
			return addAndCondition(builder.lessThanOrEqualTo(path, (Comparable)value));
		}
	}
	
	/**
	 * 小于
	 * @param fieldName 字段名
	 * @param value 值
	 * @return {@link AbstractQueryObject}
	 */
	public O andLt(String fieldName, Comparable value){
		if(value instanceof Number) {
			return addAndCondition(builder.lt(extractPath(fieldName), (Number)value));
		} else {
			Path<Comparable> path = extractPath(fieldName);
			return addAndCondition(builder.lessThan(path, (Comparable)value));
		}
	}
	
	
	/**
	 * between
	 * @param fieldName 字段名
	 * @param low				小的值
	 * @param high				大的值
	 * @return {@link AbstractQueryObject}
	 */
	public O andBetween(String fieldName, Comparable low, Comparable high){
		
		Path<Comparable> path = extractPath(fieldName);
		return addAndCondition(builder.between(path, low, high));
	}
	
	/**
	 * between
	 * @param fieldName 	字段名
	 * @param low				小的值
	 * @param high				大的值
	 * @return {@link AbstractQueryObject}
	 */
	public O orBetween(String fieldName, Comparable low, Comparable high){
		
		Path<Comparable> path = extractPath(fieldName);
		orPres.add(builder.between(path, low, high));
		
		return (O) this;
	}
	
	/**
	 * 或
	 * @param fieldName 字段名
	 * @param value 值
	 * @return {@link AbstractQueryObject}
	 */
	public O orEqual(String fieldName, Object value){
		orPres.add(builder.equal(extractPath(fieldName), value));
		return (O) this;
	}
	
	/**
	 * and like
	 * @param fieldName 字段名
	 * @param values like的值, 这里不负责加%，如果需要的话要加好传进来
	 * @return {@link AbstractQueryObject}
	 */
	public O andLike(String fieldName, String... values){
		if(values == null || values.length < 1) {
			throw new IllegalArgumentException("传入的参数不能为空!");
		}
		
		Predicate[] pres = new Predicate[values.length];
		for(int i = 0; i < values.length; i++) {
			pres[i] = builder.like(extractPath(fieldName), values[i]);
		}
		
		return addAndCondition(builder.or(pres));
	}
	
	/**
	 * or like
	 * @param fieldName 字段名
	 * @param @param values like的值
	 * @return {@link AbstractQueryObject}
	 */
	public O orLike(String fieldName, String... values){
		if(values == null || values.length < 1)
			throw new IllegalArgumentException("传入的参数不能为空!");
		
		Predicate[] pres = new Predicate[values.length];
		for(int i = 0; i < values.length; i++) {
			pres[i] = builder.like(extractPath(fieldName), values[i]);
		}
		
		orPres.add(builder.or(pres));
		return (O)this;
	}
	
	/**
	 * and in
	 * @param fieldName 字段名
	 * @param col 值
	 * @return {@link AbstractQueryObject}
	 */
	public O andIn(String fieldName, Collection col){
		return addAndCondition(builder.in(extractPath(fieldName)).value(Objects.requireNonNull(col)));
	}

	/**
	 * and in
	 * @param fieldName 字段名
	 * @param arr 值
	 * @return {@link AbstractQueryObject}
	 */
	public O andIn(String fieldName, Object[] arr){
		return andIn(fieldName, Arrays.asList(arr));
	}

	/**
	 * and not in
	 * @param fieldName 字段名
	 * @param list 值
	 * @return {@link AbstractQueryObject}
	 */
	public O andNotIn(String fieldName, List list){
		return addAndCondition(builder.not(builder.in(extractPath(fieldName)).value(Objects.requireNonNull(list))));
	}

	/**
	 * and not in
	 * @param fieldName 字段名
	 * @param arr 值
	 * @return {@link AbstractQueryObject}
	 */
	public O andNotIn(String fieldName, Object[] arr){
		return andNotIn(fieldName, Arrays.asList(arr));
	}

	/**
	 * or in
	 * @param fieldName 字段名
	 * @param list 值
	 * @return {@link AbstractQueryObject}
	 */
	public O orIn(String fieldName, List list){
		orPres.add(builder.in(extractPath(fieldName)).value(list));
		return (O)this;
	}
	
	/**
	 * or in
	 * @param fieldName 字段名
	 * @param arr 值
	 * @return {@link AbstractQueryObject}
	 */
	public O orIn(String fieldName, Object[] arr){
		return orIn(fieldName, Arrays.asList(arr));
	}
	
	// ************************************* 以下部分为获取结果 ******************************************

	/**
	 * 若直接传了字段，则不需要分解，直接使用，若传了a.b.c这种，则找到最后一级c<br>
	 * 这种情况对于关联对象为主键的，则不会产生join语句，如user  user.id，都会根据user_id查，对于第二种情况也不会产生join语句
	 * @param fieldName
	 * @return
	 */
	protected Path extractPath(String fieldName) {
		
		// 带导航属性的
		if(fieldName.contains(".")) {
			String[] arr = fieldName.split(REGEXP_DOT);
			String field = arr[arr.length - 1];		// 	最后的字段为要查询的内容
			
			// 如果是查询id时，有些时候可以不用关联表，要走以下方法，关联表时不会写有id，这个应该不冲突
			// 加上这个后，以下语句有问题 Address address = addressDao.getQueryObject()./*innerJoin("user.addresses").*/andEqual("user.addresses.id", 1).getOne();
			if("id".equals(arr[arr.length - 1])) {
				// 对于a.id这种，直接返回，否则还是进行关联
				if(arr.length <= 2) {
					Path p = root.get(arr[0]);
					for(int i = 1; i < arr.length; i++) {
						p = p.get(arr[i]);
					}
					return p;
				}
			}
			
			// 先检查fetch
			Set<Fetch> fetches = root.getFetches();
			if(fetches != null && !fetches.isEmpty()) {
				Fetch fetch = null;
				for(int i = 0; i < arr.length - 1; i++) {
					String s = arr[i];
					Optional<Fetch> op = fetches.stream().filter(f -> f.getAttribute().getName().equals(s)).findFirst();
					if(op.isPresent()) {
						fetch = op.get();
						fetches = fetch.getFetches();
					} else {
						if(fetch != null) {
							fetch = fetch.fetch(arr[i]);
						} else {
							break;
						}
					}
					
					if(fetch != null) {
						if(i == arr.length - 2) {
							return ((Join)fetch).get(field);
						}
					}
				}
			}
			
			Set<Join> joins = root.getJoins();
			if(joins != null && !joins.isEmpty()) {
				Join join = null;
				for(int i = 0; i < arr.length - 1; i++) {
					String s = arr[i];
					Optional<Join> op = joins.stream().filter(j -> j.getAttribute().getName().equals(s)).findFirst();
					if(op.isPresent()) {
						join = op.get();
						joins = join.getJoins();
					} else {
						if(join != null) {
							join = join.join(arr[i]);
						} else {
							break;
						}
					}
					
					if(join != null) {
						if(i == arr.length - 2) {
							return join.get(field);
						}
					}
				}
			}
			
			// 没有，添加新的
			Join join = root.join(arr[0]);
			for(int i = 1; i < arr.length - 1; i++) {
				join = join.join(arr[i]);
			}
			return join.get(field);
		} 
		// 单个值
		// 对于单个值，主要是考虑是不是对象，若是对象，则可能有关联过，直接获取此关联对象就行
		else {
			Set<Fetch> fetches = root.getFetches();
			Optional<Fetch> op = fetches.stream().filter(f -> f.getAttribute().getName().equals(fieldName)).findFirst();
			if(op.isPresent()) {
				Fetch f = op.get();
				if(f instanceof Path) {
					return (Path) f;
				}
			}else {
				Set<Join> joins = root.getJoins();
				Optional<Join> o = joins.stream().filter(j -> j.getAttribute().getName().equals(fieldName)).findFirst();
				if(o.isPresent()) {
					return o.get();
				}
			}
			
			// 对于是非关联对象的进行获取
			return root.get(fieldName);
		}
	}
	

    /**
     * 获取分页起始位置
     * @param pageNo 页号，从0开始
     * @param size
     * @return
     */
    protected int getStart(int pageNo, int size){
    	if(pageNo < 0) {
			pageNo = 0;
		}
    	
    	if(size < 1) {
			size = 10;
		}
    	
    	return pageNo * size;
    }

    /**
     * 添加分页
     * @param query
     * @param pageNo not null, 若只有pageNo，没有size时，pageNo作为开始行
     * @param size
     */
    protected void addPage(Query query, Integer pageNo, Integer size){
    	if(pageNo != null) {
			query.setFirstResult(size == null ? pageNo : getStart(pageNo, size));
		}

    	if(size != null) {
			query.setMaxResults(size);
		}
    }
    
}
