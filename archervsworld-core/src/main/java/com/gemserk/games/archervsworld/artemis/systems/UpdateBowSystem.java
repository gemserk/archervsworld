package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.componentsengine.properties.AbstractProperty;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.games.archervsworld.artemis.components.BowComponent;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.artemis.entities.Groups;
import com.gemserk.games.archervsworld.controllers.BowController;
import com.gemserk.games.archervsworld.controllers.ControllerSwitcher;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

public class UpdateBowSystem extends EntitySystem {

	static class ChargingArrowProperty extends AbstractProperty<Vector2> {

		private final BowComponent bowComponent;

		private final SpatialComponent spatialComponent;

		Vector2 position = new Vector2();

		Vector2 diff = new Vector2();

		ChargingArrowProperty(BowComponent bowComponent, SpatialComponent spatialComponent) {
			this.bowComponent = bowComponent;
			this.spatialComponent = spatialComponent;
		}

		@Override
		public Vector2 get() {
			position.set(spatialComponent.getPositionProperty().get());

			diff.set(1f, 0f);
			diff.rotate(spatialComponent.getAngle());
			diff.mul(bowComponent.getPower() * 0.012f);

			position.sub(diff);

			return position;
		}
	}

	private ArcherVsWorldEntityFactory entityFactory;

	ResourceManager<String> resourceManager;

	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}

	ControllerSwitcher controllerSwitcher;

	@SuppressWarnings("unchecked")
	public UpdateBowSystem(ControllerSwitcher controllerSwitcher, ArcherVsWorldEntityFactory entityFactory) {
		super(BowComponent.class);
		this.entityFactory = entityFactory;
		this.controllerSwitcher = controllerSwitcher;
//
//		ArrayList<BowController> controllers = new ArrayList<BowController>();
//
//		controllers.add(new BowControllerImpl(pointer));
//		controllers.add(new BowControllerImpl2(pointer, new Vector2(1f, 1f)));
//		controllers.add(new BowControllerImpl3(pointer));
//		controllers.add(new BowControllerImpl4(pointer, new Vector2(1f, 1f)));
//		controllers.add(new BowControllerImpl5(pointer, new Vector2(1f, 1f)));
//		
//		if (Gdx.input.isPeripheralAvailable(Peripheral.MultitouchScreen))
//			controllers.add(new BowControllerMutitouchImpl(pointer, new LibgdxPointer(1, pointer.getCamera())));
//		
//		if (Gdx.input.isPeripheralAvailable(Peripheral.HardwareKeyboard))
//			controllers.add(new BowControllerKeyboardImpl(Input.Keys.KEYCODE_DPAD_UP, Input.Keys.KEYCODE_DPAD_DOWN, Input.Keys.KEYCODE_SPACE));
//
//		controllerSwitcher = new ControllerSwitcher(controllers);
	}

	@Override
	protected void begin() {
		controllerSwitcher.update();
		bowController = controllerSwitcher.getController();
		bowController.update();
	}

	AngleUtils angleUtils = new AngleUtils();

	Vector2 direction = new Vector2();

	BowController bowController;

	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {

		entities = world.getGroupManager().getEntities(Groups.Bow);

		if (entities == null)
			return;

		if (bowController.isCharging()) {

			// update bow direction

			for (int i = 0; i < entities.size(); i++) {
				Entity entity = entities.get(i);
				final SpatialComponent spatialComponent = entity.getComponent(SpatialComponent.class);
				// Vector2 direction = pointer.getPressedPosition().cpy().sub(pointer.getPosition());

				final BowComponent bowComponent = entity.getComponent(BowComponent.class);

				float angle = bowController.getAngle();

				int minFireAngle = -70;
				int maxFireAngle = 80;

				// angle = truncate(bowController.getAngle(), minFireAngle, maxFireAngle);

				float power = truncate(bowController.getPower(), bowComponent.getMinPower(), bowComponent.getMaxPower());

				bowComponent.setPower(power);

				if (bowComponent.getArrow() == null) {

					// TODO: add it as a child using scene graph component so transformations will be handled automatically

					Entity arrow = entityFactory.createArrow(new ChargingArrowProperty(bowComponent, spatialComponent), //
							spatialComponent.getAngleProperty());

					bowComponent.setArrow(arrow);

				}

				if ((angleUtils.minimumDifference(angle, minFireAngle) < 0) && (angleUtils.minimumDifference(angle, maxFireAngle) > 0)) {
					spatialComponent.setAngle(angle);
				}

			}

		}

		if (bowController.shouldFire()) {

			for (int i = 0; i < entities.size(); i++) {

				Entity entity = entities.get(i);
				BowComponent bowComponent = entity.getComponent(BowComponent.class);

				if (bowComponent.getArrow() == null)
					continue;

				float power = bowComponent.getPower();

				Gdx.app.log("Archer vs Zombies", "Bow power: " + power);

				Entity arrow = bowComponent.getArrow();
				SpatialComponent arrowSpatialComponent = arrow.getComponent(SpatialComponent.class);

				direction.set(1f, 0f);
				direction.rotate(arrowSpatialComponent.getAngle());

				entityFactory.createPhysicsArrow(arrowSpatialComponent.getPosition(), direction, power);

				world.deleteEntity(arrow);
				bowComponent.setArrow(null);

				Resource<Sound> sound = resourceManager.get("BowSound");
				sound.get().play(1f);

			}

		}

	}

	public float truncate(float a, float min, float max) {
		if (a < min)
			a = min;
		if (a > max)
			a = max;
		return a;
	}

	@Override
	public void initialize() {

	}

	@Override
	protected boolean checkProcessing() {
		return true;
	}
}