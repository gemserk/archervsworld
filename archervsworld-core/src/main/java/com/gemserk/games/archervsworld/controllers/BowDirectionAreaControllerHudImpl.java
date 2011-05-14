package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.input.LibgdxPointer;

public class BowDirectionAreaControllerHudImpl extends BowDirectionControllerHudImpl {

	private final float radius;

	private boolean handled = false;

	public BowDirectionAreaControllerHudImpl(BowData bowData, LibgdxPointer pointer, Vector2 position, float radius) {
		super(bowData, pointer, position);
		this.radius = radius;
	}

	@Override
	public boolean wasHandled() {
		return handled;
	}

	@Override
	public void update(int delta) {
		handled = false;

		if (!pointer.touched)
			return;

		if (position.dst(pointer.getPosition()) > radius) 
			return;

		super.update(delta);
		
		handled = true;
	}

}
