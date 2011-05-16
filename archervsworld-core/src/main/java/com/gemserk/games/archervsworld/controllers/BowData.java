package com.gemserk.games.archervsworld.controllers;

public class BowData {

	public enum State {
		READY, CHARGING, FIRING, RELOADING
	}

	private float angle;

	private float power;

	private State state = State.READY;

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
		return state == State.RELOADING;
	}

	public void reload() {
		state = State.RELOADING;
	}

	public void setReady() {
		state = State.READY;
	}

	public boolean isCharging() {
		return state == State.CHARGING;
	}

	public void charge() {
		state = State.CHARGING;
	}

	public boolean isFiring() {
		return state == State.FIRING;
	}

	public void fire() {
		state = State.FIRING;
	}

}