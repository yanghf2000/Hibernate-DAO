package test;

import dao.AddressDao;
import dao.CompanyDao;
import dao.ProductDao;
import dao.UserDao;
import entity.*;
import org.apache.lucene.search.SortField;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.SessionFactoryUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class TestQuerySearchObject {
	
	private SessionFactory sf;
	
	private UserDao userDao = new UserDao();
	private AddressDao addressDao = new AddressDao();
	private CompanyDao companyDao = new CompanyDao();
	private ProductDao productDao = new ProductDao();
	
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
	 * 较为精确的匹配
	 */
	@Test
	public void testSentence() {
		//这种查找，对于有一个分词匹配不上，就获取不到结果
		List<Product> list = productDao.getQuerySearchObject().sentence("subtitle", "坏不了的手机").list(0, 20);
		System.out.println(list.size());
		list.forEach(p -> System.out.println(String.format("id: %d, name: %s, subtitle: %s", p.getId(), p.getName(), p.getSubtitle())));
		
		// 只有查找的分词都匹配上了才会有结果
		List<Product> list1 = productDao.getQuerySearchObject().sentence("subtitle", "手机").list(0, 20);
		System.out.println(list1.size());
		list1.forEach(p -> System.out.println(String.format("id: %d, name: %s, subtitle: %s", p.getId(), p.getName(), p.getSubtitle())));
	}
	
	@Test
	public void testEnum() {
		List<Company> list = companyDao.getQuerySearchObject().match("abc", "name")
								.match(new Object[] {CompanyType.LIMITED, CompanyType.STOCK}, "companyType")
								.list();
		System.out.println(list.size());
	}

	@Test
	public void testSearchCollections() {
//		List<User> users = userDao.getQuerySearchObject().match("FEMALE MALE", "sexes").list();
//		System.out.println(users.size());

		List<User> users = userDao.getQuerySearchObject().match(Arrays.asList(Sex.FEMALE, Sex.MALE), "sexes").list();
		System.out.println(users.size());

//		List<User> users = userDao.getQuerySearchObject().match("FEMALE MALE", "sexes").list();
//		System.out.println(users.size());
//
//		users = userDao.getQuerySearchObject().match("FEMALE", "sexes").list();
//		System.out.println(users.size());
//		// 这样也可以搜到
//		users = userDao.getQuerySearchObject().match("male,female", "sexes").list();
//		System.out.println(users.size());
	}
	
	@Test
	public void testCascade() {
		List<Product> list = productDao.getQuerySearchObject().sentence("company", new Company(1L)).list();
		System.out.println(list.size());
		
		List<Product> list5 = productDao.getQuerySearchObject().match(1, "company.id").list();
		System.out.println(list5.size());
		
		List<Product> list3 = productDao.getQuerySearchObject().match("abc", "company.name").list();
		System.out.println(list3.size());
		
		List<Product> list4 = productDao.getQuerySearchObject().match(1, "company.user.id").list();
		System.out.println(list4.size());
		
		// 用sentence这样执行失败
//		List<Product> list1 = productDao.getQuerySearchObject().sentence("company.name", "aaa").list();
//		System.out.println(list1.size());
		
		// 用sentence这样执行失败
//		List<Product> list2 = productDao.getQuerySearchObject().sentence("company.user", new User(1L)).list();
//		System.out.println(list2.size());
	}
	
	/**
	 * 匹配
	 */
	@Test
	public void testMatch() {
		// 使用match，只要有一个词匹配上就算查到的结果
/*		List<Product> list = productDao.getQuerySearchObject().match("坏不了的手机", "subtitle").list(0, 20);
		System.out.println(list.size());
		list.forEach(p -> System.out.println(String.format("id: %d, name: %s, subtitle: %s", p.getId(), p.getName(), p.getSubtitle())));
		
		List<Product> list1 = productDao.getQuerySearchObject().match("手机", "subtitle").list(0, 20);
		System.out.println(list1.size());
		list1.forEach(p -> System.out.println(String.format("id: %d, name: %s, subtitle: %s", p.getId(), p.getName(), p.getSubtitle())));
		
		// 用user.id查不到结果
		List<Address> list2 = addressDao.getQuerySearchObject().match("是", "street").sentence("user.name", "工有").sentence("user.id", 10).list(0, 20);
		System.out.println(list2.size());
		list2.forEach(a -> System.out.println(String.format("id: %d, street: %s, userId: %d, username: %s", a.getId(), a.getStreet(), a.getUser().getId(), a.getUser().getName())));
*/
		
		List<User> list = userDao.getQuerySearchObject().match("茅台", "name").list();
		System.out.println(list.size());
		if(list.size() > 0){
			System.out.println(list.get(0).getName());
		}
	}
	
	/**
	 * 查找加条件
	 */
	@Test
	public void test3() {
/*		// 使用match，只要有一个词匹配上就算查到的结果
		List<Product> list = productDao.getQuerySearchObject().match("手机", "name", "subtitle").list(0, 20);
		System.out.println(list.size());
		list.forEach(p -> System.out.println(String.format("id: %d, name: %s, subtitle: %s", p.getId(), p.getName(), p.getSubtitle())));
		
		// 加上小米分类
		List<Product> list1 = productDao.getQuerySearchObject().match("手机", "name", "subtitle").sentence("categories.category.name", "小米").list(0, 20);
		System.out.println(list1.size());
		list1.forEach(p -> System.out.println(String.format("id: %d, name: %s, subtitle: %s", p.getId(), p.getName(), p.getSubtitle())));*/
		
		// 加上价格范围
		List<Product> list2 = productDao.getQuerySearchObject().match("手机", "name", "subtitle").sentence("categories.category.name", "小米")
																.range("price", 1000, 8000, double.class).list(0, 20);
		System.out.println(list2.size());
		list2.forEach(p -> System.out.println(String.format("id: %d, name: %s, subtitle: %s, price: %s", p.getId(), p.getName(), p.getSubtitle(), p.getPrice() + "")));
		
		List<Product> list3 = productDao.getQuerySearchObject().match("手机", "name", "subtitle").sentence("categories.category.name", "小米")
				.above("price", 2000, double.class).list(0, 20);
		System.out.println(list3.size());
		list3.forEach(p -> System.out.println(String.format("id: %d, name: %s, subtitle: %s, price: %s", p.getId(), p.getName(), p.getSubtitle(), p.getPrice() + "")));
		
		List<Product> list4 = productDao.getQuerySearchObject().match("手机", "name", "subtitle").sentence("categories.category.name", "小米")
				.below("price", 5000, double.class).list(0, 20);
		System.out.println(list4.size());
		list4.forEach(p -> System.out.println(String.format("id: %d, name: %s, subtitle: %s, price: %s", p.getId(), p.getName(), p.getSubtitle(), p.getPrice() + "")));

	}
	
	/**
	 * 范围
	 */
	@Test
	public void testRange() {
		// 使用match，只要有一个词匹配上就算查到的结果
/*		List<User> list = userDao.getQuerySearchObject().range("age", 10, 88, int.class).sort("age", SortField.Type.INT).list(0, 20);
		System.out.println(list.size());
		list.forEach(u -> System.out.println(String.format("id: %d, name: %s, age: %d, info: %s", u.getId(), u.getName(), u.getAge(), u.getInfo())));
		
		// 倒序
		List<User> list1 = userDao.getQuerySearchObject().range("age", 10, 88, int.class).sort("age", SortField.Type.INT, true).list(0, 20);
		System.out.println(list1.size());
		list1.forEach(u -> System.out.println(String.format("id: %d, name: %s, age: %d, info: %s", u.getId(), u.getName(), u.getAge(), u.getInfo())));
		
		// above
		List<User> list2 = userDao.getQuerySearchObject().above("age", 10, int.class).list(0, 20);
		System.out.println(list2.size());
		list2.forEach(u -> System.out.println(String.format("id: %d, name: %s, age: %d, info: %s", u.getId(), u.getName(), u.getAge(), u.getInfo())));
		
		// below
		List<User> list3 = userDao.getQuerySearchObject().below("age", 88, int.class).list(0, 20);
		System.out.println(list3.size());
		list3.forEach(u -> System.out.println(String.format("id: %d, name: %s, age: %d, info: %s", u.getId(), u.getName(), u.getAge(), u.getInfo())));*/
		
		// above&below
/*		List<User> list4 = userDao.getQuerySearchObject().above("age", 10, int.class).below("age", 88, int.class).list(0, 20);
		System.out.println(list4.size());
		list4.forEach(u -> System.out.println(String.format("id: %d, name: %s, age: %d, info: %s, birthday: " + u.getBirthday(), 
				u.getId(), u.getName(), u.getAge(), u.getInfo())));*/
		
		// above&below
		List<User> list5 = userDao.getQuerySearchObject().above("age", 10, int.class).below("age", 88, int.class)
//												.range("birthday", "1955-5-5", "1999-9-9", LocalDate.class).list(0, 20);
												.range("birthday", LocalDate.of(1955, 5, 5), LocalDate.of(1999, 9, 9), LocalDate.class)
												.list(0, 20);
		System.out.println(list5.size());
		list5.forEach(u -> System.out.println(String.format("id: %d, name: %s, age: %d, info: %s, birthday: " + u.getBirthday(), 
												u.getId(), u.getName(), u.getAge(), u.getInfo())));
		
		// above&below
/*		List<User> list6 = userDao.getQuerySearchObject()
											   .range("name", "aa", "ad", String.class)		
											   .above("age", 10, int.class).below("age", 88, int.class).above("name", "ab", String.class)
											   .list(0, 20);
		System.out.println(list6.size());
		list6.forEach(u -> System.out.println(String.format("id: %d, name: %s, age: %d, info: %s, birthday: " + u.getBirthday(), 
				u.getId(), u.getName(), u.getAge(), u.getInfo())));*/
	}
	
	@Test
	public void testRangeBigDecimal() {
		List<User> list5 = userDao.getQuerySearchObject()
												// 以下两种都能获取到，BigDecimalNumericFieldBridge把BigDecimal转换成了double, 所以要用double获取，
												// 如果转换成了long，且数乘了100（比如用于货币），则在搜索时也要用long类型，且值要乘以100
//												.range("property", 10.01, 100.99, double.class)
												.range("property", 100, 180, double.class)
												
												// 用long获取不到，要用double
												// 以下方法不能用，由于BigDecimal转换成了double，所以用BigDecimal long等，则获取不到值
//												.range("property", BigDecimal.valueOf(10), BigDecimal.valueOf(100), Long.class)
//												.range("property", 10, 100, BigDecimal.class)
												
												// 以下方法不能用，会报错 HSEARCH000238: Cannot create numeric range query for field 'property', since values are not numeric (Date, int, long, short or double)
//												.range("property", BigDecimal.valueOf(10), BigDecimal.valueOf(100), null)
//												.range("property", BigDecimal.valueOf(10), BigDecimal.valueOf(100), BigDecimal.class)
												.list(0, 20);
		System.out.println(list5.size());
		list5.forEach(u -> System.out.println(String.format("id: %d, name: %s, age: %d, info: %s, property: %f, birthday: " + u.getBirthday(), 
												u.getId(), u.getName(), u.getAge(), u.getInfo(), u.getProperty().doubleValue())));
	}
	
	/**
	 * 排序 
	 */
	@Test
	public void testSort() {
		// 使用match，只要有一个词匹配上就算查到的结果
//		id: 1, name: 苹果, subtitle: 苹果手机
//		id: 2, name: 手机, subtitle: 小米手机
//		id: 8, name: 仙女果, subtitle: 仙女吃的果子
/*		List<Product> list = productDao.getQuerySearchObject().match("苹果", "name").match("坏不了的手机", "subtitle").sort("name", false).list(0, 20);
		System.out.println(list.size()); 
		list.forEach(p -> System.out.println(String.format("id: %d, name: %s, subtitle: %s", p.getId(), p.getName(), p.getSubtitle())));*/
		
/*		List<Product> list2 = productDao.getQuerySearchObject().match("果", "name").match("坏不了的手机", "subtitle").sort("publishTime", false).list(0, 20);
		System.out.println(list2.size());
		list2.forEach(p -> System.out.println(String.format("id: %d, name: %s, subtitle: %s", p.getId(), p.getName(), p.getSubtitle())));*/
		
//		id: 3, name: 电脑, subtitle: 联想电脑, price: 23423.0
//		id: 1, name: 苹果, subtitle: 苹果手机, price: 23213.0
//		id: 8, name: 仙女果, subtitle: 仙女吃的果子, price: 5345.0
//		id: 10, name: 小米大米手机, subtitle: 红米note4, price: 4000.0
//		id: 2, name: 手机, subtitle: 小米手机, price: 3000.0
//		id: 9, name: 小米红米手机, subtitle: 红米note4, price: 1500.0
//		id: 7, name: 果子, subtitle: 陕西红富士, price: 243.36
//		id: 6, name: 核桃, subtitle: 新疆纸皮核桃, price: 243.35
//		id: 4, name: 香蕉, subtitle: 海南香蕉, price: 234.0
//		id: 5, name: 板栗, subtitle: 糖炒板栗, price: 34.0
		List<Product> list3 = productDao.getQuerySearchObject()/*.match("果", "name").match("坏不了的手机", "subtitle")*/
														.range("price", 10, 500, double.class)
														.sort("price", SortField.Type.DOUBLE, true)
														.list(0, 20);
		System.out.println(list3.size());
		list3.forEach(p -> System.out.println(String.format("id: %d, name: %s, subtitle: %s, price: " + p.getPrice(), p.getId(), p.getName(), p.getSubtitle())));
		
//		id: 8, name: 仙女果, subtitle: 仙女吃的果子
//		id: 2, name: 手机, subtitle: 小米手机
//		id: 1, name: 苹果, subtitle: 苹果手机
/*		List<Product> list1 = productDao.getQuerySearchObject().match("坏不了的手机", "subtitle").sort("name", false).list(0, 20);
		System.out.println(list1.size());
		list1.forEach(p -> System.out.println(String.format("id: %d, name: %s, subtitle: %s", p.getId(), p.getName(), p.getSubtitle())));*/
		
		// 按生日排序
/*		List<User> list4 = userDao.getQuerySearchObject().above("age", 10, int.class).below("age", 88, int.class).sort("birthday", true).list(0, 20);
		System.out.println(list4.size());
		list4.forEach(u -> System.out.println(String.format("id: %d, name: %s, age: %d, info: %s, birthday: " + u.getBirthday(), u.getId(), u.getName(), u.getAge(), u.getInfo())));
		*/
	}
	
	@Test
	public void testSort1(){
		List<User> list = userDao.getQuerySearchObject().sort("birthday", true)
					.sort("property", SortField.Type.DOUBLE).list();
		
		for(User u : list){
			System.out.println(u.getBirthday() + "\t" + u.getProperty());
		}
	}
	
	/**
	 * 连表
	 */
	@Test
	public void test6() {
		List<User> list = userDao.getQuerySearchObject().join("addresses").list();
		System.out.println(list.size());
		list.forEach(u -> System.out.println(String.format("id: %d, name: %s, subtitle: %s, address: %s", u.getId(), u.getName(), u.getInfo(), 
				(u.getAddresses().size() > 0 ? u.getAddresses().get(0).getStreet() : ""))));
	}
	
	/**
	 * 查询嵌套对象的值，在查询嵌套对象值时，会隐含对需要关联的表进行关联
	 */
	@Test
	public void test7() {
		List<User> list = userDao.getQuerySearchObject().match("什么", "addresses.street").list();
		System.out.println(list.size());
		list.forEach(u -> System.out.println(String.format("id: %d, name: %s, subtitle: %s, address: %s", u.getId(), u.getName(), u.getInfo(), 
				(u.getAddresses().size() > 0 ? u.getAddresses().get(0).getStreet() : ""))));
	}
	
	/**
	 * 查询嵌套对象的值，在查询嵌套对象值时，会对需要关联的表进行关联
	 * 对于这种一对多的，排序会报错
	 */
	@Test
	public void test8() {
		List<User> list = userDao.getQuerySearchObject().match("什么", "addresses.street")/*.sort("addresses.street")*/.list();
		System.out.println(list.size());
		list.forEach(u -> System.out.println(String.format("id: %d, name: %s, subtitle: %s, address: %s", u.getId(), u.getName(), u.getInfo(), 
				(u.getAddresses().size() > 0 ? u.getAddresses().get(0).getStreet() : ""))));
	}
	
	/**
	 * 查询嵌套对象的值，在查询嵌套对象值时，会对需要关联的表进行关联, 排序
	 */
	@Test
	public void testJoinAndSort() {
		List<Address> list = addressDao.getQuerySearchObject().match("什么", "street", "user.name").sort("user.name", true).list();
		System.out.println(list.size());
		list.forEach(a -> System.out.println(String.format("id: %d, street: %s, id: %d, name: %s", a.getId(), a.getStreet(), a.getUser().getId(), a.getUser().getName()))); 
		
		List<Address> list1 = addressDao.getQuerySearchObject()/*.match("什么", "street", "user.name")*/.sentence("user.id", 3)/*.sort("user.name", true)*/.list();
		System.out.println(list1.size());
		list1.forEach(a -> System.out.println(String.format("id: %d, street: %s, id: %d, name: %s", a.getId(), a.getStreet(), a.getUser().getId(), a.getUser().getName()))); 
	}
	
	/**
	 * 附近, 获取附近30km以内的user
	 */
	@Test
	public void test10() {
		List<User> list = userDao.getQuerySearchObject().distance(30, 114.06667, 22.61667).list(0, 20);
		System.out.println(list.size());
		list.forEach(u -> System.out.println(String.format("id: %d, name: %s, age: %d, info: %s", 
								u.getId(), u.getName(), u.getAge(), u.getInfo())));
	}
	
	/**
	 * 附近, 获取附近30km以内的user, 并显示距离
	 */
	@Test
	public void test11() {
		// 																																对于distance中已传了经纬度的，list中可以不传
		List<Object[]> list = userDao.getQuerySearchObject().distance(30, 114.06667, 22.61667).listWithDistance("location", /*114.06667, 22.61667, */0, 20);
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
	
	/**
	 * 附近, 获取附近30km以内的user, 并显示距离，按距离从远到近排序（倒序）
	 */
	@Test
	public void test13() {
		// 																								location为获取Coordinates，具体见User类，排序要加@SortableField注解
		List<Object[]> list = userDao.getQuerySearchObject().distance(30, 114.06667, 22.61667).sortDistance("location", true).listWithDistance(0, 20);
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
	 * 获取所有用户的距离
	 */
	@Test
	public void test14() {
		// 使用match，只要有一个词匹配上就算查到的结果
		List<Object[]> list = userDao.getQuerySearchObject().listWithDistance("location", 114.06667, 22.61667, 0, 20);
		System.out.println(list.size());
//		list.forEach(u -> System.out.println(String.format("id: %d, name: %s, age: %d, info: %s", u.getId(), u.getName(), u.getAge(), u.getInfo())));
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
	 * 距离排序, 不加范围选项，在排序中传入经纬度
	 */
	@Test
	public void test15() {
		// 使用match，只要有一个词匹配上就算查到的结果
		List<Object[]> list = userDao.getQuerySearchObject().sortDistance("location", 114.06667, 22.61667).listWithDistance(0, 20);
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
	 * 测试用Id搜索的情况，用id可以搜到，不管是用sentence还是Match都可以，但如果用嵌套对象的id，如User.id，则搜不到。
	 */
	@Test
	public void test16() {
/*		List<Address> list = addressDao.getQuerySearchObject().sentence("user.name", "什么").list();
		System.out.println(list.size());
		list.forEach(u -> System.out.println(u));*/
		
		// 这个用match sentence都能查到，且是唯一
		List<Address> list2 = addressDao.getQuerySearchObject().match(5, "id").match("地址", "street").list();
		System.out.println(list2.size());
		list2.forEach(u -> System.out.println(u));
		
		// 这一种查不到
/*		List<Address> list1 = addressDao.getQuerySearchObject().sentence("user.id", 30).list();
		System.out.println(list1.size());
		list1.forEach(u -> System.out.println(u));*/
	}
	
/*	@Test
	public void testFacet() {
		List<Facet> list = productDao.getQuerySearchObject().list();
		System.out.println(list.size());
		list.forEach(f -> System.out.println(f));
	}*/
	
	@Test
	public void testLocalDate(){
		List<User> list5 = userDao.getQuerySearchObject().above("age", 10, int.class).below("age", 88, int.class)
//				.range("birthday", "1955-5-5", "1999-9-9", LocalDate.class).list(0, 20);
				.range("birthday", LocalDate.of(1998, 5, 5), LocalDate.of(1999, 9, 9), LocalDate.class)
				.list(0, 20);
		System.out.println(list5.size());
		
//		User user = userDao.getOne("birthday", LocalDate.of(1999, 9, 9));
//		System.out.println(user.getName() + "\t" + user.getBirthday());
		 
		List<User> list = userDao.getQuerySearchObject().sentence("birthday", 
				LocalDate.of(1998, 9, 9)).list();
//				LocalDate.of(1998, 9, 9).toString().replaceAll("-", "")).list();
		System.out.println(list.size());
		if(list.size() > 0){
			System.out.println(list.get(0).getBirthday());
		}
		
		List<User> list1 = userDao.getQuerySearchObject().match(LocalDate.of(1999, 9, 9), "birthday").list();
		System.out.println(list1.size());
		if(list1.size() > 0){
			System.out.println(list1.get(0).getBirthday());
		}
		
	}
	
	
	@Test
	public void testLocalTime(){
		List<User> list5 = userDao.getQuerySearchObject().above("age", 10, int.class).below("age", 88, int.class)
//				.range("birthday", "1955-5-5", "1999-9-9", LocalDate.class).list(0, 20);
				.range("birthday", LocalDate.of(1998, 5, 5), LocalDate.of(1999, 9, 9), LocalDate.class)
//				.sentence("time", LocalTime.of(12, 0, 1))
				.range("time", LocalTime.of(0, 0, 0), LocalTime.of(15, 0, 1))
				.list(0, 20);
		System.out.println(list5.size());
		
//		User user = userDao.getOne("birthday", LocalDate.of(1999, 9, 9));
//		System.out.println(user.getName() + "\t" + user.getBirthday());
		
		List<User> list = userDao.getQuerySearchObject().sentence("time", 
				LocalTime.of(12, 0, 0)).list();
		System.out.println(list.size());
		if(list.size() > 0){
			System.out.println(list.get(0).getBirthday());
		}
		
		List<User> list1 = userDao.getQuerySearchObject().match(LocalTime.of(12, 0, 0), "time").list();
		System.out.println(list1.size());
		if(list1.size() > 0){
			System.out.println(list1.get(0).getBirthday());
		}
		
	}
	
	
	@Test
	public void testLocalDateTime(){
		LocalDateTime dateTime = LocalDateTime.of(2018, 4, 30, 13, 18, 13);
		List<User> list5 = userDao.getQuerySearchObject().above("age", 10, int.class).below("age", 88, int.class)
//				.range("birthday", "1955-5-5", "1999-9-9", LocalDate.class).list(0, 20);
				.range("birthday", LocalDate.of(1998, 5, 5), LocalDate.of(1999, 9, 9), LocalDate.class)
				.range("dateTime", dateTime.plusSeconds(1), LocalDateTime.now())
				.list(0, 20);
		System.out.println(list5.size());
		
//		User user = userDao.getOne("birthday", LocalDate.of(1999, 9, 9));
//		System.out.println(user.getName() + "\t" + user.getBirthday());
		
		List<User> list = userDao.getQuerySearchObject()
				.sentence("birthday", LocalDate.of(1998, 9, 9))
				.sentence("dateTime", dateTime)
				.list();
//				LocalDate.of(1998, 9, 9).toString().replaceAll("-", "")).list();
		System.out.println(list.size());
		if(list.size() > 0){
			System.out.println(list.get(0).getBirthday());
		}
		
		List<User> list1 = userDao.getQuerySearchObject().match(LocalDate.of(1999, 9, 9), "birthday")
				.match(dateTime, "dateTime").list();
		System.out.println(list1.size());
		if(list1.size() > 0){
			System.out.println(list1.get(0).getBirthday());
		}
		
	}
	
	
}
