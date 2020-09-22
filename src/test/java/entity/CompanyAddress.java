package entity;

import javax.persistence.*;

@Entity
public class CompanyAddress extends BaseEntity {

	private static final long serialVersionUID = -1604205646265475995L;
	
	@Id
	@MapsId
	@ManyToOne(fetch = FetchType.LAZY)
	private Company company;
	
	@Id
	@MapsId
	@ManyToOne(fetch = FetchType.LAZY)
	private Address address;

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}
}
