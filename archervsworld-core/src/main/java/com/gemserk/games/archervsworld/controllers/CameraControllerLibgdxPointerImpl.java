package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.controllers.CameraController;
import com.gemserk.commons.gdx.input.LibgdxPointer;
import com.gemserk.commons.gdx.math.MathUtils2;

public class CameraControllerLibgdxPointerImpl implements CameraController {
	
	private final Vector2 previousPosition = new Vector2();

	private final LibgdxPointer libgdxPointer;
	
	private final Vector2 tmp = new Vector2();

	private final Rectangle area;
	
	private boolean inside = true;
	
	private Camera camera;
	
	@Override
	public Camera getCamera() {
		return camera;
	}
	
	public CameraControllerLibgdxPointerImpl(Camera camera, LibgdxPointer libgdxPointer, Rectangle area) {
		this.camera = camera;
		this.area = area;
		this.libgdxPointer = libgdxPointer;
	}
	
	@Override
	public void update(int delta) {
		
		if (libgdxPointer.wasPressed) {
			previousPosition.set(libgdxPointer.getPressedPosition());
			inside = MathUtils2.inside(area, previousPosition);
			return;
		}

		if (!libgdxPointer.touched || !inside)
			return;
		
		Vector2 pointerPosition = libgdxPointer.getPosition();

		tmp.set(previousPosition);
		tmp.sub(pointerPosition);
		tmp.mul(1f / camera.zoom);
		
		camera.position.add(tmp);
		
		previousPosition.set(pointerPosition);
	}


	
}