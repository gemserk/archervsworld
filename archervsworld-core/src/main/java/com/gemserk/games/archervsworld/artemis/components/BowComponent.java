package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;
import com.gemserk.commons.values.BooleanValue;
import com.gemserk.commons.values.FloatValue;
import com.gemserk.componentsengine.properties.Property;

public class BowComponent extends Component {

	private Property<FloatValue> power;
	
	private Property<BooleanValue> shouldFire;
	
	public float getPower() {
		return power.get().value;
	}
	
	public boolean getShouldFire() {
		return shouldFire.get().value;
	}
	
	public void setPower(float power) {
		this.power.get().value = power;
	}
	
	public void setShouldFire(boolean shouldFire) {
		this.shouldFire.get().value = shouldFire;
	}

	public BowComponent(Property<FloatValue> power, Property<BooleanValue> shouldFire) {
		this.power = power;
		this.shouldFire = shouldFire;
		
	}
	
}
