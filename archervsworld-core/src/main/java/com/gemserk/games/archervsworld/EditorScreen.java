package com.gemserk.games.archervsworld;

import java.util.ArrayList;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Peripheral;
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
import com.gemserk.commons.artemis.systems.AliveSystem;
import com.gemserk.commons.artemis.systems.HierarchySystem;
import com.gemserk.commons.artemis.systems.Layer;
import com.gemserk.commons.artemis.systems.PointerUpdateSystem;
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
import com.gemserk.componentsengine.input.MonitorUpdater;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.artemis.systems.HudButtonSystem;
import com.gemserk.games.archervsworld.artemis.systems.PhysicsSystem;
import com.gemserk.games.archervsworld.controllers.BowController;
import com.gemserk.games.archervsworld.controllers.BowControllerImpl;
import com.gemserk.games.archervsworld.controllers.BowControllerImpl2;
import com.gemserk.games.archervsworld.controllers.BowControllerImpl3;
import com.gemserk.games.archervsworld.controllers.BowControllerImpl4;
import com.gemserk.games.archervsworld.controllers.BowControllerImpl5;
import com.gemserk.games.archervsworld.controllers.BowControllerKeyboardImpl;
import com.gemserk.games.archervsworld.controllers.BowControllerMutitouchImpl;
import com.gemserk.games.archervsworld.controllers.ControllerSwitcher;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;
import com.gemserk.resources.dataloaders.StaticDataLoader;
import com.gemserk.resources.resourceloaders.CachedResourceLoader;
import com.gemserk.resources.resourceloaders.ResourceLoaderImpl;

public class EditorScreen extends ScreenAdapter {

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

	public EditorScreen(Game game) {
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

		ArrayList<Layer> layers = new ArrayList<Layer>();

		// background layer
		layers.add(new Layer(-1000, -5, new Libgdx2dCameraTransformImpl()));

		// world layer
		layers.add(new Layer(-5, 10, myCamera));

		// hud layer
		layers.add(new Layer(10, 1000, new Libgdx2dCameraTransformImpl()));

		textRendererSystem = new TextRendererSystem();
		spriteRenderSystem = new SpriteRendererSystem(myCamera, layers);
		spriteUpdateSystem = new SpriteUpdateSystem();

		Vector2 gravity = new Vector2(0f, -10f);
		physicsSystem = new PhysicsSystem(new com.badlogic.gdx.physics.box2d.World(gravity, true));

		LibgdxPointer pointer0 = new LibgdxPointer(0, myCamera);
		LibgdxPointer pointer1 = new LibgdxPointer(1, myCamera);

		ArrayList<LibgdxPointer> pointers = new ArrayList<LibgdxPointer>();

		pointers.add(pointer0);
		pointers.add(pointer1);

		ArrayList<BowController> controllers = new ArrayList<BowController>();

		controllers.add(new BowControllerImpl(pointer0));
		controllers.add(new BowControllerImpl2(pointer0, new Vector2(1f, 1f)));
		controllers.add(new BowControllerImpl3(pointer0));
		controllers.add(new BowControllerImpl4(pointer0, new Vector2(1f, 1f)));
		controllers.add(new BowControllerImpl5(pointer0, new Vector2(1f, 1f)));

		if (Gdx.input.isPeripheralAvailable(Peripheral.MultitouchScreen))
			controllers.add(new BowControllerMutitouchImpl(pointer0, pointer1));

		if (Gdx.input.isPeripheralAvailable(Peripheral.HardwareKeyboard))
			controllers.add(new BowControllerKeyboardImpl(Input.Keys.KEYCODE_DPAD_UP, Input.Keys.KEYCODE_DPAD_DOWN, Input.Keys.KEYCODE_SPACE));

		ControllerSwitcher controllerSwitcher = new ControllerSwitcher(controllers);

		hierarchySystem = new HierarchySystem();
		aliveSystem = new AliveSystem();

		hudButtonSystem = new HudButtonSystem(pointer0);
		pointerUpdateSystem = new PointerUpdateSystem(pointers);

		world = new World();
		world.getSystemManager().setSystem(textRendererSystem);
		world.getSystemManager().setSystem(spriteRenderSystem);
		world.getSystemManager().setSystem(spriteUpdateSystem);
		world.getSystemManager().setSystem(physicsSystem);
		world.getSystemManager().setSystem(hierarchySystem);
		world.getSystemManager().setSystem(aliveSystem);
		world.getSystemManager().setSystem(pointerUpdateSystem);
		world.getSystemManager().setSystem(hudButtonSystem);

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

		createBackground();

		monitorUpdater = new MonitorUpdaterImpl();
		
	}

	public void createBackground() {

		Entity entity = world.createEntity();

		Texture texture = new Texture(Gdx.files.internal("data/background-512x512.jpg"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		int layer = -100;

		entity.addComponent(new SpatialComponent( //
				new SimpleProperty<Vector2>(new Vector2(0f, 0f)), //
				new SimpleProperty<Vector2>(new Vector2(camera.viewportWidth, camera.viewportHeight)), //
				new SimpleProperty<FloatValue>(new FloatValue(0f))));
		entity.addComponent(new SpriteComponent(new SimpleProperty<Sprite>(new Sprite(texture)), //
				new SimpleProperty<IntValue>(new IntValue(layer)), //
				new SimpleProperty<Vector2>(new Vector2(0f, 0f))));

		entity.refresh();

	}

	private EntityFactory entityFactory;

	private MonitorUpdaterImpl monitorUpdater;

	private Libgdx2dCamera myCamera;

	private HierarchySystem hierarchySystem;

	private AliveSystem aliveSystem;

	private HudButtonSystem hudButtonSystem;

	private PointerUpdateSystem pointerUpdateSystem;

	@Override
	public void render(float delta) {

		camera.update();
		camera.apply(Gdx.gl10);

		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		world.loopStart();
		world.setDelta((int) (delta * 1000));
		
		physicsSystem.process();

		// add a system to process all pointers and remove the pointer.update from the controllers!!
		pointerUpdateSystem.process();

		hudButtonSystem.process();

		hierarchySystem.process();
		aliveSystem.process();

		spriteUpdateSystem.process();
		spriteRenderSystem.process();
		textRendererSystem.process();

		camera.update();
		camera.apply(Gdx.gl10);

		renderer.render(physicsWorld);

		monitorUpdater.update();
		Synchronizers.synchronize();

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

		Texture buttonTexture = new Texture(Gdx.files.internal("data/button-template-64x64.png"));
		buttonTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		resourceManager.add("Button", new CachedResourceLoader<Texture>(new ResourceLoaderImpl<Texture>(new StaticDataLoader<Texture>(buttonTexture) {
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
