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

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class TestQueryObject {
	
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
		System.out.println(user.getId() + " " + user.getName());
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

	@Test
	public void testMap() {
		Product product = productDao.getQueryObject().innerJoinFetch("productArguments").andEqual("id", 1L).getOne();
		System.out.println(product);

		Product product1 = productDao.getQueryObject().innerJoinFetch("productArguments")
				.andEqual("id", 1L)
				// 目前无法用key查询
				.andEqual("productArguments.name", 1.0)
				// 对于对象中的值，可以用这种方式获取
//				.andEqual("productArguments.minValue", 1.0)
				.getOne();
		System.out.println(product1);

		if(product != null) {
			System.out.println(product.getProductArguments());
//			product.getProductArguments().put("stopTick", new ProductArguments(1, 100, 2));
//			System.out.println(product.getProductArguments());
		}

	}
	
	@Test
	public void testEqual() {
		User u = userDao.get(1L);

		System.out.println(u == null ? null : u.getJobs() + " " + u.getInterestingNumbers());

//		Set<String> jobs = new HashSet<>(Arrays.asList("",""));
//		List<String> jobs =Arrays.asList("AA","BB");
//		u.setJobs(jobs);
		
//		User u1 = new User();
//		u1.setJobs(jobs);
//		System.out.println("u1: " + u1);
//		userDao.save(u1);
		
//		User user = userDao.getQueryObject().andEqual("name", "125").getOne();
//		System.out.println(user);
		
		// 这种无法查数据
//		List<User> list = userDao.getQueryObject().andIn("jobs", jobs).list();
//		System.out.println(list.size());
		
		
/*		List<User> users = userDao.getQueryObject().andEqual("name", "aaaa").list();
		System.out.println(users.size());*/
		
/*		List<Company> cs = companyDao.getQueryObject()
//				.innerJoin("user")
				.andEqual("user.name", "aaaa").list();
		System.out.println(cs.size());*/
		
//		Address address = addressDao.getQueryObject()./*innerJoin("user").*/andEqual("user", new User(1L)).getOne();
//		System.out.println(address);
		
//		Address address = addressDao.getQueryObject()./*innerJoin("user.addresses").*/andEqual("user.addresses.id", 1).getOne();
		
//		Company company = companyDao.getQueryObject()./*innerJoin("user.addresses").*/andEqual("address.user.id", 1).getOne();
		
	}
	
	@Test
	public void testGe() {
		List<User> users = userDao.getQueryObject().andGe("age", 88).list();
		System.out.println(users.size());
		
//		List<Company> cs = companyDao.getQueryObject().andGe("user.age", 88).list();
//		System.out.println(cs.size());
	}
	
	@Test
	public void testGt() {
		List<User> users = userDao.getQueryObject().andGt("age", 88).list();
		System.out.println(users.size());

		List<Company> cs = companyDao.getQueryObject().andGt("user.age", 88).list();
		System.out.println(cs.size());
	}
	
	@Test
	public void testLe() {
		List<User> users = userDao.getQueryObject().andLe("age", 88).list();
		System.out.println(users.size());
		
		List<Company> cs = companyDao.getQueryObject().andLe("user.age", 88).list();
		System.out.println(cs.size());
		
		users = userDao.getQueryObject().andLe("age", 88).andLe("birthday", LocalDate.of(1980, 1, 1)).list();
		System.out.println(users.size());
		
	}
	
	@Test
	public void testLt() {
		List<User> users = userDao.getQueryObject().andLt("age", 88).list();
		System.out.println(users.size());
		
		List<Company> cs = companyDao.getQueryObject().andLt("user.age", 88).list();
		System.out.println(cs.size());
	}
	
	@Test
	public void testBetwwen() {
		List<User> users = userDao.getQueryObject().andBetween("age", 10, 70).list();
		System.out.println(users.size());
		
		List<User> users1 = userDao.getQueryObject().andBetween("name", "aa", "abcde").list();
		System.out.println(users1.size());
		
		List<Company> cs = companyDao.getQueryObject().orBetween("user.age", 20, 25).list();
		System.out.println(cs.size());
		
		// 对于这种，语句为
//		select
//	        company0_.id as id1_3_,
//	        company0_.address_id as address_3_3_,
//	        company0_.name as name2_3_,
//	        company0_.`user_id` as user_id4_3_ 
//	    from
//	        Company company0_ 
//	    where
//	        company0_.`user_id` between ? and ? 
//	        or 0=1
		List<Company> cs1 = companyDao.getQueryObject().andBetween("user", new User(1L), new User(6L)).list();
		System.out.println(cs1.size());
	}
	
	@Test
	public void testOrEqual() {
		List<User> users = userDao.getQueryObject().orEqual("age", 88).orEqual("name", "ABCD").list();
		System.out.println(users.size());

		List<Company> cs = companyDao.getQueryObject().orEqual("user.age", 88).list();
		System.out.println(cs.size());
	}
	
	@Test
	public void testLike() {
//		List<User> users = userDao.getQueryObject().andLe("age", 88).andLike("name", "ABCD").list();
//		System.out.println(users.size());
		
//		users = userDao.getQueryObject().andLe("age", 88).orLike("name", "ABCD").orLike("info", "abcd").list();
//		System.out.println(users.size());

//		List<Company> cs = companyDao.getQueryObject().orLike("user.name", "ABCD").list();
//		System.out.println(cs.size());
		
		List<User> users = userDao.getQueryObject().andLe("age", 88).andLike("name", "ABCD", "BCD").list();
		System.out.println(users.size());
		
		users = userDao.getQueryObject().andLe("age", 88).orLike("name", "ABCD", "BCD").list();
		System.out.println(users.size());
		
	}
	
	@Test
	public void testIsNullAndIsNotNull() {
		List<User> users = userDao.getQueryObject().isNull("info").list();
		System.out.println(users.size());
		
		users = userDao.getQueryObject().isNotNull("info").list();
		System.out.println(users.size());
	}
	
	@Test
	public void testIsTrueAndIsFalse() {
		List<User> users = userDao.getQueryObject().isTrue("status").list();
		System.out.println(users.size());
		
		users = userDao.getQueryObject().isFalse("status").list();
		System.out.println(users.size());
	}
	
	@Test
	public void testOrder() {
		List<User> users = userDao.getQueryObject().orderAsc("age").list();
		users.forEach(System.out::println);
		
		System.out.println();
		
		users = userDao.getQueryObject().orderDesc("age").list();
		users.forEach(System.out::println);
		
		List<Company> list = companyDao.getQueryObject().orderAsc("address.street").list();
		System.out.println(list.size());
		
		List<Company> list1 = companyDao.getQueryObject().orderDesc("address.user.age").list();
		System.out.println(list1.size());
	}
	
	@Test
	public void testGroup() {
		List<User> users = userDao.getQueryObject().groupBy("name").list();
		users.forEach(System.out::println);
		
		List<Company> list1 = companyDao.getQueryObject().groupBy("address.user.age").list();
		System.out.println(list1.size());
		
/*		List<List<CompanyAddress>> list2 = companyDao.getQueryObject().list("addresses");
		System.out.println(list2.size());*/
		
		// 这种List再导航的获取不到
/*		List<Address> list3 = companyDao.getQueryObject().list("addresses.address");
		System.out.println(list3.size());*/
		
//		List<Company> list2 = companyDao.getQueryObject()./*innerJoin("addresses").*/andEqual("address.street", "aaa").list();
		List<Company> list2 = companyDao.getQueryObject()
															 .innerJoinFetch("addresses")
//															 .innerJoin("addresses.address") 
//															 .innerJoin("addresses") 
//															 .innerJoin("addresses.address.user") 
//															 .innerJoin("address")
//															 .innerJoin("address.user")
//															 .leftJoin("address.user")
//															 .innerJoinFetch("address.user")
//															 .innerJoinFetch("address.user")
//															 .innerJoinFetch("addresses")
//															 .innerJoin("addresses.address")
//															 .andEqual("address.street", "aaa")
															 .groupBy("addresses.address.street")
//															 .list();
															 .list();
//		List<Company> list2 = companyDao.getQueryObject()/*.innerJoin("addresses.address.user")*//*.andEqual("addresses.address.street", "aaa")*/.list();
	}

	/**
	 * 测试关联表，但没有独立表的
	 */
	@Test
	public void testElementCollection() {

		List<User> users = userDao.getQuerySearchObject().match(Arrays.asList(Sex.FEMALE, Sex.MALE), "sexes").list();
		System.out.println(users.size());

		/**
		 *  select
		 *         user0_.id as id1_0_,
		 *         user0_.age as age2_0_,
		 *         user0_.birthday as birthday3_0_,
		 *         user0_.dateTime as dateTime4_0_,
		 *         user0_.info as info5_0_,
		 *         user0_.latitude as latitude6_0_,
		 *         user0_.longitude as longitud7_0_,
		 *         user0_.name as name8_0_,
		 *         user0_.property as property9_0_,
		 *         user0_.sex as sex10_0_,
		 *         user0_.status as status11_0_,
		 *         user0_.`time` as time12_0_,
		 *         user0_.version as version13_0_
		 *     from
		 *         `User` user0_ cross
		 *     join
		 *         user_sexes sexes1_
		 *     where
		 *         user0_.id=sexes1_.user_id
		 *         and (
		 *             .=(
		 *                 ? , ?
		 *             )
		 *             or 0=1
		 *         )
		 */
		// 如果是这种关联表的，要加上join，否则如上面一样，无法查询
		List<User> sexes = userDao.getQueryObject().leftJoinFetch("sexes")
				.andIn("sexes", Arrays.asList(Sex.FEMALE, Sex.MALE)).list();
		System.out.println(sexes == null ? 0 : sexes.size());
	}

	@Test
	public void testInnerJoin() {

		// ************************** 报错 *************************
		// 下面的语句无论哪种形式都会报错
		// Caused by: org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags:
		// [entity.User.companies, entity.Company.addresses]
		// 不能多次获取集合，如果把adresses改为address则没事
		String hql = "select u from User u left join fetch u.companies c left join fetch c.addresses where u.id = ?0";
		List<User> users = userDao.findByHQL(hql, 1L);
		System.out.println(users.size());

		User user = userDao.getQueryObject().leftJoinFetch("companies.addresses").andEqual("id", 1).getOne();
		System.out.println(user);
		// **************************************

		// 不加fetch的，对于address中的user，需要再发一次sql
		List<Address> list = addressDao.getQueryObject()./*innerJoin("user").*/andEqual("user", new User(5L)).list();
		list.forEach(a -> System.out.println(a.getUser()));
		
		System.out.println();
		
		// 加了fetch的，不需要二次发sql
/*		list = addressDao.getQueryObject().innerJoinFetch("user").andEqual("user", new User(5L)).list();
		list.forEach(a -> System.out.println(a.getUser()));*/
		
		List<Company> list1 = companyDao.getQueryObject().innerJoin("user").groupBy("address.user.age").list();
		System.out.println(list1.size());
		
		List<Company> list2 = companyDao.getQueryObject().innerJoinFetch("address.user")
														     .groupBy("address.user.age")
															 .list();
		System.out.println(list2.size());
	}

	/**
	 * 获取多个集合的错误：Caused by: org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags
	 */
	@Test
	public void testInnerJoin1() {
		// ************************** 报错 *************************
		// 下面的语句无论queryObject还是hql都会报错
		// Caused by: org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags:
		// [entity.User.companies, entity.Company.addresses]
		// 不能多次获取集合，如果把adresses改为address则没事
		// 给addresses加上@OrderColumn可以好，不过会给数据表加一列
		// alter table `company_address` add column `addresses_order` integer
		// alter table `company_address` drop `company_id,address_id`;
		// 也可以指定表中的主键或关联表的另一个字段，address_id，如：name = "id"，则不会再生成多余的字段了，
		//如使用已存在的字段company_id，则会报生成重复字段的错
		List<User> users = userDao.getQueryObject().leftJoinFetch("companies.addresses")
				.andEqual("id", 1L).list();
	}

	@Test
	public void testFindByHsql() {
		String hql = "select u from User u left join fetch u.companies c left join fetch c.addresses where u.id = ?1";
		List<User> users = userDao.findByHQL(hql, 1L);
		System.out.println(users.size());
	}

	@Test
	public void testLeftJoin() {
		List<User> users = userDao.getQueryObject().leftJoin("addresses").distinct().andIn("id", new Long[]{1L, 2L}).list();

		String hql = "select distinct u from User u left join fetch u.addresses where u.id in (1, 2)";
		List<User> users2 = userDao.findByHQL(hql);

		User user = userDao.getQueryObject().leftJoinFetch("addresses").andEqual("id", 1).getOne();

		// 不加fetch的，对于address中的user，需要再发一次sql
		List<Address> list = addressDao.getQueryObject().leftJoin("user").andEqual("id", 5L).list();
		System.out.println(list.size());
		// 对于user为null，用manytoone时，直接返回null, 若为oneToMany，则需要查一次才知道有没有数据
		list.forEach(a -> System.out.println(a.getUser()));
		
		list = addressDao.getQueryObject().leftJoin("user").andEqual("id", 1L).list();
		System.out.println(list.size());
		// 多发一条sql
		list.forEach(a -> System.out.println(a.getUser()));
	}
	
	@Test
	public void testLeftJoinFetch() {
		// 不加fetch的，对于address中的user，需要再发一次sql
		List<Address> list = addressDao.getQueryObject().leftJoinFetch("user").andEqual("id", 5L).list();
		System.out.println(list.size());
		// 对于user为null，用manytoone时，直接返回null, 若为oneToMany，则需要查一次才知道有没有数据
		list.forEach(a -> System.out.println(a.getUser()));
		
		list = addressDao.getQueryObject().leftJoinFetch("user").andEqual("id", 1L).list();
		System.out.println(list.size());
		// 不会再触发sql
		list.forEach(a -> System.out.println(a.getUser()));
	}
	
	@Test
	public void testIn() {
		// and in
//		List<Company> list1 = companyDao.getQueryObject().andIn("address.user.age", Arrays.asList(8,2, 10)).list();
//		System.out.println(list1.size());

		// and not in
//		List<Company> list2 = companyDao.getQueryObject().andNotIn("address.user.age", Arrays.asList(8,2, 10)).list();
//		System.out.println(list2.size());

		// or in
//		List<Company> list3 = companyDao.getQueryObject().orIn("address.user.age", Arrays.asList(8,2, 10)).list();
//		System.out.println(list3.size());

//		List<User> list = userDao.getQueryObject().andIn("sex", new Sex[]{Sex.FEMALE, Sex.MALE}).list();
//		System.out.println("获取到用户数量：" + list.size());

		List<User> list4 = getUsers(Sex.FEMALE, Sex.MALE);
		System.out.println("获取到用户数量：" + list4.size());

	}

	private List<User> getUsers(Sex... sexes) {
		return userDao.getQueryObject().andIn("sex", sexes).list();
	}

	/**
	 * distinct放条件前后都一样
	 */
	@Test
	public void testDistinct() {
		List<User> users = addressDao.getQueryObject().distinct().andEqual("city", "深圳").list("user");
		System.out.println(users.size());
		users.forEach(u -> System.out.println(u));
		
		users = addressDao.getQueryObject().andEqual("city", "深圳").distinct().list("user");
		System.out.println(users.size());
		users.forEach(u -> System.out.println(u));
	}
	
	@Test
	public void testList() {
		List<User> list = userDao.getQueryObject().list();
		System.out.println(list.size());
		
		list = userDao.getQueryObject().list(2, 3);
		System.out.println(list.size());
	}
	
	@Test
	public void testListField() {
		List<String> list1 = companyDao.getQueryObject().list("user.name");
		System.out.println(list1.size());
		System.out.println(list1);
		
/*		List<User> list = userDao.getQueryObject().list("name");
		System.out.println(list);
		
		List<String> list0 = userDao.getQueryObject().list("name", 2, 3);
		System.out.println(list0); */
	}
	
	@Test
	public void testListFields() {
		List<Object[]> list1 = companyDao.getQueryObject().list("user.name", "name", "user.age", "address.user.name");
		System.out.println(list1.size());
		list1.forEach(o -> System.out.println(Arrays.toString(o)));
	}
	
	@Test
	public void testGetOne() {
		User user = userDao.getQueryObject().andEqual("name", "125").getOne();
		System.out.println(user);
	}
	
	@Test
	public void testGetOneSingleValue() {
		Integer age = userDao.getQueryObject().andEqual("name", "125").getSingleValue("age");
		System.out.println(age);
	}
	
	@Test
	public void testCount() {
		Long num = userDao.getQueryObject().andEqual("name", "bbbb").count(false);
		System.out.println(num.longValue());
		
		num = userDao.getQueryObject().andEqual("name", "bbbb").count("name", false);
		System.out.println(num.longValue());
		
		num = userDao.getQueryObject().andEqual("name", "bbbb").count("name", true);
		System.out.println(num.longValue());
		
		num = companyDao.getQueryObject().count("address.user.name", false);
		System.out.println(num.intValue());
	}
	
	@Test
	public void testMaxAndMin() {
		String max = userDao.getQueryObject().max("name");
		System.out.println(max);
		
		String min = userDao.getQueryObject().min("name");
		System.out.println(min);
		
		int maxAge = userDao.getQueryObject().max("age");
		System.out.println(maxAge);
		
		int minAge = userDao.getQueryObject().min("age");
		System.out.println(minAge);
	}
	
	@Test
	public void testSum() {
		Number total = userDao.getQueryObject().sum("age");
		System.out.println(total.intValue());
		
		Number property  = userDao.getQueryObject().sum("property");
		System.out.println(property);
		
		Number num = companyDao.getQueryObject().sum("address.user.age");
		System.out.println(num != null ? num.intValue() : null);
	}
	
	@Test
	public void testLock() throws InterruptedException {
		// 上锁 加 for update
/*		LockOptions lock = LockOptions.UPGRADE;
		Number total = userDao.getQueryObject().lock(lock).sum("age");
		System.out.println(total.intValue());*/
		
		// 这个是乐观锁，会使版本+1（强制更新版本），但如果是聚合函数，则不会
		User user = userDao.getQueryObject().lock(LockModeType.WRITE).andEqual("id", 5L).getOne();
		System.out.println(user);
		
		// 悲观锁，查询时会加上for update
		User user1 = userDao.getQueryObject().lock(LockModeType.PESSIMISTIC_WRITE).andEqual("id", 5L).getOne();
		System.out.println(user1);
	}
	
}
