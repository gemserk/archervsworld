package com.gemserk.games.archervsworld;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

public class MainAndroidApplication extends AndroidApplication  {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(new LibgdxGame(), false);
	}
	
}
