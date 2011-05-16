package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.gemserk.commons.artemis.components.Spatial;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.systems.ActivableSystem;
import com.gemserk.commons.artemis.systems.ActivableSystemImpl;
import com.gemserk.commons.artemis.triggers.Trigger;
import com.gemserk.commons.gdx.math.MathUtils2;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.games.archervsworld.artemis.components.BowComponent;
import com.gemserk.games.archervsworld.controllers.BowData;

public class UpdateBowSystem extends EntityProcessingSystem implements ActivableSystem {

	private ActivableSystemImpl activableSystem = new ActivableSystemImpl();

	public UpdateBowSystem() {
		super(BowComponent.class);
	}

	@Override
	protected void process(Entity e) {
		BowComponent bowComponent = e.getComponent(BowComponent.class);
		BowData bowData = bowComponent.getBowData();

		float angle = bowData.getAngle();
		float power = bowData.getPower();

		float minFireAngle = -70f;
		float maxFireAngle = 80f;

		SpatialComponent spatialComponent = e.getComponent(SpatialComponent.class);
		Spatial spatial = spatialComponent.getSpatial();

		power = MathUtils2.truncate(power, bowComponent.getMinPower(), bowComponent.getMaxPower());

		bowData.setPower(power);

		if (AngleUtils.between(angle, minFireAngle, maxFireAngle))
			spatial.setAngle(angle);

		if (bowData.isRecharging()) {
			bowComponent.setRechargeTime(bowComponent.getRechargeTime() - world.getDelta());
			if (bowComponent.getRechargeTime() > 0)
				return;
			bowData.setReady();
		}

		if (bowData.isCharging()) {
			Trigger trigger = bowComponent.getChargeTrigger();
			if (trigger.isAlreadyTriggered())
				return;
			trigger.trigger(e);
		}

		if (bowData.isFiring()) {
			Trigger fireTrigger = bowComponent.getFireTrigger();
			if (fireTrigger.isAlreadyTriggered())
				return;
			fireTrigger.trigger(e);
			
			System.out.println("FIRING!!");

			bowData.reload();
			bowComponent.setRechargeTime(bowComponent.getRechargeRate());
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