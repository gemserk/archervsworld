package com.gemserk.games.archervsworld.artemis.entities;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.values.IntValue;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.games.archervsworld.artemis.components.ArrowPhysicsBehavior;
import com.gemserk.games.archervsworld.artemis.components.PhysicsBehavior;
import com.gemserk.games.archervsworld.artemis.components.PhysicsComponent;
import com.gemserk.games.archervsworld.properties.Box2dAngleProperty;
import com.gemserk.games.archervsworld.properties.Box2dPositionProperty;

public class ArcherVsWorldEntityFactory {
	
	World world;
	
	com.badlogic.gdx.physics.box2d.World physicsWorld;
	
	public void setWorld(World world) {
		this.world = world;
	}
	
	public void setPhysicsWorld(com.badlogic.gdx.physics.box2d.World physicsWorld) {
		this.physicsWorld = physicsWorld;
	}
	
	Texture arrowTexture;
	
	public void setArrowTexture(Texture arrowTexture) {
		this.arrowTexture = arrowTexture;
	}
	
	public void createArrow(Vector2 position, Vector2 direction, float power) {

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.bullet = true;
		bodyDef.position.set(position);

		final Body arrowBody = physicsWorld.createBody(bodyDef);

		// body.SetMassFromShapes();

		PolygonShape polygonShape = new PolygonShape();
		polygonShape.setAsBox(0.72f / 2f, 0.05f / 2f);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = polygonShape;
		fixtureDef.density = 1f;
		fixtureDef.friction = 0.8f;

		arrowBody.createFixture(fixtureDef);

		// 0.7112 meters

		MassData massData = new MassData();

		massData.center.set(0.35f / 2f, 0f);
		massData.I = 0f;
		massData.mass = 0.8f;

		arrowBody.setMassData(massData);
		arrowBody.resetMassData();

		arrowBody.setTransform(position, (float) (direction.angle() / 180f * Math.PI));

		Vector2 impulse = new Vector2(direction);
		impulse.mul(arrowBody.getMass());
		impulse.mul(power);

		Vector2 lp = arrowBody.getWorldPoint(new Vector2(0f, 0f));
		arrowBody.applyLinearImpulse(impulse, lp);

		PhysicsBehavior arrowBehavior = new ArrowPhysicsBehavior();

		arrowBody.setUserData(arrowBehavior);

		Entity entity = world.createEntity();

		entity.addComponent(new PhysicsComponent(new SimpleProperty<Body>(arrowBody), new SimpleProperty<PhysicsBehavior>(arrowBehavior)));

		entity.addComponent(new SpatialComponent( //
				new Box2dPositionProperty(arrowBody), //
				new SimpleProperty<Vector2>(new Vector2(1f, 1f)), //
				new Box2dAngleProperty(arrowBody)));
		entity.addComponent(new SpriteComponent(new SimpleProperty<Sprite>(new Sprite(arrowTexture)), new SimpleProperty<IntValue>(new IntValue(1))));

		entity.refresh();

	}

}
