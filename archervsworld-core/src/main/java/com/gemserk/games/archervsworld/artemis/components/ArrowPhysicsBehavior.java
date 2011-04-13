package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;

public class ArrowPhysicsBehavior extends PhysicsBehavior {

	private final World world;

	private final Entity entity;

	private final ArcherVsWorldEntityFactory archerVsWorldEntityFactory;

	private boolean shouldProcess = true;

	public ArrowPhysicsBehavior(World world, Entity entity, ArcherVsWorldEntityFactory archerVsWorldEntityFactory) {
		this.world = world;
		this.entity = entity;
		this.archerVsWorldEntityFactory = archerVsWorldEntityFactory;
	}

	@Override
	public void update(com.badlogic.gdx.physics.box2d.World world, Body body) {

		if (!body.isAwake() || body.getType() == BodyType.StaticBody) {
			this.world.deleteEntity(entity);
			SpatialComponent component = entity.getComponent(SpatialComponent.class);
			archerVsWorldEntityFactory.createArrow(component.getPosition(), component.getAngle());
			return;
		}

		if (!shouldProcess)
			return;

		Vector2 linearVelocity = body.getLinearVelocity();
		float angle = linearVelocity.angle();
		body.setTransform(body.getPosition(), (float) (angle / 180f * Math.PI));
	}

	AngleUtils angleUtils = new AngleUtils();

	@Override
	public void beginContact(Body body, Contact contact) {
		shouldProcess = false;
		
		Vector2 normal = contact.GetWorldManifold().getNormal();
		
		float normalAngle = normal.cpy().mul(-1f).angle();
		
		float bodyAngle = (float) (body.getAngle() * 180.0 / Math.PI);
		
		double diff = Math.abs(angleUtils.minimumDifference(normalAngle, bodyAngle));
		
		int stickAngle = 45;
		
		if (diff < stickAngle)  {
			body.setType(BodyType.StaticBody);
		}
		
		// depend on the other body when defining whether to stuck or to do another thing...

	}

	@Override
	public void endContact(Body body, Contact contact) {

	}

}