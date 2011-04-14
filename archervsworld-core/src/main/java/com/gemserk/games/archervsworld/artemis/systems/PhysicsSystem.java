package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.World;
import com.gemserk.games.archervsworld.artemis.components.CollisionComponent;
import com.gemserk.games.archervsworld.artemis.components.PhysicsComponent;

public class PhysicsSystem extends EntitySystem {

	static class PhysicsContactListener implements ContactListener {
		@Override
		public void endContact(Contact contact) {

			Body bodyA = contact.getFixtureA().getBody();
			Body bodyB = contact.getFixtureB().getBody();
			
			Entity entityA = (Entity) bodyA.getUserData();
			Entity entityB = (Entity) bodyB.getUserData();
			
			if (entityA != null) {
				CollisionComponent collisionComponent = entityA.getComponent(CollisionComponent.class);
				collisionComponent.getContact().removeBox2dContact();
				collisionComponent.setEntity(null);
			}

			if (entityB != null) {
				CollisionComponent collisionComponent = entityB.getComponent(CollisionComponent.class);
				collisionComponent.getContact().removeBox2dContact();
				collisionComponent.setEntity(null);
			}
			
		}

		@Override
		public void beginContact(Contact contact) {

			Body bodyA = contact.getFixtureA().getBody();
			Body bodyB = contact.getFixtureB().getBody();
			
			Entity entityA = (Entity) bodyA.getUserData();
			Entity entityB = (Entity) bodyB.getUserData();
			
			if (entityA != null) {
				CollisionComponent collisionComponent = entityA.getComponent(CollisionComponent.class);
				collisionComponent.getContact().setBox2dContact(contact);
				collisionComponent.setEntity(entityB);
			}

			if (entityB != null) {
				CollisionComponent collisionComponent = entityB.getComponent(CollisionComponent.class);
				collisionComponent.getContact().setBox2dContact(contact);
				collisionComponent.setEntity(entityA);
			}

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
		physicsWorld.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);
	}

	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {
//		for (int i = 0; i < entities.size(); i++) {
//
//			Entity entity = entities.get(i);
//			PhysicsComponent physicsComponent = entity.getComponent(PhysicsComponent.class);
//
//			Body body = physicsComponent.getBody();
//
//		}
	}

	@Override
	protected boolean checkProcessing() {
		return true;
	}
	
	@Override
	protected void removed(Entity e) {
		
		// on entity being removed, should remove body from physics world
		
		PhysicsComponent component = e.getComponent(PhysicsComponent.class);
		
		Body body = component.getBody();
		body.setUserData(null);
		
		physicsWorld.destroyBody(body);
		
	}

	@Override
	public void initialize() {

	}

	public World getPhysicsWorld() {
		return physicsWorld;
	}

}
