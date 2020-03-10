package entity;

import com.github.yanghf2000.bridge.BigDecimalNumericFieldBridge;
import com.github.yanghf2000.bridge.DateTimeFieldBridge;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.*;
import org.hibernate.search.spatial.Coordinates;
import vo.UserVo;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 这个注解要加在entity类中，否则会找不到
@SqlResultSetMapping(name = "user_dto", classes = @ConstructorResult(targetClass = UserVo.class, columns = {
		@ColumnResult(name = "id", type = Long.class), @ColumnResult(name = "name"), // string类型可以不用指明
		@ColumnResult(name = "age", type = int.class) // 类似int等类型要特殊指明
}))
@Spatial(spatialMode = SpatialMode.HASH)
@Getter
@Setter
@Entity
@DynamicInsert
@DynamicUpdate
@Indexed
@Analyzer(impl = SmartChineseAnalyzer.class)
@NoArgsConstructor
@ToString(exclude = "addresses")
public class User extends BaseIdEntity implements Comparable<User>{

	private static final long serialVersionUID = 741793537894130462L;
	
	public User(Long id) {
		this(id, null, 0);
	}

	public User(String name, int age) {
		this(null, name, age);
	}

	public User(Long id, String name, int age) {
		this.id = id;
		this.name = name;
		this.age = age;
	}

	@SortableField
	@Field(index = org.hibernate.search.annotations.Index.YES, analyze = Analyze.YES, store = Store.NO)
	private String name;

	@Field
	@SortableField
	@NumericField
	private int age;

	@Enumerated(EnumType.STRING)
	private Sex sex;
	
	@SortableField
	@Field(index = org.hibernate.search.annotations.Index.YES, analyze = Analyze.NO, store = Store.NO)
	@FieldBridge(impl = DateTimeFieldBridge.class)
	private LocalDate birthday;

	@Field
	private String info;

	@Field
	@SortableField
	@FieldBridge(impl = BigDecimalNumericFieldBridge.class)
	private BigDecimal property;

	// includePaths 写出要查询的字段，这个可以不加，但如果加了，对方类的字段上必须加上@Field注解
	// @IndexedEmbedded(includePaths = {"city"})
	// 对于以下异常，可以通过加注depth解决
	// Caused by: org.hibernate.search.exception.SearchException: HSEARCH000221:
	// Circular reference. Entity entity.Address was already encountered,
	// and was encountered again in entity entity.User at path 'user.addresses.'.
	// Set the @IndexedEmbedded.depth value explicitly to fix the problem.
//	@IndexedEmbedded(depth = 1)
	@ContainedIn
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user", orphanRemoval = true)
	private List<Address> addresses;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user", orphanRemoval = true)
	private List<Company> companies;

	// 纬度
	@Latitude
	private Double latitude;

	// 经度
	@Longitude
	private Double longitude;

	@SortableField
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
	
	@Version
	private int version;
	
	private boolean status;

	@Override
	public int compareTo(User o) {
		return (int) (this.getId() - o.getId());
	}

	@Field
	@FieldBridge(impl = DateTimeFieldBridge.class)
	private LocalTime time;
	
	@Field
	@FieldBridge(impl = DateTimeFieldBridge.class)
	private LocalDateTime dateTime;
	
//	@Type(type = "descriptor.CommaDelimitedListToStringsDescriptor")
	@Transient
	private List<String> jobs = new ArrayList<>();

	// 对于以下想处理集合的，但没有独立类的，也是可以搜索的
	@Field
	@IndexedEmbedded
	@Column( name = "sex", columnDefinition = "varchar(10) NOT NULL")
	@PrimaryKeyJoinColumn(columnDefinition = "primary key (user_id, sex)")
	@Enumerated(EnumType.STRING)
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Sex> sexes = new HashSet<>();

//	@Field
//	private String sexes;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user", orphanRemoval = true)
	private List<UserInteresting> interestings = new ArrayList<>();
	
	public void addInteresting(Interesting it) {
		UserInteresting ui = new UserInteresting(this, it);
		interestings.add(ui);
		it.getUsers().add(ui);
	}
	
	public void removeInteresting(Interesting it) {
		UserInteresting ui = new UserInteresting(this, it);
		it.getUsers().removeIf(u -> u.getUser().getId().equals(id) && u.getInteresting().getId().equals(it.getId()));
		this.interestings.removeIf(u -> u.getUser().getId().equals(id) && u.getInteresting().getId().equals(it.getId()));
		
		ui.setUser(null);
		ui.setInteresting(null);
		
//		PersonAddress personAddress = new PersonAddress( this, address );
//		address.getOwners().remove( personAddress );
//		addresses.remove( personAddress );
//		personAddress.setPerson( null );
//		personAddress.setAddress( null );
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
}
