package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.gemserk.games.archervsworld.artemis.components.InformationComponent;

public class DebugInformationSystem extends EntityProcessingSystem {
	
	public DebugInformationSystem() {
		super(InformationComponent.class);
	}

	@Override
	protected void process(Entity e) {
		
	}
	
	@Override
	protected void added(Entity e) {
		
		InformationComponent c = e.getComponent(InformationComponent.class);
		System.out.println("added entity to world: " + c.getData());
		
	}
	
	@Override
	protected void removed(Entity e) {
		
		InformationComponent c = e.getComponent(InformationComponent.class);
		System.out.println("removed entity from world: " + c.getData());
		
	}

}