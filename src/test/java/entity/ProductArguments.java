package entity;

import lombok.*;

import javax.persistence.Embeddable;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductArguments  {

	private double minValue;

	private double maxValue;

	private double step;

}
