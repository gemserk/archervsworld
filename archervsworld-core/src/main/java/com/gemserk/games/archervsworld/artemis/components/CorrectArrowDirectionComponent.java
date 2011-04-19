package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;

public class CorrectArrowDirectionComponent extends Component {
	
	private boolean disabled = false;
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

}
