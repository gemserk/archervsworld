package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;

public class InformationComponent extends Component {

	private String data;
	
	public String getData() {
		return data;
	}

	public InformationComponent(String data) {
		this.data = data;
	}

}
