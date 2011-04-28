package com.gemserk.games.archervsworld;

import java.util.ArrayList;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
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
import com.gemserk.commons.gdx.ScreenAdapter;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.controllers.CameraController;
import com.gemserk.commons.gdx.controllers.Controller;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.commons.gdx.input.LibgdxPointer;
import com.gemserk.commons.gdx.resources.LibgdxResourceBuilder;
import com.gemserk.commons.gdx.resources.dataloaders.BitmapFontDataLoader;
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
import com.gemserk.games.archervsworld.artemis.systems.PhysicsSystem;
import com.gemserk.games.archervsworld.artemis.systems.UpdateBowSystem;
import com.gemserk.games.archervsworld.artemis.systems.UpdateChargingArrowSystem;
import com.gemserk.games.archervsworld.artemis.systems.WalkingDeadSystem;
import com.gemserk.games.archervsworld.controllers.BowController;
import com.gemserk.games.archervsworld.controllers.BowControllerHudImpl;
import com.gemserk.games.archervsworld.controllers.BowControllerHudImpl2;
import com.gemserk.games.archervsworld.controllers.CameraControllerLibgdxPointerImpl;
import com.gemserk.games.archervsworld.controllers.ControllerSwitcher;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;
import com.gemserk.resources.resourceloaders.CachedResourceLoader;
import com.gemserk.resources.resourceloaders.ResourceLoaderImpl;

public class GameScreen extends ScreenAdapter {

	private World world;

	private com.badlogic.gdx.physics.box2d.World physicsWorld;

	// int viewportWidth = 20;
	// int viewportHeight = 12;

	int viewportWidth = 800;

	int viewportHeight = 480;

	Box2DDebugRenderer box2dDebugRenderer;

	ArcherVsWorldEntityFactory archerVsWorldEntityFactory;

	ResourceManager<String> resourceManager = new ResourceManagerImpl<String>();

	private EntityFactory entityFactory;

	private ButtonMonitor restartButtonMonitor = new LibgdxButtonMonitor(Input.Keys.R);

	private ButtonMonitor zoomInButtonMonitor = new LibgdxButtonMonitor(Input.Keys.PLUS);

	private ButtonMonitor zoomOutButtonMonitor = new LibgdxButtonMonitor(Input.Keys.MINUS);

	private ButtonMonitor moveRightMonitor = new LibgdxButtonMonitor(Input.Keys.DPAD_RIGHT);

	private ButtonMonitor moveLeftMonitor = new LibgdxButtonMonitor(Input.Keys.DPAD_LEFT);

	private ButtonMonitor moveUpMonitor = new LibgdxButtonMonitor(Input.Keys.DPAD_UP);

	private ButtonMonitor moveDownMonitor = new LibgdxButtonMonitor(Input.Keys.DPAD_DOWN);

	private MonitorUpdaterImpl monitorUpdater;

	private Libgdx2dCamera myCamera;

	EntitySystemController entitySystemController = new EntitySystemController();

	private WorldWrapper worldWrapper;

	private BowController bowController;

	private CameraController cameraController;

	private ArrayList<Controller> controllers = new ArrayList<Controller>();

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

	public GameScreen(Game game) {
		loadResources();

		entityFactory = new EntityFactory();
		archerVsWorldEntityFactory = new ArcherVsWorldEntityFactory();

		restart();
	}

	protected void restart() {

		myCamera = new Libgdx2dCameraTransformImpl();
		myCamera.center(viewportWidth / 2, viewportHeight / 2);

		box2dDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) myCamera);

		ArrayList<RenderLayer> renderLayers = new ArrayList<RenderLayer>();

		// background layer
		renderLayers.add(new RenderLayer(-1000, -5, new Libgdx2dCameraTransformImpl()));
		// world layer
		renderLayers.add(new RenderLayer(-5, 10, myCamera));
		// hud layer
		renderLayers.add(new RenderLayer(10, 1000, new Libgdx2dCameraTransformImpl()));

		SpriteRendererSystem spriteRenderSystem = new SpriteRendererSystem(renderLayers);

		Vector2 gravity = new Vector2(0f, -10f);
		PhysicsSystem physicsSystem = new PhysicsSystem(new com.badlogic.gdx.physics.box2d.World(gravity, true));

		LibgdxPointer pointer0 = new LibgdxPointer(0, myCamera);
		LibgdxPointer pointer1 = new LibgdxPointer(1, myCamera);

		LibgdxPointer pointer2 = new LibgdxPointer(0);

		ArrayList<LibgdxPointer> pointers = new ArrayList<LibgdxPointer>();

		pointers.add(pointer0);
		pointers.add(pointer1);
		pointers.add(pointer2);

		PointerUpdateSystem pointerUpdateSystem = new PointerUpdateSystem(pointers);

		Vector2 cameraPosition = new Vector2(viewportWidth * 0.5f * 0.025f, viewportHeight * 0.5f * 0.025f);
		Camera camera = new Camera(cameraPosition, 40f, 0f);

		// cameraController = new CameraControllerButtonMonitorImpl(camera, //
		// moveLeftMonitor, moveRightMonitor, //
		// moveUpMonitor, moveDownMonitor, //
		// zoomInButtonMonitor, zoomOutButtonMonitor);
		cameraController = new CameraControllerLibgdxPointerImpl(camera, pointer2, new Rectangle(140, 0, viewportWidth - 140, viewportHeight));

		controllers.add(cameraController);

		ArrayList<BowController> bowControllers = new ArrayList<BowController>();

		// controllers.add(new BowControllerImpl2(pointer0, new Vector2(0f, 3f)));
		// controllers.add(new BowControllerImpl(pointer0));
		// controllers.add(new BowControllerImpl3(pointer0));
		// controllers.add(new BowControllerImpl4(pointer0, new Vector2(0f, 3f)));

		// controllers.add(new BowControllerImpl5(pointer0, new Vector2(2f, 1.7f + 2f + 3 + 2)));

		// bowController = new BowControllerHudImpl(pointer2, new Vector2(90f, 90f), 80f);
		bowController = new BowControllerHudImpl2(pointer2, new Vector2(70f, 70f), 60f);

		bowControllers.add(bowController);

		controllers.add(bowController);

		// if (Gdx.input.isPeripheralAvailable(Peripheral.MultitouchScreen))
		// controllers.add(new BowControllerMutitouchImpl(pointer0, pointer1));
		//
		// if (Gdx.input.isPeripheralAvailable(Peripheral.HardwareKeyboard))
		// controllers.add(new BowControllerKeyboardImpl(Input.Keys.KEYCODE_DPAD_UP, Input.Keys.KEYCODE_DPAD_DOWN, Input.Keys.KEYCODE_SPACE));

		final ControllerSwitcher controllerSwitcher = new ControllerSwitcher(bowControllers);

		controllers.add(controllerSwitcher);

		AbstractProperty<BowController> currentController = new AbstractProperty<BowController>() {
			@Override
			public BowController get() {
				return controllerSwitcher.getController();
			}
		};

		UpdateBowSystem updateBowSystem = new UpdateBowSystem(currentController);
		updateBowSystem.setEntityFactory(archerVsWorldEntityFactory);
		updateBowSystem.setResourceManager(resourceManager);

		WalkingDeadSystem walkingDeadSystem = new WalkingDeadSystem();
		GameLogicSystem gameLogicSystem = new GameLogicSystem(controllerSwitcher);

		gameLogicSystem.setArcherVsWorldEntityFactory(archerVsWorldEntityFactory);
		gameLogicSystem.setResourceManager(resourceManager);

		// HudButtonSystem hudButtonSystem = new HudButtonSystem(pointer0);

		world = new World();

		worldWrapper = new WorldWrapper(world);

		worldWrapper.add(physicsSystem);
		worldWrapper.add(new CorrectArrowDirectionSystem());
		worldWrapper.add(pointerUpdateSystem);

		// worldWrapper.add(hudButtonSystem);

		worldWrapper.add(walkingDeadSystem);
		worldWrapper.add(new MovementSystem());

		worldWrapper.add(new SpriteUpdateSystem());
		worldWrapper.add(spriteRenderSystem);
		worldWrapper.add(new TextRendererSystem());

		worldWrapper.add(updateBowSystem);
		worldWrapper.add(new UpdateChargingArrowSystem());

		worldWrapper.add(gameLogicSystem);
		worldWrapper.add(new HierarchySystem());
		worldWrapper.add(new AliveSystem());
		worldWrapper.add(new AliveAreaSystem());
		worldWrapper.add(new SpawnerSystem());

		worldWrapper.add(new DebugInformationSystem());

		worldWrapper.init();

		entityFactory.setWorld(world);

		Resource<BitmapFont> fontResource = resourceManager.get("Font");

		entityFactory.fpsEntity( //
				new SimpleProperty<Vector2>(new Vector2(0.5f, 0.5f)), //
				new SimpleProperty<BitmapFont>(fontResource.get()), //
				new SimpleProperty<Vector2>(new Vector2(10, Gdx.graphics.getHeight() - 20)));

		// currentControllerLabel(controllerSwitcher);

		physicsWorld = physicsSystem.getPhysicsWorld();

		archerVsWorldEntityFactory.setWorld(world);
		archerVsWorldEntityFactory.setPhysicsWorld(physicsWorld);
		archerVsWorldEntityFactory.setResourceManager(resourceManager);

		// I NEED AN EDITOR FOR ALL THIS STUFF!!

		archerVsWorldEntityFactory.createBackground(viewportWidth, viewportHeight);

		// archerVsWorldEntityFactory.createButton(new Vector2(viewportWidth - 2, viewportHeight - 2));

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

		archerVsWorldEntityFactory.createArcher(new Vector2(2.5f, 1.7f + y + 3 + 2));

		archerVsWorldEntityFactory.createTower(new Vector2(1f, y + 3f), new Vector2(6f, 6f));

		// archerVsWorldEntityFactory.createBow(new Vector2(1f, 2.7f + y));
		//
		// archerVsWorldEntityFactory.createBow(new Vector2(1f, 3.7f + y));

		archerVsWorldEntityFactory.createZombiesSpawner(new Vector2(20, 1.25f + y));

		Vector2 direction = new Vector2(-1, 0);

		Rectangle spawnArea = new Rectangle(10, 10, 15, 5);
		Rectangle limitArea = new Rectangle(-5, 0, 30, 20);

		float minSpeed = 0.1f;
		float maxSpeed = 0.7f;

		archerVsWorldEntityFactory.createCloudsSpawner(spawnArea, limitArea, direction, minSpeed, maxSpeed, 2000, 9000);

		// archerVsWorldEntityFactory.createCloud(position, new Vector2(-0.1f, 0f), new Vector2(5,5));

		monitorUpdater = new MonitorUpdaterImpl();
		monitorUpdater.add(restartButtonMonitor);
		monitorUpdater.add(zoomInButtonMonitor);
		monitorUpdater.add(zoomOutButtonMonitor);

		monitorUpdater.add(moveLeftMonitor);
		monitorUpdater.add(moveRightMonitor);

		monitorUpdater.add(moveUpMonitor);
		monitorUpdater.add(moveDownMonitor);

		entitySystemController.register(new ActivableSystemRegistration(updateBowSystem, Keys.NUM_1, "Bow system"));
		entitySystemController.register(new ActivableSystemRegistration(walkingDeadSystem, Keys.NUM_2, "Walking dead system"));

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

	@Override
	public void render(float delta) {

		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		monitorUpdater.update();

		int deltaInMs = (int) (delta * 1000);

		for (int i = 0; i < controllers.size(); i++)
			controllers.get(i).update(deltaInMs);

		cameraController.update(deltaInMs);

		Camera camera = cameraController.getCamera();
		Vector2 cameraPosition = camera.position;

		myCamera.zoom(camera.zoom);
		myCamera.move(cameraPosition.x, cameraPosition.y);
		myCamera.rotate(camera.angle);

		entitySystemController.update();

		worldWrapper.update(deltaInMs);

		Synchronizers.synchronize();

		if (restartButtonMonitor.isReleased())
			restart();

		if (bowController instanceof BowControllerHudImpl) {
			BowControllerHudImpl controller = (BowControllerHudImpl) bowController;
			ImmediateModeRendererUtils.drawSolidCircle(controller.getPosition(), controller.getRadius(), bowController.getAngle(), Color.WHITE);
		}

		if (bowController instanceof BowControllerHudImpl2) {
			BowControllerHudImpl2 controller = (BowControllerHudImpl2) bowController;
			ImmediateModeRendererUtils.drawSolidCircle(controller.getPosition(), controller.getRadius(), bowController.getAngle(), Color.WHITE);
		}

		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			box2dDebugRenderer.render(physicsWorld);
		}
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

				texture("Tower", "data/tower-128x128.png");

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
		// dispose resources!!
	}

}
