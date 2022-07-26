package com.github.yanghf2000.descriptor;

import jakarta.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * list保存为基本类型，使用如下：<br>
 * @Basic
 * @Convert( converter = CommaDelimitedListToStringsDescriptor.class )
 * private List<String> smallImages;<br>
 * @author 杨会锋
 * @date 2022-4-7
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CommaDelimitedListToStringsDescriptor implements AttributeConverter<List, String> {

	public static final String DELIMITER = ",";

	@Override
	public String convertToDatabaseColumn(List list) {
		if (list == null) {
			return null;
		}
		return String.join( DELIMITER, list);
	}

	@Override
	public List convertToEntityAttribute(String s) {
		if (s == null) {
			return null;
		}
		return new ArrayList(Arrays.asList(s.split(DELIMITER)));
	}

}
