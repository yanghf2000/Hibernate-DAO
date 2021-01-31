package entity;

import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Indexed
public class Product extends BaseIdEntity {

	private static final long serialVersionUID = 3190769021252599177L;

	@FullTextField(analyzer = "chinese")
	private String name;
	
	@FullTextField(analyzer = "chinese")
	private String subtitle;
	
	/**
	 * 如果查询的比较深，则要加depth
	 */
	@IndexedEmbedded(includeDepth = 2)
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "product", orphanRemoval = false)
	private List<ProductCategory> categories;
	
//	@GenericField(sortable = Sortable.YES)
	// 数字类型，排序 比较等用
	// 若使用的FieldBridge实现了MetadataProvidingFieldBridge接口，则可以不用加@NumericField注解

	// 这样会把数字转换成字符串，但排序时会出错
//	id: 5, name: 板栗, subtitle: 糖炒板栗, price: 34.00
//	id: 2, name: 手机, subtitle: 小米手机, price: 3000.00
//	id: 6, name: 核桃, subtitle: 新疆纸皮核桃, price: 243.35
//	id: 7, name: 果子, subtitle: 陕西红富士, price: 243.36
//	@FieldBridge(impl = DoubleBridge.class)	
	
	private BigDecimal price;
	
//	@GenericField(sortable = Sortable.YES)
	private LocalDate publishTime;
	
//	@IndexedEmbedded(includeEmbeddedObjectId = true, includeDepth = 2, includePaths = {"id", "name", "user.id"})
	@ManyToOne(fetch = FetchType.LAZY)
	private Company company;

	// key name，这个不需要在value里映射
	// 这一种不会生成带主键的表，不过也可以用join获取
	@ElementCollection
	@CollectionTable(name = "product_arguments")
	@MapKeyColumn(name = "name", length = 100)
	private Map<String, ProductArguments> productArguments = new HashMap<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public List<ProductCategory> getCategories() {
		return categories;
	}

	public void setCategories(List<ProductCategory> categories) {
		this.categories = categories;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public LocalDate getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(LocalDate publishTime) {
		this.publishTime = publishTime;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Map<String, ProductArguments> getProductArguments() {
		return productArguments;
	}

	public void setProductArguments(Map<String, ProductArguments> productArguments) {
		this.productArguments = productArguments;
	}

}
