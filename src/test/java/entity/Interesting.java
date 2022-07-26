package entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
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

	public Interesting() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<UserInteresting> getUsers() {
		return users;
	}

	public void setUsers(List<UserInteresting> users) {
		this.users = users;
	}
}
