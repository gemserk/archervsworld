package com.gemserk.games.archervsworld.artemis.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
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
	public void beginContact(Body body, Contact contact) {
		shouldProcess = false;
		// if the other object is a static object like a rock, then dont make it static body
		body.setType(BodyType.StaticBody);
	}

	@Override
	public void endContact(Body body, Contact contact) {

	}

}