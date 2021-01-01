package test;

import dao.SimpleEntityDao;
import entity.SimpleEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.SessionFactoryUtils;

import java.util.ArrayList;
import java.util.List;

public class TestSimpleEntityDao {
	
	private SessionFactory sf;
	
	private SimpleEntityDao simpleEntityDao = new SimpleEntityDao();

	private List<Session> sessions = new ArrayList<>();
	
	private Session ss;
	private Transaction tx;
	
	private boolean commit = true;

	@Test
	public void test() throws InterruptedException {
		SimpleEntity simpleEntity = simpleEntityDao.get(1L);
		System.out.println(simpleEntity);
	}
	
	@Before
	public void before() {
		sf = SessionFactoryUtils.build();
		ss = simpleEntityDao.getSession();
		tx = ss.beginTransaction();
	}
	
	@After
	public void close() {
		
		if (commit) {
			tx.commit();
		}
		
		if(sf != null && sf.isOpen())
			sf.close();
		
		if(ss != null && ss.isOpen())
			ss.close();
		
		for(Session s : sessions) {
			if(s != null && s.isOpen())
				s.close();
		}
	}

	@Test
    public void save() {
//		SimpleEntity user = new User("叫什么名字", 5);
//		user.setId(9999L); // 无效
//		userDao.save(user);
//		System.out.println(user);
    }

}
