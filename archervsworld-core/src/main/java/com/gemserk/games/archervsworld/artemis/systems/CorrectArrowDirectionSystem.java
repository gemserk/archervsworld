package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.gdx.box2d.Contact;
import com.gemserk.games.archervsworld.artemis.components.CorrectArrowDirectionComponent;

public class CorrectArrowDirectionSystem extends EntityProcessingSystem {

	public CorrectArrowDirectionSystem() {
		super(CorrectArrowDirectionComponent.class);
	}

	@Override
	protected void process(Entity entity) {
		
		CorrectArrowDirectionComponent correctArrowDirectionComponent = entity.getComponent(CorrectArrowDirectionComponent.class);

		if (correctArrowDirectionComponent.isDisabled())
			return;

		PhysicsComponent physicsComponent = entity.getComponent(PhysicsComponent.class);
		Body body = physicsComponent.getBody();
		Contact contact = physicsComponent.getContact();

		if (contact.isInContact()) {
			correctArrowDirectionComponent.setDisabled(true);
			return;
		}

		Vector2 linearVelocity = body.getLinearVelocity();
		float angle = linearVelocity.angle();
		body.setTransform(body.getPosition(), (float) (angle * MathUtils.degreesToRadians));
	}

}