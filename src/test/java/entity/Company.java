package entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Indexed
@NoArgsConstructor
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
	
	@Field
	private String name;
	
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@OneToOne(fetch = FetchType.LAZY)
	private Address address;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "company")
	private List<CompanyAddress> addresses;
	
	@Field
	@Enumerated(EnumType.STRING)
	private CompanyType companyType;
	
}
