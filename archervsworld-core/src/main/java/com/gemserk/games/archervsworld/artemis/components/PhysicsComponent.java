package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;
import com.badlogic.gdx.physics.box2d.Body;
import com.gemserk.componentsengine.properties.Property;

public class PhysicsComponent extends Component {

	private Property<Body> body;
	
	public Body getBody() {
		return body.get();
	}

	public PhysicsComponent(Property<Body> body) {
		this.body = body;
	}

}
