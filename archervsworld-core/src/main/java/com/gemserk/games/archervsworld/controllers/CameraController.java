package com.gemserk.games.archervsworld.controllers;

import com.badlogic.gdx.math.Vector2;

public interface CameraController extends Controller {

	Vector2 getPosition();

	float getZoom();

}