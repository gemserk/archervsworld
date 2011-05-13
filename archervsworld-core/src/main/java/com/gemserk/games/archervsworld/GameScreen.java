package com.gemserk.games.archervsworld;

import java.io.InputStream;
import java.util.ArrayList;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.w3c.dom.Element;

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
import com.gemserk.animation4j.transitions.sync.Synchronizers;
import com.gemserk.commons.artemis.WorldWrapper;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpatialImpl;
import com.gemserk.commons.artemis.components.TextComponent;
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
import com.gemserk.commons.gdx.resources.dataloaders.BitmapFontDataLoader;
import com.gemserk.commons.svg.inkscape.DocumentParser;
import com.gemserk.commons.svg.inkscape.SvgDocument;
import com.gemserk.commons.svg.inkscape.SvgDocumentHandler;
import com.gemserk.commons.svg.inkscape.SvgInkscapeGroup;
import com.gemserk.commons.svg.inkscape.SvgInkscapeGroupHandler;
import com.gemserk.commons.svg.inkscape.SvgInkscapeImage;
import com.gemserk.commons.svg.inkscape.SvgInkscapeImageHandler;
import com.gemserk.commons.svg.inkscape.SvgInkscapePath;
import com.gemserk.commons.svg.inkscape.SvgInkscapePathHandler;
import com.gemserk.commons.svg.inkscape.SvgParser;
import com.gemserk.componentsengine.input.ButtonMonitor;
import com.gemserk.componentsengine.input.LibgdxButtonMonitor;
import com.gemserk.componentsengine.input.MonitorUpdater;
import com.gemserk.componentsengine.properties.AbstractProperty;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.games.archervsworld.GameScreen.EntitySystemController.ActivableSystemRegistration;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.artemis.systems.CorrectArrowDirectionSystem;
import com.gemserk.games.archervsworld.artemis.systems.DebugInformationSystem;
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
		// worldWrapper.addUpdateSystem(new AliveSystem());
		worldWrapper.addUpdateSystem(new AliveAreaSystem());
		worldWrapper.addUpdateSystem(new TimerSystem());
		worldWrapper.addUpdateSystem(new DebugInformationSystem());

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

		Vector2 grassSize = new Vector2(1f, 0.5f);

		float x = 0f;
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

		SvgParser svgParser = new SvgParser();
		svgParser.addHandler(new SvgDocumentHandler() {
			@Override
			protected void handle(SvgParser svgParser, SvgDocument svgDocument, Element element) {
				GameScreen.this.svgDocument = svgDocument;
			}
		});
		svgParser.addHandler(new SvgInkscapeGroupHandler() {
			@Override
			protected void handle(SvgParser svgParser, SvgInkscapeGroup svgInkscapeGroup, Element element) {

				if (svgInkscapeGroup.getGroupMode().equals("layer") && !svgInkscapeGroup.getLabel().equalsIgnoreCase("World")) {
					svgParser.processChildren(false);
					return;
				}

			}
		});
		svgParser.addHandler(new SvgInkscapeImageHandler() {

			private boolean isFlipped(Matrix3f matrix) {
				return matrix.getM00() != matrix.getM11();
			}

			@Override
			protected void handle(SvgParser svgParser, SvgInkscapeImage svgImage, Element element) {

				if (svgImage.getLabel() == null)
					return;

				Resource<Texture> texture = resourceManager.get(svgImage.getLabel());

				if (texture == null)
					return;

				float width = svgImage.getWidth();
				float height = svgImage.getHeight();

				Matrix3f transform = svgImage.getTransform();

				Vector3f position = new Vector3f(svgImage.getX() + width * 0.5f, svgImage.getY() + height * 0.5f, 0f);
				transform.transform(position);

				Vector3f direction = new Vector3f(1f, 0f, 0f);
				transform.transform(direction);

				float angle = 360f - (float) (Math.atan2(direction.y, direction.x) * 180 / Math.PI);

				float sx = 1f;
				float sy = 1f;

				if (isFlipped(transform)) {
					sy = -1f;
				}

				// System.out.println(MessageFormat.format("id={0}, label={1}, x={2}, y={3}, angle={4}", svgImage.getId(), svgImage.getLabel(), svgImage.getX(), svgImage.getY(), angle));

				float x = position.x;
				float y = svgDocument.getHeight() - position.y;

				// System.out.println(MessageFormat.format("id={0}, label={1}, x={2}, y={3}, angle={4}", svgImage.getId(), svgImage.getLabel(), x, y, angle));

				Sprite sprite = new Sprite(texture.get());
				sprite.setScale(sx, sy);

				archerVsWorldEntityFactory.createStaticSprite(x, y, width, height, angle, sprite, 2, Color.WHITE, 0.5f, 0.5f);
			}
		});
		InputStream svg = Gdx.files.internal("data/scene01.svg").read();
		svgParser.parse(new DocumentParser().parse(svg));

		// parsing physics bodies...

		svgParser = new SvgParser();
		svgParser.addHandler(new SvgInkscapeGroupHandler() {
			@Override
			protected void handle(SvgParser svgParser, SvgInkscapeGroup svgInkscapeGroup, Element element) {

				if (svgInkscapeGroup.getGroupMode().equals("layer") && !svgInkscapeGroup.getLabel().equalsIgnoreCase("Physics")) {
					svgParser.processChildren(false);
					return;
				}

			}
		});
		svgParser.addHandler(new SvgInkscapePathHandler() {
			@Override
			protected void handle(SvgParser svgParser, SvgInkscapePath svgPath, Element element) {

				Vector2f[] points = svgPath.getPoints();
				Vector2[] vertices = new Vector2[points.length];

				for (int i = 0; i < points.length; i++) {
					Vector2f point = points[i];
					// this coordinates transform, should be processed when parsed
					vertices[i] = new Vector2(point.x, svgDocument.getHeight() - point.y);
					System.out.println(vertices[i]);
				}

				archerVsWorldEntityFactory.createStaticBody(new Vector2(), vertices);

			}
		});

		svg = Gdx.files.internal("data/scene01.svg").read();
		svgParser.parse(new DocumentParser().parse(svg));

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

		entity.addComponent(new SpatialComponent(new SpatialImpl(Gdx.graphics.getWidth() - 80, Gdx.graphics.getHeight() - 60, 0.5f, 0.5f, 50f)));
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
				texture("Background", "data/background-512x512.jpg", false);

				texture("Rock", "data/rock-512x512.png");
				texture("Bow", "data/bow-512x512.png");
				texture("Arrow", "data/arrow-512x512.png");
				texture("Tree", "data/tree-512x512.png");
				texture("Grass", "data/grass-128x128.png");

				texture("Ground01", internal("data/ground-01.png"), true);
				texture("Ground02", internal("data/ground-02.png"), true);
				texture("Ground03", internal("data/ground-03.png"), true);
				texture("Ground04", internal("data/ground-04.png"), true);
				texture("Ground05", internal("data/ground-05.png"), true);
				texture("Ground06", internal("data/ground-06.png"), true);

				texture("Grass01", internal("data/grass-01.png"), true);
				texture("Grass02", internal("data/grass-02.png"), true);
				texture("Grass03", internal("data/grass-03.png"), true);

				texture("Tile01", internal("data/tile01.png"), true);
				texture("Tile02", internal("data/tile02.png"), true);
				texture("Tile03", internal("data/tile03.png"), true);
				texture("Tile04", internal("data/tile04.png"), true);
				texture("Tile11", internal("data/tile11.png"), true);
				texture("Tile12", internal("data/tile12.png"), true);
				texture("Tile13", internal("data/tile13.png"), true);
				texture("Tile14", internal("data/tile14.png"), true);
				texture("Tile21", internal("data/tile21.png"), true);

				texture("Tower", "data/tower-128x128.png");

				texture("Button", "data/button-template-64x64.png");
				texture("FontTexture", "data/font.png");

				sound("HitFleshSound", "data/sounds/hit-flesh.ogg");
				sound("HitGroundSound", "data/sounds/hit-ground.ogg");
				sound("BowSound", "data/sounds/bow.ogg");

				texture("CloudsSpritesheet", "data/clouds-spritesheet.png", false);

				sprite("Cloud01", "CloudsSpritesheet", 0, 0, 512, 128);
				sprite("Cloud02", "CloudsSpritesheet", 0, 128, 512, 128);
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
