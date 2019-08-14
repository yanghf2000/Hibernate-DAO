package entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

}
