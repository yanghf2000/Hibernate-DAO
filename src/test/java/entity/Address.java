package entity;

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.hibernate.search.annotations.*;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
@Indexed
@Analyzer(impl = SmartChineseAnalyzer.class)
public class Address extends BaseIdEntity{

	private static final long serialVersionUID = 741793265894130462L;
	
	public Address(Long id, String province, String city, String county, String street) {
		this.id = id;
		this.province = province;
		this.city = city;
		this.county = county;
		this.street = street;
	}

	@IndexedEmbedded(/*depth = 1, */includeEmbeddedObjectId = true, includePaths = {"id", "name"})
//	@ContainedIn
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	
	private String province;
	
	private String city;
	
	private String county;

	@SortableField
	@Field
	private String street;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}
}
