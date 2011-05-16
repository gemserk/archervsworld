package com.gemserk.games.archervsworld.controllers;

public class BowData {
	
	public enum State { 
		Ready,
		Charging, 
		Firing, 
		Recharging
	}

	private float angle;

	private float power;
	
	private boolean recharging;

	private boolean charging;
	
	private boolean firing;
	
	public float getAngle() {
		return angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getPower() {
		return power;
	}

	public void setPower(float power) {
		this.power = power;
	}
	
	public boolean isRecharging() {
		return recharging;
	}
	
	public void setRecharging(boolean recharging) {
		this.recharging = recharging;
	}
	
	public boolean isCharging() {
		return charging;
	}

	public void setCharging(boolean charging) {
		this.charging = charging;
	}

	public boolean isFiring() {
		return firing;
	}

	public void setFiring(boolean firing) {
		this.firing = firing;
	}

}