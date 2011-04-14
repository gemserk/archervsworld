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
import com.gemserk.componentsengine.properties.Property;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.games.archervsworld.artemis.components.BowComponent;
import com.gemserk.games.archervsworld.artemis.components.PhysicsComponent;
import com.gemserk.games.archervsworld.artemis.components.WalkingDeadComponent;
import com.gemserk.games.archervsworld.box2d.CollisionDefinitions;
import com.gemserk.games.archervsworld.properties.Box2dAngleProperty;
import com.gemserk.games.archervsworld.properties.Box2dPositionProperty;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

public class ArcherVsWorldEntityFactory {

	World world;

	com.badlogic.gdx.physics.box2d.World physicsWorld;

	public void setWorld(World world) {
		this.world = world;
	}

	public void setPhysicsWorld(com.badlogic.gdx.physics.box2d.World physicsWorld) {
		this.physicsWorld = physicsWorld;
	}

	ResourceManager<String> resourceManager;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	private MassData massData = new MassData();

	public void createPhysicsArrow(Vector2 position, Vector2 direction, float power) {

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.bullet = true;
		bodyDef.position.set(position);

		Body body = physicsWorld.createBody(bodyDef);

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

		// 0.7112 mts

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

		Entity entity = world.createEntity();
		
		entity.setGroup(Groups.Arrow);

		body.setUserData(entity);

		entity.addComponent(new PhysicsComponent(new SimpleProperty<Body>(body)));
		
		entity.addComponent(new SpatialComponent( //
				new Box2dPositionProperty(body), //
				new SimpleProperty<Vector2>(new Vector2(1f, 1f)), //
				new Box2dAngleProperty(body)));

		Resource<Texture> resource = resourceManager.get("Arrow");
		Texture texture = resource.get();

		entity.addComponent(new SpriteComponent(new SimpleProperty<Sprite>(new Sprite(texture)), new SimpleProperty<IntValue>(new IntValue(1))));

		entity.refresh();
	}

	public Entity createArrow(Vector2 position, float angle) {
		SimpleProperty<Vector2> positionProperty = new SimpleProperty<Vector2>(position);
		SimpleProperty<FloatValue> angleProperty = new SimpleProperty<FloatValue>(new FloatValue(angle));
		return createArrow(positionProperty, angleProperty);
	}

	public Entity createArrow(Property<Vector2> positionProperty, Property<FloatValue> angleProperty) {
		Entity entity = world.createEntity();

		Resource<Texture> resource = resourceManager.get("Arrow");
		Texture texture = resource.get();

		entity.addComponent(new SpatialComponent( //
				positionProperty, //
				new SimpleProperty<Vector2>(new Vector2(1f, 1f)), //
				angleProperty));
		entity.addComponent(new SpriteComponent( //
				new SimpleProperty<Sprite>(new Sprite(texture)), //
				new SimpleProperty<IntValue>(new IntValue(1))));

		entity.refresh();
		return entity;
	}

	public void createBow(Vector2 position) {

		Entity entity = world.createEntity();

		entity.setGroup(Groups.Bow);

		int layer = 2;

		float bowHeight = 1.6f;
		float bowWidth = 1.6f;

		Resource<Texture> resource = resourceManager.get("Bow");
		Texture texture = resource.get();

		entity.addComponent(new SpatialComponent( //
				new SimpleProperty<Vector2>(position), //
				new SimpleProperty<Vector2>(new Vector2(bowWidth, bowHeight)), //
				new SimpleProperty<FloatValue>(new FloatValue(0f))));
		entity.addComponent(new SpriteComponent(new SimpleProperty<Sprite>(new Sprite(texture)), //
				new SimpleProperty<IntValue>(new IntValue(layer)), //
				new SimpleProperty<Vector2>(new Vector2(0.5f, 0.5f))));

		entity.addComponent(new BowComponent( //
				new SimpleProperty<FloatValue>(new FloatValue(0f)), //
				new SimpleProperty<Entity>(null),//
				new SimpleProperty<FloatValue>(new FloatValue(5f)), //
				new SimpleProperty<FloatValue>(new FloatValue(35f))));

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
		shape.setAsBox(size.x / 3f, size.y / 3f);

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
		
		body.setUserData(entity);

		Resource<Texture> resource = resourceManager.get("Rock");
		Texture texture = resource.get();

		entity.addComponent(new PhysicsComponent(new SimpleProperty<Body>(body)));
		
		entity.addComponent(new SpatialComponent( //
				new Box2dPositionProperty(body), //
				new SimpleProperty<Vector2>(size), //
				new Box2dAngleProperty(body)));
		entity.addComponent(new SpriteComponent(new SimpleProperty<Sprite>(new Sprite(texture)), new SimpleProperty<IntValue>(new IntValue(1))));

		entity.refresh();

	}

	public void createTree(Vector2 position, Vector2 size) {

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.set(position);
		bodyDef.angularDamping = 1f;
		bodyDef.linearDamping = 1f;

		Body body = physicsWorld.createBody(bodyDef);

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(size.x * 0.1f * 0.125f, size.y / 2f - 0.1f);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = 1f;
		fixtureDef.friction = 1f;
		fixtureDef.shape = shape;

		fixtureDef.filter.categoryBits = CollisionDefinitions.RockGroup;
		fixtureDef.filter.maskBits = CollisionDefinitions.All;

		body.createFixture(fixtureDef);
		shape.dispose();

		Entity entity = world.createEntity();
		
		body.setUserData(entity);

		Resource<Texture> resource = resourceManager.get("Tree");
		Texture texture = resource.get();

		entity.addComponent(new PhysicsComponent(new SimpleProperty<Body>(body)));
		
		entity.addComponent(new SpatialComponent( //
				new Box2dPositionProperty(body), //
				new SimpleProperty<Vector2>(size), //
				new Box2dAngleProperty(body)));
		entity.addComponent(new SpriteComponent(new SimpleProperty<Sprite>(new Sprite(texture)), new SimpleProperty<IntValue>(new IntValue(1))));

		entity.refresh();

	}

	public void createWalkingDead(Vector2 position, Vector2 size, Vector2 velocity) {

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(position);
		bodyDef.fixedRotation = true;
		// bodyDef.angularDamping = 1f;
		// bodyDef.linearDamping = 1f;

		Body body = physicsWorld.createBody(bodyDef);

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(size.x * 0.5f, size.y * 0.5f - 0.1f);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = 1f;
		// fixtureDef.friction = 0.5f;
		fixtureDef.shape = shape;

		fixtureDef.filter.categoryBits = CollisionDefinitions.EnemiesGroup;
		fixtureDef.filter.maskBits = CollisionDefinitions.All & ~CollisionDefinitions.EnemiesGroup;

		body.createFixture(fixtureDef);

		shape.dispose();

		Entity entity = world.createEntity();
		
		body.setUserData(entity);

		Resource<Texture> resource = resourceManager.get("Rock");
		Texture texture = resource.get();

		entity.addComponent(new PhysicsComponent(new SimpleProperty<Body>(body)));
		
		entity.addComponent(new SpatialComponent( //
				new Box2dPositionProperty(body), //
				new SimpleProperty<Vector2>(size), //
				new Box2dAngleProperty(body)));
		entity.addComponent(new SpriteComponent(new SimpleProperty<Sprite>(new Sprite(texture)), new SimpleProperty<IntValue>(new IntValue(1))));
		entity.addComponent(new WalkingDeadComponent( //
				new SimpleProperty<Vector2>(null), //
				new SimpleProperty<Vector2>(velocity), //
				new SimpleProperty<IntValue>(new IntValue(0)), //
				new SimpleProperty<IntValue>(new IntValue(1000)), new SimpleProperty<IntValue>(new IntValue(2000))));

		entity.refresh();

	}

}
