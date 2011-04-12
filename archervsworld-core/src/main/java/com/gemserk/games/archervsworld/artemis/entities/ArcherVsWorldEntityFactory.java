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
import com.gemserk.commons.values.FloatValue;
import com.gemserk.commons.values.IntValue;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.games.archervsworld.artemis.components.ArrowPhysicsBehavior;
import com.gemserk.games.archervsworld.artemis.components.PhysicsBehavior;
import com.gemserk.games.archervsworld.artemis.components.PhysicsComponent;
import com.gemserk.games.archervsworld.box2d.CollisionDefinitions;
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

	Texture bowTexture;

	public void setBowTexture(Texture bowTexture) {
		this.bowTexture = bowTexture;
	}
	
	Texture rockTexture;
	
	public void setRockTexture(Texture rockTexture) {
		this.rockTexture = rockTexture;
	}

	public void createArrow(Vector2 position, Vector2 direction, float power) {

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.bullet = true;
		bodyDef.position.set(position);

		Body body = physicsWorld.createBody(bodyDef);

		// body.SetMassFromShapes();

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(0.72f / 2f, 0.05f / 2f);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1f;
		fixtureDef.friction = 0.8f;
		
		fixtureDef.filter.categoryBits = CollisionDefinitions.ArrowGroup;
		fixtureDef.filter.maskBits = CollisionDefinitions.All & ~CollisionDefinitions.ArrowGroup;

		body.createFixture(fixtureDef);
		
		shape.dispose();

		// 0.7112 meters

		MassData massData = new MassData();

		massData.center.set(0.35f / 2f, 0f);
		massData.I = 0f;
		massData.mass = 0.8f;

		body.setMassData(massData);
		body.resetMassData();

		body.setTransform(position, (float) (direction.angle() / 180f * Math.PI));

		Vector2 impulse = new Vector2(direction);
		impulse.mul(body.getMass());
		impulse.mul(power);

		Vector2 lp = body.getWorldPoint(new Vector2(0f, 0f));
		body.applyLinearImpulse(impulse, lp);

		PhysicsBehavior arrowBehavior = new ArrowPhysicsBehavior();

		body.setUserData(arrowBehavior);

		Entity entity = world.createEntity();

		entity.addComponent(new PhysicsComponent(new SimpleProperty<Body>(body), new SimpleProperty<PhysicsBehavior>(arrowBehavior)));

		entity.addComponent(new SpatialComponent( //
				new Box2dPositionProperty(body), //
				new SimpleProperty<Vector2>(new Vector2(1f, 1f)), //
				new Box2dAngleProperty(body)));
		entity.addComponent(new SpriteComponent(new SimpleProperty<Sprite>(new Sprite(arrowTexture)), new SimpleProperty<IntValue>(new IntValue(1))));

		entity.refresh();

	}

	public void createBow(Vector2 position) {

		Entity entity = world.createEntity();

		entity.setGroup(Groups.Bow);

		int layer = 2;

		float bowHeight = 1.6f;
		float bowWidth = 1.6f;

		entity.addComponent(new SpatialComponent( //
				new SimpleProperty<Vector2>(position), //
				new SimpleProperty<Vector2>(new Vector2(bowWidth, bowHeight)), //
				new SimpleProperty<FloatValue>(new FloatValue(0f))));
		entity.addComponent(new SpriteComponent(new SimpleProperty<Sprite>(new Sprite(bowTexture)), //
				new SimpleProperty<IntValue>(new IntValue(layer)), //
				new SimpleProperty<Vector2>(new Vector2(0.5f, 0.5f))));

		// entity.addComponent(new BowComponent(
		// new SimpleProperty<FloatValue>(new FloatValue(0f)),
		// new SimpleProperty<BooleanValue>(new BooleanValue(false))));

		entity.refresh();

	}

	public void createRock(Vector2 position, Vector2 size, Vector2 startImpulse, float angle) {

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(position);
		bodyDef.angularDamping = 1f;
		bodyDef.linearDamping = 1f;

		Body body = physicsWorld.createBody(bodyDef);

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(size.x / 4f, size.y / 4f);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = 1f;
		fixtureDef.friction = 1f;
		fixtureDef.shape = shape;
		
		fixtureDef.filter.categoryBits = CollisionDefinitions.RockGroup;
		fixtureDef.filter.maskBits = CollisionDefinitions.All;

		body.createFixture(fixtureDef);
		shape.dispose();

		body.setTransform(position, (float) (angle / 180f * Math.PI));
		
		body.applyLinearImpulse(startImpulse, body.getTransform().getPosition());

		Entity entity = world.createEntity();

		entity.addComponent(new PhysicsComponent(new SimpleProperty<Body>(body), new SimpleProperty<PhysicsBehavior>(new PhysicsBehavior())));
		entity.addComponent(new SpatialComponent( //
				new Box2dPositionProperty(body), //
				new SimpleProperty<Vector2>(size), //
				new Box2dAngleProperty(body)));
		entity.addComponent(new SpriteComponent(new SimpleProperty<Sprite>(new Sprite(rockTexture)), new SimpleProperty<IntValue>(new IntValue(1))));

		entity.refresh();
		
	}
	
}
