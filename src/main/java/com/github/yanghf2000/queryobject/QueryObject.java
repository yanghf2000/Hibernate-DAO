package com.github.yanghf2000.queryobject;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.internal.OrderImpl;

import javax.persistence.LockModeType;
import javax.persistence.criteria.*;
import java.util.*;

/**
 * 封装查询对象<p>
 * 由于Dao的子类是单例的，所以这个对象的实例不能作为Dao的变量<p>
 * 但若用多实例的Dao，若将这里所有方法加到Dao中，Dao中显得太乱了<p>
 * @author 杨会锋
 * 2017-12-10
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class QueryObject<T> extends AbstraceQueryObject<QueryObject<T>, T>{
	
	public final static String COUNT = "count";
	public final static String MAX = "max";
	public final static String MIN = "min";
	public final static String SUM = "sum";
	
	private CriteriaQuery criteria;
	
	private LockOptions lockOptions;
	private LockModeType lockModeType;
	
	/**
	 * 排序集合
	 */
	private List<Order> orders = new ArrayList<>();
	
	/**
	 * 分组集合
	 */
	private List<Path<String>> groups = new ArrayList<>();
	
	public static <T>QueryObject<T> getInstance(Session session, Class<T> clazz){
		return new QueryObject(session, clazz);
	}
	
	private QueryObject(Session session, Class<T> clazz){
		super(session, clazz);
		this.criteria = builder.createQuery(clazz);
		this.root = criteria.from(clazz);
		this.criteria.select(root);
	}
	
	/**
	 * 查询单个值
	 * @param field 字段
	 * @return {@link CriteriaQuery}
	 */
	private CriteriaQuery<T> getSingleFieldCriteriaQuery(String field){
		this.criteria.select(extractPath(field));
		return getCriteriaQuery();
	}
	
	/**
	 * 获取任意几个字段，这些字段必须是表中有的字段， 返回需要为数组
	 * @param fields 要查询的字段名
	 * @return {@link CriteriaQuery}
	 */
	private CriteriaQuery<T> getAnyFieldsCriteriaQuery(String... fields){
		if(fields == null || fields.length < 1)
			throw new IllegalArgumentException("要查询的字段不能为空");
		
		Selection[] arrs = new Selection[fields.length];
		for(int i = 0; i < fields.length; i++){
			arrs[i] = extractPath(fields[i]);
		}
		this.criteria.select(builder.array(arrs));
		return getCriteriaQuery();
	}
	
	/**
	 * 计数CriteriaQuery
	 * @param field: 要传入的字段，必须为表中有的字段，若不填，默认为id
	 * @param distinct: 是否去重
	 * @return {@link CriteriaQuery}
	 */
	private CriteriaQuery<T> getCountCriteriaQuery(String field, boolean distinct){
		Expression<T> exp = root;
		if(field != null && !"".equals(field))
			exp = extractPath(field);
		
		this.criteria.select(distinct ? builder.countDistinct(exp) : builder.count(exp));
		return getCriteriaQuery();
	}
	
	/**
	 * 统计CriteriaQuery
	 * @param field: 要传入的字段，必须为表中有的字段，若不填，默认为id
	 * @param statType: 统计类型 MAX MIN SUM COUNT
	 * @return {@link CriteriaQuery}
	 */
	private CriteriaQuery<T> getStatCriteriaQuery(String field, String statType){
		if(field == null) {
			field = "id";
		}
		
		Selection selection;
		Path x = extractPath(field);
		switch (statType) {
		case MAX:
			selection = builder.max(x);
			break;
		case MIN:
			selection = builder.min(x);
			break;
		case SUM:
			selection = builder.sum(x);
			break;
		case COUNT:
			selection = builder.count(x);
			break;
		default:
			throw new IllegalArgumentException("统计类型不正确! " + statType);
		}
		
		this.criteria.select(selection);
		return getCriteriaQuery();
	}

	/**
	 * 获取CriteriaQuery
	 * @return {@link CriteriaQuery}r
	 */
	private CriteriaQuery<T> getCriteriaQuery(){
		this.criteria.where(getPredicate())
						.groupBy(groups.toArray(new Path[groups.size()]))
						.orderBy(orders.toArray(new Order[orders.size()]));

		return this.criteria;
	}

	// ********************************** 以下为添加条件 ********************************
	
	/**
	 * 升充排序
	 * @param fieldName 排序字段
	 * @return {@link QueryObject}
	 */
	public QueryObject<T> orderAsc(String fieldName){
		Path path = extractPath(fieldName);
		orders.add(new OrderImpl(path, true));
		return this;
	}
	
	/**
	 * 降充排序
	 * @param fieldName 排序字段
	 * @return {@link QueryObject}
	 */
	public QueryObject<T> orderDesc(String fieldName){
		orders.add(new OrderImpl(extractPath(fieldName), false));
		return this;
	}
	
	/**
	 * 分组
	 * @param fieldName 分组字段
	 * @return {@link QueryObject}
	 */
	public QueryObject<T> groupBy(String fieldName){
		groups.add(extractPath(fieldName));
		return this;
	}
	
	/**
	 * 内连接, 连接只能取关联对象或关联元素
	 * <br>对于要关联的字段，如a.b.c，自动会将a b c表关联
	 * @param fieldName  关联的属性必须是要查询对象中有的
	 * @return {@link QueryObject}
	 */
	public QueryObject<T> innerJoin(String fieldName){
		return join(fieldName, JOIN, JoinType.INNER);
	}
	
	/**
	 * 左连接, 连接只能取关联对象或关联元素
	 * <br>对于要关联的字段，如a.b.c，自动会将a b c表关联
	 * @param fieldName  关联的属性必须是要查询对象中有的
	 * @return {@link QueryObject}
	 */
	public QueryObject<T> leftJoin(String fieldName){
		return join(fieldName, JOIN, JoinType.LEFT);
	}

	/**
	 * 添加联表
	 * @param fieldName	要添加的字段
	 * @param joinOrFetch	fetch join
	 * @param joinType		连接方式
	 */
	private QueryObject<T> join(String fieldName, String joinOrFetch, JoinType joinType){
		String[] arr = fieldName.split(REGEXP_DOT);

		switch (joinOrFetch.toLowerCase()) {
			case FETCH:
				Fetch fetch = getFetch(arr[0], joinType);
				for(int i = 1; i < arr.length; i++) {
					fetch = getFetch(arr[i], fetch, joinType);
				}
				break;
			case JOIN:
				Join join = getJoin(arr[0], joinType);
				for(int i = 1; i < arr.length; i++) {
					join = getJoin(arr[i], join, joinType);
				}
				break;
		}
		return this;
	}

	/**
	 * 获取根节点下的关联表
	 * @param propName
	 * @return
	 */
	private Fetch getFetch(String propName, JoinType joinType) {
		Set<Fetch> fetches = root.getFetches();
		Optional<Fetch> op = fetches.stream().filter(e -> e.getAttribute().getName().equalsIgnoreCase(propName)).findFirst();
		return op.isPresent() ? op.get() : root.fetch(propName, joinType);
	}

	/**
	 * 获取连表
	 * @param propName
	 * @param fetch
	 * @param joinType
	 * @return
	 */
	private Fetch getFetch(String propName, Fetch fetch, JoinType joinType) {
		Set<Fetch> fetches = Objects.requireNonNull(fetch).getFetches();
		Optional<Fetch> op = fetches.stream().filter(e -> e.getAttribute().getName().equalsIgnoreCase(propName)).findFirst();
		return op.isPresent() ? op.get() : fetch.fetch(propName, joinType);
	}

	/**
	 * 获取根节点下的关联表
	 * @param propName
	 * @return
	 */
	private Join getJoin(String propName, JoinType joinType) {
		Set<Join> joins = root.getJoins();
		Optional<Join> op = joins.stream().filter(e -> e.getAttribute().getName().equalsIgnoreCase(propName)).findFirst();
		return op.isPresent() ? op.get() : root.join(propName, joinType);
	}

	/**
	 * 获取连表
	 * @param propName
	 * @param join
	 * @param joinType
	 * @return
	 */
	private Join getJoin(String propName, Join join, JoinType joinType) {
		Set<Join> joins = Objects.requireNonNull(join).getJoins();
		Optional<Join> op = joins.stream().filter(e -> e.getAttribute().getName().equalsIgnoreCase(propName)).findFirst();
		return op.isPresent() ? op.get() : join.join(propName, joinType);
	}

	/**
	 * 内连接抓取, 连接只能取关联对象或关联元素<br>
	 * <b>如果获取的结果中不含要抓取的对象，则会报错
	 * <br>对于要关联的字段，如a.b.c，自动会将a b c表关联
	 * @param fieldName  关联的属性必须是要查询对象中有的
	 * @return {@link QueryObject}
	 */
	public QueryObject<T> innerJoinFetch(String fieldName){
		return join(fieldName, FETCH, JoinType.INNER);
	}
	
	/**
	 * 左连接抓取, 连接只能取关联对象或关联元素<br>
	 * <b>如果获取的结果中不含要抓取的对象，则会报错
	 * <br>对于要关联的字段，如a.b.c，自动会将a b c表关联
	 * @param fieldName 关联的属性必须是要查询对象中有的
	 * @return {@link QueryObject}
	 */
	public QueryObject<T> leftJoinFetch(String fieldName){
		return join(fieldName, FETCH, JoinType.LEFT);
	}

	/**
	 * 去重，这个可以用在条件前，也可以用在条件后
	 * @return
	 */
	public QueryObject<T> distinct(){
		this.criteria.distinct(true);
		return this;
	}
	
	/**
	 * 包装结果类
	 * @param resultClass 非映射对象
	 * @param fields 字段
	 * @return {@link QueryObject}
	 */
	public <E>QueryObject<T> wrapper(Class<E> resultClass, String... fields){
		Objects.requireNonNull(resultClass, "结果对象类不能为null!");
		if(fields == null || fields.length < 1)
			throw new IllegalArgumentException("字段不能为空!");
		
		Selection[] arrs = new Selection[fields.length];
		for(int i = 0; i < fields.length; i++){
			arrs[i] = extractPath(fields[i]);
		}
		criteria.select(builder.construct(resultClass, arrs));
		return this;
	}
	
	/**
	 * 加锁
	 * @param lockOptions 
	 * @return {@link QueryObject}
	 */
	public QueryObject<T> lock(LockOptions lockOptions){
		this.lockOptions = lockOptions;
		return this;
	}
	
	/**
	 * 加锁
	 * @param lockModeType 
	 * @return {@link QueryObject}
	 */
	public QueryObject<T> lock(LockModeType lockModeType){
		this.lockModeType = lockModeType;
		return this;
	}
	
	
	// ************************************* 以下部分为获取结果 ******************************************
	
    /**
     * 取得一个集合，无分页
     * @return {@link List}
     */
    public <E>List<E> list() {
        return list(this.getCriteriaQuery(), null, null);
    }

    /**
     * 取得一个集合，有分页
     * @param pageNo 起始页号, 从0开始
     * @param size 要查询的数量, 若size为null, 则pageNo变为起始化
     * @return {@link List}
     */
    public <E>List<E> list(int pageNo, Integer size) {
    	return list(this.getCriteriaQuery(), pageNo, size);
    }

    /**
     * 取得单个值的集合，无分页
     * @param field 要查询的字段
     * @return {@link List}
     */
    public <E>List<E> list(String field) {
    	return list(this.getSingleFieldCriteriaQuery(field), null, null);
    }
    
    /**
     * 取得单个值的集合，有分页
     * @param field 要查询的字段
     * @param pageNo 起始行, 从0开始
     * @param size 要查询的数量
     * @return {@link List}
     */
    public <E>List<E> list(String field, int pageNo, Integer size) {
    	return list(this.getSingleFieldCriteriaQuery(field), pageNo, size);
    }
    
    /**
     * 取得多个值的集合，返回为数组集合，无分页, 这个返回的是数组的集合
     * @param fields 要查询的字段
     * @return {@link List}
     */
    public List<Object[]> list(String... fields) {
    	return list(this.getAnyFieldsCriteriaQuery(fields), null, null);
    }
    
    /**
     * 取得多个值的集合，返回为数组集合，有分页, 这个返回的是数组的集合
     * @param pageNo 起始行, 从0开始
     * @param size 要获取的数量
     * @param fields 要查询的字段
     * @return {@link List}
     */
    public List<Object[]> list(int pageNo, Integer size, String... fields) {
    	return list(this.getAnyFieldsCriteriaQuery(fields), pageNo, size);
    }
    
    /**
     * 取得一个集合，带分页
     * @param criteria criteria
     * @param pageNo 起始行, 从0开始
     * @param size 要获取的数量
     * @return {@link List}
     */
    private <E>List<E> list(CriteriaQuery criteria, Integer pageNo, Integer size) {
        Query<E> query = session.createQuery(criteria);
        
        addPage(query, pageNo, size);
        
        addLock(query);
        return query.getResultList();
    }
    
    /**
     * 取得一个对象
     * @return T
     */
    public T getOne() {
        Query<T> query = session.createQuery(this.getCriteriaQuery());
        addLock(query);
		return query.uniqueResult();
    }

    /**
     * 获取某一个值，返回单个值，对于有多个值的，会抛出异常
     * @param field 要查询的字段
     * @param <E>E 要返回的结果类型
     * @return 返回的结果
     */
	public <E>E getSingleValue(String field) {
    	Query<T> query = session.createQuery(this.getSingleFieldCriteriaQuery(field));
    	addLock(query);
		return (E)query.uniqueResult();
    }
	
	/**
	 * 统计所有
	 * @return {@link Number}
	 */
	public long count() {
		return count(null, false);
	}
	
	/**
	 * 根据某个字段进行统计，默认不去重
	 * @param field 要统计的字段
	 * @return {@link Number}
	 */
	public long count(String field) {
		return count(field, false);
	}
	
	/**
	 * 根据某个字段进行统计
	 * @param distinct 是否去重
	 * @return {@link Number}
	 */
	public long count(boolean distinct) {
		return count(null, distinct);
	}
    
    /**
     * 根据某个字段进行统计
     * @param field 要统计的字段
     * @param distinct 是否去重
     * @return {@link Number}
     */
    public long count(String field, boolean distinct) {
    	Query<T> query = session.createQuery(this.getCountCriteriaQuery(field, distinct));
    	addLock(query);
		Long count = (Long)query.uniqueResult();
		return count == null ? 0 : count;
    }
    
    /**
     * 统计
     * @param field	要统计的字段
     * @param statType 统计类型，支付 count max min sum，其他不支持
     * @return 返回的结果
     */
    private <E>E stat(String field, String statType) {
    	Query<T> query = session.createQuery(this.getStatCriteriaQuery(field, statType));
    	addLock(query);
		return (E)query.uniqueResult();
    }
    
    /**
     * 获取某个字段的最大值
     * @param field 字段
     * @return 返回最大值
     */
    public <E>E max(String field) {
    	return stat(field, MAX);
    }
    
    /**
     * 获取某个字段的最小值
     * @param field 字段
     * @return E 返回最小值
     */
    public <E>E min(String field) {
    	return stat(field, MIN);
    }
    
    /**
     * 根据某个字段进行求和
     * @param field 字段
     * @return {@link Number}
     */
    public Number sum(String field) {
    	return stat(field, SUM);
    }

	/**
	 * 添加锁
	 * @param query
	 */
	private void addLock(Query query) {
		if(lockOptions != null) 
			query.setLockOptions(lockOptions);
		else {
			if(lockModeType != null)
				query.setLockMode(lockModeType);
		}
	}
	
}
