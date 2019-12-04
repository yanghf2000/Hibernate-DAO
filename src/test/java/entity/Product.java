package entity;

import com.github.yanghf2000.bridge.BigDecimalNumericFieldBridge;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@Indexed
@Analyzer(impl = SmartChineseAnalyzer.class)
@NoArgsConstructor
@ToString(callSuper = true)
public class Product extends BaseIdEntity {

	private static final long serialVersionUID = 3190769021252599177L;

	@SortableField
	@Field(analyze = Analyze.YES, norms = Norms.NO)
	private String name;
	
	@Field
	@Column(length = 100)
	private String subtitle;
	
	/**
	 * 如果查询的比较深，则要加depth
	 */
	@IndexedEmbedded(depth = 2)
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "product", orphanRemoval = false)
	private List<ProductCategory> categories;
	
	@Field
	// 数字类型，排序 比较等用
	// 若使用的FieldBridge实现了MetadataProvidingFieldBridge接口，则可以不用加@NumericField注解
//	@NumericField
	@SortableField
	
	// 这样会把数字转换成字符串，但排序时会出错
//	id: 5, name: 板栗, subtitle: 糖炒板栗, price: 34.00
//	id: 2, name: 手机, subtitle: 小米手机, price: 3000.00
//	id: 6, name: 核桃, subtitle: 新疆纸皮核桃, price: 243.35
//	id: 7, name: 果子, subtitle: 陕西红富士, price: 243.36
//	@FieldBridge(impl = DoubleBridge.class)	
	
	@FieldBridge(impl = BigDecimalNumericFieldBridge.class, 
				params = {@Parameter(name = "type", value = "double"), @Parameter(name = "sortable", value = "true")})
	private BigDecimal price;
	
	@Field(norms = Norms.NO, store = Store.NO, index = Index.NO)
	@SortableField
	@DateBridge(resolution = Resolution.DAY)
	private LocalDate publishTime;
	
	@IndexedEmbedded(includeEmbeddedObjectId = true, depth = 2, includePaths = {"id", "name", "user.id"})
	@ManyToOne(fetch = FetchType.LAZY)
	private Company company;

	// key name，这个不需要在value里映射
	// 这一种不会生成带主键的表，不过也可以用join获取
	@ElementCollection
	@CollectionTable(name = "product_arguments")
	@MapKeyColumn(name = "name", length = 100)
	private Map<String, ProductArguments> productArguments = new HashMap<>();

}
