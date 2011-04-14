package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;
import com.gemserk.componentsengine.properties.Property;
import com.gemserk.componentsengine.properties.PropertyBuilder;
import com.gemserk.componentsengine.utils.Container;

public class HealthComponent extends Component {

	private final Property<Container> container;
	
	public Container getContainer() {
		return container.get();
	}

	public HealthComponent(Property<Container> container) {
		this.container = container;
	}
	
	public HealthComponent(Container container) {
		this(PropertyBuilder.property(container));
	}

}
