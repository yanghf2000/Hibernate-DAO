package entity;

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
//@Indexed
public class Category extends BaseIdEntity {

	private static final long serialVersionUID = 2140769021252599177L;

//	@FullTextField
	private String name;

//	@IndexedEmbedded(includeDepth = 1)
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "category", orphanRemoval = true)
	private List<ProductCategory> products;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ProductCategory> getProducts() {
		return products;
	}

	public void setProducts(List<ProductCategory> products) {
		this.products = products;
	}
}
