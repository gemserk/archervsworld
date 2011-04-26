package com.gemserk.games.archervsworld.controllers;

import com.gemserk.commons.gdx.controllers.Controller;

public interface BowController extends Controller {

	float getAngle();

	float getPower();

	boolean isCharging();

	boolean shouldFire();

}