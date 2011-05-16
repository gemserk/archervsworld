package com.gemserk.games.archervsworld.artemis.components;

import com.artemis.Component;

public class SpawnerComponent extends Component {
	
	private int count;
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public SpawnerComponent(int count) {
		this.count = count;
	}

}
