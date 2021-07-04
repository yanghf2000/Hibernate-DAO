package com.github.yanghf2000.dao;

import com.github.yanghf2000.queryobject.QueryDeleteObject;
import com.github.yanghf2000.queryobject.QueryObject;
import com.github.yanghf2000.queryobject.QuerySearchObject;
import com.github.yanghf2000.queryobject.QueryUpdateObject;
import org.apache.lucene.util.CollectionUtil;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;

import javax.persistence.criteria.*;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.Map.Entry;


/**
 * Dao
 * @author 杨会锋
 * @param <T> 要获取的数据类型，必须是持久化类
 */
public abstract class Dao<T> {

    /**
     * 默认超时时间，单位：毫秒
     */
	public final static int TIME_OUT = 30_000;

    protected Class<T> clazz;

    @SuppressWarnings("unchecked")
    public Dao() {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        clazz = (Class<T>) type.getActualTypeArguments()[0];
    }

    protected abstract SessionFactory getSessionFactory();

    /**
     * 获取session
     * @return
     */
    protected Session getSession() {
        return getSession(null);
    }

    /**
     * 获取session
     * @param timeout 查询超时时间，单位：毫秒
     * @return
     */
    protected Session getSession(Integer timeout) {
        Session currentSession = getSessionFactory().getCurrentSession();
        currentSession.setProperty("javax.persistence.query.timeout", timeout != null ? timeout : TIME_OUT);
        currentSession.setProperty("hibernate.order_updates", true);
        currentSession.setProperty("hibernate.order_inserts", true);
        return currentSession;
    }

    /**
     * 调用该方法必须要关闭session
     * @param timeoutMillis 超时时间，单位：毫秒
     * @return
     */
    public Session getNewSession(Integer timeoutMillis) {
        Session openSession = getSessionFactory().openSession();
        openSession.setProperty("javax.persistence.query.timeout", timeoutMillis != null ? timeoutMillis : TIME_OUT);
        return openSession;
    }

    /**
     * 无状态session，调用该方法必须要关闭session
     * @return
     */
    protected StatelessSession getStatelessSession() {
        return getSessionFactory().openStatelessSession();
    }

    /**
     * 获取全文本索引session
     * @return
     */
    protected SearchSession getSearchSession(){
        return Search.session(getSession());
    }

    /**
     * 获取全文本索引session
     * @return
     */
    protected SearchSession getSearchSession(Integer timeout){
        return Search.session(getSession(timeout));
    }

    /**
     * flush
     */
    public void flush() {
        this.getSession().flush();
    }
    /**
     * flush
     */
    public void flushAndClear() {
        this.getSession().flush();
        this.getSession().clear();
    }

    /**
     * clear
     */
    public void clear() {
        this.getSession().clear();
    }

    /**
     * evict
     * @param t
     */
    public void evict(T t) {
    	this.getSession().evict(t);
    }
    
    /**
     * refresh<p>
     * 若没有主键，报错：org.hibernate.AssertionFailure: null identifier<br>
     * 对于不存在的行会报错：org.hibernate.UnresolvableObjectException: No row with the given identifier exists: [entity.User#1362]<br>
     * @param t 要刷新的对象，必须要有id
     */
    public void refresh(T t) {
        getSession().refresh(t);
    }
    
    // *************************** 增 *************************************
    /**
     * 保存, 对于save方法，id放不放值没有什么用
     * @param t 要保存的对象
     * @return the generated identifier 返回生成的主键
     */
    public Serializable save(T t) {
        return (Serializable) getSession().save(t);
    }

    /**
     * merge
     * Copy the state of the given object onto the persistent object with the same
	 * identifier. If there is no persistent instance currently associated with
	 * the session, it will be loaded. Return the persistent instance. If the
	 * given instance is unsaved, save a copy of and return it as a newly persistent
	 * instance. The given instance does not become associated with the session.
	 * This operation cascades to associated instances if the association is mapped
	 * with {@code cascade="merge"}
	 * <p/>
	 * The semantics of this method are defined by JSR-220.
	 * 
	 * 对于临时对象，merge后会被丢弃，session中不会包含，merge后会返回一个新对象，同时被session维护<br>
	 * 对于持久化对象，查出来后再merge，新对象只是原持久化对象的复制，hash值都是一样的<br>
	 * 对于持久化对象，查出来后evict，旧的对象不被session维护，session只维护新的对象<br>
	 *
	 * @param t a detached instance with state to be copied
	 * @return an updated persistent instance
	 *
     * @param t
     * @return
     */
    @SuppressWarnings("unchecked")
	public T merge(T t) {
        return (T) getSession().merge(t);
    }
    
    // ********************************* 删 ***********************************
    /**
     * 删除
     * @param t 要删除的对象，有id就行
     */
    public void delete(T t) {
        getSession().delete(t);
    }

    /**
     * 对于大多数有id的对象，根据id删除对象，返回删除的数量
     * @param ids 主键
     * @return 返回删除成功的行数
     */
    public int deleteById(Long... ids) {
        if(ids == null || ids.length < 1) {
            throw new IllegalArgumentException("ids不能为空!");
        }
    	return delete("id", Arrays.asList(ids));
    }
    
    /**
     * 根据某个字段删除数据, 这个目前只能删类中的独立属性， 对于带导航的属性，如：a.b.c，这种是不能删除的，
     * 原因在于，join的表没有加上条件，且delete后没有指定要删除的表
     *     delete 
		    from
		        Address cross 
		    join
		        `User` user1_ 
		    where
		        name=?
     * @param propertyName		字段名
     * @param value				  	值, 可以为List，为list时条件表达式为in形式
     * @return 返回删除成功的行数
     */
    @SuppressWarnings("rawtypes")
	public int delete(String propertyName, Object value) {
    	CriteriaBuilder builder = getSession().getCriteriaBuilder();
    	CriteriaDelete<T> criteria = builder.createCriteriaDelete(clazz);
    	Root<T> root = criteria.from(clazz);
    	Path<Object> path = root.get(propertyName);
    	
    	if(value instanceof Collection) {
            criteria.where(path.in((Collection) value));
        } else {
            criteria.where(builder.equal(path, value));
        }

    	return getSession().createQuery(criteria).executeUpdate();
    }
	
    /**
     * 删除
     * @param sqls					原生sql集合
     */
    @SuppressWarnings("rawtypes")
	public void deleteBySqls(List<String> sqls) {
    	Session session = getSession();
        for (int i = 0; i < sqls.size(); i++) {
			Query query = session.createNativeQuery(sqls.get(i));
            query.executeUpdate();
            
            if(i > 1 && i % 10000 == 0) {
            	session.flush();
            	session.clear();
            }
        }
    }
    
    // 改
    /**
     * 更新, 对于不存在的行，在执行提交时会报以下错误<p>
     * javax.persistence.OptimisticLockException: Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1<br>
     * Caused by: org.hibernate.StaleStateException: Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1
     * @param t
     */
    public void update(T t) {
        getSession().update(t);
    }

    /**
     * update
     * @param id
     * @param prop
     * @param value
     * @return
     */
    public int update(Serializable id, String prop, Object value) {
        return this.getUpdateObject().set(prop, value).andEqual("id", id).update();
    }

    /**
     * 保存/更新, id为null时为保存，id不为空时为更新，但若id不为空，但不存在记录，在提交事务时会抛错：<br>
     * javax.persistence.OptimisticLockException: Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1<br>
     * Caused by: org.hibernate.StaleStateException: Batch update returned unexpected row count from update [0]; actual row count: 0; expected: 1
     * @param t
     */
    public void saveOrUpdate(T t) {
        getSession().saveOrUpdate(t);
    }

    /**
     * 批量保存对象, 不一定非T类型，但一定要是可持久化对象, 每1000条flush一次
     * @param list
     * @throws Exception
     */
    public void batchSave(Collection<T> list) {
        Session session = getSession();
        Iterator<T> iterator = list.iterator();
        for(int i = 0; iterator.hasNext(); i++){
            session.save(iterator.next());

            if (i > 0 && i % 1000 == 0) {
                session.flush();
                session.clear();
            }
        }
    }

    /**
     * 用原生sql执行更新
     * @param sql
     * @param args
     * @return 返回执行成功的行数
     */
    @SuppressWarnings("rawtypes")
	public int updateBySql(String sql, Object... args) {
        Query query = getSession().createNativeQuery(sql);
        addParameters(query, true, args);
        return query.executeUpdate();
    }

    /**
     * 用原生sql执行更新
     * @param sql
     * @param args
     * @return 返回执行成功的行数
     */
    @SuppressWarnings("rawtypes")
	public int updateBySql(String sql, Integer timeOut, Object... args) {
        Query query = getSession(timeOut == null ? TIME_OUT : timeOut).createNativeQuery(sql);
        addParameters(query, true, args);
        return query.executeUpdate();
    }

    /**
     * 为query添加参数
     * @param query
     * @param isNativeQuery
     * @param args
     */
    private void addParameters(Query query, boolean isNativeQuery, Object... args) {
        if (args != null && args.length > 0) {
            int offset = isNativeQuery ? 1 : 0;
            for (int i = 0; i < args.length; i++) {
                Object value = args[i];
                if (value instanceof Collection) {
                    // native的方法是从1开始
                    query.setParameterList(i + offset, (Collection) value);
                } else if (value instanceof Object[]) {
                    query.setParameterList(i + offset, (Object[]) value);
                } else {
                    query = query.setParameter(i + offset, value);
                }
            }
        }
    }

    /**
     * 执行更新, hql语句
     * @param hql
     * @param args	如果有in语句，如果是集合可以直接传入；如果是数组，不能直接放进来，要用new Object[]{new Object[]{1, 2, 3}}，否则会处理成单个值，造成不起作用
     */
    @SuppressWarnings({ "rawtypes", "deprecation" })
	public int updateByHql(String hql, Object... args) {
        Query query = getSession().createQuery(hql);
        addParameters(query, false, args);
        return query.executeUpdate();
    }
    
    // 查
    // ***************** 查单个值或一条记录 ***********************
    /**
     * 根据id获取对象
     * @param id
     * @return
     */
    public T get(Long id) {
        return getSession().get(clazz, id);
    }
    
    /**
     * 根据id获取对象， 可以加锁，对于锁为null的，则不加锁
     * @param id
     * @param lockOptions
     * @return
     */
    public T get(Long id, LockOptions lockOptions) {
    	return lockOptions == null ? get(id) : getSession().get(clazz, id, lockOptions);
    }

    /**
     * 该方法不会直接查询数据库
     * @param id
     * @return
     */
    public T getReference(Long id) {
        return getSession().getReference(clazz, id);
    }
    
    /**
     * 用原生sql查询单条数据, 不用加类型转换，实际过个类型不是映射对象也用不了
     * @param sql
     * @param objects
     * @return
     */
    public <E> E findSingleBySql(String sql, Object... objects) {
        return findSingleValueBySql(null, sql, objects);
    }

    /**
     * 查询单条数据, 可以添加映射类型，实际不是entity注解的类映射用不了
     *
     * @param objectClass
     * @param sql
     * @param objects
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    public <E> E findSingleValueBySql(Class objectClass, String sql, Object... objects) {
        Query<E> query = objectClass == null ? getSession().createNativeQuery(sql) : getSession().createNativeQuery(sql, objectClass);
        addParameters(query, true, objects);
        return query.uniqueResult();
    }
    
    // ****************** 查多条记录 *************************
    /**
     * 查出表中所有数据，这种是针对一些数量较少的表用的，数量较多的表不能用此方法。
     * @return
     */
    public List<T> getAll() {
    	CriteriaBuilder builder = getSession().getCriteriaBuilder();
    	CriteriaQuery<T> cq = builder.createQuery(clazz);
    	Root<T> root = cq.from(clazz);
    	cq.select(root);
    	
    	return this.getSession().createQuery(cq).getResultList();
    }
    
    
    // 查多条记录，带分页
    
    
    // ************************ 根据HQL查询单个值 ********************************
    /**
     * 用hql获取单个值，包括单个字段或单条记录
     * @param hql
     * @param objects
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public <E> E findSingleValueByHql(String hql, Object... objects) {
        Query query = getSession().createQuery(hql);
        addParameters(query, false, objects);
        return (E) query.uniqueResult();
    }

    // ************************ 根据HQL查询list ********************************
    /**
     * 获取集合
     * @param hql
     * @param objects  对于in参数，要传入list，不接收数组
     * @return
     */
    public <E> List<E> findByHQL(String hql, Object... objects) {
    	return findByHQL(hql, null, objects);
    }

    /**
     * 获取集合
     * @param hql
     * @param timeout
     * @param objects  对于in参数，要传入list，不接收数组
     * @return
     */
    public <E> List<E> findByHQL(String hql, Integer timeout, Object... objects) {
    	return findByHQLWithLimit(hql, timeout, null, null, objects);
    }

    /**
     * 用hql获取集合，带分页参数
     * @param hql
     * @param timeout
     * @param first
     * @param size
     * @param objects
     * @return
     */
    public <E> List<E> findByHQLWithLimit(String hql, Integer timeout, Integer first, Integer size, Object... objects) {
    	return this.findByHQLWithLockLimit(hql, timeout, null, first, size, objects);
    }
    
    /**
     * 查询，可选择是否加锁，分页
     * @param hql
     * @param timeout
     * @param lockOptions
     * @param first
     * @param size
     * @param objects
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
	public <E> List<E> findByHQLWithLockLimit(String hql, Integer timeout,
                          LockOptions lockOptions, Integer first, Integer size, Object... objects) {
    	Query query = (timeout == null ? getSession() : getSession(timeout)).createQuery(hql);
    	
    	if (lockOptions != null) {
            query.setLockOptions(lockOptions);
        }

        addParameters(query, false, objects);
    	setLimitProperty(query, first, size);
    	
    	return query.getResultList();
    }
    
    /**
     * 参数带in的, 这一种要用命名参数
     * @param hql
     * @param first
     * @param size
     * @param params
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public <E> List<E> findByHQLWithInLimit(String hql, Integer first, Integer size, Map<String, Object> params) {
        Query query = getSession().createQuery(hql);
        
        params.forEach((k, v) -> {
        	if (v instanceof Collection) {
                query.setParameterList(k, (Collection) v);
            } else {
                query.setParameter(k, v);
            }
        });
        
        setLimitProperty(query, first, size);

        return query.getResultList();
    }
    
    // ******************** 统计 count *****************
    
    /**
     * 通过hql获取统计数量, 如果是不是以 select count 开头，自动加上 select count(*)
     * @param hql			要执行的hql
     * @param objects		参数
     * @return
     */
    public long countByHql(String hql, Object... objects) {
        // 如果不是以select count 开头则加上select count(*)
        String pre = hql.substring(0, hql.indexOf("from")).trim().toLowerCase();
        if (!(pre.contains("select") && pre.contains("count"))) {
            hql = "select count(*) " + hql.substring(hql.indexOf("from"));
        }

        Number num = findSingleValueByHql(hql, objects);
        return num == null ? 0 : num.longValue();
    }
    
    /**
     * 通过原生sql获取统计数量, 如果是以 from 开头，自动加上 select count(*)
     * @param sql			要执行的sql
     * @param objects		参数
     * @return
     */
    public long countBySql(String sql, Object... objects) {
        String pre = sql.substring(0, sql.indexOf("from")).trim().toLowerCase();
        if (!(pre.contains("select") && pre.contains("count"))) {
            sql = "select count(*) " + sql.substring(sql.indexOf("from"));
        }

        Number num = findSingleValueBySql(null, sql, objects);
        return num == null ? 0 : num.longValue();
    }
    

    // ********************根据原生sql查询 ****************
    
    /**
     * 用原生sql获取集合查询，带分页，加映射对象类型
     * @param sql			要执行的sql
     * @param objects		参数
     * @return
     */
	public <E> List<E> findBySql(String sql, Object... objects) {
        return findBySqlWithLimit(null, sql, null, null, objects);
    }

    /**
     * 用原生sql获取集合查询，带分页，加映射对象类型
     * @param session		可以使用自定义session
     * @param sql			要执行的sql
     * @param objects		参数
     * @return
     */
	public <E> List<E> findBySql(Session session, String sql, Object... objects) {
        return findBySqlWithLimit(session, null, sql, null, null, objects);
    }

    /**
     * 用原生sql获取集合查询，带分页，加映射对象类型
     * @param objectClass		查询结果类
     * @param sql			要执行的sql
     * @param objects		参数
     * @return
     */
	public <E> List<E> findBySql(Class objectClass, String sql, Object... objects) {
        return findBySqlWithLimit(objectClass, sql, null, null, objects);
    }

	/**
	 * 用原生sql获取集合查询，带分页，加映射对象类型
	 * @param objectClass		查询结果类
	 * @param sql			要执行的sql
	 * @param first			查询起始位置
	 * @param size			要查询的数量
	 * @param objects		参数
	 * @return
	 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <E> List<E> findBySqlWithLimit(Class objectClass, String sql, Integer first, Integer size, Object... objects) {
    	return findBySqlWithLimit(null, objectClass, sql, first, size, objects);
    }

	/**
	 * 用原生sql获取集合查询，带分页，加映射对象类型
	 * @param session		可以使用自定义session
	 * @param objectClass		查询结果类
	 * @param sql			要执行的sql
	 * @param first			查询起始位置
	 * @param size			要查询的数量
	 * @param objects		参数
	 * @return
	 */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <E> List<E> findBySqlWithLimit(Session session, Class objectClass, String sql, Integer first, Integer size, Object... objects) {
        Session s = session == null ? getSession() : session;
    	Query query = objectClass == null ? s.createNativeQuery(sql) : s.createNativeQuery(sql, objectClass);
        addParameters(query, true, objects);
    	setLimitProperty(query, first, size);

    	return query.getResultList();
    }

    /**
     * 执行查询，要传入映射结果类型
     * 该方法用6.0.0.Alpha的包后进行了改变，sql要与注解放在一起，既然这样，不用单独写，这里就不需要了
     * @param resultSetMapping	影射的结果类型, 可以参数User上面的ResultSetMapping，具体见Hibernate说明书
     * @param sql						要执行的sql
     * @param first						查询起始位置
     * @param size						要查询的数量
     * @param objects					参数
     * @return
     */
    /*@SuppressWarnings({ "rawtypes", "unchecked" })
	public <E> List<E> findBySqlUseResultMapping(String resultSetMapping, String sql, Integer first, Integer size, Object... objects) {
        NativeQuery query = getSession().createNativeQuery(sql);

        addParameters(query, true, objects);

        if (resultSetMapping != null && !"".equals(resultSetMapping.trim())) {
            query.setResultSetMapping(resultSetMapping);
        }

        setLimitProperty(query, first, size);

        return query.getResultList();
    }*/

    /**
     * 取得一个查询对象
     * @return {@link QueryObject}
     */
    public QueryObject<T> getQueryObject() {
        return QueryObject.getInstance(getSession(), clazz);
    }

    /**
     * 取得一个查询对象
     * @param timeout 超时时间，单位：秒
     * @return {@link QueryObject}
     */
    public QueryObject<T> getQueryObject(Integer timeout) {
        return QueryObject.getInstance(getSession(timeout), clazz);
    }

    /**
     * 取得一个更新对象
     * @return {@link QueryUpdateObject}
     */
    public QueryUpdateObject<T> getUpdateObject() {
    	return QueryUpdateObject.getInstance(getSession(), clazz);
    }

    /**
     * 取得一个更新对象
     * @param timeout 查询超时时间，单位：秒
     * @return {@link QueryUpdateObject}
     */
    public QueryUpdateObject<T> getUpdateObject(int timeout) {
    	return QueryUpdateObject.getInstance(getSession(timeout), clazz);
    }

    /**
     * 取得一个删除对象
     * @return {@link QueryDeleteObject}
     */
    public QueryDeleteObject<T> getDeleteObject() {
    	return QueryDeleteObject.getInstance(getSession(), clazz);
    }
    
    /**
     * 取得一个search查询对象
     * @return {@link QueryObject}
     */
    public QuerySearchObject<T> getQuerySearchObject() {
    	return QuerySearchObject.getInstance(getSession(), clazz);
    }

    /**
     * 取得一个search查询对象
     * @param timeout 超时时间，单位：秒
     * @return {@link QueryObject}
     */
    public QuerySearchObject<T> getQuerySearchObject(Integer timeout) {
    	return QuerySearchObject.getInstance(getSession(timeout), clazz);
    }

    /**
     * 设置分页参数
     * @param query	{@link Query}
     * @param first		起始记录
     * @param size		获取数量
     */
    private void setLimitProperty(Query<Object> query, Integer first, Integer size) {
        if (first != null && first >= 0 && size != null && size > 0) {
            query.setFirstResult(first).setMaxResults(size);
        }
    }
    
    
    /** 
     * 获取单个值（单个条件）
     * @param field	 要查询的字段
     * @param value  值
     * @return T 返回的结果
     */
    public T getOne(String field, Object value) {
    	return this.getQueryObject().andEqual(field, value).getOne();
    }
    
    /**
     * 根据多个条件（都为相等条件）获取单个值
     * @param fv 条件
     * @return 返回的结果
     */
    public T getOne(FieldsAndValuesMap fv) {
    	QueryObject<T> queryObject = this.getQueryObject();
    	for(Entry<String, Object> e : fv.getMap().entrySet()) {
    		queryObject.andEqual(e.getKey(), e.getValue());
    	}
    	return queryObject.getOne();
    }

    /** 
     * 获取list（单个条件）
     * @param field	 要查询的字段
     * @param value  值
     * @return T 返回的结果
     */
    public List<T> getList(String field, Object value) {
    	return this.getQueryObject().andEqual(field, value).list();
    }
    
    /**
     * 根据多个条件（都为相等条件）获取list
     * @param fv 条件
     * @return 返回的结果
     */
	@SuppressWarnings("rawtypes")
	public List<T> getList(FieldsAndValuesMap fv) {
    	QueryObject<T> queryObject = this.getQueryObject();
    	for(Entry<String, Object> e : fv.getMap().entrySet()) {
    		Object value = e.getValue();
			if(value instanceof List) {
				queryObject.andIn(e.getKey(), (List)value);
			}else {
				queryObject.andEqual(e.getKey(), value);
			}
    	}
    	return queryObject.list();
    }
    
	// ************** 以下查询方法是针对一些简单查询，没有添加查询条件 *********************
	
	/**
     * 取得一个集合，无分页
     * @return {@link List}
     */
    public List<T> list() {
        return getAll();
    }
    
    /**
     * 取得一个集合，有分页
     * @param pageNo 起始行，从0开始
     * @param size 要查询的数量
     * @return {@link List}
     */
    public List<T> list(Integer pageNo, Integer size) {
    	return getQueryObject().list(pageNo, size);
    }
    
    /**
     * 取得单个值的集合，无分页
     * @param field 要查询的字段
     * @return {@link List}
     */
    public <E>List<E> list(String field) {
    	return getQueryObject().list(field);
    }
    
    /**
     * 取得单个值的集合，有分页
     * @param field 要查询的字段
     * @param pageNo 起始行，从0开始
     * @param size 要查询的数量
     * @return {@link List}
     */
    public <E>List<E> list(String field, Integer pageNo, Integer size) {
    	return getQueryObject().list(field, pageNo, size);
    }
    
    /**
     * 取得多个值的集合，返回为数组集合，无分页, 这个返回的是数组的集合
     * @param fields 要查询的字段
     * @return {@link List}
     */
    public List<Object[]> list(String... fields) {
    	return getQueryObject().list(fields);
    }
    
    /**
     * 取得多个值的集合，返回为数组集合，有分页, 这个返回的是数组的集合
     * @param pageNo 起始行，从0开始
     * @param size 要获取的数量
     * @param fields 要查询的字段
     * @return {@link List}
     */
    public List<Object[]> list(Integer pageNo, Integer size, String... fields) {
    	return getQueryObject().list(pageNo, size, Objects.requireNonNull(fields));
    }
    
	/**
	 * 根据某个字段进行统计
	 * @return {@link Number}
	 */
	public long count() {
		return count(false);
	}
    

	/**
	 * 根据某个字段进行统计
	 * @param distinct 是否去重
	 * @return {@link Number}
	 */
	public long count(boolean distinct) {
		return getQueryObject().count(distinct);
	}

    /**
     * 根据某个字段进行统计
     * @param field 要统计的字段
     * @param distinct 是否去重
     * @return {@link Number}
     */
    public long count(String field, boolean distinct) {
    	return getQueryObject().count(field, distinct);
    }

    /**
     * 根据多个条件（都为相等条件）获取条数
     * @param fv 条件
     * @param field 要统计的字段
     * @param distinct 是否去重
     */
    public long getByParamsCount(FieldsAndValuesMap fv,String field,boolean distinct) {
        QueryObject<T> queryObject = this.getQueryObject();
        for(Entry<String, Object> e : fv.getMap().entrySet()) {
            queryObject.andEqual(e.getKey(), e.getValue());
        }
        return queryObject.count(field,distinct);
    }


    /**
     * 获取某个字段的最大值
     * @param field 字段
     * @return 返回最大值
     */
    public <E>E max(String field) {
    	return getQueryObject().max(field);
    }
    
    /**
     * 获取某个字段的最小值
     * @param field 字段
     * @return E 返回最小值
     */
    public <E>E min(String field) {
    	return getQueryObject().min(field);
    }
    
    /**
     * 根据某个字段进行求和
     * @param field 字段
     * @return {@link Number}
     */
    public Number sum(String field) {
    	return getQueryObject().sum(field);
    }

    /**
     * 搜索
     * @param key 关键字
     * @param pageNo			第几页，从0开始
     * @param pageSize			每页数量
     * @param fields				要查找的字段, 不能为空
     * @return {@link List}
     */
	public List<T> search(String key, int pageNo, int pageSize, String... fields) {
        return search(key, true, null, pageNo, pageSize, fields);
    }
	
	 /**
     * 搜索
     * @param key 关键字
     * @param pageNo			第几页，从0开始
     * @param pageSize			每页数量
     * @param fields				要查找的字段, 不能为空
     * @param joinFields			要连表的字段, 可以为空
     * @return {@link List}
     */
	public List<T> search(String key, int pageNo, int pageSize, String[] fields, String[] joinFields) {
		return search(key, true, null, pageNo, pageSize, fields, joinFields);
	}

    /**
     * 搜索
     * @param key					要查找的关键字
     * @param asc				是否升序
     * @param orderField			排序字段
     * @param pageNo			第几页，从0开始
     * @param pageSize			每页数量
     * @param fields				要查找的字段, 不能为空, 对于类中的嵌套对象，查询为：属性名.字段名  或 属性名.属性名.字段名，所有的导航到的属性都要加上, 
     * 										对于这种带导航的查找，会自动进行关联表
     * @return {@link List} 结果
     */
    public List<T> search(String key, boolean asc, String orderField, int pageNo, int pageSize, String... fields) {
    	return search(key, asc, orderField, pageNo, pageSize, fields, null);
    }
    
    /**
     * 搜索
     * @param key					要查找的关键字
     * @param reverse			如果倒自然排序，请设为true，
     * @param orderField			排序字段，对于要排序的字段，需要加@SortableField注解
     * @param pageNo			第几页，从0开始
     * @param pageSize			每页数量
     * @param fields				要查找的字段, 不能为空, 对于类中的嵌套对象，查询为：属性名.字段名  属性名.属性名.字段名，所有的导航到的属性都要加上, 
     * 										对于这种带导航的查找，会自动进行关联表
     * @param joinFields			要连表的字段, 可以为空，如果查找的字段不需要关联表，但获取的内容需要关联表，则可以使用此字段
     * @return
     */
    public List<T> search(String key, boolean reverse, String orderField, int pageNo, int pageSize, String[] fields, String[] joinFields) {
    	if(fields == null || fields.length < 1) {
            throw new IllegalArgumentException("要查找的字段不能为空!");
        }
    	
    	QuerySearchObject<T> qo = this.getQuerySearchObject().match(key, fields);
    	if(joinFields != null && joinFields.length > 0) {
            for(String join : joinFields) {
                qo.join(join);
            }
        }
    	
    	if(orderField != null && !"".equals(orderField)) {
            qo.sort(orderField, reverse);
        }
    	
    	return qo.list(pageNo, pageSize);
    }
    
    /**
     * hibernate search索引维护, 这一种有个问题，就是数据太多的情况下占用内存严重，最后可能导致内存溢出
     * @param types			要维护的类，为空时维护所有
     * @throws InterruptedException
     */
    @SuppressWarnings("rawtypes")
	public void batchMaintainIndex(Class... types) throws InterruptedException {
        if(types == null || types.length < 1) {
            Search.session(getSession()).massIndexer().startAndWait();
        } else {
            // 这一种费内存
            Search.session(getSession()).massIndexer(Objects.requireNonNull(types)).startAndWait();
        }
    }

    /**
     * 索引，每批处理
     * @param type
     * @param size  每次处理的数量，如为null，默认为10000个
     * @throws InterruptedException
     */
	public void maintainIndex(Class type, Integer size) throws InterruptedException {
        for(int i = 0; ; i++) {
            List list = QueryObject.getInstance(getSession(), type).list(i, size == null ? 10_000 : size);
            if(list == null || list.isEmpty()) {
                break;
            }
            list.forEach(e -> getSearchSession().indexingPlan().addOrUpdate(e));
            flushAndClear();
        }
    }

    /**
     * Force the (re)indexing of a given <b>managed</b> object.
     * Indexation is batched per transaction: if a transaction is active, the operation
     * will not affect the index at least until commit.
     * <p>
     * this method forces an index operation.
     */
    public void index(T t) {
        getSearchSession().indexingPlan().addOrUpdate(t);
    }

    /**
     * 批量维护索引
     * @param ids
     */
    public void batchIndex(Serializable... ids) {
        if(ids != null && ids.length > 0) {
            for(int i = 0, qty = 1000; ; i++) {
                List<T> objs = this.getQueryObject().andIn("id", ids).list(i, qty);
                if(objs == null || objs.isEmpty()) {
                    break;
                }
                objs.forEach(this::index);
            }
        }
    }

    /**
     * 批量维护索引
     * @param ids
     */
    public void batchIndex(Collection<? extends Serializable> ids) {
        if(ids != null && ids.size() > 0) {
            batchIndex(ids.toArray(new Serializable[0]));
        }
    }

    /**
     * Remove the entity with the type <code>entityType</code> and the identifier <code>id</code> from the index.
     * If <code>id == null</code> all indexed entities of this type and its indexed subclasses are deleted. In this
     * case this method behaves like {@link #purgeAll(Class)}.
     * <p>
     * this method forces a purge operation.
     * @param id
     *
     */
    public void deleteIndex(Serializable id) {
        T t = this.getQueryObject().andEqual("id", id).getOne();
        if(t != null) {
            getSearchSession().indexingPlan().delete(t);
        }
    }

    /**
     * 删除索引
     * @param t
     */
    public void deleteIndex(T t) {
        getSearchSession().indexingPlan().delete(t);
    }

    /**
     * 批量删除索引
     * @param ids
     */
    public void batchDeleteIndex(Serializable... ids) {
        if(ids != null && ids.length > 0) {
            List<T> list = this.getQueryObject().andIn("id", ids).list();
            if(list != null && !list.isEmpty()) {
                for (T t : list) {
                    getSearchSession().indexingPlan().delete(t);
                }
            }
        }
    }

    /**
     * 批量删除索引
     * @param ids
     */
    public void batchDeleteIndex(Collection<? extends Serializable> ids) {
        if(ids != null && ids.size() > 0) {
            batchDeleteIndex(ids.toArray(new Serializable[0]));
        }
    }

    /**
     * Remove all entities from of particular class and all its subclasses from the index.
     * @param clazz
     */
    public void purgeAll(Class<T> clazz) {
        getSearchSession().workspace(clazz).purge();
    }

    /**
     * Flush all index changes forcing Hibernate Search to apply all changes to the index not waiting for the batch limit.
     */
    public void flushToIndexes() {
        getSearchSession().workspace().flush();
    }

}
