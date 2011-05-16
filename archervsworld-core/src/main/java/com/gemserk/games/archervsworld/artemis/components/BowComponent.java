package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;
import com.artemis.Entity;
import com.gemserk.commons.artemis.triggers.Trigger;
import com.gemserk.games.archervsworld.controllers.BowData;

/**
 * Whether the entity can shoot arrows or not.
 */
public class BowComponent extends Component {

	private final BowData bowData;

	private final Trigger fireTrigger;

	private float minPower;

	private float maxPower;

	private float power;

	private Entity arrow;

	public float getPower() {
		return power;
	}

	public void setPower(float power) {
		this.power = power;
	}

	public Entity getArrow() {
		return arrow;
	}

	public void setArrow(Entity arrow) {
		this.arrow = arrow;
	}

	public float getMinPower() {
		return minPower;
	}

	public float getMaxPower() {
		return maxPower;
	}

	public BowData getBowData() {
		return bowData;
	}

	public Trigger getFireTrigger() {
		return fireTrigger;
	}

	public BowComponent(float power, Entity arrow, float minPower, float maxPower, BowData bowData, Trigger fireTrigger) {
		this.power = power;
		this.arrow = arrow;
		this.minPower = minPower;
		this.maxPower = maxPower;
		this.bowData = bowData;
		this.fireTrigger = fireTrigger;
	}

}
