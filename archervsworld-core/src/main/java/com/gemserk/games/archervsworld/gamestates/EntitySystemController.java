package com.gemserk.games.archervsworld.gamestates;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.gemserk.commons.artemis.systems.ActivableSystem;
import com.gemserk.componentsengine.input.ButtonMonitor;
import com.gemserk.componentsengine.input.LibgdxButtonMonitor;

public class EntitySystemController {

	static class ActivableSystemRegistration {

		ActivableSystem activableSystem;

		ButtonMonitor buttonMonitor;

		String name;

		public ActivableSystemRegistration(ActivableSystem activableSystem, ButtonMonitor buttonMonitor, String name) {
			this.activableSystem = activableSystem;
			this.buttonMonitor = buttonMonitor;
			this.name = name;
		}

		public ActivableSystemRegistration(ActivableSystem activableSystem, int key, String name) {
			this.activableSystem = activableSystem;
			this.buttonMonitor = new LibgdxButtonMonitor(key);
			this.name = name;
		}
	}

	ArrayList<EntitySystemController.ActivableSystemRegistration> registrations = new ArrayList<EntitySystemController.ActivableSystemRegistration>();

	public void register(EntitySystemController.ActivableSystemRegistration registration) {
		registrations.add(registration);
	}

	public void update() {
		for (int i = 0; i < registrations.size(); i++) {

			EntitySystemController.ActivableSystemRegistration registration = registrations.get(i);

			registration.buttonMonitor.update();

			if (registration.buttonMonitor.isPressed()) {
				registration.activableSystem.toggle();
				if (registration.activableSystem.isEnabled()) {
					Gdx.app.log("Archer Vs Zombies", registration.name + " enabled");
				} else {
					Gdx.app.log("Archer Vs Zombies", registration.name + " disabled");
				}
			}

		}
	}

}