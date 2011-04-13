package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.gdx.input.LibgdxPointer;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.games.archervsworld.artemis.components.BowComponent;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.artemis.entities.Groups;

public class UpdateBowSystem extends EntitySystem {
	
	private LibgdxPointer pointer;

	private ArcherVsWorldEntityFactory entityFactory;
	
	public UpdateBowSystem(LibgdxPointer pointer, ArcherVsWorldEntityFactory entityFactory) {
		super(BowComponent.class);
		this.entityFactory = entityFactory;
		this.pointer = pointer;
	}
	
	@Override
	protected void begin() {
		pointer.update();
	}
	
	AngleUtils angleUtils = new AngleUtils();
	
	Vector2 direction = new Vector2();
	
	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {
		
		entities = world.getGroupManager().getEntities(Groups.Bow);
		
		if (pointer.touched) {
			
			// update bow direction
			
			for (int i = 0; i < entities.size(); i++) {
				Entity entity = entities.get(i);
				SpatialComponent spatialComponent = entity.getComponent(SpatialComponent.class);
				Vector2 direction = pointer.getPressedPosition().cpy().sub(pointer.getPosition());
				
				BowComponent bowComponent = entity.getComponent(BowComponent.class);
				
				float angle = direction.angle();
				
				int minFireAngle = -70;
				int maxFireAngle = 80;
				
				if (bowComponent.getArrow() == null) {
				
					Entity arrow = entityFactory.createArrow(spatialComponent.getPositionProperty(), spatialComponent.getAngleProperty());
					bowComponent.setArrow(arrow);
					
				}
				
//				bowComponent.setShouldFire(true);

				if ((angleUtils.minimumDifference(angle, minFireAngle) < 0) && (angleUtils.minimumDifference(angle, maxFireAngle) > 0)) {
					spatialComponent.setAngle(angle);
				}
				
			}
			
		}
		
		if (pointer.wasReleased) {
			
			for (int i = 0; i < entities.size(); i++) {
				
				Entity entity = entities.get(i);
				SpatialComponent spatialComponent = entity.getComponent(SpatialComponent.class);
				BowComponent bowComponent = entity.getComponent(BowComponent.class);
				
				if (bowComponent.getArrow() == null)
					continue;

				Vector2 p0 = pointer.getPressedPosition();
				Vector2 p1 = pointer.getReleasedPosition();
				
				Vector2 mul = p1.cpy().sub(p0).mul(-5f);
				
				float len = mul.len();
				mul.nor();
				
				direction.set(1f,0f);
				direction.rotate(spatialComponent.getAngle());
				
				entityFactory.createPhysicsArrow(spatialComponent.getPosition(), direction, len);
				
				world.deleteEntity(bowComponent.getArrow());
				bowComponent.setArrow(null);
				
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