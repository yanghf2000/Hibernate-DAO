package com.github.yanghf2000.queryobject;

import java.util.Map;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;

import org.hibernate.Session;

import com.github.yanghf2000.dao.FieldsAndValuesMap;

/**
 * 封装更新对象<p>
 * 由于Dao的子类是单例的，所以这个对象的实例不能作为Dao的变量<p>
 * @author 杨会锋
 * 2018-1-25
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class QueryUpdateObject<T> extends AbstraceQueryObject<QueryUpdateObject<T>, T>{
	
	private CriteriaUpdate<T> criteria;
	
	public static <T>QueryUpdateObject<T> getInstance(Session session, Class<T> clazz){
		return new QueryUpdateObject(session, clazz);
	}
	
	private QueryUpdateObject(Session session, Class<T> clazz){
		super(session, clazz);
		this.criteria = builder.createCriteriaUpdate(clazz);
		this.root = criteria.from(clazz);
	}
	
	/**
	 * 设置字段和值
	 * @param fieldName
	 * @param value
	 * @return
	 */
	public QueryUpdateObject<T> set(String fieldName, Object value) {
		this.criteria.set(fieldName, value);
		return this;
	}
	
	/**
	 * set
	 * @param fav {@link FieldsAndValuesMap}
	 * @return 
	 */
	public QueryUpdateObject<T> set(FieldsAndValuesMap fav) {
		return this.set(fav.getMap());
	}
	
	/**
	 * set
	 * @param map
	 * @return 
	 */
	public QueryUpdateObject<T> set(Map<String, Object> map) {
		map.forEach((k, v) -> this.set(k, v));
		return this;
	}
	
	/**
	 * update
	 * @return 更新成功的数量
	 */
	public int update() {
		return this.session.createQuery(this.getCriteriaUpdate()).executeUpdate();
	}

	/**
	 * 获取CriteriaQuery
	 * @return {@link CriteriaQuery}
	 */
	private CriteriaUpdate<T> getCriteriaUpdate(){
		this.criteria.where(getPredicate());
		return this.criteria;
	}

}
