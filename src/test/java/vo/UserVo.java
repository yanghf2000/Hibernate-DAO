package vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserVo {
	
	public UserVo(Long id, String name, int age) {
		this.id = id;
		this.name = name;
		this.age = age;
	}
	
	private Long id;
	
	private String name;
	
	private int age;

}
