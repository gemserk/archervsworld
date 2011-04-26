package com.gemserk.games.archervsworld;

import java.util.ArrayList;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.World;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
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
import com.gemserk.commons.artemis.entities.EntityFactory;
import com.gemserk.commons.artemis.systems.AliveSystem;
import com.gemserk.commons.artemis.systems.HierarchySystem;
import com.gemserk.commons.artemis.systems.PointerUpdateSystem;
import com.gemserk.commons.artemis.systems.RenderLayer;
import com.gemserk.commons.artemis.systems.SpriteRendererSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.systems.TextRendererSystem;
import com.gemserk.commons.gdx.Libgdx2dCamera;
import com.gemserk.commons.gdx.Libgdx2dCameraTransformImpl;
import com.gemserk.commons.gdx.ScreenAdapter;
import com.gemserk.commons.gdx.input.LibgdxPointer;
import com.gemserk.commons.gdx.math.MathUtils2;
import com.gemserk.commons.gdx.resources.dataloaders.BitmapFontDataLoader;
import com.gemserk.commons.gdx.resources.dataloaders.SoundDataLoader;
import com.gemserk.commons.gdx.resources.dataloaders.TextureDataLoader;
import com.gemserk.componentsengine.input.ButtonMonitor;
import com.gemserk.componentsengine.input.MonitorUpdater;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.artemis.systems.CorrectArrowDirectionSystem;
import com.gemserk.games.archervsworld.artemis.systems.HudButtonSystem;
import com.gemserk.games.archervsworld.artemis.systems.PhysicsSystem;
import com.gemserk.resources.Resource;
import com.gemserk.resources.ResourceManager;
import com.gemserk.resources.ResourceManagerImpl;
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

	private World world;

	private PhysicsSystem physicsSystem;

	private SpriteRendererSystem spriteRenderSystem;

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

//		myCamera = new Libgdx2dCameraTransformImpl(camera);
		myCamera = new Libgdx2dCameraTransformImpl();

		ArrayList<RenderLayer> renderLayers = new ArrayList<RenderLayer>();

		// background layer
		renderLayers.add(new RenderLayer(-1000, -5, new Libgdx2dCameraTransformImpl()));

		// world layer
		renderLayers.add(new RenderLayer(-5, 10, myCamera));

		// hud layer
		renderLayers.add(new RenderLayer(10, 1000, new Libgdx2dCameraTransformImpl()));

		spriteRenderSystem = new SpriteRendererSystem(renderLayers);

		Vector2 gravity = new Vector2(0f, -10f);
		physicsSystem = new PhysicsSystem(new com.badlogic.gdx.physics.box2d.World(gravity, true));

		if (physicsSystem.isEnabled())
			physicsSystem.toggle();

		pointer0 = new LibgdxPointer(0, myCamera);
		LibgdxPointer pointer1 = new LibgdxPointer(1, myCamera);

		ArrayList<LibgdxPointer> pointers = new ArrayList<LibgdxPointer>();

		pointers.add(pointer0);
		pointers.add(pointer1);

		hudButtonSystem = new HudButtonSystem(pointer0);
		pointerUpdateSystem = new PointerUpdateSystem(pointers);

		world = new World();

		worldWrapper = new WorldWrapper(world);

		worldWrapper.add(new CorrectArrowDirectionSystem());
		worldWrapper.add(physicsSystem);
		worldWrapper.add(pointerUpdateSystem);
		worldWrapper.add(hudButtonSystem);
		worldWrapper.add(new HierarchySystem());
		worldWrapper.add(new AliveSystem());
		worldWrapper.add(new SpriteUpdateSystem());
		worldWrapper.add(spriteRenderSystem);
		worldWrapper.add(new TextRendererSystem());

		// worldWrapper.addSystem(new EditorSystem(pointer0));

		worldWrapper.add(new CameraMovementSystem());

		worldWrapper.init();

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

		archerVsWorldEntityFactory.createBackground(camera.viewportWidth, camera.viewportHeight);

		Entity cameraEntity = world.createEntity();

		// cameraEntity.addComponent(new PointerComponent());
		cameraEntity.addComponent(new CameraControllerComponent(new CameraController(new LibgdxPointer(0, new Libgdx2dCameraTransformImpl()), myCamera, //
				new Rectangle(0, Gdx.graphics.getHeight() - 200, 200, 200))));

		cameraEntity.refresh();

		Vector2 grassSize = new Vector2(0.5f, 0.5f);

		float x = 0f;
		final float y = 0f;

		archerVsWorldEntityFactory.createGround(new Vector2(40f, 0.22f), new Vector2(80f, 0.44f));

		for (int i = 0; i < 60; i++) {
			archerVsWorldEntityFactory.createGrass(new Vector2(x + grassSize.x / 2f, y + grassSize.y / 2f), grassSize);
			x += grassSize.x;
		}

		archerVsWorldEntityFactory.createArcher(new Vector2(1f, 1.7f));

		monitorUpdater = new MonitorUpdaterImpl();

	}

	private EntityFactory entityFactory;

	private MonitorUpdaterImpl monitorUpdater;

	private Libgdx2dCamera myCamera;

	private HudButtonSystem hudButtonSystem;

	private PointerUpdateSystem pointerUpdateSystem;

	private WorldWrapper worldWrapper;

	private LibgdxPointer pointer0;

	@Override
	public void render(float delta) {

		camera.update();
		camera.apply(Gdx.gl10);

		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		worldWrapper.update((int) (delta * 1000));

		camera.update();
		camera.apply(Gdx.gl10);

		renderer.render(physicsWorld);

		monitorUpdater.update();

		Synchronizers.synchronize();

	}

	class EditorSystem extends EntitySystem {

		private final LibgdxPointer pointer;

		private Entity selectedEntity = null;

		@SuppressWarnings("unchecked")
		public EditorSystem(LibgdxPointer pointer) {
			super(SpatialComponent.class);
			this.pointer = pointer;
		}

		@Override
		protected void processEntities(ImmutableBag<Entity> entities) {

			if (pointer.wasPressed) {
				Vector2 pressedPosition = pointer.getPressedPosition();

				for (int i = 0; i < entities.size(); i++) {

					Entity entity = entities.get(i);

					SpatialComponent spatialComponent = entity.getComponent(SpatialComponent.class);

					if (pressedPosition.dst(spatialComponent.getPosition()) < 1f) {
						// selected entity!
						selectedEntity = entity;
						break;
					}

				}

				if (selectedEntity == null) {
					// add new entity?
					// selectedEntity = archerVsWorldEntityFactory.createBow(new Vector2(pressedPosition));
					selectedEntity = archerVsWorldEntityFactory.createGround(pressedPosition, new Vector2(5f, 1f));
					// selectedEntity = archerVsWorldEntityFactory.createPhysicsArrow(new Vector2(pressedPosition), new Vector2(1f, 0f), 10f);
				}

			}

			if (pointer.touched) {

				if (selectedEntity != null) {

					SpatialComponent spatialComponent = selectedEntity.getComponent(SpatialComponent.class);

					Vector2 position = pointer.getPosition();

					spatialComponent.setPosition(position);

				}

			}

			if (pointer.wasReleased) {
				selectedEntity = null;
			}

		}

		@Override
		protected boolean checkProcessing() {
			return true;
		}

		@Override
		public void initialize() {

		}

	}

	static class CameraControllerComponent extends Component {

		private CameraController cameraController;

		public CameraController getCameraController() {
			return cameraController;
		}

		public CameraControllerComponent(CameraController cameraController) {
			this.cameraController = cameraController;
		}

	}

	static class CameraController {

		private Vector2 previousPosition = new Vector2();

		private LibgdxPointer pointer;

		private Libgdx2dCamera camera;

		private Rectangle rectangle;

		boolean moving = false;

		public CameraController(LibgdxPointer pointer, Libgdx2dCamera camera, Rectangle pointerArea) {
			this.pointer = pointer;
			this.camera = camera;
			this.rectangle = pointerArea;
		}

		public void update() {
			pointer.update();

			if (pointer.wasPressed) {
				if (MathUtils2.inside(rectangle, pointer.getPressedPosition())) {
					previousPosition.set(pointer.getPressedPosition());
					moving = true;
				} 
				return;
			}

			if (pointer.wasReleased) {
				moving = false;
				return;
			}

			if (!moving)
				return;

			Vector2 pointerPosition = new Vector2(pointer.getPosition());
			Vector2 pressedPosition = new Vector2(previousPosition);

			Vector2 cameraPosition = new Vector2(0f, 0f);

			camera.unproject(cameraPosition);
			camera.unproject(pressedPosition);
			camera.unproject(pointerPosition);

			pointerPosition.sub(pressedPosition);

			camera.move(pointerPosition.x, pointerPosition.y);

			previousPosition.set(pointer.getPosition());
		}

	}

	class CameraMovementSystem extends EntitySystem {

		// could be an abstraction of the camera controller in the middle

		@SuppressWarnings("unchecked")
		public CameraMovementSystem() {
			super(CameraControllerComponent.class);
		}

		@Override
		protected void processEntities(ImmutableBag<Entity> entities) {

			for (int i = 0; i < entities.size(); i++) {

				Entity entity = entities.get(i);

				CameraControllerComponent cameraControllerComponent = entity.getComponent(CameraControllerComponent.class);
				cameraControllerComponent.getCameraController().update();

			}

		}

		@Override
		protected boolean checkProcessing() {
			return true;
		}

		@Override
		public void initialize() {

		}

	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void show() {

	}

	protected void loadResources() {
		
		texture("Background", "data/background-512x512.jpg");
		texture("Rock", "data/rock-512x512.png");
		texture("Bow", "data/bow-512x512.png");
		texture("Arrow", "data/arrow-512x512.png");
		texture("Tree", "data/tree-512x512.png");
		texture("Grass", "data/grass-128x128.png");
		texture("Button", "data/button-template-64x64.png");
		texture("FontTexture", "data/font.png");

		Resource<Texture> fontTextureResource = resourceManager.get("FontTexture");
		resourceManager.add("Font", new CachedResourceLoader<BitmapFont>(new ResourceLoaderImpl<BitmapFont>(new BitmapFontDataLoader(Gdx.files.internal("data/font.fnt"), new Sprite(fontTextureResource.get())))));

		sound("HitFleshSound", "data/hit-flesh.ogg");
		sound("HitGroundSound", "data/hit-ground.ogg");
		sound("BowSound", "data/bow.ogg");
		
	}
	
	protected void texture(String name, String fileName) {
		resourceManager.add(name, new CachedResourceLoader<Texture>(new ResourceLoaderImpl<Texture>(new TextureDataLoader(Gdx.files.internal(fileName)))));
	}
	
	protected void sound(String name, String fileName) {
		resourceManager.add(name, new CachedResourceLoader<Sound>(new ResourceLoaderImpl<Sound>(new SoundDataLoader(Gdx.files.internal(fileName)))));
	}

	@Override
	public void dispose() {

	}

}
