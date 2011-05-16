package com.gemserk.games.archervsworld.gamestates;

import java.io.InputStream;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.artemis.World;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Circle;
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
import com.gemserk.commons.gdx.GameStateImpl;
import com.gemserk.commons.gdx.box2d.Box2DCustomDebugRenderer;
import com.gemserk.commons.gdx.camera.Camera;
import com.gemserk.commons.gdx.camera.CameraRestrictedImpl;
import com.gemserk.commons.gdx.camera.Libgdx2dCamera;
import com.gemserk.commons.gdx.camera.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.controllers.CameraController;
import com.gemserk.commons.gdx.graphics.ImmediateModeRendererUtils;
import com.gemserk.commons.gdx.input.LibgdxPointer;
import com.gemserk.commons.gdx.resources.LibgdxResourceBuilder;
import com.gemserk.commons.svg.inkscape.DocumentParser;
import com.gemserk.commons.svg.inkscape.SvgInkscapeImage;
import com.gemserk.commons.svg.inkscape.SvgInkscapePath;
import com.gemserk.componentsengine.input.ButtonMonitor;
import com.gemserk.componentsengine.input.LibgdxButtonMonitor;
import com.gemserk.componentsengine.input.LibgdxInputMappingBuilder;
import com.gemserk.componentsengine.input.MonitorUpdater;
import com.gemserk.games.archervsworld.LayerProcessor;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.artemis.systems.CorrectArrowDirectionSystem;
import com.gemserk.games.archervsworld.artemis.systems.UpdateBowSystem;
import com.gemserk.games.archervsworld.artemis.systems.UpdateChargingArrowSystem;
import com.gemserk.games.archervsworld.artemis.systems.WalkingDeadSystem;
import com.gemserk.games.archervsworld.controllers.BowController;
import com.gemserk.games.archervsworld.controllers.BowData;
import com.gemserk.games.archervsworld.controllers.BowDirectionAreaControllerHudImpl;
import com.gemserk.games.archervsworld.controllers.BowDirectionControllerHudImpl;
import com.gemserk.games.archervsworld.controllers.BowPowerControllerButonMonitorImpl;
import com.gemserk.games.archervsworld.controllers.BowPowerHudControllerImpl;
import com.gemserk.games.archervsworld.controllers.CameraControllerLibgdxPointerImpl;
import com.gemserk.games.archervsworld.controllers.CameraMovementControllerImpl;
import com.gemserk.games.archervsworld.controllers.MultitouchCameraControllerImpl;
import com.gemserk.games.archervsworld.gamestates.PlayGameState.EntitySystemController.ActivableSystemRegistration;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;

public class PlayGameState extends GameStateImpl {

	private World world;

	private com.badlogic.gdx.physics.box2d.World physicsWorld;

	int viewportWidth = 800;

	int viewportHeight = 480;

	Box2DCustomDebugRenderer box2dDebugRenderer;

	ArcherVsWorldEntityFactory archerVsWorldEntityFactory;

	ResourceManager<String> resourceManager;

	private EntityFactory entityFactory;

	private ButtonMonitor restartButtonMonitor = new LibgdxButtonMonitor(Input.Keys.R);

	private MonitorUpdaterImpl monitorUpdater;

	private Libgdx2dCamera myCamera;

	EntitySystemController entitySystemController = new EntitySystemController();

	private WorldWrapper worldWrapper;

	private CameraController cameraController;

	private BowData realBowController;

	private BowController bowDirectionController;

	private BowController bowPowerController;

	private Rectangle powerButtonArea;

	private Circle directionButtonArea;

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

	public PlayGameState(Game game) {
		entityFactory = new EntityFactory();
		archerVsWorldEntityFactory = new ArcherVsWorldEntityFactory();
	}

	@Override
	public void init() {
		// if gameOver
		restart();
	}

	protected void restart() {

		resourceManager = new ResourceManagerImpl<String>();

		loadResources();

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
		LibgdxPointer pointer3 = new LibgdxPointer(1);

		ArrayList<LibgdxPointer> pointers = new ArrayList<LibgdxPointer>();

		pointers.add(pointer0);
		pointers.add(pointer1);
		pointers.add(pointer2);
		pointers.add(pointer3);

		PointerUpdateSystem pointerUpdateSystem = new PointerUpdateSystem(pointers);

		Vector2 cameraPosition = new Vector2(viewportWidth * 0.5f * 0.025f, viewportHeight * 0.5f * 0.025f);
		// Camera camera = new CameraImpl(cameraPosition.x, cameraPosition.y, 40f, 0f);
		Camera camera = new CameraRestrictedImpl(cameraPosition.x, cameraPosition.y, 40f, 0f, viewportWidth, viewportHeight, new Rectangle(-5f, -2f, 35f, 20f));

		Rectangle controllerArea = new Rectangle(140, 0, viewportWidth - 140, viewportHeight);

		// cameraController = new CameraControllerLibgdxPointerImpl(camera, pointer2, controllerArea);

		if (Gdx.input.isPeripheralAvailable(Peripheral.MultitouchScreen)) {
			cameraController = new MultitouchCameraControllerImpl(camera, pointer2, pointer3, controllerArea);
		} else if (Gdx.app.getType() == ApplicationType.Android) {
			cameraController = new CameraControllerLibgdxPointerImpl(camera, pointer2, controllerArea);
		} else {
			cameraController = new CameraMovementControllerImpl(camera, //
					LibgdxInputMappingBuilder.pointerXCoordinateMonitor(Gdx.input, 0), //
					LibgdxInputMappingBuilder.pointerYCoordinateMonitor(Gdx.input, 0), //
					LibgdxInputMappingBuilder.rightMouseButtonMonitor(Gdx.input));
		}

		// controllers.add(cameraController);

		realBowController = new BowData();

		bowDirectionController = null;
		bowPowerController = null;

		directionButtonArea = new Circle(70f, 70f, 60f);
		powerButtonArea = new Rectangle(Gdx.graphics.getWidth() - 100, 20, 80, 80);

		if (Gdx.app.getType() != ApplicationType.Android) {
			// bowDirectionController = new BowDirectionAreaControllerHudImpl(realBowController, pointer2, new Vector2(directionButtonArea.x, directionButtonArea.y), 60f);
			bowDirectionController = new BowDirectionControllerHudImpl(realBowController, pointer2, new Vector2(directionButtonArea.x, directionButtonArea.y));
			bowPowerController = new BowPowerControllerButonMonitorImpl(realBowController, LibgdxInputMappingBuilder.leftMouseButtonMonitor(Gdx.input));
			// bowPowerController = new BowPowerHudControllerImpl(realBowController, //
			// LibgdxInputMappingBuilder.anyPointerButtonMonitor(Gdx.input), //
			// LibgdxInputMappingBuilder.pointerXCoordinateMonitor(Gdx.input, 0), //
			// LibgdxInputMappingBuilder.pointerYCoordinateMonitor(Gdx.input, 0), //
			// powerButtonArea);
		} else {
			bowDirectionController = new BowDirectionAreaControllerHudImpl(realBowController, pointer2, //
					new Vector2(directionButtonArea.x, directionButtonArea.y), directionButtonArea.radius);
			bowPowerController = new BowPowerHudControllerImpl(realBowController, //
					LibgdxInputMappingBuilder.pointerDownButtonMonitor(Gdx.input, 1), //
					LibgdxInputMappingBuilder.pointerXCoordinateMonitor(Gdx.input, 1), //
					LibgdxInputMappingBuilder.pointerYCoordinateMonitor(Gdx.input, 1), //
					powerButtonArea);
		}

		UpdateBowSystem updateBowSystem = new UpdateBowSystem();
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

		physicsWorld = physicsSystem.getPhysicsWorld();

		archerVsWorldEntityFactory.setWorld(world);
		archerVsWorldEntityFactory.setPhysicsWorld(physicsWorld);
		archerVsWorldEntityFactory.setResourceManager(resourceManager);

		// I NEED AN EDITOR FOR ALL THIS STUFF!!
		// I HAVE NOW AN EDITOR FOR ALL THIS STUFF!!!

		archerVsWorldEntityFactory.createBackground(viewportWidth, viewportHeight);
		archerVsWorldEntityFactory.createZombiesSpawner(new Vector2(28, 1.25f + 2f));

		Vector2 direction = new Vector2(-1, 0);

		Rectangle spawnArea = new Rectangle(10, 12, 25, 5);
		Rectangle limitArea = new Rectangle(-15, 0, 45, 20);

		float minSpeed = 0.1f;
		float maxSpeed = 0.7f;

		archerVsWorldEntityFactory.createCloudsSpawner(spawnArea, limitArea, direction, minSpeed, maxSpeed, 2000, 9000);

		monitorUpdater = new MonitorUpdaterImpl();
		monitorUpdater.add(restartButtonMonitor);

		entitySystemController.register(new ActivableSystemRegistration(updateBowSystem, Keys.NUM_1, "Bow system"));
		entitySystemController.register(new ActivableSystemRegistration(walkingDeadSystem, Keys.NUM_2, "Walking dead system"));

		InputStream svg = Gdx.files.internal("data/scenes/scene01.svg").read();
		Document document = new DocumentParser().parse(svg);

		new LayerProcessor("World") {
			protected void handleImageObject(SvgInkscapeImage svgImage, Element element, float x, float y, float width, float height, float sx, float sy, float angle) {
				// create stuff..

				if (element.hasAttribute("start")) {
					archerVsWorldEntityFactory.createArcher(x, y, realBowController);
					return;
				}

				Sprite sprite = resourceManager.getResourceValue(svgImage.getLabel());
				if (sprite == null)
					return;
				int layer = 2;
				if (element.hasAttribute("layer"))
					layer = Integer.parseInt(element.getAttribute("layer"));
				
				sprite.setScale(sx, sy);
				
				archerVsWorldEntityFactory.createStaticSprite(x, y, width, height, angle, sprite, layer, Color.WHITE, 0.5f, 0.5f);
			};
		}.processWorld(document);

		new LayerProcessor("Physics") {
			@Override
			protected void handlePathObject(SvgInkscapePath svgPath, Element element, Vector2[] vertices) {
				archerVsWorldEntityFactory.createStaticBody(new Vector2(), vertices);
			}
		}.processWorld(document);

		world.loopStart();
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
	public void render(int delta) {
		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		Camera camera = cameraController.getCamera();
		Vector2 cameraPosition = new Vector2(camera.getX(), camera.getY());

		myCamera.zoom(camera.getZoom());
		myCamera.move(cameraPosition.x, cameraPosition.y);
		myCamera.rotate(camera.getAngle());

		worldWrapper.render();

		// BowData bowData = bowController.getBowData();
		// ImmediateModeRendererUtils.drawSolidCircle(70f, 70f, 60f, realBowController.getAngle(), Color.WHITE);

		ImmediateModeRendererUtils.drawSolidCircle(directionButtonArea, realBowController.getAngle(), Color.WHITE);

		if (realBowController.isCharging())
			ImmediateModeRendererUtils.drawSolidCircle(Gdx.graphics.getWidth() - 70f, 70f, realBowController.getPower() * 2f, Color.WHITE);

		// if (Gdx.app.getType() == ApplicationType.Android) {
		// ImmediateModeRendererUtils.drawRectangle(Gdx.graphics.getWidth() - 100, 20, Gdx.graphics.getWidth() - 20, 100, Color.GREEN);
		ImmediateModeRendererUtils.drawRectangle(powerButtonArea, Color.GREEN);

		// }

		// if (bowController instanceof BowControllerHudImpl) {
		// }
		//
		// if (bowController instanceof BowControllerHudImpl2) {
		// BowControllerHudImpl2 controller = (BowControllerHudImpl2) bowController;
		// BowData bowData = bowController.getBowData();
		// ImmediateModeRendererUtils.drawSolidCircle(controller.getPosition(), controller.getRadius(), bowData.getAngle(), Color.WHITE);
		// }

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
	public void update(int delta) {
		Synchronizers.synchronize(delta);
		monitorUpdater.update();

		// check controllers in order, to avoid handle both camera and bow controllerS?

		bowDirectionController.update(delta);
		bowPowerController.update(delta);

		if (!bowDirectionController.wasHandled() && !bowPowerController.wasHandled())
			cameraController.update(delta);

		entitySystemController.update();

		worldWrapper.update(delta);

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

				texture("Tile01Texture", "data/images/tile01.png");
				texture("Tile02Texture", "data/images/tile02.png");
				texture("Tile03Texture", "data/images/tile03.png");
				texture("Tile04Texture", "data/images/tile04.png");
				texture("Tile05Texture", "data/images/tile05.png");
				texture("Tile06Texture", "data/images/tile06.png");
				texture("Tile07Texture", "data/images/tile07.png");
				texture("Tile08Texture", "data/images/tile08.png");

				sprite("Tile01", "Tile01Texture");
				sprite("Tile02", "Tile02Texture");
				sprite("Tile03", "Tile03Texture");
				sprite("Tile04", "Tile04Texture");
				sprite("Tile05", "Tile05Texture");
				sprite("Tile06", "Tile06Texture");
				sprite("Tile07", "Tile07Texture");
				sprite("Tile08", "Tile08Texture");

				// WallTile01

				texture("BuildingSpritesheet", "data/images/building-spritesheet.png");

				sprite("WindowTile01", "BuildingSpritesheet", 0, 0, 64, 64);
				sprite("WallTile01", "BuildingSpritesheet", 64 * 0, 64 * 1, 64, 64);
				sprite("WallTile02", "BuildingSpritesheet", 64 * 1, 64 * 1, 64, 64);
				sprite("WallTile03", "BuildingSpritesheet", 64 * 2, 64 * 1, 64, 64);

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
		resourceManager.unloadAll();
	}

}
