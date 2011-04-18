package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.gdx.input.LibgdxPointer;
import com.gemserk.games.archervsworld.artemis.components.HudButtonComponent;

public class HudButtonSystem extends EntitySystem {

	private final LibgdxPointer pointer;

	@SuppressWarnings("unchecked")
	public HudButtonSystem(LibgdxPointer pointer) {
		super(HudButtonComponent.class, SpatialComponent.class);
		this.pointer = pointer;
	}

	@Override
	protected void begin() {

	}

	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {
		
		for (int i = 0; i < entities.size(); i++) {
			
			Entity entity = entities.get(i);
			
			SpatialComponent spatialComponent = entity.getComponent(SpatialComponent.class);
			HudButtonComponent buttonComponent = entity.getComponent(HudButtonComponent.class);
			
			buttonComponent.setPressed(false);

			if (!pointer.touched) 
				continue;
			
			Vector2 position = spatialComponent.getPosition();
			Vector2 size = spatialComponent.getSize();
			
			Vector2 pointerPosition = pointer.getPosition();
			
			float distance = pointerPosition.tmp().sub(position).len();
			
			if (Math.abs(distance) > size.x)
				continue;

			if (Math.abs(distance) > size.y)
				continue;
			
			buttonComponent.setPressed(true);
			
		}
		
	}

	@Override
	protected boolean checkProcessing() {
		return true;
	}

	@Override
	protected void removed(Entity entity) {
		HudButtonComponent buttonComponent = entity.getComponent(HudButtonComponent.class);
		if (buttonComponent != null)
			entity.removeComponent(buttonComponent);
	}

	@Override
	public void initialize() {

	}

}
