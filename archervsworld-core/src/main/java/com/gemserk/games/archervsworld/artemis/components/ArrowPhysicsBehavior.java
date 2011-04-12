package com.gemserk.games.archervsworld.artemis.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;

public class ArrowPhysicsBehavior extends PhysicsBehavior {

	boolean shouldProcess = true;

	@Override
	public void update(com.badlogic.gdx.physics.box2d.World world, Body body) {
		if (!shouldProcess)
			return;
		Vector2 linearVelocity = body.getLinearVelocity();
		float angle = linearVelocity.angle();
		body.setTransform(body.getPosition(), (float) (angle / 180f * Math.PI));
	}

	@Override
	public void beginContact(Contact contact) {
		shouldProcess = false;
	}

	@Override
	public void endContact(Contact contact) {

	}

}