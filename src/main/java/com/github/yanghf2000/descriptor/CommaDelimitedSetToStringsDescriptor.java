package com.github.yanghf2000.descriptor;

import jakarta.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * set保存为基本类型
 * @Basic
 * @Convert( converter = CommaDelimitedSetToStringsDescriptor.class )
 * private Set<String> smallImages;<br>
 * @author 杨会锋
 * @date 2022-4-7
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CommaDelimitedSetToStringsDescriptor implements AttributeConverter<Set, String> {

	public static final String DELIMITER = ",";

	@Override
	public String convertToDatabaseColumn(Set set) {
		if (set == null) {
			return null;
		}
		return String.join( DELIMITER, set);
	}

	@Override
	public Set convertToEntityAttribute(String s) {
		if (s == null) {
			return null;
		}
		return new HashSet(Arrays.asList(s.split(DELIMITER)));
	}

}
