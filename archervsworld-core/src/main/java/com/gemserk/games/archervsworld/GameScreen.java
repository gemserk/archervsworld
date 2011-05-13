package com.gemserk.games.archervsworld;

import java.io.InputStream;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.SpatialImpl;
import com.gemserk.commons.artemis.entities.EntityFactory;
import com.gemserk.commons.artemis.systems.ActivableSystem;
import com.gemserk.commons.artemis.systems.AliveAreaSystem;
import com.gemserk.commons.artemis.systems.HierarchySystem;
import com.gemserk.commons.artemis.systems.HitDetectionSystem;
import com.gemserk.commons.artemis.systems.MovementSystem;
import com.gemserk.commons.artemis.systems.PhysicsSystem;
import com.gemserk.commons.artemis.systems.PointerUpdateSystem;
import com.gemserk.commons.artemis.systems.RenderLayer;
import com.gemserk.commons.artemis.systems.SpriteRendererSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.systems.TextRendererSystem;
import com.gemserk.commons.artemis.systems.TimerSystem;
import com.gemserk.commons.gdx.ScreenAdapter;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.controllers.CameraController;
import com.gemserk.commons.gdx.controllers.Controller;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.commons.gdx.input.LibgdxPointer;
import com.gemserk.commons.gdx.resources.LibgdxResourceBuilder;
import com.gemserk.commons.svg.inkscape.DocumentParser;
import com.gemserk.commons.svg.inkscape.SvgDocument;
import com.gemserk.commons.svg.inkscape.SvgInkscapeImage;
import com.gemserk.commons.svg.inkscape.SvgInkscapePath;
import com.gemserk.componentsengine.input.ButtonMonitor;
import com.gemserk.componentsengine.input.LibgdxButtonMonitor;
import com.gemserk.componentsengine.input.MonitorUpdater;
import com.gemserk.componentsengine.properties.AbstractProperty;
import com.gemserk.games.archervsworld.GameScreen.EntitySystemController.ActivableSystemRegistration;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.artemis.systems.CorrectArrowDirectionSystem;
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

public class GameScreen extends ScreenAdapter {

	private World world;

	private com.badlogic.gdx.physics.box2d.World physicsWorld;

	int viewportWidth = 800;

	int viewportHeight = 480;

	Box2DCustomDebugRenderer box2dDebugRenderer;

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

	private SvgDocument svgDocument;

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

		box2dDebugRenderer = new Box2DCustomDebugRenderer((Libgdx2dCameraTransformImpl) myCamera, physicsSystem.getPhysicsWorld());

		LibgdxPointer pointer0 = new LibgdxPointer(0, myCamera);
		LibgdxPointer pointer1 = new LibgdxPointer(1, myCamera);

		LibgdxPointer pointer2 = new LibgdxPointer(0);

		ArrayList<LibgdxPointer> pointers = new ArrayList<LibgdxPointer>();

		pointers.add(pointer0);
		pointers.add(pointer1);
		pointers.add(pointer2);

		PointerUpdateSystem pointerUpdateSystem = new PointerUpdateSystem(pointers);

		Vector2 cameraPosition = new Vector2(viewportWidth * 0.5f * 0.025f, viewportHeight * 0.5f * 0.025f);
		// Camera camera = new CameraImpl(cameraPosition.x, cameraPosition.y, 40f, 0f);
		Camera camera = new CameraRestrictedImpl(cameraPosition.x, cameraPosition.y, 40f, 0f, viewportWidth, viewportHeight, new Rectangle(-5f, -2f, 35f, 20f));

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

		world = new World();

		worldWrapper = new WorldWrapper(world);

		worldWrapper.addRenderSystem(new SpriteUpdateSystem());
		worldWrapper.addRenderSystem(spriteRenderSystem);
		worldWrapper.addRenderSystem(new TextRendererSystem());

		worldWrapper.addUpdateSystem(physicsSystem);
		worldWrapper.addUpdateSystem(new HitDetectionSystem());
		worldWrapper.addUpdateSystem(new CorrectArrowDirectionSystem());
		worldWrapper.addUpdateSystem(pointerUpdateSystem);
		worldWrapper.addUpdateSystem(walkingDeadSystem);
		worldWrapper.addUpdateSystem(new MovementSystem());
		worldWrapper.addUpdateSystem(updateBowSystem);
		worldWrapper.addUpdateSystem(new UpdateChargingArrowSystem());
		worldWrapper.addUpdateSystem(new HierarchySystem());
		worldWrapper.addUpdateSystem(new AliveAreaSystem());
		worldWrapper.addUpdateSystem(new TimerSystem());
		// worldWrapper.addUpdateSystem(new DebugInformationSystem());

		worldWrapper.init();

		entityFactory.setWorld(world);

		Resource<BitmapFont> fontResource = resourceManager.get("Font");

		entityFactory.fpsEntity( //
				new Vector2(0.5f, 0.5f), //
				fontResource.get(), //
				new SpatialImpl(10f, Gdx.graphics.getHeight() - 20, 1f, 1f, 0f));

		// currentControllerLabel(controllerSwitcher);

		physicsWorld = physicsSystem.getPhysicsWorld();

		archerVsWorldEntityFactory.setWorld(world);
		archerVsWorldEntityFactory.setPhysicsWorld(physicsWorld);
		archerVsWorldEntityFactory.setResourceManager(resourceManager);

		// I NEED AN EDITOR FOR ALL THIS STUFF!!
		// I HAVE NOW AN EDITOR FOR ALL THIS STUFF!!!

		archerVsWorldEntityFactory.createBackground(viewportWidth, viewportHeight);

		// archerVsWorldEntityFactory.createButton(new Vector2(viewportWidth - 2, viewportHeight - 2));

		final float y = 2f;

		archerVsWorldEntityFactory.createArcher(new Vector2(3.5f, 1.7f + y + 3 + 1f));
		archerVsWorldEntityFactory.createZombiesSpawner(new Vector2(28, 1.25f + y));

		Vector2 direction = new Vector2(-1, 0);

		Rectangle spawnArea = new Rectangle(10, 12, 25, 5);
		Rectangle limitArea = new Rectangle(-15, 0, 45, 20);

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

		InputStream svg = Gdx.files.internal("data/scenes/scene01.svg").read();
		Document document = new DocumentParser().parse(svg);

		new LayerProcessor("World") {
			protected void handleImageObject(SvgInkscapeImage svgImage, Element element, float x, float y, float width, float height, float sx, float sy, float angle) {
				// create stuff..
				Texture texture = resourceManager.getResourceValue(svgImage.getLabel());
				if (texture == null)
					return;
				archerVsWorldEntityFactory.createStaticSprite(x, y, width, height, angle, new Sprite(texture), 2, Color.WHITE, 0.5f, 0.5f);
			};
		}.processWorld(document);

		new LayerProcessor("Physics") {
			@Override
			protected void handlePathObject(SvgInkscapePath svgPath, Element element, Vector2[] vertices) {
				archerVsWorldEntityFactory.createStaticBody(new Vector2(), vertices);
			}
		}.processWorld(document);

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
	public void internalRender(float delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		Camera cameraImpl = cameraController.getCamera();
		Vector2 cameraPosition = new Vector2(cameraImpl.getX(), cameraImpl.getY());

		myCamera.zoom(cameraImpl.getZoom());
		myCamera.move(cameraPosition.x, cameraPosition.y);
		myCamera.rotate(cameraImpl.getAngle());

		worldWrapper.render();

		if (bowController instanceof BowControllerHudImpl) {
			BowControllerHudImpl controller = (BowControllerHudImpl) bowController;
			ImmediateModeRendererUtils.drawSolidCircle(controller.getPosition(), controller.getRadius(), bowController.getAngle(), Color.WHITE);
		}

		if (bowController instanceof BowControllerHudImpl2) {
			BowControllerHudImpl2 controller = (BowControllerHudImpl2) bowController;
			ImmediateModeRendererUtils.drawSolidCircle(controller.getPosition(), controller.getRadius(), bowController.getAngle(), Color.WHITE);
		}

		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			box2dDebugRenderer.render();
			Libgdx2dCameraTransformImpl myCamera2 = (Libgdx2dCameraTransformImpl) myCamera;
			myCamera2.push();
			ImmediateModeRendererUtils.drawHorizontalAxis(0, Color.RED);
			ImmediateModeRendererUtils.drawVerticalAxis(0f, Color.RED);
			myCamera2.pop();
		}
	}

	@Override
	public void internalUpdate(float delta) {
		int deltaInMs = (int) (delta * 1000);

		Synchronizers.synchronize(deltaInMs);

		monitorUpdater.update();

		for (int i = 0; i < controllers.size(); i++)
			controllers.get(i).update(deltaInMs);

		cameraController.update(deltaInMs);
		entitySystemController.update();

		worldWrapper.update(deltaInMs);

		if (restartButtonMonitor.isReleased())
			restart();
	}

	protected void loadResources() {

		new LibgdxResourceBuilder(resourceManager) {
			{
				setCacheWhenLoad(true);

				texture("Background", "data/images/background-512x512.jpg", false);

				texture("Rock", "data/images/rock-512x512.png");
				texture("Bow", "data/images/bow-512x512.png");
				texture("Arrow", "data/images/arrow-512x512.png");

				texture("Tile01", "data/images/tile01.png");
				texture("Tile02", "data/images/tile02.png");
				texture("Tile03", "data/images/tile03.png");
				texture("Tile04", "data/images/tile04.png");
				texture("Tile11", "data/images/tile11.png");
				texture("Tile12", "data/images/tile12.png");
				texture("Tile13", "data/images/tile13.png");
				texture("Tile14", "data/images/tile14.png");
				texture("Tile21", "data/images/tile21.png");

				texture("CloudsSpritesheet", "data/images/clouds-spritesheet.png", false);
				sprite("Cloud01", "CloudsSpritesheet", 0, 0, 512, 128);
				sprite("Cloud02", "CloudsSpritesheet", 0, 128, 512, 128);

				sound("HitFleshSound", "data/sounds/hit-flesh.ogg");
				sound("HitGroundSound", "data/sounds/hit-ground.ogg");
				sound("BowSound", "data/sounds/bow.ogg");

				font("Font", "data/fonts/font.png", "data/fonts/font.fnt");
			}
		};

	}

	@Override
	public void dispose() {
		// dispose resources!!
	}

}
