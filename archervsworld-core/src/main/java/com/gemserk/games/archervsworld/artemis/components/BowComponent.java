package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;
import com.artemis.Entity;
import com.gemserk.commons.values.FloatValue;
import com.gemserk.componentsengine.properties.Property;

public class BowComponent extends Component {

	private Property<FloatValue> power;

	private Property<Entity> arrow;

	private Property<FloatValue> minPower;

	private Property<FloatValue> maxPower;

	public float getPower() {
		return power.get().value;
	}

	public void setPower(float power) {
		this.power.get().value = power;
	}
	
	public float getMinPower() {
		return minPower.get().value;
	}
	
	public float getMaxPower() {
		return maxPower.get().value;
	}

	public Entity getArrow() {
		return arrow.get();
	}

	public void setArrow(Entity arrow) {
		this.arrow.set(arrow);
	}

	public BowComponent(Property<FloatValue> power, Property<Entity> arrow, Property<FloatValue> minPower, Property<FloatValue> maxPower) {
		this.power = power;
		this.arrow = arrow;
		this.minPower = minPower;
		this.maxPower = maxPower;
	}

}
