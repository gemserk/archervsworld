package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.input.LibgdxPointer;

public class BowDirectionControllerHudImpl implements BowController {

	private final LibgdxPointer pointer;

	private final Vector2 position;

	private final BowData bowData;

	@Override
	public BowData getBowData() {
		return bowData;
	}

	public BowDirectionControllerHudImpl(BowData bowData, LibgdxPointer pointer, Vector2 position) {
		this.bowData = bowData;
		this.pointer = pointer;
		this.position = position;
	}

	@Override
	public void update(int delta) {
		Vector2 p0 = position;
		Vector2 p1 = pointer.getPosition();

		// if (p0.x > p1.x) {
		// Vector2 tmp = p0;
		// p0 = p1;
		// p1 = tmp;
		// }
		
		if (p0.x > p1.x)
			return;

		Vector2 direction = p1.cpy().sub(p0);

		bowData.setAngle(direction.angle());
	}

}