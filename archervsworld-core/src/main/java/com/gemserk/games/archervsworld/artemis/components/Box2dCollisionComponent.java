package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;
import com.artemis.Entity;
import com.gemserk.componentsengine.properties.Property;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.games.archervsworld.box2d.Contact;

public class Box2dCollisionComponent extends Component {

	private Property<Entity> entity;
	
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
	
	/**
	 * The entity we are in contact to. 
	 */
	public Entity getEntity() {
		return entity.get();
	}
	
	public void setEntity(Entity entity) {
		this.entity.set(entity);
	}

	public Box2dCollisionComponent() {
		this(new SimpleProperty<Entity>(null), new SimpleProperty<Contact>(new Contact()));
	}

	public Box2dCollisionComponent(Property<Entity> entity, Property<Contact> contact) {
		this.entity = entity;
		this.contact = contact;
	}

}
