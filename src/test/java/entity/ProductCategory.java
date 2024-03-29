package entity;

import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class ProductCategory extends BaseIdEntity {

	private static final long serialVersionUID = 2140849021252599177L;

//	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	private Product product;
	
//	@Id
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProductCategory that = (ProductCategory) o;
		return Objects.equals(product, that.product) && Objects.equals(category, that.category);
	}

	@Override
	public int hashCode() {
		return Objects.hash(product, category);
	}

}
