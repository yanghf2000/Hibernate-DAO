package test;

import dao.AddressDao;
import dao.CategoryDao;
import dao.ProductDao;
import dao.UserDao;
import entity.Address;
import entity.Product;
import entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.SessionFactoryUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestSearch {
	
	private SessionFactory sf;
	
	private UserDao userDao = new UserDao();
	private AddressDao addressDao = new AddressDao();
	private ProductDao productDao = new ProductDao();
	private CategoryDao categoryDao = new CategoryDao();
	
	private List<Session> sessions = new ArrayList<>();
	
	private Session ss;
	private Transaction tx;
	
	private boolean commit = true;

	@Test
	public void test() throws InterruptedException {
		userDao.batchMaintainIndex();
		User user = userDao.get(1L);
		System.out.println(user);
	}
	
	@Before
	public void before() throws InterruptedException {
		sf = SessionFactoryUtils.build();
		ss = userDao.getSession();
		tx = ss.beginTransaction();
		
		userDao.batchMaintainIndex();
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
    
	/**
	 * 测试搜索单个值，只要相应的类上加上@Indexed, 字段上加上@Field就行<br>
	 * 若字段上加了@Field，
	 */
	@Test
    public void testSearch() {
		List<User> list = userDao.search("什么", 0, 20, "name");
		System.out.println(list.size());
		list.forEach(u -> System.out.println(u.getId() + " " + u.getName()));
    }
	
	/**
	 * 测试搜索多个值，对于每个要搜索的字段，都必须加上@Field
	 */
	@Test
	public void testSearch2() {
		List<User> list = userDao.search("什么", 0, 20, "name", "info");
		System.out.println(list.size());
		list.forEach(u -> System.out.println(u.getId() + " " + u.getName()));
	}
	
	/**
	 * 测试搜索带嵌套对象的值，在类的属性上要加上@IndexedEmbedded，在相应的类的字段上要加上@Field，对于这种情况，在被嵌套的类上可以不用加@Indexed，
	 * 但是不能单独搜索被嵌套类中的属性，
	 */
	@Test
	public void testSearch3() {
		List<User> list = userDao.search("什么", 0, 20, "name", "addresses.street");
		System.out.println(list.size());
		list.forEach(u -> System.out.println(u.getId() + " " + u.getName()));
		
		// 对于这种情况，Address上没有加@Indexed注解，是不能用的
		List<Address> list1 = addressDao.search("什么", 0, 20, "street");
		System.out.println(list1.size());
		list1.forEach(u -> System.out.println(u.getId() + " " + u.getStreet()));
	}
	
	/**
	 * 测试搜索带嵌套对象的值，在类的属性上要加上@IndexedEmbedded，在相应的类的字段上要加上@Field<br>
	 * 对于查询的深度比较大的，要加depth，查询时，对于查对象的对象的字段时，用 属性名.类名.具体查询属性名 组成<br>
	 * 像本例子，categories productCategory上的category上都要加IndexedEmbedded，category的name字段上要加@Field
	 */
	@Test
	public void testSearch4() {
		List<Product> list = productDao.search("仙女果", false, "name", 1, 1, "name", "categories.category.name");
		System.out.println(list.size());
		list.forEach(u -> System.out.println(u.getId() + " " + u.getName() + " "
				 + u.getCategories().stream().map(pc -> pc.getCategory().getName()).collect(Collectors.toList())
				));
	}
	
}
