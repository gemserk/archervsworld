package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.GroupManager;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.values.FloatValue;
import com.gemserk.componentsengine.properties.AbstractProperty;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.componentsengine.utils.Container;
import com.gemserk.games.archervsworld.artemis.components.DamageComponent;
import com.gemserk.games.archervsworld.artemis.components.HealthComponent;
import com.gemserk.games.archervsworld.artemis.components.PhysicsComponent;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.artemis.entities.Groups;
import com.gemserk.games.archervsworld.box2d.Contact;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

public class GameLogicSystem extends EntitySystem {

	ArcherVsWorldEntityFactory archerVsWorldEntityFactory;

	ResourceManager<String> resourceManager;

	AngleUtils angleUtils = new AngleUtils();

	public void setArcherVsWorldEntityFactory(ArcherVsWorldEntityFactory archerVsWorldEntityFactory) {
		this.archerVsWorldEntityFactory = archerVsWorldEntityFactory;
	}

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public GameLogicSystem() {
		super();
	}

	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {

		GroupManager groupManager = world.getGroupManager();

		entities = groupManager.getEntities(Groups.Arrow);

		if (entities == null)
			return;

		for (int i = 0; i < entities.size(); i++) {

			Entity entity = entities.get(i);

			PhysicsComponent physicsComponent = entity.getComponent(PhysicsComponent.class);
			Body body = physicsComponent.getBody();

			Contact contact = physicsComponent.getContact();

			if (!contact.inContact) {

				Vector2 linearVelocity = body.getLinearVelocity();
				float angle = linearVelocity.angle();
				body.setTransform(body.getPosition(), (float) (angle / 180f * Math.PI));

				continue;
			}

			Vector2 normal = contact.normal;

			float normalAngle = normal.cpy().mul(-1f).angle();

			float bodyAngle = (float) (body.getAngle() * 180.0 / Math.PI);

			double diff = Math.abs(angleUtils.minimumDifference(normalAngle, bodyAngle));

			int stickAngle = 45;

			if (!body.isAwake()) {

				SpatialComponent component = entity.getComponent(SpatialComponent.class);
				archerVsWorldEntityFactory.createArrow(component.getPosition(), component.getAngle());
				this.world.deleteEntity(entity);

			} else if (diff < stickAngle) {
				// remove the physics arrow and convert it to a

				Entity target = contact.entity;

				if (target != null) {
					
					HealthComponent healthComponent = target.getComponent(HealthComponent.class);

					String targetGroup = groupManager.getGroupOf(target);

					if (healthComponent != null) {
					// if (Groups.Pierceable.equals(collisionEntityGroup)) {

						final SpatialComponent targetSpatialComponent = target.getComponent(SpatialComponent.class);
						PhysicsComponent targetPhysicsComponent = target.getComponent(PhysicsComponent.class);

						Body targetBody = targetPhysicsComponent.getBody();

						if (targetBody.getType().equals(BodyType.DynamicBody))
							targetBody.applyLinearImpulse(new Vector2(0.5f, 0f), targetSpatialComponent.getPosition());

						SpatialComponent spatialComponent = entity.getComponent(SpatialComponent.class);

						Vector2 arrowPosition = spatialComponent.getPosition();
						float arrowAngle = spatialComponent.getAngle();

						Vector2 displacement = new Vector2(1f, 0f).mul(0.2f);
						displacement.rotate(arrowAngle);

						final Vector2 targetPosition = targetSpatialComponent.getPosition();
						final Vector2 difference = targetPosition.cpy().sub(arrowPosition).sub(displacement);

						// Use layer - 1 for the sprite component

						archerVsWorldEntityFactory.createArrow(new AbstractProperty<Vector2>() {

							Vector2 position = new Vector2();

							@Override
							public Vector2 get() {
								Vector2 targetPosition = targetSpatialComponent.getPosition();
								position.set(targetPosition).sub(difference);
								return position;
							}

						}, new SimpleProperty<FloatValue>(new FloatValue(arrowAngle)));

						this.world.deleteEntity(entity);

						if (Groups.Enemy.equals(targetGroup)) {
						// if (targetBody.getType().equals(BodyType.DynamicBody)) {
							
							Container healthContainer = healthComponent.getContainer();
							float currentHealth = healthContainer.getCurrent();
							
							DamageComponent damageComponent = entity.getComponent(DamageComponent.class);
							
							currentHealth -= damageComponent.getDamage() - damageComponent.getDamage() * healthComponent.getResistance();
							
							healthContainer.setCurrent(currentHealth);
							
							Resource<Sound> hitSound = resourceManager.get("HitFleshSound");
							hitSound.get().play(1f);
							
							// System.out.println("currentHealth: " + currentHealth);
							
						} else {
							Resource<Sound> hitSound = resourceManager.get("HitGroundSound");
							hitSound.get().play(1f);
						}

					}
				}

			}

		}

	}

	@Override
	public void initialize() {

	}

	@Override
	protected boolean checkProcessing() {
		return true;
	}
}