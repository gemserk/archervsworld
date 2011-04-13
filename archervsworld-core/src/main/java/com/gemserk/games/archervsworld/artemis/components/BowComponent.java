package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;
import com.artemis.Entity;
import com.gemserk.commons.values.FloatValue;
import com.gemserk.componentsengine.properties.Property;

public class BowComponent extends Component {

	private Property<FloatValue> power;

	private Property<Entity> arrow;

	public float getPower() {
		return power.get().value;
	}

	public void setPower(float power) {
		this.power.get().value = power;
	}

	public Entity getArrow() {
		return arrow.get();
	}

	public void setArrow(Entity arrow) {
		this.arrow.set(arrow);
	}

	public BowComponent(Property<FloatValue> power, Property<Entity> arrow) {
		this.power = power;
		this.arrow = arrow;
	}

}
