package entity;

import com.github.yanghf2000.analyzer.AnalyzerName;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
@Indexed
public class Address extends BaseIdEntity{

	private static final long serialVersionUID = 741793265894130462L;

	public Address(){}
	
	public Address(Long id, String province, String city, String county, String street) {
		this.id = id;
		this.province = province;
		this.city = city;
		this.county = county;
		this.street = street;
	}

	@IndexedEmbedded(includeDepth = 1, includeEmbeddedObjectId = true, includePaths = {"id", "name"})
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@GenericField
	private String province;

	@GenericField
	private String city;

	@GenericField
	private String county;

	@FullTextField(analyzer = AnalyzerName.CHINESE)
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
