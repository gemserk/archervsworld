package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.input.LibgdxPointer;

public class BowControllerHudImpl2 implements BowController {

	private final LibgdxPointer pointer;

	private final Vector2 position;

	private final float radius;

	private final BowData bowData;

	@Override
	public BowData getBowData() {
		return bowData;
	}

	public float getRadius() {
		return radius;
	}

	public Vector2 getPosition() {
		return position;
	}
	
	@Override
	public boolean wasHandled() {
		return false;
	}

	public BowControllerHudImpl2(LibgdxPointer pointer, Vector2 position, float radius, BowData bowData) {
		this.pointer = pointer;
		this.position = position;
		this.radius = radius;
		this.bowData = bowData;
	}

	@Override
	public void update(int delta) {
		Vector2 p0 = position;
		Vector2 p1 = pointer.getPosition();

		if (p0.x > p1.x) {
			Vector2 tmp = p0;
			p0 = p1;
			p1 = tmp;
		}

		Vector2 direction = p1.cpy().sub(p0);

		bowData.setAngle(direction.angle());

		if (pointer.touched) {
			if (direction.len() > radius)
				return;
			bowData.setPower(bowData.getPower() + 0.03f * delta);
			bowData.charge();
		}

		if (pointer.wasReleased) {
			bowData.fire();
			bowData.setPower(0f);
		}
	}

}