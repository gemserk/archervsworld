package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;
import com.gemserk.componentsengine.properties.Property;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.games.archervsworld.box2d.Contact;

public class CollisionComponent extends Component {

	private Property<Contact> contact;
	
	/**
	 * The current contact.
	 */
	public Contact getContact() {
		return contact.get();
	}
	
	public void setContact(Contact contact) {
		this.contact.set(contact);
	}
	
	public CollisionComponent() {
		this(new SimpleProperty<Contact>(new Contact()));
	}

	public CollisionComponent(Property<Contact> contact) {
		this.contact = contact;
	}

}
