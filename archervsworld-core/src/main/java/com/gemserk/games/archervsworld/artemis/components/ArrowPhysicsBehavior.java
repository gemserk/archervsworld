package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;

public class ArrowPhysicsBehavior extends PhysicsBehavior {

	boolean shouldRemove = true;
	
	private final World world;

	private final Entity entity;

	private final ArcherVsWorldEntityFactory archerVsWorldEntityFactory;
	
	public ArrowPhysicsBehavior(World world, Entity entity, ArcherVsWorldEntityFactory archerVsWorldEntityFactory) {
		this.world = world;
		this.entity = entity;
		this.archerVsWorldEntityFactory = archerVsWorldEntityFactory;
	}

	@Override
	public void update(com.badlogic.gdx.physics.box2d.World world, Body body) {
		
		if (!shouldRemove) {
			
			this.world.deleteEntity(entity);
			
			SpatialComponent component = entity.getComponent(SpatialComponent.class);
			
			archerVsWorldEntityFactory.createArrow(component.getPosition(), component.getAngle());

			return;
		}
		
		Vector2 linearVelocity = body.getLinearVelocity();
		float angle = linearVelocity.angle();
		body.setTransform(body.getPosition(), (float) (angle / 180f * Math.PI));
	}

	@Override
	public void beginContact(Body body, Contact contact) {
		shouldRemove = false;
		// if the other object is a static object like a rock, then dont make it static body
		// body.setType(BodyType.StaticBody);
	}

	@Override
	public void endContact(Body body, Contact contact) {

	}

}