package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;
import com.gemserk.commons.values.FloatValue;
import com.gemserk.commons.values.ValueBuilder;
import com.gemserk.componentsengine.properties.Property;
import com.gemserk.componentsengine.properties.PropertyBuilder;

public class DamageComponent extends Component {

	private Property<FloatValue> damage;
	
	public float getDamage() {
		return damage.get().value;
	}
	
	public DamageComponent setDamage(float damage) {
		this.damage.get().value = damage;
		return this;
	}

	public DamageComponent(Property<FloatValue> damage) {
		this.damage = damage;
	}
	
	public DamageComponent(float damage) {
		this(PropertyBuilder.property(ValueBuilder.floatValue(damage)));
	}

}
