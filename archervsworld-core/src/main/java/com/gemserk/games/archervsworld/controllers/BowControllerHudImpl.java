package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.input.LibgdxPointer;

public class BowControllerHudImpl implements BowController {
	
	private final LibgdxPointer pointer;

	private final Vector2 position;

	private final float radius;
	
	private float angle;
	
	private float power = 0f;
	
	private boolean charging;
	
	private boolean firing;

	public float getRadius() {
		return radius;
	}
	
	public Vector2 getPosition() {
		return position;
	}
	
	public BowControllerHudImpl(LibgdxPointer pointer, Vector2 position, float radius) {
		this.pointer = pointer;
		this.position = position;
		this.radius = radius;
	}
	
	@Override
	public void update(int delta) {
		
		firing = false;

		Vector2 p0 = position;
		Vector2 p1 = pointer.getPosition();
		
		if (p0.x> p1.x) {
			Vector2 tmp = p0;
			p0 = p1;
			p1 = tmp;
		}
		
		Vector2 direction = p1.cpy().sub(p0);
		
		angle = direction.angle();
		
		if (pointer.touched) {

			if (direction.len() > radius)
				return;

			// the power multiplier
			float multiplier = 0.5f;
			
			power = direction.len() * multiplier;
			
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