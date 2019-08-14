package com.github.yanghf2000.bridge;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.MetadataProvidingFieldBridge;
import org.hibernate.search.bridge.ParameterizedBridge;
import org.hibernate.search.bridge.TwoWayFieldBridge;
import org.hibernate.search.bridge.spi.FieldMetadataBuilder;
import org.hibernate.search.bridge.spi.FieldType;

/**
 * 对于默认的数字搜索，是不包含BigDecimal类型的，则可以使用这个<br>
 * 参考自hibernate search说明书<br>
 * 该转换器将BigDecimal转换成了double类型，则搜索时要指定用double类型<br>
 * 若像说明书中的例子，将数据转换成了long类型，且乘以了100，则获取值时也要乘以100，否则比对范围会出错，导致搜索不到结果<br>
 * <br>
 * 
 * 对于实体类上的注解如下：<br>
 * |@Field<br>
 * // @NumericField<br>
 * |@FieldBridge(impl = BigDecimalNumericFieldBridge.class, params = @org.hibernate.search.annotations.Parameter(name = "type", value = "double"))<br>
 * private BigDecimal property;<br>
 * <br>
 * 其中注解中可以传递的参数有：type 指定类型  string integer long float double, sortable 是否排序 ture/false <br>
 * 
 * 若不想加@NumericField注解，只要实现MetadataProvidingFieldBridge即可，实现此类增加了configureFieldMetadata方法，指定了要转换的数据类型<br>
 * 若想扩展成自己指定的类型，可以通过实现ParameterizedBridge接口，实现setParameterValues方法，如上面property上面的注解，
 * 在启动时将需要转换的类型传入，然后进行初始化就行，该方法会在configureFieldMetadata前面执行<br>
 * 
 * @author 杨会锋 2018-1-19
 */
public class BigDecimalNumericFieldBridge implements MetadataProvidingFieldBridge, TwoWayFieldBridge, ParameterizedBridge {

	private FieldType fieldType = FieldType.DOUBLE;
	
	private boolean sortable = false;

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		if (value != null) {
			BigDecimal decimalValue = (BigDecimal) value;
			luceneOptions.addNumericFieldToDocument(name, decimalValue.doubleValue(), document);
			
			// 加上这个索引时会报错
//			luceneOptions.addSortedDocValuesFieldToDocument(name, value.toString(), document);
			
			// 说明书上没有加这行代码，没有行代码查询，以后范围查询都没问题，但排序会出错，报下面的错误
			// java.lang.IllegalStateException: unexpected docvalues type NONE for field 'price' (expected=NUMERIC). Use UninvertingReader or index with docvalues.
//			  public static NumericDocValues getNumeric(LeafReader reader, String field) throws IOException {
//				    NumericDocValues dv = reader.getNumericDocValues(field);                         // 在此处获取不到正确的类型，为null，再往下就报错了
//				    if (dv == null) {
//				      checkField(reader, field, DocValuesType.NUMERIC);
//				      return emptyNumeric();
//				    } else {
//				      return dv;
//				    }
//				  }
			luceneOptions.addNumericDocValuesFieldToDocument(name, decimalValue.doubleValue(), document);
		}
	}

	@Override
	public Object get(String name, Document document) {
		String fromLucene = document.get(name);
		BigDecimal storedBigDecimal = new BigDecimal(fromLucene);
		return storedBigDecimal.doubleValue();
	}

	@Override
	public String objectToString(Object object) {
		return object.toString();
	}

	@Override
	public void configureFieldMetadata(String name, FieldMetadataBuilder builder) {
		builder.field(name, Objects.requireNonNull(fieldType, "要转换的字段类型不能为null!")).sortable(sortable);
	}

	public void setParameterValues(Map<String, String> parameters) {
		
		// 此时可以设置类型
		String type = parameters.get("type");
		if (type == null || "".equals(type)) {
			return; // 在没有此值的情况下，使用默认值
		}

		type = type.toUpperCase();

		switch (type) {
		case "STRING":
			this.fieldType = FieldType.STRING;
			break;
		case "BOOLEAN":
			this.fieldType = FieldType.BOOLEAN; // A boolean field, mapped to the String "true" or "false" in Lucene.
			break;
		case "DATE":
			this.fieldType = FieldType.DATE; // A date, mapped to a long in Lucene, corresponding to the number of milliseconds from Epoch.
			break;
		case "INTEGER":
			this.fieldType = FieldType.INTEGER;
			break;
		case "LONG":
			this.fieldType = FieldType.LONG;
			break;
		case "FLOAT":
			this.fieldType = FieldType.FLOAT;
			break;
		case "DOUBLE":
			this.fieldType = FieldType.DOUBLE;
			break;
		/**
		 * A composite object that will benefit from using a different encoding on each
		 * indexing technology.
		 * <p>
		 * For example, it could identify a field that should be mapped in Elasticsearch
		 * as an inner object and in Lucene as multiple string fields (Lucene does not
		 * have inner objects).
		 */
		case "OBJECT":
			this.fieldType = FieldType.OBJECT;
			break;
		default:
			throw new IllegalArgumentException("不正确的数据类型");
		}
		
		// 获取排序属性
		String sort = parameters.get("sortable");
		sortable = Boolean.valueOf(sort);
	}

}
