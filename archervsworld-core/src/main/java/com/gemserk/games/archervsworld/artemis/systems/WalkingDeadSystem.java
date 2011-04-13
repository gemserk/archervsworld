package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.gemserk.games.archervsworld.artemis.components.PhysicsComponent;
import com.gemserk.games.archervsworld.artemis.components.WalkingDeadComponent;

public class WalkingDeadSystem extends EntitySystem {

	public WalkingDeadSystem() {
		super(WalkingDeadComponent.class);
	}

	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {

		for (int i = 0; i < entities.size(); i++) {

			Entity entity = entities.get(i);

			WalkingDeadComponent walkingDeadComponent = entity.getComponent(WalkingDeadComponent.class);
			
			int walkSleep = walkingDeadComponent.getWalkSleep();
			walkSleep -= world.getDelta();
			
			walkingDeadComponent.setWalkSleep(walkSleep);
			
			if (walkSleep > 0) 
				continue;
			
			
			PhysicsComponent physicsComponent = entity.getComponent(PhysicsComponent.class);

			Body body = physicsComponent.getBody();

			Vector2 force = walkingDeadComponent.getForce();
			Vector2 position = body.getTransform().getPosition();

			body.applyLinearImpulse(force, position);
			// body.applyForce(force, position);

			walkingDeadComponent.setWalkSleep(1500);
			
		}

	}

	@Override
	public void initialize() {

	}

	@Override
	protected boolean checkProcessing() {
		return true;
	}
}