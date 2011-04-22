package com.gemserk.games.archervsworld;

import java.util.ArrayList;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.gemserk.commons.gdx.ScreenAdapter;
import com.gemserk.commons.gdx.input.LibgdxPointer;

public class EditorScreen2 extends ScreenAdapter {

	private ImmediateModeRenderer renderer;

	public EditorScreen2(Game game) {

		renderer = new ImmediateModeRenderer();

		spriteBatch = new SpriteBatch();

		texture = new Texture(Gdx.files.internal("data/background-512x512.jpg"));

		pointer0 = new LibgdxPointer(0);

		physicsPolygonShape.add(20, 20);
		physicsPolygonShape.add(20, 40);
		physicsPolygonShape.add(40, 40);
		physicsPolygonShape.add(40, 20);

		// inputMonitor = new LibgdxInputMapperImpl<String>(Gdx.input);
		//
		// inputMonitor.monitorPointerDown("insertPoint", 0);
		// inputMonitor.monitorPointerX("insertPointX", 0);
		// inputMonitor.monitorPointerY("insertPointY", 0);

	}

	GL10 gl = Gdx.graphics.getGL10();

	SpriteBatch spriteBatch;

	private Texture texture;

	private LibgdxPointer pointer0;

	static class PhysicsPolygonShape {

		ArrayList<Vector2> vertices = new ArrayList<Vector2>();

		public void add(float x, float y) {
			vertices.add(new Vector2(x, y));
		}

	}

	PhysicsPolygonShape physicsPolygonShape = new PhysicsPolygonShape();

	// private LibgdxInputMapperImpl<String> inputMonitor;

	@Override
	public void render(float delta) {

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		spriteBatch.begin();
		spriteBatch.draw(texture, 0, 0);
		spriteBatch.end();

		Matrix4 transform = new Matrix4();
		transform.idt();

		gl.glPushMatrix();
		gl.glMultMatrixf(transform.val, 0);

		renderer.begin(GL10.GL_LINE_LOOP);

		for (int i = 0; i < physicsPolygonShape.vertices.size(); i++) {
			Vector2 v = physicsPolygonShape.vertices.get(i);
			renderer.color(1f, 1f, 1f, 1f);
			renderer.vertex(v.x, v.y, 0f);
		}

		renderer.end();

		gl.glPopMatrix();

		// inputMonitor.update();
		//
		// ButtonMonitor insertPointButton = inputMonitor.getButton("insertPoint");
		//
		// if (insertPointButton.isPressed()) {
		//
		// System.out.println("x = " + inputMonitor.getAnalog("insertPointX").getValue());
		// System.out.println("y = " + inputMonitor.getAnalog("insertPointY").getValue());
		//
		// }

	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void show() {

	}

	@Override
	public void dispose() {

	}

	// static interface InputMapper<K> {
	//
	// void monitorKey(K id, int keyCode);
	//
	// void monitorPointerDown(K id, int pointer);
	//
	// void monitorPointerX(K id, int pointer);
	//
	// void monitorPointerY(K id, int pointer);
	//
	// }
	//
	// static class LibgdxInputMapperImpl<K> extends InputDevicesMonitorImpl<K> implements InputMapper<K> {
	//
	// private final Input input;
	//
	// public LibgdxInputMapperImpl(Input input) {
	// this.input = input;
	// }
	//
	// @Override
	// public void monitorKey(K id, final int keyCode) {
	// button(id, new ButtonMonitor() {
	// @Override
	// protected boolean isDown() {
	// return input.isKeyPressed(keyCode);
	// }
	// });
	// }
	//
	// @Override
	// public void monitorPointerDown(K id, final int pointer) {
	// button(id, new ButtonMonitor() {
	// @Override
	// protected boolean isDown() {
	// return input.isTouched(pointer);
	// }
	// });
	// }
	//
	// @Override
	// public void monitorPointerX(K id, final int pointer) {
	// analog(id, new AnalogInputMonitor() {
	// @Override
	// protected float newValue() {
	// if (!input.isTouched(pointer))
	// return getValue();
	// return input.getX(pointer);
	// }
	// });
	// }
	//
	// @Override
	// public void monitorPointerY(K id, final int pointer) {
	// analog(id, new AnalogInputMonitor() {
	// @Override
	// protected float newValue() {
	// if (!input.isTouched(pointer))
	// return getValue();
	// return input.getY(pointer);
	// }
	// });
	// }
	//
	// }
}
