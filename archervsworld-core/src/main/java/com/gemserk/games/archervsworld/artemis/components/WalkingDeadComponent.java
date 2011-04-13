package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.values.IntValue;
import com.gemserk.componentsengine.properties.Property;

public class WalkingDeadComponent extends Component {

	private Property<Vector2> target;
	
	private Property<Vector2> force;

	private Property<IntValue> walkSleep;
	
	public Vector2 getTarget() {
		return target.get();
	}
	
	public Vector2 getForce() {
		return force.get();
	}
	
	public int getWalkSleep() {
		return walkSleep.get().value;
	}
	
	public void setWalkSleep(int walkSleep) {
		this.walkSleep.get().value = walkSleep;
	}

	public WalkingDeadComponent(Property<Vector2> target, Property<Vector2> force, Property<IntValue> walkSleep) {
		this.target = target;
		this.force = force;
		this.walkSleep = walkSleep;
	}
	
}
