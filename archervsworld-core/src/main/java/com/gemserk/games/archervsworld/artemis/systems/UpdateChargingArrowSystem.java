package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.systems.ActivableSystem;
import com.gemserk.commons.artemis.systems.ActivableSystemImpl;
import com.gemserk.games.archervsworld.artemis.components.BowComponent;

public class UpdateChargingArrowSystem extends EntityProcessingSystem implements ActivableSystem {

	private final Vector2 diff = new Vector2();

	private final Vector2 position = new Vector2();

	public UpdateChargingArrowSystem() {
		super(BowComponent.class);
	}

	@Override
	protected boolean checkProcessing() {
		return isEnabled();
	}

	ActivableSystemImpl activableSystem = new ActivableSystemImpl();

	public boolean isEnabled() {
		return activableSystem.isEnabled();
	}

	public void toggle() {
		activableSystem.toggle();
	}

	@Override
	protected void process(Entity bow) {

		BowComponent bowComponent = bow.getComponent(BowComponent.class);

		Entity arrow = bowComponent.getArrow();

		if (arrow == null)
			return;

		SpatialComponent spatialComponent = bow.getComponent(SpatialComponent.class);
		SpatialComponent arrowSpatialComponent = arrow.getComponent(SpatialComponent.class);

		position.set(spatialComponent.getPosition());

		diff.set(1f, 0f);
		diff.rotate(spatialComponent.getAngle());
		diff.mul(bowComponent.getPower() * 0.012f);

		position.sub(diff);

		arrowSpatialComponent.setPosition(position);
		arrowSpatialComponent.setAngle(spatialComponent.getAngle());

	}

}