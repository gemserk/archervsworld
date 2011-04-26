package com.gemserk.games.archervsworld.controllers;

public interface BowController extends Controller {

	float getAngle();

	float getPower();

	boolean isCharging();

	boolean shouldFire();

}