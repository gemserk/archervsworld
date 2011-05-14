package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.input.LibgdxPointer;

public class BowControllerMutitouchImpl implements BowController {

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

	private LibgdxPointer pointer0;

	private LibgdxPointer pointer1;

	public BowControllerMutitouchImpl(LibgdxPointer pointer0, LibgdxPointer pointer1) {
		this.pointer0 = pointer0;
		this.pointer1 = pointer1;
	}

	@Override
	public void update(int delta) {

		firing = false;
		
		if (pointer0.touched && pointer1.touched) {

			Vector2 p0 = pointer0.getPosition();
			Vector2 p1 = pointer1.getPosition();

			Vector2 direction;
			
			if (p0.x > p1.x)
				direction = p0.cpy().sub(p1);
			else
				direction = p1.cpy().sub(p0);

			// the power multiplier
			float multiplier = 3f;

			angle = direction.angle();
			power = direction.len() * multiplier;

			charging = true;
		} else {
		
			if (charging) {
				charging = false;
				firing = true;
			}
			
		}

	}
	
	@Override
	public BowData getBowData() {
		// TODO Auto-generated function stub
		return null;
		
	}

}