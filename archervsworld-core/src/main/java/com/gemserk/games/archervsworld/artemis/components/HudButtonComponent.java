package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;
import com.gemserk.commons.values.BooleanValue;
import com.gemserk.commons.values.ValueBuilder;
import com.gemserk.componentsengine.properties.Property;
import com.gemserk.componentsengine.properties.PropertyBuilder;

public class HudButtonComponent extends Component {

	private Property<BooleanValue> pressed;
	
	public boolean getPressed() {
		return pressed.get().value;
	}
	
	public void setPressed(boolean pressed) {
		this.pressed.get().value = pressed;
	}

	public HudButtonComponent(Property<BooleanValue> pressed) {
		this.pressed = pressed;
	}
	
	public HudButtonComponent() {
		this(PropertyBuilder.property(ValueBuilder.booleanValue(false)));
	}

}
