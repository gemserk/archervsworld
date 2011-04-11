package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.games.archervsworld.artemis.components.PhysicsBehavior;
import com.gemserk.games.archervsworld.artemis.components.PhysicsComponent;

public class PhysicsSystem extends EntitySystem {

	static class PhysicsContactListener implements ContactListener {
		@Override
		public void endContact(Contact contact) {

			Body bodyA = contact.getFixtureA().getBody();
			Body bodyB = contact.getFixtureB().getBody();

			endContactBodyUserData(contact, bodyA);
			endContactBodyUserData(contact, bodyB);

		}

		@Override
		public void beginContact(Contact contact) {

			Body bodyA = contact.getFixtureA().getBody();
			Body bodyB = contact.getFixtureB().getBody();

			beginContactBodyUserData(contact, bodyA);
			beginContactBodyUserData(contact, bodyB);

		}

		protected void beginContactBodyUserData(Contact contact, Body body) {
			Object userData = body.getUserData();

			if (userData instanceof PhysicsBehavior)
				((PhysicsBehavior) userData).beginContact(contact);
		}

		protected void endContactBodyUserData(Contact contact, Body body) {
			Object userData = body.getUserData();

			if (userData instanceof PhysicsBehavior)
				((PhysicsBehavior) userData).endContact(contact);
		}
	}

	World physicsWorld;

	@SuppressWarnings("unchecked")
	public PhysicsSystem(World physicsWorld) {
		super(PhysicsComponent.class);
		this.physicsWorld = physicsWorld;
		physicsWorld.setContactListener(new PhysicsContactListener());
	}

	@Override
	protected void begin() {
		physicsWorld.step(Gdx.app.getGraphics().getDeltaTime(), 7, 7);
	}

	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {
		for (int i = 0; i < entities.size(); i++) {

			Entity entity = entities.get(i);
			PhysicsComponent physicsComponent = entity.getComponent(PhysicsComponent.class);

			Body body = physicsComponent.getBody();
			physicsComponent.getPhysicsBehavior().update(physicsWorld, body);

		}
	}

	@Override
	protected boolean checkProcessing() {
		return true;
	}

	@Override
	public void initialize() {

	}

	public World getPhysicsWorld() {
		return physicsWorld;
	}

}
