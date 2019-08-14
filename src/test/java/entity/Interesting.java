package entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@NoArgsConstructor
@ToString(callSuper = true)
public class Interesting extends BaseIdEntity {
	
	private static final long serialVersionUID = -4985602715419515632L;
	
	public Interesting(Long id) {
		this.id = id;
	}
	
	public Interesting(String name) {
		this.name = name;
	}

	private String name;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "interesting", orphanRemoval = true)
	private List<UserInteresting> users = new ArrayList<>();
}
