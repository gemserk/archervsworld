package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.Input;
import com.gemserk.componentsengine.input.ButtonMonitor;
import com.gemserk.componentsengine.input.LibgdxButtonMonitor;

public class BowControllerKeyboardImpl implements BowController {
	
	private float angle;
	
	private float power;
	
	private boolean charging;
	
	private boolean firing;
	
	@Override
	public float getAngle() {
		return angle;
	}
	
	@Override
	public float getPower() {
		return power;
	}
	
	@Override
	public boolean isCharging() {
		return charging;
	}
	
	@Override
	public boolean shouldFire() {
		return firing;
	}
	
	private ButtonMonitor increaseAngleMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_DPAD_UP);

	private ButtonMonitor decreaseAngleMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_DPAD_DOWN);
	
	private ButtonMonitor powerMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_SPACE);
	
	public BowControllerKeyboardImpl() {
		
	}
	
	@Override
	public void update() {
		
		increaseAngleMonitor.update();
		decreaseAngleMonitor.update();
		powerMonitor.update();
		
		firing = false;
		
		if (increaseAngleMonitor.isPressed() || decreaseAngleMonitor.isPressed() || powerMonitor.isPressed())
			charging = true;
		
		if (increaseAngleMonitor.isHolded()) {
			angle += 1f;
		}

		if (decreaseAngleMonitor.isHolded()) {
			angle -= 1f;
		}
		
		if (powerMonitor.isHolded()) {
			power += 0.5f;
		}
		
		if (powerMonitor.isReleased()) {
			firing = true;
			charging = false;
			power = 0f;
		}
		
	}
	
}