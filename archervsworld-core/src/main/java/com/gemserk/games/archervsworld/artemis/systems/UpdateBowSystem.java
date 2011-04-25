package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.EntityDebugger;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.systems.ActivableSystem;
import com.gemserk.commons.artemis.systems.ActivableSystemImpl;
import com.gemserk.commons.gdx.math.MathUtils2;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.games.archervsworld.artemis.components.BowComponent;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.controllers.BowController;
import com.gemserk.games.archervsworld.controllers.ControllerSwitcher;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

public class UpdateBowSystem extends EntityProcessingSystem implements ActivableSystem {

	private final Vector2 direction = new Vector2();

	private ArcherVsWorldEntityFactory entityFactory;

	private ResourceManager<String> resourceManager;

	private ControllerSwitcher controllerSwitcher;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	public UpdateBowSystem(ControllerSwitcher controllerSwitcher, ArcherVsWorldEntityFactory entityFactory) {
		super(BowComponent.class);
		this.entityFactory = entityFactory;
		this.controllerSwitcher = controllerSwitcher;
	}

	@Override
	protected void process(Entity bow) {

		BowController bowController = controllerSwitcher.getController();

		float angle = bowController.getAngle();
		float power = bowController.getPower();
		
		if (bowController.isCharging()) {

			// updates bow direction based on the controller

			SpatialComponent spatialComponent = bow.getComponent(SpatialComponent.class);
			BowComponent bowComponent = bow.getComponent(BowComponent.class);

			bowComponent.setPower(MathUtils2.truncate(power, bowComponent.getMinPower(), bowComponent.getMaxPower()));
			
			if (bowComponent.getArrow() == null) {

				// TODO: add it as a child using scene graph component so transformations will be handled automatically?

				Entity arrow = entityFactory.createArrow(new Vector2(), 0f);
				bowComponent.setArrow(arrow);

			}

			float minFireAngle = -70f;
			float maxFireAngle = 80f;

			if (AngleUtils.between(angle, minFireAngle, maxFireAngle)) 
				spatialComponent.setAngle(angle);

		}

		if (bowController.shouldFire()) {

			BowComponent bowComponent = bow.getComponent(BowComponent.class);

			Entity arrow = bowComponent.getArrow();

			if (arrow == null)
				return;

			SpatialComponent arrowSpatialComponent = arrow.getComponent(SpatialComponent.class);

			if (arrowSpatialComponent == null) {
				EntityDebugger.debug("arrow spatial component missing in arrow entity", bow);
				throw new RuntimeException("spatial component missing on arrow entity " + arrow.getUniqueId() );
			}

			direction.set(1f, 0f);
			direction.rotate(arrowSpatialComponent.getAngle());

			entityFactory.createPhysicsArrow(arrowSpatialComponent.getPosition(), direction, bowComponent.getPower());

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

	// implementation of activable system.

	ActivableSystemImpl activableSystem = new ActivableSystemImpl();

	public boolean isEnabled() {
		return activableSystem.isEnabled();
	}

	public void toggle() {
		activableSystem.toggle();
	}

}