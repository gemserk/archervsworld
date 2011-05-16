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
	
	/**
	 * Called whenever the bow starts charging.
	 */
	private final Trigger chargeTrigger;

	private final Trigger fireTrigger;

	private int rechargeRate;
	
	private int rechargeTime;

	private float minPower;

	private float maxPower;

	private Entity arrow;

	
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

	public Trigger getChargeTrigger() {
		return chargeTrigger;
	}

	public Trigger getFireTrigger() {
		return fireTrigger;
	}
	
	public int getRechargeRate() {
		return rechargeRate;
	}
	
	public int getRechargeTime() {
		return rechargeTime;
	}
	
	public void setRechargeTime(int rechargeTime) {
		this.rechargeTime = rechargeTime;
	}
	
	public BowComponent(Entity arrow, float minPower, float maxPower, BowData bowData, int rechargeRate, Trigger fireTrigger, Trigger chargeTrigger) {
		this.arrow = arrow;
		this.minPower = minPower;
		this.maxPower = maxPower;
		this.bowData = bowData;
		this.rechargeRate = rechargeRate;
		this.fireTrigger = fireTrigger;
		this.chargeTrigger = chargeTrigger;
		this.rechargeTime = rechargeRate;
	}

}
