package com.github.yanghf2000.queryobject;

import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.Session;

/**
 * 封装更新对象<p>
 * 由于Dao的子类是单例的，所以这个对象的实例不能作为Dao的变量<p>
 * @author 杨会锋
 * 2018-1-25
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class QueryDeleteObject<T> extends AbstraceQueryObject<QueryDeleteObject<T>, T>{
	
	private CriteriaDelete<T> criteria;
	
	public static <T>QueryDeleteObject<T> getInstance(Session session, Class<T> clazz){
		return new QueryDeleteObject(session, clazz);
	}
	
	private QueryDeleteObject(Session session, Class<T> clazz){
		super(session, clazz);
		this.criteria = builder.createCriteriaDelete(clazz);
		this.root = criteria.from(clazz);
	}
	
	/**
	 * delete<p>
	 * <b>若不加条件，则会删除所有行
	 * @return 删除成功的数量
	 */
	public int delete() {
		if(andPres.isEmpty() && orPres.isEmpty())
			throw new IllegalArgumentException("请为删除语句添加条件!");
		
		return this.session.createQuery(this.getCriteriaUpdate()).executeUpdate();
	}
	
	/**
	 * 删除所有行
	 * @return
	 */
	public int deleteAll() {
		return this.session.createQuery(this.criteria).executeUpdate();
	}

	/**
	 * 获取CriteriaQuery
	 * @return {@link CriteriaQuery}
	 */
	private CriteriaDelete<T> getCriteriaUpdate(){
		this.criteria.where(getPredicate());
		return this.criteria;
	}
	
}
