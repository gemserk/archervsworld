package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.gdx.input.LibgdxPointer;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.artemis.entities.Groups;

public class UpdateBowSystem extends EntitySystem {
	
	private LibgdxPointer pointer;

	private ArcherVsWorldEntityFactory entityFactory;
	
	public UpdateBowSystem(LibgdxPointer pointer, ArcherVsWorldEntityFactory entityFactory) {
		super();
		this.entityFactory = entityFactory;
		this.pointer = pointer;
	}
	
	@Override
	protected void begin() {
		pointer.update();
	}

	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {
		
		entities = world.getGroupManager().getEntities(Groups.Bow);
		
		if (pointer.touched) {
			
			// update bow direction
			
			for (int i = 0; i < entities.size(); i++) {
				Entity entity = entities.get(i);
				SpatialComponent spatialComponent = entity.getComponent(SpatialComponent.class);
				Vector2 direction = pointer.getPressedPosition().cpy().sub(pointer.getPosition());
				spatialComponent.setAngle(direction.angle());
			}
			
		}
		
		if (pointer.wasReleased) {
			
			for (int i = 0; i < entities.size(); i++) {
				
				Entity entity = entities.get(i);
				SpatialComponent spatialComponent = entity.getComponent(SpatialComponent.class);

				System.out.println("new arrow");
				
				Vector2 p0 = pointer.getPressedPosition();
				Vector2 p1 = pointer.getReleasedPosition();
				
				Vector2 mul = p1.cpy().sub(p0).mul(-5f);
				
				float len = mul.len();
				mul.nor();
				
				entityFactory.createPhysicsArrow(spatialComponent.getPosition(), mul, len);
				
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