package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.input.LibgdxPointer;

public class CameraControllerLibgdxPointerImpl implements CameraController {
	
	private final Vector2 position = new Vector2();
	
	private final Vector2 previousPosition = new Vector2();

	private final LibgdxPointer libgdxPointer;
	
	private final Vector2 tmp = new Vector2();

	private final float zoom;

	@Override
	public Vector2 getPosition() {
		return position;
	}

	@Override
	public float getZoom() {
		return zoom;
	}
	
	public CameraControllerLibgdxPointerImpl(Vector2 position, float zoom, LibgdxPointer libgdxPointer) {
		this.zoom = zoom;
		this.position.set(position);
		this.libgdxPointer = libgdxPointer;
	}
	
	@Override
	public void update(int delta) {
		
		if (libgdxPointer.wasPressed) {
			previousPosition.set(libgdxPointer.getPressedPosition());
			return;
		}
		
		if (!libgdxPointer.touched)
			return;
		
		Vector2 pointerPosition = libgdxPointer.getPosition();

		tmp.set(previousPosition);
		tmp.sub(pointerPosition);
		tmp.mul(1f / zoom);
		
		position.add(tmp);
		
		previousPosition.set(pointerPosition);
	}
	
}