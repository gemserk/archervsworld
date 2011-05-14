package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.EntityDebugger;
import com.gemserk.commons.artemis.components.Spatial;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.systems.ActivableSystem;
import com.gemserk.commons.artemis.systems.ActivableSystemImpl;
import com.gemserk.commons.gdx.math.MathUtils2;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.games.archervsworld.artemis.components.BowComponent;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.controllers.BowData;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

public class UpdateBowSystem extends EntityProcessingSystem implements ActivableSystem {

	private final Vector2 direction = new Vector2();

	private ArcherVsWorldEntityFactory entityFactory;

	private ResourceManager<String> resourceManager;

	private ActivableSystemImpl activableSystem = new ActivableSystemImpl();

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public void setEntityFactory(ArcherVsWorldEntityFactory entityFactory) {
		this.entityFactory = entityFactory;
	}

	public UpdateBowSystem() {
		super(BowComponent.class);
	}

	@Override
	protected void process(Entity e) {

		BowComponent bowComponent = e.getComponent(BowComponent.class);
		BowData bowData = bowComponent.getBowData();

		// BowController bowController = currentBowController.get();
		// BowData bowData = bowController.getBowData();

		float angle = bowData.getAngle();
		float power = bowData.getPower();

		float minFireAngle = -70f;
		float maxFireAngle = 80f;

		SpatialComponent spatialComponent = e.getComponent(SpatialComponent.class);
		Spatial spatial = spatialComponent.getSpatial();

		if (AngleUtils.between(angle, minFireAngle, maxFireAngle))
			spatial.setAngle(angle);

		if (bowData.isCharging()) {

			// updates bow direction based on the controller

			bowComponent.setPower(MathUtils2.truncate(power, bowComponent.getMinPower(), bowComponent.getMaxPower()));

			if (bowComponent.getArrow() == null) {

				// TODO: add it as a child using scene graph component so transformations will be handled automatically?

				Entity arrow = entityFactory.createArrow(new Vector2(), 0f);
				bowComponent.setArrow(arrow);

			}

		}

		if (bowData.isFiring()) {

			Entity arrow = bowComponent.getArrow();

			if (arrow == null)
				return;

			SpatialComponent arrowSpatialComponent = arrow.getComponent(SpatialComponent.class);

			if (arrowSpatialComponent == null) {
				EntityDebugger.debug("arrow spatial component missing in arrow entity", e);
				throw new RuntimeException("spatial component missing on arrow entity " + arrow.getUniqueId());
			}

			Spatial arrowSpatial = arrowSpatialComponent.getSpatial();

			direction.set(1f, 0f);
			direction.rotate(arrowSpatial.getAngle());

			entityFactory.createPhysicsArrow(arrowSpatial.getPosition(), direction, bowComponent.getPower());

			world.deleteEntity(arrow);
			bowComponent.setArrow(null);

			Resource<Sound> sound = resourceManager.get("BowSound");
			sound.get().play(1f);

		}

	}

	@Override
	protected boolean checkProcessing() {
		return isEnabled();
	}

	public boolean isEnabled() {
		return activableSystem.isEnabled();
	}

	public void toggle() {
		activableSystem.toggle();
	}

}