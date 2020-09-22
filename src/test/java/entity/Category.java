package entity;

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@Indexed
@Analyzer(impl = SmartChineseAnalyzer.class)
public class Category extends BaseIdEntity {

	private static final long serialVersionUID = 2140769021252599177L;

	@Field
	private String name;
	
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
