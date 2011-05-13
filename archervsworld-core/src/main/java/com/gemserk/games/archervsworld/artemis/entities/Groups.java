package com.gemserk.games.archervsworld.artemis.entities;

public class Groups {

	public static final String Bow = "Bow".intern();
	
	public static final String Arrow = "Arrow".intern();
	
	public static final String Enemy = "Enemy".intern();
	
	public static boolean isArrow(String group) {
		return Arrow.equalsIgnoreCase(group);
	}
	
	public static boolean isEnemy(String group) {
		return Enemy.equalsIgnoreCase(group);
	}
	
}
