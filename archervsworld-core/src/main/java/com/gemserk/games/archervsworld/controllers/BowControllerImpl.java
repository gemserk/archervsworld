package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.input.LibgdxPointer;

public class BowControllerImpl implements BowController {
	
	private float angle;
	
	private float power;
	
	private boolean charging;
	
	private boolean firing;
	
	private LibgdxPointer pointer;
	
	@Override
	public boolean wasHandled() {
		return false;
	}
	
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
	

	@Override
	public BowData getBowData() {
		// TODO Auto-generated function stub
		return null;
		
	}
	
}