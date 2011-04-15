package com.gemserk.games.archervsworld;

import java.util.ArrayList;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.entities.EntityFactory;
import com.gemserk.commons.artemis.systems.SpriteRendererSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.systems.TextRendererSystem;
import com.gemserk.commons.gdx.Libgdx2dCamera;
import com.gemserk.commons.gdx.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.ScreenAdapter;
import com.gemserk.commons.gdx.input.LibgdxPointer;
import com.gemserk.commons.values.FloatValue;
import com.gemserk.commons.values.IntValue;
import com.gemserk.componentsengine.input.ButtonMonitor;
import com.gemserk.componentsengine.input.LibgdxButtonMonitor;
import com.gemserk.componentsengine.input.MonitorUpdater;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.artemis.systems.GameLogicSystem;
import com.gemserk.games.archervsworld.artemis.systems.PhysicsSystem;
import com.gemserk.games.archervsworld.artemis.systems.UpdateBowSystem;
import com.gemserk.games.archervsworld.artemis.systems.WalkingDeadSystem;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;
import com.gemserk.resources.dataloaders.StaticDataLoader;
import com.gemserk.resources.resourceloaders.CachedResourceLoader;
import com.gemserk.resources.resourceloaders.ResourceLoaderImpl;

public class GameScreen extends ScreenAdapter {

	static class MonitorUpdaterImpl implements MonitorUpdater {

		ArrayList<ButtonMonitor> buttonMonitors = new ArrayList<ButtonMonitor>();

		@Override
		public void update() {
			for (int i = 0; i < buttonMonitors.size(); i++) {
				ButtonMonitor buttonMonitor = buttonMonitors.get(i);
				buttonMonitor.update();
			}
		}

		public void add(ButtonMonitor buttonMonitor) {
			buttonMonitors.add(buttonMonitor);
		}

	}

	private final Game game;

	private TextRendererSystem textRendererSystem;

	private World world;

	private PhysicsSystem physicsSystem;

	private SpriteRendererSystem spriteRenderSystem;

	private SpriteUpdateSystem spriteUpdateSystem;

	private OrthographicCamera camera;

	private com.badlogic.gdx.physics.box2d.World physicsWorld;

	int viewportWidth = 20;

	int viewportHeight = 12;

	Box2DDebugRenderer renderer = new Box2DDebugRenderer();

	ArcherVsWorldEntityFactory archerVsWorldEntityFactory;

	ResourceManager<String> resourceManager = new ResourceManagerImpl<String>();

	public GameScreen(Game game) {
		this.game = game;

		loadResources();

		entityFactory = new EntityFactory();
		archerVsWorldEntityFactory = new ArcherVsWorldEntityFactory();

		camera = new OrthographicCamera(viewportWidth, viewportHeight);
		camera.position.set(viewportWidth / 2, viewportHeight / 2, 0);

		restart();

	}

	protected void restart() {

		myCamera = new Libgdx2dCameraTransformImpl(camera);

		textRendererSystem = new TextRendererSystem();
		spriteRenderSystem = new SpriteRendererSystem(myCamera);
		spriteUpdateSystem = new SpriteUpdateSystem();

		Vector2 gravity = new Vector2(0f, -10f);
		physicsSystem = new PhysicsSystem(new com.badlogic.gdx.physics.box2d.World(gravity, true));

		updateBowSystem = new UpdateBowSystem(new LibgdxPointer(0, myCamera), archerVsWorldEntityFactory);
		updateBowSystem.setResourceManager(resourceManager);

		walkingDeadSystem = new WalkingDeadSystem();
		gameLogicSystem = new GameLogicSystem();

		gameLogicSystem.setArcherVsWorldEntityFactory(archerVsWorldEntityFactory);
		gameLogicSystem.setResourceManager(resourceManager);

		world = new World();
		world.getSystemManager().setSystem(textRendererSystem);
		world.getSystemManager().setSystem(spriteRenderSystem);
		world.getSystemManager().setSystem(spriteUpdateSystem);
		world.getSystemManager().setSystem(physicsSystem);
		world.getSystemManager().setSystem(updateBowSystem);
		world.getSystemManager().setSystem(walkingDeadSystem);
		world.getSystemManager().setSystem(gameLogicSystem);
		world.getSystemManager().initializeAll();

		entityFactory.setWorld(world);

		Resource<BitmapFont> fontResource = resourceManager.get("Font");

		entityFactory.fpsEntity( //
				new SimpleProperty<Vector2>(new Vector2(0.5f, 0.5f)), //
				new SimpleProperty<BitmapFont>(fontResource.get()), //
				new SimpleProperty<Vector2>(new Vector2(10, Gdx.graphics.getHeight() - 20)));

		physicsWorld = physicsSystem.getPhysicsWorld();

		archerVsWorldEntityFactory.setWorld(world);
		archerVsWorldEntityFactory.setPhysicsWorld(physicsWorld);
		archerVsWorldEntityFactory.setResourceManager(resourceManager);

		// I NEED AN EDITOR FOR ALL THIS STUFF!!

		Vector2 grassSize = new Vector2(0.5f, 0.5f);

		float x = 0f;
		float y = 0f;

		archerVsWorldEntityFactory.createGround(new Vector2(40f, 0.22f), new Vector2(80f, 0.44f));

		for (int i = 0; i < 60; i++) {
			archerVsWorldEntityFactory.createGrass(new Vector2(x + grassSize.x / 2f, y + grassSize.y / 2f), grassSize);
			x += grassSize.x;
		}

		// archerVsWorldEntityFactory.createRock(new Vector2(5, 3), new Vector2(3f, 3f), new Vector2(0f, 0f), 120f);

		// archerVsWorldEntityFactory.createRock(new Vector2(7, 1), new Vector2(2f, 2f), new Vector2(0f, 0f), 210f);

		// archerVsWorldEntityFactory.createTree(new Vector2(15, 4.1f), new Vector2(8f, 8f));

		// archerVsWorldEntityFactory.createRock(new Vector2(10, 10), new Vector2(1f, 1f), new Vector2(0f, 0f), 50f);

		archerVsWorldEntityFactory.createWalkingDead(new Vector2(20, 1.25f + y), new Vector2(0.5f, 2f), new Vector2(-1.2f, 0f));

		archerVsWorldEntityFactory.createWalkingDead(new Vector2(18, 1.25f + y), new Vector2(0.5f, 1.9f), new Vector2(-1.2f, 0f));

		archerVsWorldEntityFactory.createWalkingDead(new Vector2(16, 1.25f + y), new Vector2(0.5f, 2.1f), new Vector2(-1.2f, 0f));

		createBackground();

		archerVsWorldEntityFactory.createBow(new Vector2(1f, 1.7f));

		monitorUpdater = new MonitorUpdaterImpl();
		monitorUpdater.add(restartButtonMonitor);
		monitorUpdater.add(zoomInButtonMonitor);
		monitorUpdater.add(zoomOutButtonMonitor);

		monitorUpdater.add(moveLeftMonitor);
		monitorUpdater.add(moveRightMonitor);

		monitorUpdater.add(moveUpMonitor);
		monitorUpdater.add(moveDownMonitor);

	}

	public void createBackground() {

		Entity entity = world.createEntity();

		Texture texture = new Texture(Gdx.files.internal("data/background-512x512.jpg"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		int layer = -10;

		entity.addComponent(new SpatialComponent( //
				new SimpleProperty<Vector2>(new Vector2(0f, 0f)), //
				new SimpleProperty<Vector2>(new Vector2(camera.viewportWidth, camera.viewportHeight)), //
				new SimpleProperty<FloatValue>(new FloatValue(0f))));
		entity.addComponent(new SpriteComponent(new SimpleProperty<Sprite>(new Sprite(texture)), //
				new SimpleProperty<IntValue>(new IntValue(layer)), //
				new SimpleProperty<Vector2>(new Vector2(0f, 0f))));

		entity.refresh();

	}

	private UpdateBowSystem updateBowSystem;

	private EntityFactory entityFactory;

	private ButtonMonitor restartButtonMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_R);

	private ButtonMonitor zoomInButtonMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_PLUS);

	private ButtonMonitor zoomOutButtonMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_MINUS);

	private ButtonMonitor moveRightMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_DPAD_RIGHT);

	private ButtonMonitor moveLeftMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_DPAD_LEFT);

	private ButtonMonitor moveUpMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_DPAD_UP);

	private ButtonMonitor moveDownMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_DPAD_DOWN);

	private MonitorUpdaterImpl monitorUpdater;

	private WalkingDeadSystem walkingDeadSystem;

	private FloatValue zoom = new FloatValue(1f);

	private GameLogicSystem gameLogicSystem;

	private Libgdx2dCamera myCamera;

	@Override
	public void render(float delta) {

		camera.update();
		camera.apply(Gdx.gl10);

		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		world.loopStart();
		world.setDelta((int) (delta * 1000));

		updateBowSystem.process();
		walkingDeadSystem.process();

		physicsSystem.process();
		gameLogicSystem.process();

		spriteUpdateSystem.process();
		spriteRenderSystem.process();
		textRendererSystem.process();

		camera.update();
		camera.apply(Gdx.gl10);

		if (Gdx.input.isKeyPressed(Input.Keys.KEYCODE_D))
			renderer.render(physicsWorld);

		monitorUpdater.update();
		Synchronizers.synchronize();

		if (zoomInButtonMonitor.isHolded()) {
			zoom.value = zoom.value + 1f * delta;
			myCamera.zoom(zoom.value);
			// Synchronizers.transition(zoom, Transitions.transitionBuilder(zoom).end(nextZoom).time(300).build());
		}

		if (zoomOutButtonMonitor.isHolded()) {
			zoom.value = zoom.value - 1f * delta;
			myCamera.zoom(zoom.value);
			// Synchronizers.transition(zoom, Transitions.transitionBuilder(zoom).end(nextZoom).time(300).build());
		}

		if (moveDownMonitor.isHolded()) {
			myCamera.rotate(36f * delta);
		}

		if (moveUpMonitor.isHolded()) {
			myCamera.rotate(-36f * delta);
		}

		if (moveRightMonitor.isHolded()) {
			myCamera.move(-2f * delta, 0f);
		}

		if (moveLeftMonitor.isHolded()) {
			myCamera.move(2f * delta, 0f);
		}

		if (restartButtonMonitor.isReleased())
			restart();

		// if (Gdx.input.justTouched()) {
		// Vector2 position = new Vector2(Gdx.input.getX(), Gdx.input.getY());
		// System.out.println("local position: " + position);
		// myCamera.unproject(position);
		// System.out.println("world position: " + position);
		// }

	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void show() {

	}

	protected void loadResources() {

		Texture rockTexture = new Texture(Gdx.files.internal("data/rock-512x512.png"));
		rockTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		resourceManager.add("Rock", new CachedResourceLoader<Texture>(new ResourceLoaderImpl<Texture>(new StaticDataLoader<Texture>(rockTexture) {
			@Override
			public void dispose(Texture t) {
				t.dispose();
			}
		}, false)));

		Texture bowTexture = new Texture(Gdx.files.internal("data/bow-512x512.png"));
		bowTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		resourceManager.add("Bow", new CachedResourceLoader<Texture>(new ResourceLoaderImpl<Texture>(new StaticDataLoader<Texture>(bowTexture) {
			@Override
			public void dispose(Texture t) {
				t.dispose();
			}
		}, false)));

		Texture arrowTexture = new Texture(Gdx.files.internal("data/arrow-512x512.png"));
		arrowTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		resourceManager.add("Arrow", new CachedResourceLoader<Texture>(new ResourceLoaderImpl<Texture>(new StaticDataLoader<Texture>(arrowTexture) {
			@Override
			public void dispose(Texture t) {
				t.dispose();
			}
		}, false)));

		Texture treeTexture = new Texture(Gdx.files.internal("data/tree-512x512.png"));
		treeTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		resourceManager.add("Tree", new CachedResourceLoader<Texture>(new ResourceLoaderImpl<Texture>(new StaticDataLoader<Texture>(treeTexture) {
			@Override
			public void dispose(Texture t) {
				t.dispose();
			}
		}, false)));

		Texture grassTexture = new Texture(Gdx.files.internal("data/grass-128x128.png"));
		grassTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		resourceManager.add("Grass", new CachedResourceLoader<Texture>(new ResourceLoaderImpl<Texture>(new StaticDataLoader<Texture>(grassTexture) {
			@Override
			public void dispose(Texture t) {
				t.dispose();
			}
		}, false)));

		Texture fontTexture = new Texture(Gdx.files.internal("data/font.png"));
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		resourceManager.add("FontTexture", new CachedResourceLoader<Texture>(new ResourceLoaderImpl<Texture>(new StaticDataLoader<Texture>(fontTexture) {
			@Override
			public void dispose(Texture t) {
				t.dispose();
			}
		}, false)));

		resourceManager.add("Font", new CachedResourceLoader<BitmapFont>(new ResourceLoaderImpl<BitmapFont>(new StaticDataLoader<BitmapFont>(new BitmapFont(Gdx.files.internal("data/font.fnt"), new Sprite(fontTexture), false)) {
			@Override
			public void dispose(BitmapFont t) {
				t.dispose();
			}
		}, false)));

		resourceManager.add("HitFleshSound", new CachedResourceLoader<Sound>(new ResourceLoaderImpl<Sound>( //
				new StaticDataLoader<Sound>(Gdx.audio.newSound(Gdx.files.internal("data/hit-flesh.ogg"))) {
					@Override
					public void dispose(Sound t) {
						t.dispose();
					}
				}, false)));

		resourceManager.add("HitGroundSound", new CachedResourceLoader<Sound>(new ResourceLoaderImpl<Sound>( //
				new StaticDataLoader<Sound>(Gdx.audio.newSound(Gdx.files.internal("data/hit-ground.ogg"))) {
					@Override
					public void dispose(Sound t) {
						t.dispose();
					}
				}, false)));

		resourceManager.add("BowSound", new CachedResourceLoader<Sound>(new ResourceLoaderImpl<Sound>( //
				new StaticDataLoader<Sound>(Gdx.audio.newSound(Gdx.files.internal("data/bow.ogg"))) {
					@Override
					public void dispose(Sound t) {
						t.dispose();
					}
				}, false)));

	}

	@Override
	public void dispose() {

	}

}
