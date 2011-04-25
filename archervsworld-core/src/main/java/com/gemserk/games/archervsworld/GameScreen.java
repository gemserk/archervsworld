package com.gemserk.games.archervsworld;

import java.util.ArrayList;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.TextComponent;
import com.gemserk.commons.artemis.entities.EntityFactory;
import com.gemserk.commons.artemis.systems.ActivableSystem;
import com.gemserk.commons.artemis.systems.AliveAreaSystem;
import com.gemserk.commons.artemis.systems.AliveSystem;
import com.gemserk.commons.artemis.systems.HierarchySystem;
import com.gemserk.commons.artemis.systems.MovementSystem;
import com.gemserk.commons.artemis.systems.PointerUpdateSystem;
import com.gemserk.commons.artemis.systems.RenderLayer;
import com.gemserk.commons.artemis.systems.SpawnerSystem;
import com.gemserk.commons.artemis.systems.SpriteRendererSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.systems.TextRendererSystem;
import com.gemserk.commons.gdx.Libgdx2dCamera;
import com.gemserk.commons.gdx.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.ScreenAdapter;
import com.gemserk.commons.gdx.input.LibgdxPointer;
import com.gemserk.commons.gdx.resources.LibgdxResourceBuilder;
import com.gemserk.commons.gdx.resources.dataloaders.BitmapFontDataLoader;
import com.gemserk.commons.values.FloatValue;
import com.gemserk.componentsengine.input.ButtonMonitor;
import com.gemserk.componentsengine.input.LibgdxButtonMonitor;
import com.gemserk.componentsengine.input.MonitorUpdater;
import com.gemserk.componentsengine.properties.AbstractProperty;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.games.archervsworld.GameScreen.EntitySystemController.ActivableSystemRegistration;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.artemis.systems.CorrectArrowDirectionSystem;
import com.gemserk.games.archervsworld.artemis.systems.DebugInformationSystem;
import com.gemserk.games.archervsworld.artemis.systems.GameLogicSystem;
import com.gemserk.games.archervsworld.artemis.systems.HudButtonSystem;
import com.gemserk.games.archervsworld.artemis.systems.PhysicsSystem;
import com.gemserk.games.archervsworld.artemis.systems.UpdateBowSystem;
import com.gemserk.games.archervsworld.artemis.systems.WalkingDeadSystem;
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

		loadResources();

		entityFactory = new EntityFactory();
		archerVsWorldEntityFactory = new ArcherVsWorldEntityFactory();

		camera = new OrthographicCamera(viewportWidth, viewportHeight);
		camera.position.set(viewportWidth / 2, viewportHeight / 2, 0);

		restart();

	}

	protected void restart() {

		myCamera = new Libgdx2dCameraTransformImpl(camera);

		ArrayList<RenderLayer> renderLayers = new ArrayList<RenderLayer>();

		// background layer
		renderLayers.add(new RenderLayer(-1000, -5, new Libgdx2dCameraTransformImpl()));

		// world layer
		renderLayers.add(new RenderLayer(-5, 10, myCamera));

		// hud layer
		renderLayers.add(new RenderLayer(10, 1000, new Libgdx2dCameraTransformImpl()));

		textRendererSystem = new TextRendererSystem();
		spriteRenderSystem = new SpriteRendererSystem(myCamera, renderLayers);
		spriteUpdateSystem = new SpriteUpdateSystem();

		Vector2 gravity = new Vector2(0f, -10f);
		physicsSystem = new PhysicsSystem(new com.badlogic.gdx.physics.box2d.World(gravity, true));

		LibgdxPointer pointer0 = new LibgdxPointer(0, myCamera);
		LibgdxPointer pointer1 = new LibgdxPointer(1, myCamera);

		ArrayList<LibgdxPointer> pointers = new ArrayList<LibgdxPointer>();

		pointers.add(pointer0);
		pointers.add(pointer1);

		ArrayList<BowController> controllers = new ArrayList<BowController>();

		controllers.add(new BowControllerImpl2(pointer0, new Vector2(1f, 1f)));
		controllers.add(new BowControllerImpl(pointer0));
		controllers.add(new BowControllerImpl3(pointer0));
		controllers.add(new BowControllerImpl4(pointer0, new Vector2(1f, 1f)));
		controllers.add(new BowControllerImpl5(pointer0, new Vector2(1f, 1f)));

		if (Gdx.input.isPeripheralAvailable(Peripheral.MultitouchScreen))
			controllers.add(new BowControllerMutitouchImpl(pointer0, pointer1));

		if (Gdx.input.isPeripheralAvailable(Peripheral.HardwareKeyboard))
			controllers.add(new BowControllerKeyboardImpl(Input.Keys.KEYCODE_DPAD_UP, Input.Keys.KEYCODE_DPAD_DOWN, Input.Keys.KEYCODE_SPACE));

		final ControllerSwitcher controllerSwitcher = new ControllerSwitcher(controllers);

		updateBowSystem = new UpdateBowSystem(controllerSwitcher, archerVsWorldEntityFactory);
		updateBowSystem.setResourceManager(resourceManager);

		walkingDeadSystem = new WalkingDeadSystem();
		gameLogicSystem = new GameLogicSystem(controllerSwitcher);

		gameLogicSystem.setArcherVsWorldEntityFactory(archerVsWorldEntityFactory);
		gameLogicSystem.setResourceManager(resourceManager);

		hierarchySystem = new HierarchySystem();
		aliveSystem = new AliveSystem();

		hudButtonSystem = new HudButtonSystem(pointer0);
		pointerUpdateSystem = new PointerUpdateSystem(pointers);

		correctArrowDirectionSystem = new CorrectArrowDirectionSystem();

		spawnerSystem = new SpawnerSystem();

		world = new World();

		worldWrapper = new WorldWrapper(world);

		worldWrapper.add(physicsSystem);
		worldWrapper.add(correctArrowDirectionSystem);
		worldWrapper.add(pointerUpdateSystem);
		worldWrapper.add(hudButtonSystem);
		worldWrapper.add(walkingDeadSystem);
		worldWrapper.add(new MovementSystem());

		worldWrapper.add(spriteUpdateSystem);
		worldWrapper.add(spriteRenderSystem);
		worldWrapper.add(textRendererSystem);

		worldWrapper.add(updateBowSystem);
		worldWrapper.add(gameLogicSystem);
		worldWrapper.add(hierarchySystem);
		worldWrapper.add(aliveSystem);
		worldWrapper.add(new AliveAreaSystem());
		worldWrapper.add(spawnerSystem);

		worldWrapper.add(new DebugInformationSystem());

		worldWrapper.init();

		entityFactory.setWorld(world);

		Resource<BitmapFont> fontResource = resourceManager.get("Font");

		entityFactory.fpsEntity( //
				new SimpleProperty<Vector2>(new Vector2(0.5f, 0.5f)), //
				new SimpleProperty<BitmapFont>(fontResource.get()), //
				new SimpleProperty<Vector2>(new Vector2(10, Gdx.graphics.getHeight() - 20)));

		currentControllerLabel(controllerSwitcher);

		physicsWorld = physicsSystem.getPhysicsWorld();

		archerVsWorldEntityFactory.setWorld(world);
		archerVsWorldEntityFactory.setPhysicsWorld(physicsWorld);
		archerVsWorldEntityFactory.setResourceManager(resourceManager);

		// I NEED AN EDITOR FOR ALL THIS STUFF!!

		archerVsWorldEntityFactory.createBackground(camera.viewportWidth, camera.viewportHeight);

		archerVsWorldEntityFactory.createButton(new Vector2(viewportWidth - 2, viewportHeight - 2));

		Vector2 grassSize = new Vector2(1f, 0.5f);

		float x = 0f;
		final float y = 2f;

		archerVsWorldEntityFactory.createGround(new Vector2(40f, 0.22f + y), new Vector2(80f, 0.44f));

		// Vector2[] polygon = new Vector2[] {
		// new Vector2(-0.25f, 0.22f),
		// new Vector2(0.25f, 0.20f),
		// };

		// archerVsWorldEntityFactory.createGround(new Vector2(2.5f, 0.22f), new Vector2(5f, 0.5f));
		// archerVsWorldEntityFactory.createGround(new Vector2(7.5f, 0.22f), new Vector2(5f, 0.5f));
		// archerVsWorldEntityFactory.createGround(new Vector2(12.5f, 0.22f), new Vector2(5f, 0.5f));
		// archerVsWorldEntityFactory.createGround(new Vector2(17.5f, 0.22f), new Vector2(5f, 0.5f));
		// archerVsWorldEntityFactory.createGround(new Vector2(22.5f, 0.22f), new Vector2(5f, 0.5f));

		// archerVsWorldEntityFactory.createGround(new Vector2(2.5f, 0.22f), polygon);
		// archerVsWorldEntityFactory.createGround(new Vector2(7.5f, 0.22f), polygon);
		// archerVsWorldEntityFactory.createGround(new Vector2(12.5f, 0.22f), polygon);
		// archerVsWorldEntityFactory.createGround(new Vector2(17.5f, 0.22f), polygon);
		// archerVsWorldEntityFactory.createGround(new Vector2(22.5f, 0.22f), polygon);

		// archerVsWorldEntityFactory.createGround(new Vector2(3f, 1f), new Vector2(5f, 0.44f));

		for (int i = 0; i < 20; i++) {
			// archerVsWorldEntityFactory.createGround(new Vector2(x + grassSize.x / 2f, y + grassSize.y / 2f), polygon);
			archerVsWorldEntityFactory.createGrass(new Vector2(x + grassSize.x / 2f, y + grassSize.y / 2f), grassSize);
			x += grassSize.x;
		}

		archerVsWorldEntityFactory.createGrass2(new Vector2(10, 1), new Vector2(20f, 2f));

		archerVsWorldEntityFactory.createBow(new Vector2(1f, 1.7f + y));

		// archerVsWorldEntityFactory.createSpawner(new Vector2(20, 1.25f + y));

		Vector2 direction = new Vector2(-1, 0);

		Rectangle spawnArea = new Rectangle(10, 7, 15, 5);
		Rectangle limitArea = new Rectangle(-5, 0, 30, 12);

		float minSpeed = 0.1f;
		float maxSpeed = 0.7f;

		archerVsWorldEntityFactory.createCloudsSpawner(spawnArea, limitArea, direction, minSpeed, maxSpeed, 2000, 2001);

		// archerVsWorldEntityFactory.createCloud(position, new Vector2(-0.1f, 0f), new Vector2(5,5));

		monitorUpdater = new MonitorUpdaterImpl();
		monitorUpdater.add(restartButtonMonitor);
		monitorUpdater.add(zoomInButtonMonitor);
		monitorUpdater.add(zoomOutButtonMonitor);

		monitorUpdater.add(moveLeftMonitor);
		monitorUpdater.add(moveRightMonitor);

		monitorUpdater.add(moveUpMonitor);
		monitorUpdater.add(moveDownMonitor);

		entitySystemController.register(new ActivableSystemRegistration(updateBowSystem, Keys.KEYCODE_1, "Bow system"));
		entitySystemController.register(new ActivableSystemRegistration(walkingDeadSystem, Keys.KEYCODE_2, "Walking dead system"));

	}

	private void currentControllerLabel(final ControllerSwitcher controllerSwitcher) {
		Entity entity = world.createEntity();

		Resource<BitmapFont> fontResource = resourceManager.get("Font");

		entity.addComponent(new TextComponent( //
				new AbstractProperty<String>() {
					@Override
					public String get() {
						return "" + controllerSwitcher.getCurrentController();
					}
				}, //
				new SimpleProperty<BitmapFont>(fontResource.get()), //
				new SimpleProperty<Color>(new Color(0f, 0f, 0f, 1f)) //
		));

		entity.addComponent(new SpatialComponent(new Vector2(Gdx.graphics.getWidth() - 80, Gdx.graphics.getHeight() - 60), new Vector2(0.5f, 0.5f), 50f));
		entity.refresh();
	}

	private UpdateBowSystem updateBowSystem;

	private EntityFactory entityFactory;

	private ButtonMonitor restartButtonMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_R);

	private ButtonMonitor zoomInButtonMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_PLUS);

	private ButtonMonitor zoomOutButtonMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_MINUS);

	private ButtonMonitor moveRightMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_DPAD_RIGHT);

	private ButtonMonitor moveLeftMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_DPAD_LEFT);

	private ButtonMonitor moveUpMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_U);

	private ButtonMonitor moveDownMonitor = new LibgdxButtonMonitor(Input.Keys.KEYCODE_J);

	private MonitorUpdaterImpl monitorUpdater;

	private WalkingDeadSystem walkingDeadSystem;

	private FloatValue zoom = new FloatValue(1f);

	private GameLogicSystem gameLogicSystem;

	private Libgdx2dCamera myCamera;

	private HierarchySystem hierarchySystem;

	private AliveSystem aliveSystem;

	private HudButtonSystem hudButtonSystem;

	private PointerUpdateSystem pointerUpdateSystem;

	static class EntitySystemController {

		static class ActivableSystemRegistration {

			ActivableSystem activableSystem;

			ButtonMonitor buttonMonitor;

			String name;

			public ActivableSystemRegistration(ActivableSystem activableSystem, ButtonMonitor buttonMonitor, String name) {
				this.activableSystem = activableSystem;
				this.buttonMonitor = buttonMonitor;
				this.name = name;
			}

			public ActivableSystemRegistration(ActivableSystem activableSystem, int key, String name) {
				this.activableSystem = activableSystem;
				this.buttonMonitor = new LibgdxButtonMonitor(key);
				this.name = name;
			}
		}

		ArrayList<ActivableSystemRegistration> registrations = new ArrayList<ActivableSystemRegistration>();

		public void register(ActivableSystemRegistration registration) {
			registrations.add(registration);
		}

		public void update() {
			for (int i = 0; i < registrations.size(); i++) {

				ActivableSystemRegistration registration = registrations.get(i);

				registration.buttonMonitor.update();

				if (registration.buttonMonitor.isPressed()) {
					registration.activableSystem.toggle();
					if (registration.activableSystem.isEnabled()) {
						Gdx.app.log("Archer Vs Zombies", registration.name + " enabled");
					} else {
						Gdx.app.log("Archer Vs Zombies", registration.name + " disabled");
					}
				}

			}
		}

	}

	EntitySystemController entitySystemController = new EntitySystemController();

	private CorrectArrowDirectionSystem correctArrowDirectionSystem;

	private SpawnerSystem spawnerSystem;

	private WorldWrapper worldWrapper;

	@Override
	public void render(float delta) {

		camera.update();
		camera.apply(Gdx.gl10);

		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		entitySystemController.update();

		worldWrapper.update((int) (delta * 1000));

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

	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void show() {

	}

	protected void loadResources() {

		new LibgdxResourceBuilder(resourceManager) {
			{
				texture("Background", "data/background-512x512.jpg");
				texture("Rock", "data/rock-512x512.png");
				texture("Bow", "data/bow-512x512.png");
				texture("Arrow", "data/arrow-512x512.png");
				texture("Tree", "data/tree-512x512.png");

				texture("Grass", "data/grass-128x128.png");
				texture("Grass02", "data/grass-02-128x128.png");

				texture("Cloud", "data/cloud-256x256.png");

				texture("Button", "data/button-template-64x64.png");
				texture("FontTexture", "data/font.png");

				sound("HitFleshSound", "data/hit-flesh.ogg");
				sound("HitGroundSound", "data/hit-ground.ogg");
				sound("BowSound", "data/bow.ogg");
			}
		};

		Resource<Texture> fontTextureResource = resourceManager.get("FontTexture");
		resourceManager.add("Font", new CachedResourceLoader<BitmapFont>(new ResourceLoaderImpl<BitmapFont>(new BitmapFontDataLoader(Gdx.files.internal("data/font.fnt"), new Sprite(fontTextureResource.get())))));

	}

	@Override
	public void dispose() {

	}

}
