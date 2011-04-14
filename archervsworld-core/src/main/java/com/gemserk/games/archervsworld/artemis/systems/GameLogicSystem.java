package com.gemserk.games.archervsworld.artemis.systems;

import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.componentsengine.utils.AngleUtils;
import com.gemserk.games.archervsworld.artemis.components.CollisionComponent;
import com.gemserk.games.archervsworld.artemis.components.PhysicsComponent;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.artemis.entities.Groups;
import com.gemserk.games.archervsworld.box2d.Contact;

public class GameLogicSystem extends EntitySystem {
	
	ArcherVsWorldEntityFactory archerVsWorldEntityFactory;
	
	AngleUtils angleUtils = new AngleUtils();
	
	public void setArcherVsWorldEntityFactory(ArcherVsWorldEntityFactory archerVsWorldEntityFactory) {
		this.archerVsWorldEntityFactory = archerVsWorldEntityFactory;
	}

	public GameLogicSystem() {
		super();
	}

	@Override
	protected void processEntities(ImmutableBag<Entity> entities) {

		entities = world.getGroupManager().getEntities(Groups.Arrow);
		
		if (entities == null)
			return;

		for (int i = 0; i < entities.size(); i++) {

			Entity entity = entities.get(i);

			PhysicsComponent physicsComponent = entity.getComponent(PhysicsComponent.class);
			Body body = physicsComponent.getBody();
			
			CollisionComponent collisionComponent = entity.getComponent(CollisionComponent.class);

			Contact contact = collisionComponent.getContact();
			
			if (!contact.inContact) {

				Vector2 linearVelocity = body.getLinearVelocity();
				float angle = linearVelocity.angle();
				body.setTransform(body.getPosition(), (float) (angle / 180f * Math.PI));

				continue;
			}
			
			Vector2 normal = contact.normal;
			
			float normalAngle = normal.cpy().mul(-1f).angle();
			
			float bodyAngle = (float) (body.getAngle() * 180.0 / Math.PI);
			
			double diff = Math.abs(angleUtils.minimumDifference(normalAngle, bodyAngle));
			
			int stickAngle = 45;
			
			if (diff < stickAngle || !body.isAwake() )  {
				SpatialComponent component = entity.getComponent(SpatialComponent.class);
				archerVsWorldEntityFactory.createArrow(component.getPosition(), component.getAngle());
				this.world.deleteEntity(entity);
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