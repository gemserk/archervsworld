package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.values.IntValue;
import com.gemserk.componentsengine.properties.Property;

public class WalkingDeadComponent extends Component {

	private Property<Vector2> force;

	private Property<IntValue> walkSleep;

	private Property<IntValue> minSleepTime;

	private Property<IntValue> maxSleepTime;
	
	public Vector2 getForce() {
		return force.get();
	}
	
	public int getWalkSleep() {
		return walkSleep.get().value;
	}
	
	public void setWalkSleep(int walkSleep) {
		this.walkSleep.get().value = walkSleep;
	}
	
	public int getMinSleepTime() {
		return minSleepTime.get().value;
	}
	
	public int getMaxSleepTime() {
		return maxSleepTime.get().value;
	}

	public WalkingDeadComponent(Property<Vector2> force, Property<IntValue> walkSleep, Property<IntValue> minSleepTime, Property<IntValue> maxSleepTime) {
		this.force = force;
		this.walkSleep = walkSleep;
		this.minSleepTime = minSleepTime;
		this.maxSleepTime = maxSleepTime;
	}
	
}
