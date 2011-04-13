package com.gemserk.games.archervsworld.box2d;

import com.badlogic.gdx.math.Vector2;

public class Contact {

	public Vector2 normal = new Vector2();

	public boolean inContact = false;

	public void setBox2dContact(com.badlogic.gdx.physics.box2d.Contact contact) {
		this.normal.set(contact.GetWorldManifold().getNormal());
		// other info...
		this.inContact = true;
	}

	public void removeBox2dContact() {
		this.inContact = false;
	}

}
