package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.controllers.CameraController;

public class CameraControllerButtonMonitorImpl implements CameraController {

	private final Vector2 position = new Vector2();

	private final int left;

	private final int right;

	private final int up;

	private final int down;

	private final int zoomIn;

	private final int zoomOut;

	private final Camera camera;

	@Override
	public Camera getCamera() {
		return camera;
	}

	public CameraControllerButtonMonitorImpl(Camera camera, //
			int left, int right, int up, int down, //
			int zoomIn, int zoomOut) {
		this.left = left;
		this.right = right;
		this.up = up;
		this.down = down;
		this.zoomIn = zoomIn;
		this.zoomOut = zoomOut;
		this.camera = camera;
	}

	@Override
	public void update(int delta) {
		
		position.set(camera.getX(), camera.getY());

		if (Gdx.input.isKeyPressed(down))
			position.y -= 0.01f * delta;

		if (Gdx.input.isKeyPressed(up))
			position.y += 0.01f * delta;

		if (Gdx.input.isKeyPressed(right))
			position.x += 0.01f * delta;

		if (Gdx.input.isKeyPressed(left))
			position.x -= 0.01f * delta;

		if (Gdx.input.isKeyPressed(zoomIn)) 
			camera.setZoom(camera.getZoom() + delta * 0.05f);

		if (Gdx.input.isKeyPressed(zoomOut)) 
			camera.setZoom(camera.getZoom() - delta * 0.05f);

		camera.setPosition(position.x, position.y);
	}

}