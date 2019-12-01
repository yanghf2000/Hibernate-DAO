package entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

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
