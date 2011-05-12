package com.gemserk.games.archervsworld.artemis.entities;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gemserk.animation4j.transitions.Transitions;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.components.AliveAreaComponent;
import com.gemserk.commons.artemis.components.AliveComponent;
import com.gemserk.commons.artemis.components.MovementComponent;
import com.gemserk.commons.artemis.components.ParentComponent;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpatialImpl;
import com.gemserk.commons.artemis.components.SpatialPhysicsImpl;
import com.gemserk.commons.artemis.components.SpawnerComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.triggers.AbstractTrigger;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.values.FloatValue;
import com.gemserk.commons.values.IntValue;
import com.gemserk.commons.values.ValueBuilder;
import com.gemserk.componentsengine.properties.Property;
import com.gemserk.componentsengine.properties.PropertyBuilder;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.componentsengine.timers.CountDownTimer;
import com.gemserk.componentsengine.utils.Container;
import com.gemserk.games.archervsworld.artemis.components.BowComponent;
import com.gemserk.games.archervsworld.artemis.components.CorrectArrowDirectionComponent;
import com.gemserk.games.archervsworld.artemis.components.DamageComponent;
import com.gemserk.games.archervsworld.artemis.components.HealthComponent;
import com.gemserk.games.archervsworld.artemis.components.InformationComponent;
import com.gemserk.games.archervsworld.artemis.components.WalkingDeadComponent;
import com.gemserk.games.archervsworld.box2d.CollisionDefinitions;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

public class ArcherVsWorldEntityFactory {

	private World world;

	private BodyBuilder bodyBuilder;

	private ResourceManager<String> resourceManager;

	public void setWorld(World world) {
		this.world = world;
	}

	public void setPhysicsWorld(com.badlogic.gdx.physics.box2d.World physicsWorld) {
		this.bodyBuilder = new BodyBuilder(physicsWorld);
	}

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public void createBackground(float w, float h) {
		Texture texture = resourceManager.getResourceValue("Background");
		createStaticSprite(0f, 0f, w, h, 0f, new Sprite(texture), -100, Color.WHITE, 0f, 0f);
	}

	public void createStaticSprite(float x, float y, float w, float h, float angle, Sprite sprite, int layer, Color color, float centerx, float centery) {
		Entity entity = world.createEntity();
		entity.addComponent(new SpatialComponent(new SpatialImpl(x, y, w, h, angle)));
		entity.addComponent(new SpriteComponent(sprite, layer, new Vector2(centerx, centery), color));
		entity.refresh();
	}

	public void createPhysicsArrow(Vector2 position, Vector2 direction, float power) {
		Entity entity = world.createEntity();

		entity.setGroup(Groups.Arrow);

		short categoryBits = CollisionDefinitions.ArrowGroup;
		short maskBits = CollisionDefinitions.All & ~CollisionDefinitions.ArrowGroup;

		Body body = bodyBuilder.type(BodyType.DynamicBody) //
				.bullet() //
				.boxShape(0.72f / 2f, 0.05f / 2f) //
				.density(1f)//
				.friction(3f) //
				.mass(0.8f) //
				.categoryBits(categoryBits) //
				.maskBits(maskBits) //
				.userData(entity).build();

		body.setTransform(position, (float) (direction.angle() / 180f * Math.PI));

		Vector2 impulse = new Vector2(direction);
		impulse.mul(body.getMass());
		impulse.mul(power);

		Vector2 lp = body.getWorldPoint(new Vector2(0f, 0f));
		body.applyLinearImpulse(impulse, lp);

		Resource<Texture> resource = resourceManager.get("Arrow");
		Texture texture = resource.get();

		entity.addComponent(new PhysicsComponent(new SimpleProperty<Body>(body)));
		entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, 1f, 1f)));
		entity.addComponent(new SpriteComponent(new Sprite(texture), 1, new Vector2(0.5f, 0.5f), Color.WHITE));
		entity.addComponent(new DamageComponent(1f));
		entity.addComponent(new CorrectArrowDirectionComponent());
		entity.addComponent(new InformationComponent("physical arrow"));

		entity.refresh();
	}

	public Entity createArrow(Vector2 position, float angle) {
		Property<Vector2> positionProperty = PropertyBuilder.vector2(position);
		Property<FloatValue> angleProperty = PropertyBuilder.property((new FloatValue(angle)));
		return createArrow(positionProperty, angleProperty);
	}

	public Entity createArrow(Property<Vector2> positionProperty, Property<FloatValue> angleProperty) {
		Entity entity = world.createEntity();

		Resource<Texture> resource = resourceManager.get("Arrow");
		Texture texture = resource.get();

		Vector2 position = positionProperty.get();
		float angle = angleProperty.get().value;

		entity.addComponent(new SpatialComponent(new SpatialImpl(position.x, position.y, 1f, 1f, angle)));
		// entity.addComponent(new SpatialComponent( //
		// positionProperty, //
		// new SimpleProperty<Vector2>(new Vector2(1f, 1f)), //
		// angleProperty));
		entity.addComponent(new SpriteComponent(new Sprite(texture), 1, new Vector2(0.5f, 0.5f), Color.WHITE));
		entity.addComponent(new InformationComponent("graphical arrow"));

		entity.refresh();
		return entity;
	}

	public void createArcher(Vector2 position) {
		Entity entity = world.createEntity();

		float bowHeight = 1.6f;
		float bowWidth = 1.6f;

		Resource<Texture> resource = resourceManager.get("Bow");
		Texture texture = resource.get();

		entity.addComponent(new SpatialComponent(new SpatialImpl(position.x, position.y, bowWidth, bowHeight, 0f)));
		entity.addComponent(new SpriteComponent(new Sprite(texture), 2, new Vector2(0.5f, 0.5f), Color.WHITE));
		entity.addComponent(new BowComponent( //
				new SimpleProperty<FloatValue>(new FloatValue(0f)), //
				new SimpleProperty<Entity>(null),//
				new SimpleProperty<FloatValue>(new FloatValue(5f)), //
				new SimpleProperty<FloatValue>(new FloatValue(15f))));

		entity.refresh();
	}

	public void createWalkingDead(Vector2 position, Vector2 size, Vector2 velocity, float health) {
		Entity entity = world.createEntity();

		short categoryBits = CollisionDefinitions.EnemiesGroup;
		short maskBits = CollisionDefinitions.All & ~CollisionDefinitions.EnemiesGroup;

		Body body = bodyBuilder.type(BodyType.DynamicBody) //
				.fixedRotation() //
				.boxShape(size.x * 0.5f, size.y * 0.5f - 0.1f) //
				.density(1f)//
				.friction(0.1f) //
				.categoryBits(categoryBits) //
				.maskBits(maskBits) //
				.position(position.x, position.y) //
				.userData(entity).build();

		Resource<Texture> resource = resourceManager.get("Rock");
		Texture texture = resource.get();

		entity.setGroup(Groups.Enemy);

		entity.addComponent(new PhysicsComponent(new SimpleProperty<Body>(body)));
		entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, size.x, size.y)));
		entity.addComponent(new SpriteComponent(new SimpleProperty<Sprite>(new Sprite(texture)), new SimpleProperty<IntValue>(new IntValue(2))));
		entity.addComponent(new WalkingDeadComponent( //
				PropertyBuilder.vector2(velocity), //
				new SimpleProperty<IntValue>(new IntValue(0)), //
				new SimpleProperty<IntValue>(new IntValue(1000)), //
				new SimpleProperty<IntValue>(new IntValue(2000))));
		entity.addComponent(new HealthComponent(new Container(health, health), 0f));
		entity.addComponent(new ParentComponent());
		entity.addComponent(new InformationComponent("zombie"));

		entity.refresh();
	}

	Color endColor = new Color(1f, 1f, 1f, 0f);

	public Entity createDyingZombie(Vector2 position, Vector2 size) {

		Entity entity = world.createEntity();

		Resource<Texture> resource = resourceManager.get("Rock");
		Texture texture = resource.get();

		int aliveTime = 600;

		Color color = new Color(1f, 1f, 1f, 1f);

		Synchronizers.transition(color, Transitions.transitionBuilder(color) //
				.end(endColor) //
				.time(aliveTime) //
				.build());

		entity.addComponent(new SpatialComponent(new SpatialImpl(position.x, position.y, size.x, size.y, 0f)));
		// entity.addComponent(new SpatialComponent( //
		// PropertyBuilder.vector2(position), //
		// new SimpleProperty<Vector2>(size), //
		// PropertyBuilder.property(new FloatValue(0f))));
		entity.addComponent(new SpriteComponent( //
				new SimpleProperty<Sprite>(new Sprite(texture)), //
				new SimpleProperty<IntValue>(new IntValue(2)), //
				PropertyBuilder.property(new Vector2(0.5f, 0.5f)), //
				PropertyBuilder.property(color))); //
		entity.addComponent(new AliveComponent(aliveTime));

		entity.addComponent(new InformationComponent("fade out zombie"));

		entity.refresh();

		return entity;
	}

	public Entity createDyingArrow(Vector2 position, float angle, int aliveTime, Color startColor) {
		return createDyingArrow(PropertyBuilder.vector2(position), //
				PropertyBuilder.property(ValueBuilder.floatValue(angle)), //
				aliveTime, startColor);
	}

	public Entity createDyingArrow(Property<Vector2> position, Property<FloatValue> angle, int aliveTime, Color startColor) {
		Entity entity = world.createEntity();

		Resource<Texture> resource = resourceManager.get("Arrow");
		Texture texture = resource.get();

		Color color = startColor;

		Synchronizers.transition(color, Transitions.transitionBuilder(color) //
				.end(endColor) //
				.time(aliveTime) //
				.build());

		Vector2 positionValue = position.get();
		float angleValue = angle.get().value;

		entity.addComponent(new SpatialComponent(new SpatialImpl(positionValue.x, positionValue.y, 1f, 1f, angleValue)));
		// entity.addComponent(new SpatialComponent( //
		// position, //
		// PropertyBuilder.property(arrowSize), //
		// angle));
		entity.addComponent(new SpriteComponent( //
				new SimpleProperty<Sprite>(new Sprite(texture)), //
				new SimpleProperty<IntValue>(new IntValue(1)), //
				PropertyBuilder.property(new Vector2(0.5f, 0.5f)), //
				PropertyBuilder.property(color))); //
		entity.addComponent(new AliveComponent(aliveTime));

		entity.addComponent(new InformationComponent("fade out arrow"));

		entity.refresh();

		return entity;
	}

	public Entity createStaticBody(Vector2 position, Vector2 size) {

		Vector2[] vertices = new Vector2[] { //
		new Vector2(-size.x * 0.5f, -size.y * 0.5f), //
				new Vector2(size.x * 0.5f, -size.y * 0.5f), //
				new Vector2(size.x * 0.5f, size.y * 0.5f), //
				new Vector2(-size.x * 0.5f, size.y * 0.5f), //
		};

		return createStaticBody(position, vertices);
	}

	public Entity createStaticBody(Vector2 position, final Vector2[] vertices) {
		Entity entity = world.createEntity();

		Body body = bodyBuilder.type(BodyType.StaticBody) //
				.position(position.x, position.y)//
				.polygonShape(vertices)//
				.density(1f)//
				.friction(0.5f) //
				.userData(entity).build();

		body.setUserData(entity);

		entity.addComponent(new PhysicsComponent(body));
		entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, 0f, 0f)));
		entity.addComponent(new HealthComponent(new Container(0, 0), 1f));
		entity.addComponent(new ParentComponent());
		entity.refresh();

		return entity;
	}

	public void createZombiesSpawner(final Vector2 position) {
		Entity spawner = world.createEntity();
		spawner.addComponent(new SpawnerComponent(new CountDownTimer(0, true), 7000, 9000, new AbstractTrigger() {
			@Override
			protected boolean handle(Entity e) {
				Gdx.app.log("Archer Vs Zombies", "new zombie spawned!");
				createWalkingDead(position, new Vector2(0.5f, 2f), new Vector2(-1.4f, 0f), 5f);
				return true;
			}
		}));
		spawner.refresh();
	}

	public void createCloud(Vector2 position, Vector2 velocity, Vector2 size, Rectangle areaLimit, int layer, float alpha) {
		Entity entity = world.createEntity();

		Texture texture = resourceManager.getResourceValue("Cloud");

		Color color = new Color();
		Color endColor = new Color(1f, 1f, 1f, alpha);

		Synchronizers.transition(color, Transitions.transitionBuilder(endColor) //
				.end(endColor) //
				.time(2000) //
				.build());

		entity.addComponent(new SpatialComponent(new SpatialImpl(position.x, position.y, size.x, size.y, 0f)));
		entity.addComponent(new SpriteComponent(new SimpleProperty<Sprite>(new Sprite(texture)), //
				new SimpleProperty<IntValue>(new IntValue(layer)), //
				new SimpleProperty<Vector2>(new Vector2(0.5f, 0.5f)), //
				PropertyBuilder.property(color)));
		entity.addComponent(new MovementComponent(velocity, 0f));
		entity.addComponent(new AliveAreaComponent(areaLimit));

		entity.refresh();
	}

	public void createCloudsSpawner(final Rectangle spawnArea, final Rectangle limitArea, Vector2 direction, final float minSpeed, final float maxSpeed, int minTime, int maxTime) {
		Entity entity = world.createEntity();
		// TODO: Limit entity type component/system, to avoid creating a lot of entities of the same type?
		entity.addComponent(new SpawnerComponent(new CountDownTimer(0, false), minTime, maxTime, new AbstractTrigger() {

			@Override
			protected boolean handle(Entity e) {
				Gdx.app.log("Archer Vs Zombies", "new cloud spawned!");

				Vector2 size = new Vector2(5, 5).mul(MathUtils.random(0.5f, 1.2f));

				Vector2 velocity = new Vector2(-1f, 0f).mul(MathUtils.random(minSpeed, maxSpeed));

				Vector2 newPosition = new Vector2( //
						MathUtils.random(spawnArea.x, spawnArea.x + spawnArea.width), //
						MathUtils.random(spawnArea.y, spawnArea.y + spawnArea.height));

				// newPosition.set(position);

				// newPosition.y += MathUtils.random(-3, 3);

				int layer = -4;

				if (size.x > 4f)
					layer = 5;

				createCloud(newPosition, velocity, size, limitArea, layer, 1f);
				return true;
			}
		}));
		entity.refresh();
	}

}
