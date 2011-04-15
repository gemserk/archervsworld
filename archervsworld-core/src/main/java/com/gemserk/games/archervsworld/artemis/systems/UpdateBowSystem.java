package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.gdx.input.LibgdxPointer;
import com.gemserk.componentsengine.properties.AbstractProperty;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.games.archervsworld.artemis.components.BowComponent;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.artemis.entities.Groups;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;

public class UpdateBowSystem extends EntitySystem {
	
	static class BowController {
		
		private float angle;
		
		private float power;
		
		private boolean charging;
		
		private boolean firing;
		
		public float getAngle() {
			return angle;
		}
		
		public float getPower() {
			return power;
		}
		
		public boolean isCharging() {
			return charging;
		}
		
		public boolean isFiring() {
			return firing;
		}
		
		///
		
		private LibgdxPointer pointer;
		
		public BowController(LibgdxPointer pointer) {
			this.pointer = pointer;
		}
		
		public void update() {
			
			pointer.update();
			
			firing = false;
			
			if (pointer.touched) {
				Vector2 p0 = pointer.getPressedPosition();
				Vector2 p1 = pointer.getPosition();
				
				Vector2 direction = p0.cpy().sub(p1);
				
				// the power multiplier
				float multiplier = 3f;
				
				angle = direction.angle();
				power = direction.len() * multiplier; 
				
				charging = true;
			} 
			
			if (pointer.wasReleased) {
				charging = false;
				firing = true;
			}
			
		}
		
	}
	
	static class ChargingArrowProperty extends AbstractProperty<Vector2> {
		
		private final BowComponent bowComponent;
		
		private final SpatialComponent spatialComponent;
		
		Vector2 position= new Vector2();
		
		Vector2 diff = new Vector2();

		ChargingArrowProperty(BowComponent bowComponent, SpatialComponent spatialComponent) {
			this.bowComponent = bowComponent;
			this.spatialComponent = spatialComponent;
		}

		@Override
		public Vector2 get() {
			position.set(spatialComponent.getPositionProperty().get());
			
			diff.set(1f,0f);
			diff.rotate(spatialComponent.getAngle());
			diff.mul(bowComponent.getPower() * 0.005f);
			
			position.sub(diff);
			
			return position;
		}
	}

	private ArcherVsWorldEntityFactory entityFactory;
	
	ResourceManager<String> resourceManager;
	
	public void setResourceManager(ResourceManager<String> resourceManager) {
		this.resourceManager = resourceManager;
	}
	
	@SuppressWarnings("unchecked")
	public UpdateBowSystem(LibgdxPointer pointer, ArcherVsWorldEntityFactory entityFactory) {
		super(BowComponent.class);
		this.entityFactory = entityFactory;
		bowController = new BowController(pointer);
	}
	
	@Override
	protected void begin() {
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
		
		if (bowController.isFiring()) {

			for (int i = 0; i < entities.size(); i++) {
				
				Entity entity = entities.get(i);
				BowComponent bowComponent = entity.getComponent(BowComponent.class);
				
				if (bowComponent.getArrow() == null)
					continue;

				float power = bowComponent.getPower();
				
				Entity arrow = bowComponent.getArrow();
				SpatialComponent arrowSpatialComponent = arrow.getComponent(SpatialComponent.class);

				direction.set(1f,0f);
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