package entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

/**
 * 对于级联操作，若有单独的主键，则关联的对象不需要加@MapsId，否则生成的表不对<br>
 * 若是要用联合主键，就不要再用独立主键了，联合主键上都要加@Id注解
 * @author 杨会锋
 * 2019-1-28
 */
@Entity
public class UserInteresting extends BaseIdEntity {

	private static final long serialVersionUID = -160420121215475995L;
	
	public UserInteresting(User user, Interesting it) {
		this.user = user;
		this.interesting = it;
	}
	
//	@Id
//	@MapsId
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	
//	@Id
//	@MapsId
	@ManyToOne(fetch = FetchType.LAZY)
	private Interesting interesting;

	public UserInteresting() {

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((interesting == null) ? 0 : interesting.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserInteresting other = (UserInteresting) obj;
		if (interesting == null) {
			if (other.interesting != null)
				return false;
		} else if (!interesting.equals(other.interesting))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Interesting getInteresting() {
		return interesting;
	}

	public void setInteresting(Interesting interesting) {
		this.interesting = interesting;
	}
}
