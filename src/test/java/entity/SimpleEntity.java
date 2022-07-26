package entity;


import jakarta.persistence.Entity;

@Entity
public class SimpleEntity extends BaseIdEntity{

    private static final long serialVersionUID = 2065493632727415340L;

    private String name;

    private int sequence;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
