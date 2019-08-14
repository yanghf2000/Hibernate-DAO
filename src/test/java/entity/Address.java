package entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.SortableField;

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
public class Address extends BaseIdEntity{

	private static final long serialVersionUID = 741793265894130462L;
	
	public Address(Long id, String province, String city, String county, String street) {
		this.id = id;
		this.province = province;
		this.city = city;
		this.county = county;
		this.street = street;
	}

	@IndexedEmbedded(/*depth = 1, */includeEmbeddedObjectId = true, includePaths = {"id", "name"})
//	@ContainedIn
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	
	private String province;
	
	private String city;
	
	private String county;

	@SortableField
	@Field
	private String street;
}
