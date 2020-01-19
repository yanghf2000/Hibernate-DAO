# Hibernate-DAO
Hibernate通用DAO

	本项目的目的是为了方便对数据库的操作，提供了一些常用的方法。
	在test文件夹下提供了测试方法，方便进行测试。
	若有问题或建议可以联系我: yanghf2000@163.com
	

2019-12-4
测试映射map的情形
	
2019-9-13
hibernate, hibernate search都采用最新版本
解决同时关联a.b.c  a.d时报a重复关联的问题

2019-2-13
hibernate search可以维护指定类的索引

2019-1-24
exeupdateByHql增加in参数	

2019-1-7
原版本固定为0.0.1-release, 
过后要升级到0.0.2-release

hibernate更新为5.4.0，支持jdk11
hibernate search更新为5.11.0, 支持jdk11
新的hql要用 ?0 ?1, 然后设置值，不能再只用?, ?，要加数字或命名参数方式才行
对于旧的，用?0这种方式是可以的，这是jpa的形式
	
2018-10-3
添加处理枚举或对象类多值搜索

2018-4-30
解决hibernate search时间类搜索的问题
解决搜索时排序的问题

2018-4-29
优化like方法，可以传入多个值，用（field like exp_1 or field like exp_2）作为整体的方式跟在where条件后
	
2018-3-25
原来的le lt ge gt只适用于Number类型，现在增加了，只要实现了Comparable接口的类型都可以
增加between方法
	
2018-3-2
处理类似于a.b.c这种导航形式的关联、查询、分类或排序等，对于已经有关联的，如果在查询、分类或排序中使用，直接使用已经关联的联表，否则新建一个连接。
对于多次调用关联的，如：innerJoin(a.b).innerJoin(a.b)，会重复关联两遍，不做排重处理。

2018-2-16
在用a.b.c形式的join时，没有问题，但fetch会报错，现在已找到了一个解决方法，但也有点问题，具体见相应方法上的注解

2018-1-25
增加QueryUpdateObject和QueryDeleteObject, 对于此类更新和删除，都不能用join。

2018-1-20
在搜索时，对于数字，默认支持byte short int double long float，对于BigDecimal等则不支持，现在加了BigDecimalNumericFieldBridge, 可以如下使用：
	@Field
	@FieldBridge(impl = BigDecimalNumericFieldBridge.class, params = @org.hibernate.search.annotations.Parameter(name = "type", value = "double"))
	private BigDecimal property;
    对于使用的转换类型，可以在Parameter中通过type指定，若不指定，默认为double类型，现在可以实现此类数据的范围查找，排序功能

2018-1-19
增加锁功能，查询时可以加锁

2018-1-16
加入距离搜索功能，具体见QuerySearchObject.java和TestQuerySearchObject.java
对于位置搜索的，类上要加上@Spatial注解，经纬度要加上相应注解
	// 纬度
	@Latitude
	private Double latitude;
	
	// 经度
	@Longitude
	private Double longitude;
	
	若要计算距离，则要获取到Coordinates，
	@SortableField   // 排序，不排序可不加
	@Spatial(spatialMode = SpatialMode.HASH)
	public Coordinates getLocation() {
		return new Coordinates() {
			@Override
			public Double getLatitude() {
				return latitude;
			}

			@Override
			public Double getLongitude() {
				return longitude;
			}
		};
	}
	以上例子可以参考User类，具体见hiernate search说明文档

2018-1-15
添加搜索功能，搜索功能中可以进行模糊查询，也可以加较精确的表达式（值匹配，范围），具体见相应方法上的注解。
具有排序和关联功能。

2018-1-11
导航中a.b.c，如果b为list set的，会失败，暂时还没有解决

2018-1-10
增加hibernate search

2018-1-9
join fetch实现类似a.b.c这种导航查询
增加wrapper功能，实现非实体对象映射

2018-1-5
增加类似a.b.c这种导航查询，可以作为条件，也可以作为查询字段

2017.1.3 
0.0.1-SNAPSHOT版
使用说明：
  Dao是一个抽象类，使用时只要将实现此类即可，为了方便使用，可用一个(抽象)类继承Dao类，然后其他持久化类继承实现类就行。例如：
  
public abstract class BaseDao<T extends BaseEntity> extends Dao<T>{
	
	@Autowired
    protected SessionFactory sessionFactory;
    
    protected SessionFactory getSessionFactory() {
		return sessionFactory;
	}
    
}

