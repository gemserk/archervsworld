package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.gemserk.commons.artemis.components.Contact;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.games.archervsworld.artemis.components.CorrectArrowDirectionComponent;

public class CorrectArrowDirectionSystem extends EntitySystem {

	@SuppressWarnings("unchecked")
	public CorrectArrowDirectionSystem() {
		super(CorrectArrowDirectionComponent.class);
	}

	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {
		
		for (int i = 0; i < entities.size(); i++) {

			Entity entity = entities.get(i);
			
			CorrectArrowDirectionComponent correctArrowDirectionComponent = entity.getComponent(CorrectArrowDirectionComponent.class);
			
			if (correctArrowDirectionComponent == null) {
				// shouldnt be happening
				continue;
			}
			
			if (correctArrowDirectionComponent.isDisabled())
				continue;

			PhysicsComponent physicsComponent = entity.getComponent(PhysicsComponent.class);
			Body body = physicsComponent.getBody();

			Contact contact = physicsComponent.getContact();

			if (!contact.isInContact()) {

				Vector2 linearVelocity = body.getLinearVelocity();
				float angle = linearVelocity.angle();
				body.setTransform(body.getPosition(), (float) (angle / 180f * Math.PI));

				continue;
			} else {
				// once the arrow collides with something the direction correction should be disabled
				correctArrowDirectionComponent.setDisabled(true);
			}

		}

	}
	
	@Override
	protected void removed(Entity e) {
		CorrectArrowDirectionComponent c = e.getComponent(CorrectArrowDirectionComponent.class);
		if (c != null)
			e.removeComponent(c);
	}

	@Override
	public void initialize() {

	}

	@Override
	protected boolean checkProcessing() {
		return true;
	}
}