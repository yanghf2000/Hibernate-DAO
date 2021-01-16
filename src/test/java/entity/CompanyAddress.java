package entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class CompanyAddress extends BaseIdEntity {

	private static final long serialVersionUID = -1604205646265475995L;

	@ManyToOne(fetch = FetchType.LAZY)
	private Company company;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompanyAddress that = (CompanyAddress) o;
		return Objects.equals(company, that.company) && Objects.equals(address, that.address);
	}

	@Override
	public int hashCode() {
		return Objects.hash(company, address);
	}
}
