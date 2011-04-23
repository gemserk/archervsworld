package com.gemserk.games.archervsworld.artemis;

import org.junit.Test;

import com.artemis.Entity;
import com.artemis.EntityProcessingSystem;
import com.artemis.EntitySystem;
import com.artemis.World;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.artemis.components.AliveComponent;
import com.gemserk.commons.artemis.components.MovementComponent;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.systems.AliveSystem;
import com.gemserk.commons.artemis.systems.MovementSystem;
import com.gemserk.componentsengine.timers.CountDownTimer;
import com.gemserk.games.archervsworld.WorldWrapper;

public class SomeTest {

	@Test
	public void test() {

		World world = new World();

		WorldWrapper worldWrapper = new WorldWrapper(world);
		
		worldWrapper.add(new AliveSystem());
		
		worldWrapper.add(new MovementSystem());

		worldWrapper.add(new EntityProcessingSystem(SpatialComponent.class) {

			@Override
			public void initialize() {

			}

			@Override
			protected void process(Entity e) {
				SpatialComponent spatialComponent = e.getComponent(SpatialComponent.class);
				System.out.println(spatialComponent.getPosition());
			}

		});

		worldWrapper.add(new EntityProcessingSystem(SpatialComponent.class) {

			@Override
			public void initialize() {

			}

			@Override
			protected void process(Entity e) {
				SpatialComponent spatialComponent = e.getComponent(SpatialComponent.class);
				if (spatialComponent.getPosition().x > 100) {
					System.out.println("removing entity");
					world.deleteEntity(e);
				}
			}

		});

		worldWrapper.add(new EntitySystem() {

			CountDownTimer countDownTimer = new CountDownTimer(0, false);

			@Override
			protected void processEntities(ImmutableBag<Entity> entities) {

				if (countDownTimer.update(world.getDelta())) {

					Entity entity = world.createEntity();

					if (MathUtils.randomBoolean()) {
						entity.addComponent(new SpatialComponent(new Vector2(10, MathUtils.random(0f, 20f)), new Vector2(1, 1), 0f));
						entity.addComponent(new MovementComponent(new Vector2(5, 0), 0f));
					} else {
						entity.addComponent(new AliveComponent(3000));
					}

					entity.refresh();

				}

				if (!countDownTimer.isRunning()) {

					countDownTimer = new CountDownTimer(1000, true);

				}

			}

			@Override
			protected boolean checkProcessing() {
				return true;
			}

			@Override
			public void initialize() {

			}

		});

		

		worldWrapper.init();

		Entity entity = world.createEntity();

		entity.addComponent(new SpatialComponent(new Vector2(10, 10), new Vector2(1, 1), 0f));
		entity.addComponent(new MovementComponent(new Vector2(5, 0), 0f));

		entity.refresh();
		
		while (true) {
//		for (int i = 0; i < 10000; i++) {
			worldWrapper.update(16);
		}

	}

}
