package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;

public class ArrowPhysicsBehavior extends PhysicsBehavior {

	private final World world;

	private final Entity entity;

	private final ArcherVsWorldEntityFactory archerVsWorldEntityFactory;

	private boolean shouldRemove = false;

	private boolean shouldProcess = true;

	public ArrowPhysicsBehavior(World world, Entity entity, ArcherVsWorldEntityFactory archerVsWorldEntityFactory) {
		this.world = world;
		this.entity = entity;
		this.archerVsWorldEntityFactory = archerVsWorldEntityFactory;
	}

	@Override
	public void update(com.badlogic.gdx.physics.box2d.World world, Body body) {

		if (!body.isAwake()) {
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
		shouldRemove = true;
		shouldProcess = false;

		// Fixture fixture = body.getFixtureList().get(0);
		//
		// Filter filterData = fixture.getFilterData();
		//
		// filterData.categoryBits = CollisionDefinitions.EnemiesGroup;
		// filterData.maskBits = CollisionDefinitions.All & ~CollisionDefinitions.EnemiesGroup;
		//
		// fixture.setFilterData(filterData);

		// if (!shouldProcess)
		// return;
		//
		// shouldProcess = false;
		//
		// int numberOfContactPoints = contact.GetWorldManifold().getNumberOfContactPoints();
		// if (numberOfContactPoints <= 0)
		// return;
		//
		// System.out.println("contact points!");
		//
		// Vector2 contactPosition = contact.GetWorldManifold().getPoints()[0];
		//
		// Vector2 diff = contactPosition.cpy().sub(body.getTransform().getPosition());
		// diff.nor();
		//
		// System.out.println(body.getTransform().getPosition());
		// System.out.println(contactPosition);
		//
		// System.out.println(diff);
		//
		// float arrowAngle = (float) (body.getAngle() * 180 / Math.PI);
		// // float angle = contactPosition.cpy().sub(body.getTransform().getPosition()).angle();
		// float angle = diff.angle();
		//
		// float angle2 = contact.GetWorldManifold().getNormal().angle();
		//
		// System.out.println(angle2);
		//
		// if (angle2 < 25) {
		// // shouldRemove = false;
		// body.setType(BodyType.StaticBody);
		// System.out.println("menor a 45");
		// } else {
		// System.out.println("mayor a 45");
		// // body.setType(BodyType.StaticBody);
		//
		// }

	}

	@Override
	public void endContact(Body body, Contact contact) {

	}

}