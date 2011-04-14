package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;
import com.badlogic.gdx.physics.box2d.Body;
import com.gemserk.componentsengine.properties.Property;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.games.archervsworld.box2d.Contact;

public class PhysicsComponent extends Component {

	private Property<Body> body;
	
	private Property<Contact> contact;
	
	public Body getBody() {
		return body.get();
	}
	
	public Contact getContact() {
		return contact.get();
	}
	
	public void setContact(Contact contact) {
		this.contact.set(contact);
	}
	
	public PhysicsComponent(Property<Body> body) {
		this(body, new SimpleProperty<Contact>(new Contact()));
	}
	
	public PhysicsComponent(Property<Body> body, Property<Contact> contact) {
		this.body = body;
		this.contact = contact;
	}

}
