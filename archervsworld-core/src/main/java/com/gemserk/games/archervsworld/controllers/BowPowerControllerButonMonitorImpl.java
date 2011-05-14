package com.gemserk.games.archervsworld.controllers;

import com.gemserk.componentsengine.input.ButtonMonitor;

public class BowPowerControllerButonMonitorImpl implements BowController {

	private final ButtonMonitor buttonMonitor;

	private final BowData bowData;

	@Override
	public BowData getBowData() {
		return bowData;
	}

	public BowPowerControllerButonMonitorImpl(BowData bowData, ButtonMonitor buttonMonitor) {
		this.bowData = bowData;
		this.buttonMonitor = buttonMonitor;
	}

	@Override
	public boolean wasHandled() {
		return false;
	}

	@Override
	public void update(int delta) {
		buttonMonitor.update();

		if (buttonMonitor.isPressed()) {
			bowData.setCharging(true);
			bowData.setFiring(false);
			bowData.setPower(0f);
		}

		if (buttonMonitor.isHolded()) {
			bowData.setPower(bowData.getPower() + 0.03f * delta);
		}

		if (buttonMonitor.isReleased()) {
			bowData.setFiring(true);
			bowData.setCharging(false);
		}
	}

}