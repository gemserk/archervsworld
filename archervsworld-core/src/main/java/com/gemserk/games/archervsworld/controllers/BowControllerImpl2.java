package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.input.LibgdxPointer;

public class BowControllerImpl2 implements BowController {
	
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

	private final Vector2 source;
	
	public BowControllerImpl2(LibgdxPointer pointer, Vector2 source) {
		this.pointer = pointer;
		this.source = source;
	}
	
	@Override
	public void update() {
		
		firing = false;
		
		if (pointer.wasPressed) {
			Vector2 p0 = pointer.getPressedPosition();
			Vector2 p1 = source;
			
			Vector2 direction = p0.cpy().sub(p1);
			
			angle = direction.angle();
			power = direction.len(); 
			
			charging = true;
			firing = true;
		} 
		
	}
	
}