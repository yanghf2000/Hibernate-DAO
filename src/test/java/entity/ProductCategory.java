package entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.search.annotations.IndexedEmbedded;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@NoArgsConstructor
@ToString(callSuper = true)
public class ProductCategory extends BaseEntity {

	private static final long serialVersionUID = 2140849021252599177L;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	private Product product;
	
	@Id
	@IndexedEmbedded
	@ManyToOne(fetch = FetchType.LAZY)
	private Category category;
	
}
