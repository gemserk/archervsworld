package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.input.LibgdxPointer;

public class BowControllerImpl5 implements BowController {
	
	private float angle;
	
	private float power = 0f;
	
	private boolean charging;
	
	private boolean firing;
	
	private LibgdxPointer pointer;

	private Vector2 source;
	
	public BowControllerImpl5(LibgdxPointer pointer, Vector2 source) {
		this.pointer = pointer;
		this.source = source;
	}
	
	@Override
	public boolean wasHandled() {
		return false;
	}
	
	@Override
	public void update(int delta) {
		
		firing = false;

		Vector2 p0 = source;
		Vector2 p1 = pointer.getPosition();
		
		Vector2 direction = p1.cpy().sub(p0);
		
		angle = direction.angle();

		if (pointer.touched) {
			// the power multiplier
			float multiplier = 0.3f;
			
			power += 1f * multiplier;
			
			charging = true;
		} 
		
		if (pointer.wasReleased) {
			charging = false;
			firing = true;
			
			power = 0f;
		}
		
	}
	

	@Override
	public BowData getBowData() {
		// TODO Auto-generated function stub
		return null;
		
	}
	
}