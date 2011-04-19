package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;

public class ArrowComponent extends Component {
	
	private boolean disabled = false;
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

}
