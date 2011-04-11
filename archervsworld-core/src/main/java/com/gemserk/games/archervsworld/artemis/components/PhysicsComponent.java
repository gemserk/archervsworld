package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;
import com.badlogic.gdx.physics.box2d.Body;
import com.gemserk.componentsengine.properties.Property;

public class PhysicsComponent extends Component {

	private Property<Body> body;
	
	private Property<PhysicsBehavior> physicsBehavior;
	
	public Body getBody() {
		return body.get();
	}
	
	public PhysicsBehavior getPhysicsBehavior() {
		return physicsBehavior.get();
	}

	public PhysicsComponent(Property<Body> body, Property<PhysicsBehavior> physicsBehavior) {
		this.body = body;
		this.physicsBehavior = physicsBehavior;
	}

}
