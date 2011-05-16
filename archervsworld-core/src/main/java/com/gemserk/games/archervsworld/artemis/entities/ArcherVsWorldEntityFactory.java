package com.gemserk.games.archervsworld.artemis.entities;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.gemserk.animation4j.transitions.Transitions;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.components.AliveAreaComponent;
import com.gemserk.commons.artemis.components.Contact;
import com.gemserk.commons.artemis.components.HitComponent;
import com.gemserk.commons.artemis.components.MovementComponent;
import com.gemserk.commons.artemis.components.ParentComponent;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.Spatial;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpatialImpl;
import com.gemserk.commons.artemis.components.SpatialPhysicsImpl;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.components.TimerComponent;
import com.gemserk.commons.artemis.triggers.AbstractTrigger;
import com.gemserk.commons.gdx.box2d.BodyBuilder;
import com.gemserk.commons.gdx.graphics.SpriteUtils;
import com.gemserk.commons.values.FloatValue;
import com.gemserk.commons.values.IntValue;
import com.gemserk.commons.values.ValueBuilder;
import com.gemserk.componentsengine.properties.Property;
import com.gemserk.componentsengine.properties.PropertyBuilder;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.componentsengine.utils.Container;
import com.gemserk.games.archervsworld.artemis.components.BowComponent;
import com.gemserk.games.archervsworld.artemis.components.CorrectArrowDirectionComponent;
import com.gemserk.games.archervsworld.artemis.components.DamageComponent;
import com.gemserk.games.archervsworld.artemis.components.HealthComponent;
import com.gemserk.games.archervsworld.artemis.components.InformationComponent;
import com.gemserk.games.archervsworld.artemis.components.SpawnerComponent;
import com.gemserk.games.archervsworld.artemis.components.WalkingDeadComponent;
import com.gemserk.games.archervsworld.box2d.CollisionDefinitions;
import com.gemserk.games.archervsworld.controllers.BowData;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

public class ArcherVsWorldEntityFactory {

	private static class RemoveEntityTrigger extends AbstractTrigger {

		private final World world;

		public RemoveEntityTrigger(World world) {
			this.world = world;
		}

		@Override
		protected boolean handle(Entity e) {
			world.deleteEntity(e);
			return true;
		}
	}

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
		Sprite sprite = resourceManager.getResourceValue("Background");
		createStaticSprite(0f, 0f, sprite.getWidth(), sprite.getHeight(), 0f, sprite, -100, Color.WHITE, 0f, 0f);
		
		sprite = resourceManager.getResourceValue("Background");
		createStaticSprite(sprite.getWidth(), 0f, sprite.getWidth(), sprite.getHeight(), 0f, sprite, -100, Color.WHITE, 0f, 0f);
	}

	public void createStaticSprite(float x, float y, float w, float h, float angle, Sprite sprite, int layer, Color color, float centerx, float centery) {
		Entity entity = world.createEntity();
		entity.addComponent(new SpatialComponent(new SpatialImpl(x, y, w, h, angle)));
		entity.addComponent(new SpriteComponent(sprite, layer, new Vector2(centerx, centery), color));
		entity.refresh();
	}

	private final Vector2 impulse = new Vector2();

	public void createPhysicsArrow(Vector2 position, float angle, float power) {
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

		body.setTransform(position, angle * MathUtils.degreesToRadians);

		impulse.set(1, 0);
		impulse.rotate(angle);
		impulse.mul(body.getMass());
		impulse.mul(power);

		body.applyLinearImpulse(impulse, body.getTransform().getPosition());

		Resource<Texture> resource = resourceManager.get("Arrow");
		Texture texture = resource.get();

		entity.addComponent(new PhysicsComponent(body));
		entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, 1f, 1f)));
		entity.addComponent(new SpriteComponent(new Sprite(texture), 1, new Vector2(0.5f, 0.5f), new Color(Color.WHITE)));
		entity.addComponent(new DamageComponent(1f));
		entity.addComponent(new CorrectArrowDirectionComponent());
		entity.addComponent(new InformationComponent("physical arrow"));
		entity.addComponent(new HitComponent(new AbstractTrigger() {
			@Override
			protected boolean handle(Entity e) {
				PhysicsComponent physicsComponent = e.getComponent(PhysicsComponent.class);
				Body body = physicsComponent.getBody();
				Contact contact = physicsComponent.getContact();

				for (int i = 0; i < contact.getContactCount(); i++) {
					if (!contact.isInContact(i))
						continue;

					Entity targetEntity = contact.getEntity(i);
					String group = world.getGroupManager().getGroupOf(targetEntity);

					Vector2 normal = contact.getNormal();
					float normalAngle = normal.cpy().mul(-1f).angle();
					float bodyAngle = (float) (body.getAngle() * 180.0 / Math.PI);
					double diff = Math.abs(AngleUtils.minimumDifference(normalAngle, bodyAngle));

					int stickAngle = 60;

					if (Groups.Enemy.equals(group))
						stickAngle = 180;

					// if the arrow hits something but not in the expected angle, then it will never be able to hit again.
					if (diff > stickAngle) {
						Fixture fixture = body.getFixtureList().get(0);
						Filter filter = fixture.getFilterData();
						filter.maskBits = CollisionDefinitions.All & ~CollisionDefinitions.ArrowGroup & ~CollisionDefinitions.EnemiesGroup;
						fixture.setFilterData(filter);
						SpriteComponent spriteComponent = e.getComponent(SpriteComponent.class);
						Synchronizers.transition(spriteComponent.getColor(), Transitions.transitionBuilder(spriteComponent.getColor()).end(endColor).time(5000));

						// cant damage any more...
						// DamageComponent damageComponent = e.getComponent(DamageComponent.class);
						// e.removeComponent(damageComponent);

						e.addComponent(new TimerComponent(5000, new RemoveEntityTrigger(world)));
						// e.addComponent(new AliveComponent(5000));
						e.refresh();

						return true;
					}

					// if target is dynamic body....
					if (Groups.Enemy.equalsIgnoreCase(group)) {
						// in this case create a special arrow which follows the enemy...
						Sound sound = resourceManager.getResourceValue("HitFleshSound");
						sound.play();
					} else {
						// just for now, we only create dying arrows if we hit no enemy
						SpatialComponent spatialComponent = e.getComponent(SpatialComponent.class);
						createDyingArrow(spatialComponent.getSpatial(), 5000, Color.WHITE);

						Sound sound = resourceManager.getResourceValue("HitGroundSound");
						sound.play();
					}

					world.deleteEntity(e);
					return true;
				}

				return false;
			}
		}));

		entity.refresh();
	}

	public Entity createArrow(Spatial spatial) {
		Property<Vector2> positionProperty = PropertyBuilder.vector2(spatial.getPosition());
		Property<FloatValue> angleProperty = PropertyBuilder.property((new FloatValue(spatial.getAngle())));
		return createArrow(positionProperty, angleProperty);
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
		entity.addComponent(new SpriteComponent(new Sprite(texture), 1, new Vector2(0.5f, 0.5f), Color.WHITE));
		entity.addComponent(new InformationComponent("graphical arrow"));
		entity.refresh();
		return entity;
	}

	public void createArcher(float x, float y, BowData bowData) {
		Entity entity = world.createEntity();
		float bowHeight = 1.6f;
		float bowWidth = 1.6f;
		Texture texture = resourceManager.getResourceValue("Bow");
		entity.addComponent(new SpatialComponent(new SpatialImpl(x, y, bowWidth, bowHeight, 0f)));
		entity.addComponent(new SpriteComponent(new Sprite(texture), 2, new Vector2(0.5f, 0.5f), Color.WHITE));
		entity.addComponent(new BowComponent(null, 5f, 15f, bowData, new AbstractTrigger() {
			@Override
			protected boolean handle(Entity e) {
				BowComponent bowComponent = e.getComponent(BowComponent.class);
				BowData bowData = bowComponent.getBowData();
				
				Entity arrow = bowComponent.getArrow();

				if (arrow == null)
					return false;

				SpatialComponent arrowSpatialComponent = arrow.getComponent(SpatialComponent.class);
				Spatial arrowSpatial = arrowSpatialComponent.getSpatial();

				createPhysicsArrow(arrowSpatial.getPosition(), bowData.getAngle(), bowData.getPower());

				world.deleteEntity(arrow);
				bowComponent.setArrow(null);

				Resource<Sound> sound = resourceManager.get("BowSound");
				sound.get().play(1f);

				return false;
			}
		}, new AbstractTrigger() {
			@Override
			protected boolean handle(Entity e) {
				BowComponent bowComponent = e.getComponent(BowComponent.class);
				if (bowComponent.getArrow() == null) {
					Entity arrow = createArrow(new Vector2(), 0f);
					bowComponent.setArrow(arrow);
				}
				return false;
			}
		}));
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

		entity.addComponent(new PhysicsComponent(body));
		entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, size.x, size.y)));
		entity.addComponent(new SpriteComponent(new Sprite(texture), 2));
		entity.addComponent(new WalkingDeadComponent( //
				PropertyBuilder.vector2(velocity), //
				new SimpleProperty<IntValue>(new IntValue(0)), //
				new SimpleProperty<IntValue>(new IntValue(1000)), //
				new SimpleProperty<IntValue>(new IntValue(2000))));
		entity.addComponent(new HealthComponent(new Container(health, health), 0f));
		entity.addComponent(new ParentComponent());
		entity.addComponent(new InformationComponent("zombie"));
		entity.addComponent(new HitComponent(new AbstractTrigger() {
			@Override
			protected boolean handle(Entity e) {

				PhysicsComponent phyiscsComponent = e.getComponent(PhysicsComponent.class);
				Contact contact = phyiscsComponent.getContact();

				if (!contact.isInContact())
					return false;

				Entity contactEntity = null;

				for (int i = 0; i < contact.getContactCount(); i++) {

					if (!contact.isInContact(i))
						continue;

					// hack to avoid processing the same entity twice because we have multiple contacts with it...
					if (contactEntity == contact.getEntity(i))
						continue;

					contactEntity = contact.getEntity(i);
					// if contactEntity is arrow?

					String contactEntityGroup = world.getGroupManager().getGroupOf(contactEntity);
					// is bullet maybe?
					if (!Groups.isArrow(contactEntityGroup))
						continue;

					HealthComponent healthComponent = e.getComponent(HealthComponent.class);
					DamageComponent damageComponent = contactEntity.getComponent(DamageComponent.class);

					if (damageComponent == null)
						continue;

					Container health = healthComponent.getContainer();
					health.remove(damageComponent.getDamage());

					if (health.isEmpty()) {
						// createDyingZombie(position, size);
						world.deleteEntity(e);
						return true;
					}

				}

				return false;
			}
		}));

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
				.time(aliveTime));

		entity.addComponent(new SpatialComponent(new SpatialImpl(position.x, position.y, size.x, size.y, 0f)));
		entity.addComponent(new SpriteComponent(new Sprite(texture), 2, color));
		entity.addComponent(new TimerComponent(aliveTime, new RemoveEntityTrigger(world)));
		entity.addComponent(new InformationComponent("fade out zombie"));

		entity.refresh();

		return entity;
	}

	public void createDyingArrow(Spatial spatial, int aliveTime, Color startColor) {
		createDyingArrow(PropertyBuilder.vector2(spatial.getPosition()), //
				PropertyBuilder.property(ValueBuilder.floatValue(spatial.getAngle())), //
				aliveTime, startColor);
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

		Color color = new Color(startColor);

		Synchronizers.transition(color, Transitions.transitionBuilder(color) //
				.end(endColor) //
				.time(aliveTime));

		Vector2 positionValue = position.get();
		float angleValue = angle.get().value;

		entity.addComponent(new SpatialComponent(new SpatialImpl(positionValue.x, positionValue.y, 1f, 1f, angleValue)));
		entity.addComponent(new SpriteComponent(new Sprite(texture), 1, color));
		entity.addComponent(new TimerComponent(aliveTime, new RemoveEntityTrigger(world)));
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
				.userData(entity) //
				.build();

		entity.addComponent(new PhysicsComponent(body));
		entity.addComponent(new SpatialComponent(new SpatialPhysicsImpl(body, 0f, 0f)));
		entity.addComponent(new HealthComponent(new Container(0, 0), 1f));
		entity.addComponent(new ParentComponent());
		entity.refresh();

		return entity;
	}

	public void createZombiesSpawner(final Vector2 position, int count, final int minTime, final int maxTime) {
		Entity entity = world.createEntity();
		int time = MathUtils.random(minTime, maxTime);
		entity.addComponent(new SpawnerComponent(count));
		entity.addComponent(new TimerComponent(time, new AbstractTrigger() {
			@Override
			protected boolean handle(Entity e) {
				TimerComponent timerComponent = e.getComponent(TimerComponent.class);
				timerComponent.setCurrentTime(MathUtils.random(minTime, maxTime));
				Gdx.app.log("Archer Vs Zombies", "New Zombie spawned");
				createWalkingDead(position, new Vector2(0.5f, 2f), new Vector2(-1.4f, 0f), 2f);
				SpawnerComponent spawnerComponent = e.getComponent(SpawnerComponent.class);
				spawnerComponent.setCount(spawnerComponent.getCount() - 1);
				if (spawnerComponent.getCount() == 0)
					world.deleteEntity(e);
				return false;
			}
		}));
		entity.refresh();
	}

	public void createCloud(Vector2 position, Vector2 velocity, Rectangle areaLimit, int layer, float size, Sprite sprite, Color cloudColor) {
		Entity entity = world.createEntity();

		Color color = new Color(cloudColor.r, cloudColor.g, cloudColor.b, 0f);
		Synchronizers.transition(color, Transitions.transitionBuilder(color) //
				.end(cloudColor) //
				.time(2000));

		SpriteUtils.resize(sprite, size);
		entity.addComponent(new SpatialComponent(new SpatialImpl(position.x, position.y, sprite.getWidth(), sprite.getHeight(), 0f)));
		entity.addComponent(new SpriteComponent(sprite, layer, new Vector2(0.5f, 0.5f), color));
		entity.addComponent(new MovementComponent(velocity, 0f));
		entity.addComponent(new AliveAreaComponent(areaLimit));
		entity.refresh();
	}

	public void createCloudsSpawner(final Rectangle spawnArea, final Rectangle limitArea, Vector2 direction, final float minSpeed, final float maxSpeed, final int minTime, final int maxTime) {
		Entity entity = world.createEntity();
		// TODO: Limit entity type component/system, to avoid creating a lot of entities of the same type?
		int time = MathUtils.random(minTime, maxTime);
		entity.addComponent(new TimerComponent(time, new AbstractTrigger() {

			@Override
			protected boolean handle(Entity e) {
				TimerComponent timerComponent = e.getComponent(TimerComponent.class);
				timerComponent.setCurrentTime(MathUtils.random(minTime, maxTime));

				Gdx.app.log("Archer Vs Zombies", "new cloud spawned!");

				float size = MathUtils.random(10f, 20f);
				// float gray = MathUtils.random(0.7f, 1f);
				float gray = 1f;

				Vector2 velocity = new Vector2(-1f, 0f).mul(MathUtils.random(minSpeed, maxSpeed));
				Vector2 newPosition = new Vector2( //
						MathUtils.random(spawnArea.x, spawnArea.x + spawnArea.width), //
						MathUtils.random(spawnArea.y, spawnArea.y + spawnArea.height));

				int layer = -4;

				// if (size > 9f)
				// layer = 5;

				Sprite sprite = null;

				if (MathUtils.randomBoolean()) {
					sprite = resourceManager.getResourceValue("Cloud02");
				} else {
					sprite = resourceManager.getResourceValue("Cloud01");
				}

				createCloud(newPosition, velocity, limitArea, layer, size, sprite, new Color(gray, gray, gray, 1f));
				return false;
			}
		}));
		// entity.addComponent(new SpawnerComponent(new CountDownTimer(0, false), minTime, maxTime, ));
		entity.refresh();
	}

}
