package com.gemserk.games.archervsworld;

import java.util.ArrayList;

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
import com.gemserk.games.archervsworld.artemis.systems.CorrectArrowDirectionSystem;
import com.gemserk.games.archervsworld.artemis.systems.HudButtonSystem;
import com.gemserk.games.archervsworld.artemis.systems.PhysicsSystem;
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

		myCamera = new Libgdx2dCameraTransformImpl(camera);

		ArrayList<Layer> layers = new ArrayList<Layer>();

		// background layer
		layers.add(new Layer(-1000, -5, new Libgdx2dCameraTransformImpl()));

		// world layer
		layers.add(new Layer(-5, 10, myCamera));

		// hud layer
		layers.add(new Layer(10, 1000, new Libgdx2dCameraTransformImpl()));

		spriteRenderSystem = new SpriteRendererSystem(myCamera, layers);

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

		worldWrapper.addSystem(new CorrectArrowDirectionSystem());
		worldWrapper.addSystem(physicsSystem);
		worldWrapper.addSystem(pointerUpdateSystem);
		worldWrapper.addSystem(hudButtonSystem);
		worldWrapper.addSystem(new HierarchySystem());
		worldWrapper.addSystem(new AliveSystem());
		worldWrapper.addSystem(new SpriteUpdateSystem());
		worldWrapper.addSystem(spriteRenderSystem);
		worldWrapper.addSystem(new TextRendererSystem());
		
		worldWrapper.addSystem(new EditorSystem(pointer0));

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

	private HudButtonSystem hudButtonSystem;

	private PointerUpdateSystem pointerUpdateSystem;

	private WorldWrapper worldWrapper;

	private LibgdxPointer pointer0;

	static class WorldWrapper {

		private final World world;

		ArrayList<EntitySystem> systems;

		public WorldWrapper(World world) {
			this.world = world;
			systems = new ArrayList<EntitySystem>();
		}

		public void addSystem(EntitySystem entitySystem) {
			world.getSystemManager().setSystem(entitySystem);
			systems.add(entitySystem);
		}

		public void init() {
			world.getSystemManager().initializeAll();
		}

		public void update(int delta) {

			world.loopStart();
			world.setDelta(delta);

			for (int i = 0; i < systems.size(); i++) {
				EntitySystem system = systems.get(i);
				system.process();
			}

		}

	}
	
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
					selectedEntity = archerVsWorldEntityFactory.createBow(new Vector2(pressedPosition));
//					selectedEntity = archerVsWorldEntityFactory.createPhysicsArrow(new Vector2(pressedPosition), new Vector2(1f, 0f), 10f);
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
