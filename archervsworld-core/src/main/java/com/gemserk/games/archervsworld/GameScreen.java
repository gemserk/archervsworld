package com.gemserk.games.archervsworld;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.entities.EntityFactory;
import com.gemserk.commons.artemis.systems.SpriteRendererSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.systems.TextRendererSystem;
import com.gemserk.commons.gdx.ScreenAdapter;
import com.gemserk.commons.gdx.input.LibgdxPointer;
import com.gemserk.commons.values.FloatValue;
import com.gemserk.commons.values.IntValue;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.games.archervsworld.artemis.entities.ArcherVsWorldEntityFactory;
import com.gemserk.games.archervsworld.artemis.systems.PhysicsSystem;
import com.gemserk.games.archervsworld.artemis.systems.UpdateBowSystem;

public class GameScreen extends ScreenAdapter {

	private final Game game;

	private TextRendererSystem textRendererSystem;

	private World world;

	private PhysicsSystem physicsSystem;

	private SpriteRendererSystem spriteRenderSystem;

	private SpriteUpdateSystem spriteUpdateSystem;

	private OrthographicCamera camera;

	private com.badlogic.gdx.physics.box2d.World physicsWorld;

	Box2DDebugRenderer renderer = new Box2DDebugRenderer();
	
	ArcherVsWorldEntityFactory archerVsWorldEntityFactory;

	public GameScreen(Game game) {
		this.game = game;

		Texture fontTexture = new Texture(Gdx.files.internal("data/font.png"));
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		Texture rockTexture = new Texture(Gdx.files.internal("data/rock-512x512.png"));
		rockTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		Texture arrowTexture = new Texture(Gdx.files.internal("data/arrow-512x512.png"));
		arrowTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		Texture texture = new Texture(Gdx.files.internal("data/bow-512x512.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		Sprite fontSprite = new Sprite(fontTexture);
		BitmapFont font = new BitmapFont(Gdx.files.internal("data/font.fnt"), fontSprite, false);
		// BitmapFont font = new BitmapFont();

		// camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		int viewportWidth = 20;
		int viewportHeight = 12;

		camera = new OrthographicCamera(viewportWidth, viewportHeight);
		camera.position.set(viewportWidth / 2, viewportHeight / 2, 0);
		
		// camera.zoom = 0.05f;
		// camera.translate(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 0f);
		
		archerVsWorldEntityFactory = new ArcherVsWorldEntityFactory();

		textRendererSystem = new TextRendererSystem();
		spriteRenderSystem = new SpriteRendererSystem(camera);
		spriteUpdateSystem = new SpriteUpdateSystem();
		physicsSystem = new PhysicsSystem(new com.badlogic.gdx.physics.box2d.World(new Vector2(0f, -10f), true));
		updateBowSystem = new UpdateBowSystem(new LibgdxPointer(0, camera), archerVsWorldEntityFactory);

		world = new World();
		world.getSystemManager().setSystem(textRendererSystem);
		world.getSystemManager().setSystem(spriteRenderSystem);
		world.getSystemManager().setSystem(spriteUpdateSystem);
		world.getSystemManager().setSystem(physicsSystem);
		world.getSystemManager().setSystem(updateBowSystem);
		world.getSystemManager().initializeAll();
		
		EntityFactory entityFactory = new EntityFactory(world);
		entityFactory.fpsEntity( //
				new SimpleProperty<Vector2>(new Vector2(0.5f, 0.5f)), //
				new SimpleProperty<BitmapFont>(font), //
				new SimpleProperty<Vector2>(new Vector2(10, Gdx.graphics.getHeight() - 20)));
		
		physicsWorld = physicsSystem.getPhysicsWorld();
		
		archerVsWorldEntityFactory.setWorld(world);
		archerVsWorldEntityFactory.setPhysicsWorld(physicsWorld);
		archerVsWorldEntityFactory.setArrowTexture(arrowTexture);
		archerVsWorldEntityFactory.setRockTexture(rockTexture);
		archerVsWorldEntityFactory.setBowTexture(texture);
		
		PolygonShape groundPoly = new PolygonShape();
		groundPoly.setAsBox(40, 1);

		// next we create the body for the ground platform. It's
		// simply a static body.
		BodyDef groundBodyDef = new BodyDef();
		groundBodyDef.type = BodyType.StaticBody;
		groundBodyDef.position.set(0, -0.5f);
		Body groundBody = physicsWorld.createBody(groundBodyDef);

		// finally we add a fixture to the body using the polygon
		// defined above. Note that we have to dispose PolygonShapes
		// and CircleShapes once they are no longer used. This is the
		// only time you have to care explicitely for memomry managment.
		groundBody.createFixture(groundPoly, 10);
		groundPoly.dispose();

//		archerVsWorldEntityFactory.createRock(new Vector2(5, 3), new Vector2(3f, 3f), new Vector2(0f, 0f), 120f);
		
		archerVsWorldEntityFactory.createRock(new Vector2(7, 1), new Vector2(2f, 2f), new Vector2(0f, 0f), 210f);
		
//		archerVsWorldEntityFactory.createRock(new Vector2(10, 10), new Vector2(1f, 1f), new Vector2(0f, 0f), 50f);
		

		createBackground();
		
		archerVsWorldEntityFactory.createBow(new Vector2(1f, 1.7f));

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

	boolean wasTouched = false;

	private UpdateBowSystem updateBowSystem;
	
	@Override
	public void render(float delta) {
		
		camera.update();
		camera.apply(Gdx.gl10);

		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		world.loopStart();
		world.setDelta((int) (delta * 1000));

		physicsSystem.process();
		updateBowSystem.process();

		spriteRenderSystem.process();
		spriteUpdateSystem.process();
		textRendererSystem.process();
		
		camera.update();
		camera.apply(Gdx.gl10);

		if (Gdx.input.isKeyPressed(Input.Keys.KEYCODE_D)) 
			renderer.render(physicsWorld);

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

}