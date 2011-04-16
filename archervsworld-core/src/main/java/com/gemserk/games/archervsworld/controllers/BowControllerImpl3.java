package com.gemserk.games.archervsworld.controllers;

import com.gemserk.commons.gdx.input.LibgdxPointer;

public class BowControllerImpl3 implements BowController {
	
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
	
	public BowControllerImpl3(LibgdxPointer pointer) {
		this.pointer = pointer;
		this.angle = 0f;
	}
	
	@Override
	public void update() {
		
		pointer.update();
		
		firing = false;
		
		if (pointer.touched) {
			
			angle = pointer.getPosition().y * 5;
			power = pointer.getPosition().x; 
			
			charging = true;
		} 
		
		if (pointer.wasReleased) {
			charging = false;
			firing = true;
		}
		
	}
	
}