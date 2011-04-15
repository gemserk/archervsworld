package com.gemserk.games.archervsworld.controllers;

public interface BowController {

	float getAngle();

	float getPower();

	boolean isCharging();

	boolean shouldFire();

	void update();

}