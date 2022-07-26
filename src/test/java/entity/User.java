package entity;

import com.github.yanghf2000.analyzer.AnalyzerName;
import com.github.yanghf2000.descriptor.CommaDelimitedListToStringsDescriptor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.bridge.builtin.annotation.GeoPointBinding;
import org.hibernate.search.mapper.pojo.bridge.builtin.annotation.Latitude;
import org.hibernate.search.mapper.pojo.bridge.builtin.annotation.Longitude;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;
import vo.UserVo;

import jakarta.persistence.*;
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
@Entity
@DynamicInsert
@DynamicUpdate
@Indexed
@GeoPointBinding(fieldName = "location", sortable = Sortable.YES)
public class User extends BaseIdEntity implements Comparable<User>{

	private static final long serialVersionUID = 741793537894130462L;

	public User(){}
	
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

	@FullTextField(analyzer = AnalyzerName.CHINESE)
	@GenericField(name = "name_1", indexNullAs = "")
	private String name;

	@GenericField
	private String code;

	@GenericField(sortable = Sortable.YES)
	private int age;

	@GenericField
	@Enumerated(EnumType.STRING)
	private Sex sex;

	@GenericField(sortable = Sortable.YES)
	private LocalDate birthday;

	@FullTextField
	private String info;

	// 若加了null的替代值，不能再直接使用null搜索
	@ScaledNumberField(sortable = Sortable.YES)
//	@ScaledNumberField(sortable = Sortable.YES, indexNullAs = "-1")
	private BigDecimal property;

	// includePaths 写出要查询的字段，这个可以不加，但如果加了，对方类的字段上必须加上@Field注解
	// @IndexedEmbedded(includePaths = {"city"})
	// 对于以下异常，可以通过加注depth解决
	// Caused by: org.hibernate.search.exception.SearchException: HSEARCH000221:
	// Circular reference. Entity entity.Address was already encountered,
	// and was encountered again in entity entity.User at path 'user.addresses.'.
	// Set the @IndexedEmbedded.depth value explicitly to fix the problem.
	@IndexedEmbedded(includeDepth = 1)
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user", orphanRemoval = true)
	private List<Address> addresses;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user", orphanRemoval = true)
	private Set<Address> companyAddresses;

//	@Transient
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user", orphanRemoval = true)
	private List<Company> companies;

	// 纬度
	@Latitude
	private Double latitude;

	// 经度
	@Longitude
	private Double longitude;

	@Version
	private int version;
	
	private boolean status;

	@Override
	public int compareTo(User o) {
		return (int) (this.getId() - o.getId());
	}

//	@GenericField
	private LocalTime time;
	
	@GenericField
	private LocalDateTime dateTime;

	@Convert( converter = CommaDelimitedListToStringsDescriptor.class )
	private List<String> jobs = new ArrayList<>();

	@Convert( converter = CommaDelimitedListToStringsDescriptor.class )
	private List<Integer> interestingNumbers = new ArrayList<>();

	// 对于以下想处理集合的，但没有独立类的，也是可以搜索的
//	@IndexedEmbedded
	@Column( name = "sex", columnDefinition = "varchar(10) NOT NULL")
	@JoinTable(name = "user_sexes", joinColumns = {@JoinColumn(name = "user_id")},
			indexes = {@Index(name = "idx_user_id", columnList = "user_id")})
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

	public LocalDate getBirthday() {
		return birthday;
	}

	public void setBirthday(LocalDate birthday) {
		this.birthday = birthday;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public BigDecimal getProperty() {
		return property;
	}

	public void setProperty(BigDecimal property) {
		this.property = property;
	}

	public List<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}

	public List<Company> getCompanies() {
		return companies;
	}

	public void setCompanies(List<Company> companies) {
		this.companies = companies;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public List<String> getJobs() {
		return jobs;
	}

	public void setJobs(List<String> jobs) {
		this.jobs = jobs;
	}

	public Set<Sex> getSexes() {
		return sexes;
	}

	public void setSexes(Set<Sex> sexes) {
		this.sexes = sexes;
	}

	public List<UserInteresting> getInterestings() {
		return interestings;
	}

	public void setInterestings(List<UserInteresting> interestings) {
		this.interestings = interestings;
	}

	public Set<Address> getCompanyAddresses() {
		return companyAddresses;
	}

	public void setCompanyAddresses(Set<Address> companyAddresses) {
		this.companyAddresses = companyAddresses;
	}

	public List<Integer> getInterestingNumbers() {
		return interestingNumbers;
	}

	public void setInterestingNumbers(List<Integer> interestingNumbers) {
		this.interestingNumbers = interestingNumbers;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
