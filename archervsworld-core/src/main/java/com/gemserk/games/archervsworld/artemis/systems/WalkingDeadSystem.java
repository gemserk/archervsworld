package com.gemserk.games.archervsworld.artemis.systems;

import java.util.Random;

import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.gemserk.commons.artemis.EntityDebugger;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.systems.ActivableSystem;
import com.gemserk.commons.artemis.systems.ActivableSystemImpl;
import com.gemserk.games.archervsworld.artemis.components.WalkingDeadComponent;

public class WalkingDeadSystem extends EntityProcessingSystem implements ActivableSystem {
	
	private static final Random random = new Random();

	public WalkingDeadSystem() {
		super(WalkingDeadComponent.class);
	}

	@Override
	protected void process(Entity e) {
		WalkingDeadComponent walkingDeadComponent = e.getComponent(WalkingDeadComponent.class);
		
		if (walkingDeadComponent == null) {
			EntityDebugger.debug("walking dead component missing in walking dead", e);
			return;
		}
		
		int walkSleep = walkingDeadComponent.getWalkSleep();
		walkSleep -= world.getDelta();
		
		walkingDeadComponent.setWalkSleep(walkSleep);
		
		if (walkSleep > 0) 
			return;
		
		PhysicsComponent physicsComponent = e.getComponent(PhysicsComponent.class);

		Body body = physicsComponent.getBody();

		Vector2 force = walkingDeadComponent.getForce();
		Vector2 position = body.getTransform().getPosition();

		body.applyLinearImpulse(force, position);
		// body.applyForce(force, position);

		int minSleepTime = walkingDeadComponent.getMinSleepTime();
		int maxSleepTime = walkingDeadComponent.getMaxSleepTime();
		
		int sleepTime = random.nextInt(maxSleepTime - minSleepTime) + minSleepTime;
		
		walkingDeadComponent.setWalkSleep(sleepTime);
	}
	
	@Override
	public void initialize() {

	}

	@Override
	protected boolean checkProcessing() {
		return isEnabled();
	}
	
	// implementation of activable system.
	
	ActivableSystemImpl activableSystem = new ActivableSystemImpl();

	public boolean isEnabled() {
		return activableSystem.isEnabled();
	}

	public void toggle() {
		activableSystem.toggle();
	}


	
}