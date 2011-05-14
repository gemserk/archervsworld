package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.controllers.CameraController;
import com.gemserk.commons.gdx.input.LibgdxPointer;
import com.gemserk.commons.gdx.math.MathUtils2;

public class MultitouchCameraControllerImpl implements CameraController {

	private final Vector2 previousPosition = new Vector2();

	private final LibgdxPointer libgdxPointer0;

	private final LibgdxPointer libgdxPointer1;

	private final Vector2 tmp = new Vector2();

	private final Rectangle area;

	private boolean inside = true;

	private Camera camera;

	private float maxZoom;

	private float minZoom;

	private float lastDistance = 0f;

	boolean zooming = false;

	@Override
	public Camera getCamera() {
		return camera;
	}

	public MultitouchCameraControllerImpl(Camera camera, LibgdxPointer libgdxPointer0, LibgdxPointer libgdxPointer1, Rectangle area) {
		this.camera = camera;
		this.area = area;
		this.maxZoom = camera.getZoom() * 1.5f;
		this.minZoom = camera.getZoom() * 0.5f;
		this.libgdxPointer0 = libgdxPointer0;
		this.libgdxPointer1 = libgdxPointer1;
	}

	@Override
	public void update(int delta) {

		// libgdxPointer0.update();
		// libgdxPointer1.update();

		zooming = false;

		if (libgdxPointer0.touched && libgdxPointer1.touched) {
			zooming = true;
			// zooming
			if (libgdxPointer0.wasPressed)
				lastDistance = libgdxPointer0.getPressedPosition().dst(libgdxPointer1.getPosition());
			else if (libgdxPointer1.wasPressed)
				lastDistance = libgdxPointer1.getPressedPosition().dst(libgdxPointer0.getPosition());

			float currentDistance = libgdxPointer1.getPosition().dst(libgdxPointer0.getPosition());
			float difference = currentDistance - lastDistance;
			camera.setZoom(camera.getZoom() + difference * 0.1f);
			lastDistance = currentDistance;
			camera.setZoom(MathUtils2.truncate(camera.getZoom(), minZoom, maxZoom));
		}

		if (zooming)
			return;

		// moving
		LibgdxPointer libgdxPointer = libgdxPointer0;

		if (!libgdxPointer.touched)
			libgdxPointer = libgdxPointer1;

		if (!libgdxPointer.touched)
			return;

		if (libgdxPointer.wasPressed) {
			previousPosition.set(libgdxPointer.getPressedPosition());
			// inside = MathUtils2.inside(area, previousPosition);
		}

		// if (!inside)
		// return;

		Vector2 pointerPosition = libgdxPointer.getPosition();

		tmp.set(previousPosition);
		tmp.sub(pointerPosition);
		tmp.mul(1f / camera.getZoom());

		camera.setPosition(camera.getX() + tmp.x, camera.getY() + tmp.y);
		previousPosition.set(pointerPosition);

	}

}