package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.gemserk.games.archervsworld.artemis.components.ArrowComponent;
import com.gemserk.games.archervsworld.artemis.components.PhysicsComponent;
import com.gemserk.games.archervsworld.box2d.Contact;

public class ArrowDirectionSystem extends EntitySystem {

	@SuppressWarnings("unchecked")
	public ArrowDirectionSystem() {
		super(ArrowComponent.class);
	}

	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {
		
		for (int i = 0; i < entities.size(); i++) {

			Entity entity = entities.get(i);
			
			ArrowComponent arrowComponent = entity.getComponent(ArrowComponent.class);
			
			if (arrowComponent.isDisabled())
				continue;

			PhysicsComponent physicsComponent = entity.getComponent(PhysicsComponent.class);
			Body body = physicsComponent.getBody();

			Contact contact = physicsComponent.getContact();

			if (!contact.inContact) {

				Vector2 linearVelocity = body.getLinearVelocity();
				float angle = linearVelocity.angle();
				body.setTransform(body.getPosition(), (float) (angle / 180f * Math.PI));

				continue;
			} else {
				// once the arrow collides with something, then the direction correction should be disabled
				arrowComponent.setDisabled(true);
			}

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