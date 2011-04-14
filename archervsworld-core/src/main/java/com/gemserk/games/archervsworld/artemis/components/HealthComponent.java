package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;
import com.gemserk.commons.values.FloatValue;
import com.gemserk.commons.values.ValueBuilder;
import com.gemserk.componentsengine.properties.Property;
import com.gemserk.componentsengine.properties.PropertyBuilder;
import com.gemserk.componentsengine.utils.Container;

public class HealthComponent extends Component {

	private final Property<Container> container;
	private final Property<FloatValue> resistance;
	
	public Container getContainer() {
		return container.get();
	}
	
	public float getResistance() {
		return resistance.get().value;
	}

	public HealthComponent(Property<Container> container, Property<FloatValue> resistance) {
		this.container = container;
		this.resistance = resistance;
	}
	
	public HealthComponent(Container container, float resistance) {
		this(PropertyBuilder.property(container), PropertyBuilder.property(ValueBuilder.floatValue(resistance)));
	}

}
