package com.gemserk.games.archervsworld.artemis.systems;

import java.util.ArrayList;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.GroupManager;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.gemserk.commons.artemis.components.Contact;
import com.gemserk.commons.artemis.components.ParentComponent;
import com.gemserk.commons.artemis.components.PhysicsComponent;
import com.gemserk.commons.artemis.components.Spatial;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.values.FloatValue;
import com.gemserk.componentsengine.input.ButtonMonitor;
import com.gemserk.componentsengine.properties.AbstractProperty;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.componentsengine.utils.Container;
import com.gemserk.games.archervsworld.artemis.components.DamageComponent;
import com.gemserk.games.archervsworld.artemis.components.HealthComponent;
import com.gemserk.games.archervsworld.artemis.components.HudButtonComponent;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.artemis.entities.Groups;
import com.gemserk.games.archervsworld.box2d.CollisionDefinitions;
import com.gemserk.games.archervsworld.controllers.ControllerSwitcher;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

public class GameLogicSystem extends EntitySystem {

	static class StickArrowProperty extends AbstractProperty<Vector2> {

		private final SpatialComponent spatial;

		private final Vector2 difference;

		Vector2 position = new Vector2();

		public StickArrowProperty(SpatialComponent spatial, Vector2 difference) {
			this.spatial = spatial;
			this.difference = difference;
		}

		@Override
		public Vector2 get() {
			Vector2 targetPosition = spatial.getSpatial().getPosition();
			position.set(targetPosition).sub(difference);
			return position;
		}

	}

	ArcherVsWorldEntityFactory archerVsWorldEntityFactory;

	ResourceManager<String> resourceManager;

	boolean switchControllersButtonDown = false;

	public void setArcherVsWorldEntityFactory(ArcherVsWorldEntityFactory archerVsWorldEntityFactory) {
		this.archerVsWorldEntityFactory = archerVsWorldEntityFactory;
	}

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public GameLogicSystem(ControllerSwitcher controllerSwitcher) {
		super();
		controllerSwitcher.setSwitchButtonMonitor(new ButtonMonitor() {
			@Override
			protected boolean isDown() {
				return switchControllersButtonDown;
			}
		});
	}

	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {

		processButtons();

		processArrows();

		processEnemies();

	}


	private void processButtons() {

		int entityCount = world.getEntityManager().getEntityCount();

		switchControllersButtonDown = false;

		for (int i = 0; i < entityCount; i++) {

			Entity entity = world.getEntity(i);

			if (entity == null)
				continue;

			HudButtonComponent buttonComponent = entity.getComponent(HudButtonComponent.class);

			if (buttonComponent == null)
				continue;

			SpatialComponent spatialComponent = entity.getComponent(SpatialComponent.class);

			if (spatialComponent == null)
				continue;

			if (buttonComponent.getPressed()) {
				switchControllersButtonDown = true;
			}

		}

	}
	
	private void processEnemies() {
		GroupManager groupManager = world.getGroupManager();

		ImmutableBag<Entity> entities = groupManager.getEntities(Groups.Enemy);

		if (entities == null)
			return;

		for (int i = 0; i < entities.size(); i++) {
			Entity entity = entities.get(i);
			HealthComponent healthComponent = entity.getComponent(HealthComponent.class);

			if (!healthComponent.getContainer().isEmpty())
				continue;

			SpatialComponent spatialComponent = entity.getComponent(SpatialComponent.class);
			ParentComponent parentComponent = entity.getComponent(ParentComponent.class);

			world.deleteEntity(entity);
			
			Spatial zombieSpatial = spatialComponent.getSpatial();
			archerVsWorldEntityFactory.createDyingZombie(zombieSpatial.getPosition(), new Vector2(zombieSpatial.getWidth(), zombieSpatial.getHeight()));

			ArrayList<Entity> arrows = parentComponent.getChildren();
			for (int j = 0; j < arrows.size(); j++) {
				Entity arrow = arrows.get(j);
				SpatialComponent arrowSpatialComponent = arrow.getComponent(SpatialComponent.class);
				SpriteComponent spriteComponent = arrow.getComponent(SpriteComponent.class);
				
				Spatial arrowSpatial = arrowSpatialComponent.getSpatial();

				// should not be null
				if (arrowSpatialComponent != null)
					archerVsWorldEntityFactory.createDyingArrow(arrowSpatial.getPosition(), //
							arrowSpatial.getAngle(), 300, spriteComponent.getColor());
			}
			

		}

	}

	private void processArrows() {

		GroupManager groupManager = world.getGroupManager();

		ImmutableBag<Entity> entities = groupManager.getEntities(Groups.Arrow);

		if (entities == null)
			return;

		for (int i = 0; i < entities.size(); i++) {

			Entity entity = entities.get(i);

			PhysicsComponent physicsComponent = entity.getComponent(PhysicsComponent.class);
			Body body = physicsComponent.getBody();

			int arrowAliveTime = 6000;

			if (!body.isAwake()) {

				SpatialComponent component = entity.getComponent(SpatialComponent.class);
				Spatial spatial = component.getSpatial();
				archerVsWorldEntityFactory.createDyingArrow(spatial.getPosition(), spatial.getAngle(), arrowAliveTime, new Color(1f, 1f, 1f, 1f));
				this.world.deleteEntity(entity);
				continue;

			}

			Contact contact = physicsComponent.getContact();

			if (!contact.isInContact())
				continue;
			
			Entity target = contact.getEntity();

			if (target == null)
				continue;

			String targetGroup = groupManager.getGroupOf(target);
			
			Vector2 normal = contact.getNormal();
			float normalAngle = normal.cpy().mul(-1f).angle();
			float bodyAngle = (float) (body.getAngle() * 180.0 / Math.PI);
			double diff = Math.abs(AngleUtils.minimumDifference(normalAngle, bodyAngle));
			
			int stickAngle = 60;
			
			if (Groups.Enemy.equals(targetGroup)) 
				stickAngle = 180;

			// if the arrow hits something but not in the expected angle, then it will never be able to hit again.
			if (diff > stickAngle) {
				
				Fixture fixture = body.getFixtureList().get(0);
				Filter filter = fixture.getFilterData();
				
				filter.maskBits = CollisionDefinitions.All & ~CollisionDefinitions.ArrowGroup & ~CollisionDefinitions.EnemiesGroup; 
				
				fixture.setFilterData(filter);
				
				continue;
			}

			if (diff < stickAngle) {

				HealthComponent healthComponent = target.getComponent(HealthComponent.class);

				if (healthComponent != null) {
					// if (Groups.Pierceable.equals(collisionEntityGroup)) {

					final SpatialComponent targetSpatialComponent = target.getComponent(SpatialComponent.class);
					PhysicsComponent targetPhysicsComponent = target.getComponent(PhysicsComponent.class);

					Body targetBody = targetPhysicsComponent.getBody();

					if (targetBody.getType().equals(BodyType.DynamicBody))
						targetBody.applyLinearImpulse(new Vector2(0.5f, 0f), targetSpatialComponent.getPosition());

					SpatialComponent spatialComponent = entity.getComponent(SpatialComponent.class);
					Spatial spatial = spatialComponent.getSpatial();

					Vector2 arrowPosition = spatial.getPosition();
					float arrowAngle = spatial.getAngle();

					Vector2 displacement = new Vector2(1f, 0f).mul(0.2f);
					displacement.rotate(arrowAngle);

					final Vector2 targetPosition = targetSpatialComponent.getPosition();
					final Vector2 difference = targetPosition.cpy().sub(arrowPosition).sub(displacement);

					// Use layer - 1 for the sprite component

					Entity newArrow = archerVsWorldEntityFactory.createDyingArrow(new StickArrowProperty(targetSpatialComponent, difference), 
							new SimpleProperty<FloatValue>(new FloatValue(arrowAngle)), arrowAliveTime, new Color(1f, 1f, 1f, 1f));

					ParentComponent parentComponent = target.getComponent(ParentComponent.class);
					parentComponent.addChild(newArrow);

					// add owner to the arrow, so it is deleted when the owner is deleted...

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

	@Override
	public void initialize() {

	}

	@Override
	protected boolean checkProcessing() {
		return true;
	}
}