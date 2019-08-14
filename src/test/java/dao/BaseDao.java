package dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.github.yanghf2000.dao.Dao;

import entity.BaseEntity;
import util.SessionFactoryUtils;

public abstract class BaseDao<T extends BaseEntity> extends Dao<T> {
	
	// 正式用的baseDao只需要加以下代码，去掉注释即可
	
/*	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	protected SessionFactory getSessionFactory() {
		return sessionFactory;
	}*/
	
	
	// 以下是测试代码用
	
	private volatile static Session session;
	
	private static SessionFactory sessionFactory;
	
	static {
		sessionFactory = SessionFactoryUtils.build();
	}
	
	@Override
	protected SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
    public synchronized Session getSession() {
    	if(session != null)
    		return session;
    	
		session = getSessionFactory().openSession();
		session.setProperty("javax.persistence.query.timeout", TIME_OUT);
		session.setProperty("hibernate.order_updates", true);
		session.setProperty("hibernate.order_inserts", true);
		
		return session;
    }
    
}
