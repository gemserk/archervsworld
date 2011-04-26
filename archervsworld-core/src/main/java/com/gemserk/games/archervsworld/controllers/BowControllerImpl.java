package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.input.LibgdxPointer;

public class BowControllerImpl implements BowController {
	
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
	
	private LibgdxPointer pointer;
	
	public BowControllerImpl(LibgdxPointer pointer) {
		this.pointer = pointer;
	}
	
	@Override
	public void update(int delta) {
		
		firing = false;
		
		if (pointer.touched) {
			Vector2 p0 = pointer.getPressedPosition();
			Vector2 p1 = pointer.getPosition();
			
			Vector2 direction = p0.cpy().sub(p1);
			
			// the power multiplier
			float multiplier = 3f;
			
			angle = direction.angle();
			power = direction.len() * multiplier; 
			
			charging = true;
		} 
		
		if (pointer.wasReleased) {
			charging = false;
			firing = true;
		}
		
	}
	
}