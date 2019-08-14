package entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Indexed
@Analyzer(impl = SmartChineseAnalyzer.class)
@NoArgsConstructor
@ToString(callSuper = true)
public class Category extends BaseIdEntity {

	private static final long serialVersionUID = 2140769021252599177L;

	@Field
	private String name;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "category", orphanRemoval = true)
	private List<ProductCategory> products;
	
}
