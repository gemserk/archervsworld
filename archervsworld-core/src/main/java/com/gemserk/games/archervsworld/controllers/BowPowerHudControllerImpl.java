package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.gemserk.commons.gdx.math.MathUtils2;
import com.gemserk.componentsengine.input.AnalogInputMonitor;
import com.gemserk.componentsengine.input.ButtonMonitor;

public class BowPowerHudControllerImpl extends BowPowerControllerButonMonitorImpl {

	private final AnalogInputMonitor xMonitor;

	private final AnalogInputMonitor yMonitor;

	private final Rectangle area;
	
	private boolean handled = false;

	public BowPowerHudControllerImpl(BowData bowData, ButtonMonitor buttonMonitor, AnalogInputMonitor xMonitor, AnalogInputMonitor yMonitor, Rectangle area) {
		super(bowData, buttonMonitor);
		this.xMonitor = xMonitor;
		this.yMonitor = yMonitor;
		this.area = area;
	}
	
	@Override
	public boolean wasHandled() {
		return handled;
	}

	@Override
	public void update(int delta) {
		float x = xMonitor.getValue();
		float y = yMonitor.getValue();

		xMonitor.update();
		yMonitor.update();
		
		handled = false;

		if (!MathUtils2.inside(area, x, Gdx.graphics.getHeight() - y))
			return;
		
		handled = true;

		super.update(delta);
	}

}
