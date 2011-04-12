package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.gdx.input.LibgdxPointer;
import com.gemserk.games.archervsworld.artemis.entities.Groups;

public class UpdateBowDirectionSystem extends EntitySystem {
	
	Vector2 pointerPosition2d = new Vector2();

	Vector3 pointerPosition3d = new Vector3();
	
	int pointerIndex = 0;

	private OrthographicCamera camera;

	private LibgdxPointer libgdxPointer;
	
	public UpdateBowDirectionSystem(OrthographicCamera camera) {
		super();
		this.camera = camera;
		libgdxPointer = new LibgdxPointer(pointerIndex, camera);
	}

	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {
		
		entities = world.getGroupManager().getEntities(Groups.Bow);
		
		if (!Gdx.input.isTouched(pointerIndex))
			return;
		
		pointerPosition3d.set(Gdx.input.getX(pointerIndex), Gdx.input.getY(pointerIndex), 0f);

		// transform the point to world coordinates
		camera.unproject(pointerPosition3d);
		
		pointerPosition2d.set(pointerPosition3d.x, pointerPosition3d.y);
		
		for (int i = 0; i < entities.size(); i++) {
			
			Entity entity = entities.get(i);
			SpatialComponent spatialComponent = entity.getComponent(SpatialComponent.class);
			
			Vector2 position = spatialComponent.getPosition();
			
			Vector2 direction = pointerPosition2d.cpy().sub(position);
			
			spatialComponent.setAngle(direction.angle());
			
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