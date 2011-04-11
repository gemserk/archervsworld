package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.World;

public class PhysicsSystem extends EntitySystem {

	World physicsWorld;
	
	public PhysicsSystem(World physicsWorld) {
		this.physicsWorld = physicsWorld;
	}
	
	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {
		physicsWorld.step(Gdx.app.getGraphics().getDeltaTime(), 7, 7);
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
