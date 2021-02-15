package test;

import dao.AddressDao;
import dao.CompanyDao;
import dao.ProductDao;
import dao.UserDao;
import entity.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.SessionFactoryUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestQuerySearchObject {
	
	private SessionFactory sf;
	
	private UserDao userDao = new UserDao();
	private AddressDao addressDao = new AddressDao();
	private CompanyDao companyDao = new CompanyDao();
	private ProductDao productDao = new ProductDao();
	
	private Session ss;
	private Transaction tx;
	
	private boolean commit = true;

	@Before
	public void before() throws InterruptedException {
		sf = SessionFactoryUtils.build();
		ss = userDao.getSession();
		tx = ss.beginTransaction();
		
		userDao.maintainIndex();
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

	/**
	 * 查询所有
	 */
	@Test
	public void testMatchAll() {
		List<User> list = userDao.getQuerySearchObject().list(1, 2);
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	/**
	 * 查询null值，加上 @GenericField(name = "name_1", indexNullAs = "")，指定一个别名，加上null值代替值，
	 * 注解@FullTextField 不能指定null值
	 */
	@Test
	public void testMatchNull1() {
		List<User> list = userDao.getQuerySearchObject().match("", "name_1").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	/**
	 * 查询指定字段
	 */
	@Test
	public void testMatchNull2() {
		List<User> list = userDao.getQuerySearchObject().match(null, "name", "property").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	/**
	 * 查询指定字段
	 */
	@Test
	public void testMatch1() {
		List<User> list = userDao.getQuerySearchObject().match("JHON", "name").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	/**
	 * 查询指定字段
	 */
	@Test
	public void testMatch2() {
		List<User> list = userDao.getQuerySearchObject().match("JHON 茅台", "name").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	/**
	 * 中文查询
	 */
	@Test
	public void testMatch3_Chinese() {
		List<User> list = userDao.getQuerySearchObject().match("茅台", "name").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	/**
	 * 中文查询
	 */
	@Test
	public void testMatch3_Chinese_1() {
		List<Product> list = productDao.getQuerySearchObject().match("摔坏的手机", "name").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	/**
	 * 中文查询
	 */
	@Test
	public void testMatch3_Chinese_2() {
		List<Product> list = productDao.getQuerySearchObject().match("5G", "subTitle").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	/**
	 * 中文查询
	 */
	@Test
	public void testCascade1() {
		List<User> list = userDao.getQuerySearchObject().match("龙华区", "addresses.street").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	@Test
	public void testCascade2() {
		//这种查找，对于有一个分词匹配不上，就获取不到结果
		List<Address> list = addressDao.getQuerySearchObject()
				.match("1231", "user.name").list();
		System.out.println(list.size());
		list.forEach(p -> System.out.println(p.getStreet()));
	}

	/**
	 * id查询
	 */
	@Test
	public void testMatchId() {
		List<User> list = userDao.getQuerySearchObject().match(1L, "id").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	/**
	 * id查询
	 */
	@Test
	public void testMatchId2() {
		List<User> list = userDao.getQuerySearchObject().matchId(1L).list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	/**
	 * id查询
	 */
	@Test
	public void testMatchId3() {
		List<User> list = userDao.getQuerySearchObject().matchId(Arrays.asList(1L, 2L, 3L)).list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	/**
	 * id查询，id不能为null值
	 */
	@Test
	public void testMatchId3_null() {
//		List<User> list = userDao.getQuerySearchObject().matchId(null).list();
		List<User> list = userDao.getQuerySearchObject().match(null, "id").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	/**
	 * 中文查询
	 */
	@Test
	public void testMatchId4() {
		List<Address> list = addressDao.getQuerySearchObject().match(2L, "user.id").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getUser().getId() + " " + e.getId() + " " + e.getStreet()));
	}

	@Test
	public void testMatchId5() {
		List<Company> list = companyDao.getQuerySearchObject().match(2L, "user.id").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	/**
	 * 较为精确的匹配
	 */
	@Test
	public void testPhrase() {
		//这种查找，对于有一个分词匹配不上，就获取不到结果
		List<Product> list = productDao.getQuerySearchObject()
				.phrase("小米 手机", "name").list(0, 20);
		System.out.println(list.size());
		list.forEach(p -> System.out.println(String.format("id: %d, name: %s, subtitle: %s",
				p.getId(), p.getName(), p.getSubtitle())));
	}

	/**
	 * 较为精确的匹配
	 */
	@Test
	public void testPhrase1() {
		//这种查找，对于有一个分词匹配不上，就获取不到结果
		List<Address> list = addressDao.getQuerySearchObject()
				.phrase("茅之台", "user.name").list(0, 20);
		System.out.println(list.size());
		list.forEach(p -> System.out.println(p.getStreet()));
	}

	@Test
	public void testWildcardMatch() {
		//这种查找，对于有一个分词匹配不上，就获取不到结果
		List<User> list = userDao.getQuerySearchObject()
				.wildcardMatch("茅*台", "name").list(0, 20);
		System.out.println(list.size());
		list.forEach(p -> System.out.println(p.getName()));
	}

	@Test
	public void testEnum() {
		List<Company> list = companyDao.getQuerySearchObject().match("菜鸟", "name")
				// 类型不匹配
//				.match("LIMITED STOCK", "companyType")
				.match(CompanyType.LIMITED, "companyType")
				.match(Arrays.asList(CompanyType.LIMITED, CompanyType.STOCK), "companyType")
				.list();
		System.out.println(list.size());
		list.forEach(p -> System.out.println(p.getName()));
	}

	/**
	 * 查询指定字段
	 */
	@Test
	public void testMatchCollection() {
		List<User> list = userDao.getQuerySearchObject().match(Arrays.asList("JHON", "茅台"), "name").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	@Test
	public void testMatchArray() {
		List<User> list = userDao.getQuerySearchObject().match(new String[]{"JHON", "茅台"}, "name").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	@Test
	public void testRange1() {
		List<User> list = userDao.getQuerySearchObject().range(10, 20, "age").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	@Test
	public void testAbove() {
		List<User> list = userDao.getQuerySearchObject().above(20, "age").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	@Test
	public void testBelow() {
		List<User> list = userDao.getQuerySearchObject()
				.below(LocalDate.of(2000, 1, 1), "birthday").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getName()));
	}

	@Test
	public void testBigDecimal() {
		List<User> list5 = userDao.getQuerySearchObject()
				.range(BigDecimal.valueOf(100), BigDecimal.valueOf(10000), "property")
				.list(0, 20);
		System.out.println(list5.size());
		list5.forEach(u -> System.out.println(u.getName()));
	}

	@Test
	public void testSort1() {
		List<User> list5 = userDao.getQuerySearchObject().sort("age")
				.sort("property", true).list(0, 20);
		System.out.println(list5.size());
		list5.forEach(u -> System.out.println(u.getName() + "\t" + u.getProperty() + "\t" + u.getAge()));
	}

	@Test
	public void testSort3() {
		List<User> list5 = userDao.getQuerySearchObject()
				.sort("birthday", true)
				.sort("property", false)
				.list(0, 20);
		System.out.println(list5.size());
		list5.forEach(u -> System.out.println(u.getName() + "\t" + u.getProperty() + "\t" + u.getAge()));
	}

	@Test
	public void testSortId() {
		List<User> list5 = userDao.getQuerySearchObject()
				.sort("id2", true).list(0, 20);
		System.out.println(list5.size());
		list5.forEach(u -> System.out.println(u.getId() + "\t" + u.getName() + "\t" + u.getProperty() + "\t" + u.getAge()));
	}

	@Test
	public void testJoin() {
		List<Address> list = addressDao.getQuerySearchObject().join("user").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getId() + "\t" + e.getStreet() + "\tUser: " +
				(e.getUser() != null ? e.getUser().getName() : null)));
	}

	@Test
	public void testJoin1() {
		List<User> list = userDao.getQuerySearchObject().join("addresses.user").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getId() + "\t" + e.getName() +
				"\tAddresses: " + (e.getAddresses() != null ? e.getAddresses().stream()
					.map(a -> a.getStreet() + "\tUser:" + (a.getUser() != null ? a.getUser().getName() : null))
						.collect(Collectors.joining(", ")) : null)));
	}

	@Test
	public void testJoin2() {
		List<User> list = userDao.getQuerySearchObject().join("addresses", "companyAddresses").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getId() + "\t" + e.getName() +
				"\tAddresses: " + (e.getAddresses() != null ? e.getAddresses().stream()
					.map(Address::getStreet).collect(Collectors.joining(", ")) : null)));
	}

	@Test
	public void testJoin3() {
		List<Address> list = addressDao.getQuerySearchObject().join("user.addresses", "user.companyAddresses").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getId() + "\t" + e.getStreet()));
	}

	@Test
	public void testJoin4() {
		List<Address> list = addressDao.getQuerySearchObject()
				.join("user.addresses.user", "user.companyAddresses.user").list();
		System.out.println(list.size());
		list.forEach(e -> System.out.println(e.getId() + "\t" + e.getStreet()));
	}

	@Test
	public void testLocalDateTime(){
		List<User> list = userDao.getQuerySearchObject()
				.above(LocalDateTime.of(2019, 1, 1, 0, 0), "dateTime").list();
		System.out.println(list.size());
		list.forEach(u -> System.out.println(String.format("id: %d, name: %s, age: %d, property: %s",
				u.getId(), u.getName(), u.getAge(), u.getProperty())));
	}

	/**
	 * 根据蹗查询
	 */
	@Test
	public void testDistance() {
		List<User> list = userDao.getQuerySearchObject()
				.distance(30, 114.06667, 22.61667, "location")
				.list();
		System.out.println(list.size());
		list.forEach(u -> System.out.println(String.format("id: %d, name: %s, age: %d, info: %s",
				u.getId(), u.getName(), u.getAge(), u.getInfo())));
	}

	@Test
	public void testSortByDistance() {
		List<User> list = userDao.getQuerySearchObject()
				.distance(30, 114.06667, 22.61667, "location")
				.sortDistance("location", true)
				.list();
		System.out.println(list.size());
		list.forEach(u -> System.out.println(String.format("id: %d, name: %s, age: %d, info: %s",
				u.getId(), u.getName(), u.getAge(), u.getInfo())));
	}

	@Test
	public void testSortByDistance2() {
		List<User> list = userDao.getQuerySearchObject()
				.distance(30, 114.06667, 22.61667, "location")
				.sortDistance("location", true)
				.sort("property", true)
				.list();
		System.out.println(list.size());
		list.forEach(u -> System.out.println(String.format("id: %d, name: %s, age: %d, property: %s",
				u.getId(), u.getName(), u.getAge(), u.getProperty())));
	}

	@Test
	public void testSortByDistance3() {
		List<User> list = userDao.getQuerySearchObject()
				.sortDistance("location", 114.06667, 22.61667)
				.sort("property", false)
				.list();
		System.out.println(list.size());
		list.forEach(u -> System.out.println(String.format("id: %d, name: %s, age: %d, property: %s",
				u.getId(), u.getName(), u.getAge(), u.getProperty())));
	}

	// 以下还没有测好

	/**
	 * 附近, 获取附近30km以内的user, 并显示距离
	 */
	@Test
	public void test11() {
		// 																																对于distance中已传了经纬度的，list中可以不传
		List<Object[]> list = userDao.getQuerySearchObject()
				.distance(30, 114.06667, 22.61667)
				.listWithDistance("location", /*114.06667, 22.61667, */0, 20);
		System.out.println(list.size());
		list.forEach(d -> {
			System.out.print(String.format("距离：%.3f", d[0]) + "\t\t");
			if(d[1] instanceof User) {
				User u = (User)d[1];
				System.out.println(String.format("id: %d, name: %s, age: %d, info: %s",
						u.getId(), u.getName(), u.getAge(), u.getInfo()));
			}
		});
	}

	/**
	 * 附近, 获取附近30km以内的user, 并显示距离，按距离从近到远排序
	 */
	@Test
	public void test12() {
		// 																								location为获取Coordinates，具体见User类，排序要加@SortableField注解
		List<Object[]> list = userDao.getQuerySearchObject().distance(30, 114.06667, 22.61667).sortDistance("location").listWithDistance(0, 20);
		System.out.println(list.size());
		list.forEach(d -> {
			System.out.print(String.format("距离：%.3f", d[0]) + "\t\t");
			if(d[1] instanceof User) {
				User u = (User)d[1];
				System.out.println(String.format("id: %d, name: %s, age: %d, info: %s", 
						u.getId(), u.getName(), u.getAge(), u.getInfo()));
			}
		});
	}



	
}
