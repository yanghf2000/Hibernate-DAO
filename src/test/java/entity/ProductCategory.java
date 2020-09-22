package entity;

import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class ProductCategory extends BaseEntity {

	private static final long serialVersionUID = 2140849021252599177L;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	private Product product;
	
	@Id
	@IndexedEmbedded
	@ManyToOne(fetch = FetchType.LAZY)
	private Category category;

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
}
