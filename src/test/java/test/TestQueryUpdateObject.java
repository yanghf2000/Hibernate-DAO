package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.LockModeType;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.yanghf2000.dao.FieldsAndValuesMap;

import dao.AddressDao;
import dao.CompanyDao;
import dao.UserDao;
import entity.Address;
import entity.Company;
import entity.CompanyAddress;
import entity.User;
import util.SessionFactoryUtils;

public class TestQueryUpdateObject {
	
	private SessionFactory sf;
	
	private UserDao userDao = new UserDao();
	private AddressDao addressDao = new AddressDao();
	private CompanyDao companyDao = new CompanyDao();
	
	private Session ss;
	private Transaction tx;
	
	private boolean commit = true;

	@Test
	public void test()  {
		UserDao userDao = new UserDao();
		User user = (User) userDao.get(1L);
		System.out.println(user);
	}
	
	@Before
	public void before() {
		sf = SessionFactoryUtils.build();
		ss = userDao.getSession();
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
	}
	
	// *************************** 更新测试 *******************************
	
	@Test
	public void testUpdate() {
		int num = userDao.getUpdateObject().set("name", "FFFAVDSA").set("age", 100).andIn("id", Arrays.asList(5L, 6L, 9L, 10L)).update();
		System.out.println(num);
	}
	
	@Test
	public void testUpdateByMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("name", "RST");
		map.put("age", 35);
		
		int num = userDao.getUpdateObject().set(map).andIn("id", Arrays.asList(5L, 6L, 9L, 10L)).update();
		System.out.println(num);
	}
	
	@Test
	public void testUpdateByFieldAndValueMap() {
		int num = userDao.getUpdateObject().set(FieldsAndValuesMap.init().add("name", "XYZ").add("age", 25)).andIn("id", Arrays.asList(5L, 6L, 9L, 10L)).update();
		System.out.println(num);
	}
	
	/**
	 *     update
		        Address cross 
		    join
		        
		    set
		        street=? 
		    where
		        name=? 
		        or 0=1
		      
		  由于更新时的sql字段并没有加别名，所以不能用连表
		  对于显示的关联表，报的错是 UPDATE/DELETE criteria queries cannot define joins
	 */
	@Test
	public void testUpdate2() {
		int num = addressDao.getUpdateObject()/*.innerJoin("user")*/.set("street", "蘑菇屯").andEqual("user.name", "HJK").update();
		System.out.println(num);
	}

	/**
	 * 对于什么条件也不加的，报以下错误
	 * org.opentest4j.MultipleFailuresError: Multiple Failures (2 failures)
		Error occurred validating the Criteria
		Transaction was marked for rollback only; cannot commit
	 */
	@Test
	public void testUpdate3() {
		int num = userDao.getUpdateObject().update();
		System.out.println(num);
	}

	
	// **************************** 删除测试 ************************************
	
	/**
	 * 删除
	 */
	@Test
	public void testDelete() {
		int num = userDao.getDeleteObject().isNull("info").delete();
		System.out.println(num);
	}
	
	/**
	 * 不加条件则通不过
	 */
	@Test
	public void testDelete2() {
		int num = addressDao.getDeleteObject().delete();
		System.out.println(num);
	}
	
	/**
	 * 删除所有行
	 */
	@Test
	public void testDeleteAll() {
		int num = addressDao.getDeleteObject().deleteAll();
		System.out.println(num);
	}
}
