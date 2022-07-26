package test;

import com.github.yanghf2000.dao.FieldsAndValuesMap;
import dao.*;
import entity.Address;
import entity.Interesting;
import entity.SimpleEntity;
import entity.User;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.SessionFactoryUtils;

import java.util.*;

public class TestDao {
	
	private SessionFactory sf;
	
	private UserDao userDao = new UserDao();
	private AddressDao addressDao = new AddressDao();
	private ProductDao productDao = new ProductDao();
	private InterestingDao interestingDao = new InterestingDao();
	private SimpleEntityDao simpleEntityDao = new SimpleEntityDao();

	private List<Session> sessions = new ArrayList<>();
	
	private Session ss;
	private Transaction tx;
	
	private boolean commit = true;

	@Test
	public void prepare() throws InterruptedException {
		System.out.println(userDao);
	}

	@Test
	public void test() throws InterruptedException {
		User user = userDao.get(1L);
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
		
		for(Session s : sessions) {
			if(s != null && s.isOpen())
				s.close();
		}
	}
    
	@Test
    public void testFlush() {
		userDao.flush();
    }

	@Test
    public void clear() {
    	userDao.clear();
    }

	@Test
    public void refresh() {
    	User user = new User("bbb", 5);
    	user.setId(1362L);
    	System.out.println(user);
    	
    	userDao.refresh(user);
    	System.out.println(user);
    }
    
	@Test
    public void save() {
		User user = new User("叫什么名字", 5);
		user.setId(9999L); // 无效
		userDao.save(user);
		System.out.println(user);
    }

	/**
	 * 对于级联操作的，若是用单独的主键，则不需要的
	 */
	@Test
	public void testCascade() {
		User user = userDao.get(1L);
		System.out.println(user);
		System.out.println(user.getInterestings());
		
		Interesting it = new Interesting("抽烟");
		it.setId(1L);
		Interesting it1 = new Interesting("喝酒");
		it1.setId(2L);
		Interesting it2 = new Interesting("打麻将");
		it2.setId(3L);
//		interestingDao.batchSave(Arrays.asList(it, it1, it2));
		
		user.addInteresting(it);
		user.addInteresting(it1);
		user.addInteresting(it2);
//		user.addInteresting(interestingDao.get(1L));
//		user.addInteresting(interestingDao.get(2L));
		
		userDao.flush();
		
//		System.out.println(user.getInteresting());
		
		user.removeInteresting(user.getInterestings().get(1).getInteresting());
		
	}
	
	@Test
	public void merge() {
		User user = userDao.get(1L);
		userDao.merge(user);
		System.out.println(user);
		
		User user2 = new User("bbbb", 3);
		userDao.merge(user2);
		System.out.println(user2);
		
		user.setAge(8);
		User user3 = userDao.merge(user);
		System.out.println(user);
		System.out.println(user3);
		
    }

	@Test
	public void testMerge() {
		// 对于临时对象，merge后会被丢弃，session中不会包含，merge后会返回一个新对象，同时被session维护
		User user = new User("aaaa", 15);
		User copy = userDao.merge(user);
		
		//null 由于session维护新的对象，所以原来的对象中获取不到id
		System.out.println(user.getId());
		// 24 
		System.out.println(copy.getId());
		
		// 1338368149
		System.out.println(user.hashCode());
		// 1433208870
		System.out.println(copy.hashCode());
		
		// false
		System.out.println(userDao.getSession().contains(user));
		// true
		System.out.println(userDao.getSession().contains(copy));
		
	}
	
	@Test
	public void testMerge2() {
		// 对于持久化对象，查出来后再merge，新对象只是原持久化对象的复制，hash值都是一样的
		User user = userDao.get(24L);
		User copy = userDao.merge(user);
		
		// 24
		System.out.println(user.getId());
		// 24 
		System.out.println(copy.getId());
		
		// 1535026957
		System.out.println(user.hashCode());
		// 1535026957
		System.out.println(copy.hashCode());
		
		// true
		System.out.println(userDao.getSession().contains(user));
		// true
		System.out.println(userDao.getSession().contains(copy));
	}
	
	@Test
	public void testMerge3() {
		User user = userDao.get(24L);
		User copy = userDao.merge(user);
		
		// 24
		System.out.println(user.getId());
		// 24 
		System.out.println(copy.getId());
		
		// 这两个效果一样
		user.setName("AAAAA");
		copy.setName("AAAAA");
		
		// 1535026957
		System.out.println(user.hashCode());
		// 1535026957
		System.out.println(copy.hashCode());
		
		// true
		System.out.println(userDao.getSession().contains(user));
		// true
		System.out.println(userDao.getSession().contains(copy));
	}
	
	@Test
	public void testMerge4() {
		User user = userDao.get(34L);
		// 这个会报错 deleted instance passed to merge: [entity.User#<null>]
//		userDao.delete(user);
		userDao.evict(user);
		User copy = userDao.merge(user);
		
		// 不会触发update
		user.setName("12312121");
		// 会触发update
		copy.setName("12312121");
		
		// 24
		System.out.println(user.getId());
		// 24 
		System.out.println(copy.getId());
		
		// 对于先把对象evict后，再merge，会返回一个新的对象，原对象不再被维护
		// 1436944861
		System.out.println(user.hashCode());
		// 838820617
		System.out.println(copy.hashCode());
		
		// false
		System.out.println(userDao.getSession().contains(user));
		// true
		System.out.println(userDao.getSession().contains(copy));
	}
	
    // ********************************* 删 ***********************************
	@Test
    public void delete() {
        User user = new User(8L);
        userDao.delete(user);
    }

	@Test
    public void deleteById() {
    	int n = userDao.deleteById(15L);
    	System.out.println(n);
    }
    
	@Test
    public void deleteByField() {
/*    	int n = userDao.delete("age", Arrays.asList(2, 3));
    	System.out.println(n);
    	
    	n = userDao.delete("age", 5);
    	System.out.println(n);
    	
    	// 不存在的字段，java.lang.IllegalArgumentException: Unable to locate Attribute  with the the given name [age1] on this ManagedType [entity.BaseEntity]
    	n = userDao.delete("age1", 5);
    	System.out.println(n);*/
		
		int num = addressDao.delete("user", new User(6L));
		System.out.println(num);
    	
//    	int num = productDao.delete("categories.category.id", 4);
//    	System.out.println(num);
    }
    
	@Test
	public void deleteBySqls() {
        List<String> list = Arrays.asList("delete u from User u where u.id = 19", "delete u from User u where u.name = 'b'");
        userDao.deleteBySqls(list);
    }
    
    // 改
	@Test
    public void update() {
		User user = userDao.get(1L);
//        User user = new User(1L, "1231", 88);
		user.setName("1231");
		user.setInfo(null);
        userDao.update(user);
        System.out.println(user);
    }

    @Test
    public void saveOrUpdate() {
    	// 无id，保存
    	User user = new User("1231", 88);	
        userDao.saveOrUpdate(user);
        
        // 有id，更新
        User user1 = new User(5L, "AA", 19);
        userDao.saveOrUpdate(user1);
        
        // 有id，更新，但id不存在，报错
        User user2 = new User(1115L, "AA", 19);
        userDao.saveOrUpdate(user2);
    }
    
    @Test
    public void batchUpdate() {
    	Address a1 = new Address(null, "广东", "深圳", "南山", new Random().nextInt(1231825812) + "");
    	Address a2 = new Address(null, "广东", "深圳", "南山", new Random().nextInt(1231825812) + "");
    	Address a3 = new Address(null, "广东", "深圳", "南山", new Random().nextInt(1671825812) + "");
    	Address a4 = new Address(null, "广东", "深圳", "南山", new Random().nextInt(1671825812) + "");
    	Address a5 = new Address(null, "广东", "深圳", "南山", new Random().nextInt(1671825812) + "");
    	
    	List<Address> list = Arrays.asList(a1, a2, a3, a4, a5);
    	addressDao.batchSave(list);
    }
    
    @Test
    public void updateBySql() {
       int num = userDao.updateBySql("update User set name = ? where id = ?", "ABCD", 20);
       System.out.println(num);
    }

    @Test
	public void updateByHql() {
        String hql = "update Address a set a.street = a.id + 1 where a.id < ?0";
        int num = addressDao.updateByHql(hql, 8L);
        System.out.println(num);
        
        
        hql = "update Address a set a.street = a.id + 1 where a.id in (?0)";
        num = addressDao.updateByHql(hql, new Object[] {new Object[] {1L, 2L, 3L}});
        System.out.println(num);
        
        
        hql = "update Address a set a.street = a.id + 1 where a.id in (?0)";
        num = addressDao.updateByHql(hql, Arrays.asList(1L, 2L, 3L));
        System.out.println(num);
        
    }

    // 查
    // ***************** 查单个值或一条记录 ***********************
    @Test
    public void get() {
        Address a = addressDao.get(1L);
        System.out.println(a);
    }
    
    // TODO 这个锁定后超时时间不起作用，再处理
    @Test
    public void getWithLock() throws InterruptedException {
    	Address a = addressDao.get(1L, LockOptions.UPGRADE);
        System.out.println(a);
    }

    @Test
    public void getReference() {
    	// 直接触发sql
    	Address a = addressDao.get(1L);
    	
    	Address b = addressDao.getReference(2L);
    	// 没有这个sysout不会触发sql
    	System.out.println(b);
    }
    
    @Test
    public void findSingleBySql() {
        Object address = addressDao.findSingleBySql("select province, city from Address a where a.id = ?", 3L);
        System.out.println(Arrays.toString((Object[])address));
    }

    @Test
    public void findSingleValueBySql() {
    	Address address = addressDao.findSingleValueBySql(Address.class, "select * from Address a where a.id = ?", 3L);
        System.out.println(address);
    }
    
    // ****************** 查多条记录 *************************
    @Test
    public void getAll() {
    	List<Address> all = addressDao.getAll();
		System.out.println(all.size());
    	System.out.println(all);
    }
    
    
    // 查多条记录，带分页
    
    
    // ************************ 根据HQL查询单个值 ********************************
    @Test
	public void findSingleValueByHql() {
	     Address a = addressDao.findSingleValueByHql("from Address a where a.id = ?", 1L);
	     System.out.println(a);
    }
    
    // ************************ 根据HQL查询list ********************************
    @Test
    public void findByHQL() {
    	String hql = "from Address a where a.id < ?";
    	List<Address> list = addressDao.findByHQL(hql, 3L);
    	System.out.println(list.size());
    }
    
    @Test
    public void findByHQLWithLimit() {
    	String hql = "from Address a where a.id < ?";
    	List<Address> list = addressDao.findByHQLWithLimit(hql, null, 1, 1, 5L);
    	System.out.println(list.size());
    }
    
    @Test
	public void findByHQLWithLockLimit() {
    	String hql = "from Address a where a.id < ?";
    	List<Address> list = addressDao.findByHQLWithLockLimit(hql, null, LockOptions.READ, 1, 2, 5L);
    	System.out.println(list.size());
    }
    
    @Test
	public void findByHQLWithInLimit() {
/*    	String hql = "from Address a where a.id in :id";
    	Map<String, Object> map = new HashMap<>();
    	map.put("id", Arrays.asList(1L, 2L, 5L));
    	List<Address> list = addressDao.findByHQLWithInLimit(hql, 1, 2, map);
    	System.out.println(list.size());*/
    	
    	String hql = "from Address a where (a.id, a.city) in :id";
    	Map<String, Object> map = new HashMap<>();
    	List<Object[]> args = new ArrayList<>();
    	Object[] arr = new Object[] {1, "AA"};
    	args.add(arr);
		map.put("id", args);
//		map.put("id", Arrays.asList(arr));
//    	map.put("id", Arrays.asList(new Object[] {1, "AA"}, new Object[] {2, "BB"}));
    	List<Address> list = addressDao.findByHQLWithInLimit(hql, 1, 2, map);
    	System.out.println(list.size());
    	
    }
    
    // ******************** 统计 count *****************


    @Test
    public void countByHql() {
    	String hql = "from Address a where a.id < ?0";
    	long result = addressDao.countByHql(hql, 3L);
    	System.out.println(result);
    }
    
    @Test
    public void countBySql() {
    	String hql = "select * from Address a where a.id < ?0";
    	long result = addressDao.countByHql(hql, 3L);
    	System.out.println(result);
    }
    
    // ********************根据原生sql查询 ****************
    
    @Test
	public void findBySqlWithLimit() {
/*    	String sql = "select * from Address a where a.id < ?";
    	List<Address> list = addressDao.findBySqlWithLimit(Address.class, sql, 1, 1, 5L);
    	System.out.println(list.size());*/
    	
    	// 这样执行带limit，但加上带集合的fetch后，没有limit，也会报下面的错，实际查询时不会分页，是在内存中进行的分页
    	// 多对一的查询则不会出现这种情况
    	List<User> users = userDao.getQueryObject()/*.innerJoinFetch("addresses")*/.list(0, 2);
    	System.out.println(users.size());
    	users.forEach(u -> {
    		System.out.println(u.getName()/* + " " + u.getAddresses().size()*/);
    	});
    	
    	/**
    	 * 对于这种带查集合的，会报这种错WARN: HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!
    	 * 执行的语句也没有limit
    	 * Hibernate: 
		    select
		        user0_.id as id1_0_0_,
		        addresses1_.id as id1_1_1_,
		        user0_.age as age2_0_0_,
		        user0_.birthday as birthday3_0_0_,
		        user0_.dateTime as dateTime4_0_0_,
		        user0_.info as info5_0_0_,
		        user0_.latitude as latitude6_0_0_,
		        user0_.longitude as longitud7_0_0_,
		        user0_.name as name8_0_0_,
		        user0_.property as property9_0_0_,
		        user0_.status as status10_0_0_,
		        user0_.`time` as time11_0_0_,
		        user0_.version as version12_0_0_,
		        addresses1_.city as city2_1_1_,
		        addresses1_.county as county3_1_1_,
		        addresses1_.province as province4_1_1_,
		        addresses1_.street as street5_1_1_,
		        addresses1_.`user_id` as user_id6_1_1_,
		        addresses1_.`user_id` as user_id6_1_0__,
		        addresses1_.id as id1_1_0__ 
		    from
		        `User` user0_ 
		    inner join
		        Address addresses1_ 
		            on user0_.id=addresses1_.`user_id`
    	 */
    	users = userDao.findByHQLWithLimit("select u from User u join fetch u.addresses", null, 0, 2);
    	System.out.println(users.size());
    	users.forEach(u -> {
    		System.out.println(u.getName() + " " + u.getAddresses().size());
    	});
    }

   /* @Test
	public void findBySqlUseResultMapping() {
        List<UserVo> list = userDao.findBySqlUseResultMapping("user_dto", "select id, name, age from User u where u.id < ?", 0, 3, 15L);
        System.out.println(list.size());
    }*/
    
    @Test
    public void getOne() {
    	System.out.println(userDao.getOne("name", "ABCD"));
    }
    
    @Test
    public void getOneByMultiFields() {
    	User one = userDao.getOne(FieldsAndValuesMap.init().add("name", "ABCD").add("age", 88));
    	System.out.println(one);
    }

    @Test
    public void getList() {
    	List<User> list = userDao.getList("age", 88);
		System.out.println(list.size());
    }
    
    @Test
	public void getListByMultiFields() {
    	List<User> list = userDao.getList(FieldsAndValuesMap.init().add("name", "bbbb").add("age", 4));
    	System.out.println(list.size());
    	
    	// 带in的
    	list = userDao.getList(FieldsAndValuesMap.init().add("name", "bbbb").add("age", Arrays.asList(4, 5)));
    	System.out.println(list.size());
    }
    
    @Test
    public void testSql() {
    	String sql = "select 5 * 5";
    	Number num = userDao.findSingleBySql(sql);
    	System.out.println(num);
    	
//    	SET @pt1 = ST_GeomFromText('POINT(0 0)');
//    	mysql> SET @pt2 = ST_GeomFromText('POINT(180 0)');
//    	mysql> SELECT ST_Distance_Sphere(@pt1, @pt2);
    	
    	sql = "select ST_Distance_Sphere(ST_GeomFromText('POINT(78 23.96)'), ST_GeomFromText('POINT(75 24.75)'))";
    	num = userDao.findSingleBySql(sql);
    	
    	num = getDistance(78, 23.96, 75, 24.75);
    	System.out.println(num.doubleValue() / 1000 + " km");
    }
    
    @Test
    public void maintainIndex() throws InterruptedException{
    	userDao.batchMaintainIndex(User.class);
    }
    
    /**
     * 根据经纬度计算跨度，单位: 米
     * @param longitude1
     * @param latitude1
     * @param longitude2
     * @param latitude2
     * @return
     */
    // latitude 纬度		[-90, 90]
    // longitude 经度	(-180, 180]
    private double getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
    	String sql = String.format("select ST_Distance_Sphere(ST_GeomFromText('POINT(%f %f)'), ST_GeomFromText('POINT(%f %f)'))", 
    			longitude1, latitude1, longitude2, latitude2);
    	return userDao.findSingleBySql(sql);
    }
    
}
