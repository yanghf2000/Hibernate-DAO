package entity;

import com.github.yanghf2000.analyzer.AnalyzerName;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Indexed
public class Company extends BaseIdEntity{

	private static final long serialVersionUID = 7417935378941308762L;
	
	public Company(Long id) {
		this(id, null);
	}
	
	public Company(String name) {
		this(null, name);
	}
	
	public Company(Long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	@FullTextField(analyzer = AnalyzerName.CHINESE)
	private String name;
	
	@IndexedEmbedded(includeEmbeddedObjectId = true, includePaths = {"id"})
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@OneToOne(fetch = FetchType.LAZY)
	private Address address;

//	@Transient
	// 加上@OrderColumn该注解会在中间表里生成一列addresses_order(默认字段)，但在查询时可以一次获取多个集合了，
	// 也可以指定表中的主键或关联表的另一个字段，address_id，如：name = "id"，则不会再生成多余的字段了，
	// 如使用已存在的字段company_id，则会报生成重复字段的错
	@OrderColumn(name = "address_id")
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "company")
	private List<CompanyAddress> addresses;
	
	@GenericField
	@Enumerated(EnumType.STRING)
	private CompanyType companyType;

	public Company() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public List<CompanyAddress> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<CompanyAddress> addresses) {
		this.addresses = addresses;
	}

	public CompanyType getCompanyType() {
		return companyType;
	}

	public void setCompanyType(CompanyType companyType) {
		this.companyType = companyType;
	}
}
