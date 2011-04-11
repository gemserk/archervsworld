package com.gemserk.games.archervsworld;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.gemserk.commons.artemis.components.SpatialComponent;
import com.gemserk.commons.artemis.components.SpriteComponent;
import com.gemserk.commons.artemis.entities.EntityFactory;
import com.gemserk.commons.artemis.systems.SpriteRendererSystem;
import com.gemserk.commons.artemis.systems.SpriteUpdateSystem;
import com.gemserk.commons.artemis.systems.TextRendererSystem;
import com.gemserk.commons.gdx.ScreenAdapter;
import com.gemserk.commons.values.FloatValue;
import com.gemserk.commons.values.IntValue;
import com.gemserk.componentsengine.properties.AbstractProperty;
import com.gemserk.componentsengine.properties.SimpleProperty;
import com.gemserk.games.archervsworld.artemis.components.PhysicsBehavior;
import com.gemserk.games.archervsworld.artemis.components.PhysicsComponent;
import com.gemserk.games.archervsworld.artemis.systems.PhysicsSystem;

public class GameScreen extends ScreenAdapter {

	public static class ArrowPhysicsBehavior extends PhysicsBehavior {

		boolean shouldProcess = true;

		@Override
		public void update(com.badlogic.gdx.physics.box2d.World world, Body body) {
			if (!shouldProcess)
				return;
			Vector2 linearVelocity = body.getLinearVelocity();
			float angle = linearVelocity.angle();
			body.setTransform(body.getPosition(), (float) (angle / 180f * Math.PI));
		}

		@Override
		public void beginContact(Contact contact) {
			shouldProcess = false;
		}
		
		@Override
		public void endContact(Contact contact) {
			// shouldProcess = true;
		}

	}

	static class Box2dAngleProperty extends AbstractProperty<FloatValue> {

		private FloatValue floatValue = new FloatValue(0f);

		private final Body body;

		public Box2dAngleProperty(Body body) {
			this.body = body;
		}

		public FloatValue get() {
			floatValue.value = (float) (body.getAngle() * 180f / Math.PI);
			return floatValue;
		}
	}

	static class Box2dPositionProperty extends AbstractProperty<Vector2> {

		private final Body body;

		public Box2dPositionProperty(Body body) {
			this.body = body;
		}

		@Override
		public Vector2 get() {
			return body.getTransform().getPosition();
		}

		@Override
		public void set(Vector2 value) {
			body.getTransform().setPosition(value);
		}
	}

	private final Game game;

	private TextRendererSystem textRendererSystem;

	private World world;

	private PhysicsSystem physicsSystem;

	private SpriteRendererSystem spriteRenderSystem;

	private SpriteUpdateSystem spriteUpdateSystem;

	private OrthographicCamera camera;

	private Body body;

	private Texture rockTexture;

	private com.badlogic.gdx.physics.box2d.World physicsWorld;

	Box2DDebugRenderer renderer = new Box2DDebugRenderer();

	public GameScreen(Game game) {
		this.game = game;

		Texture fontTexture = new Texture(Gdx.files.internal("data/font.png"));
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		Sprite fontSprite = new Sprite(fontTexture);
		BitmapFont font = new BitmapFont(Gdx.files.internal("data/font.fnt"), fontSprite, false);
		// BitmapFont font = new BitmapFont();

		// camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		int viewportWidth = 80;
		int viewportHeight = 48;

		camera = new OrthographicCamera(viewportWidth, viewportHeight);
		camera.position.set(viewportWidth / 2, viewportHeight / 2, 0);

		// camera.zoom = 0.05f;
		// camera.translate(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 0f);

		textRendererSystem = new TextRendererSystem();
		spriteRenderSystem = new SpriteRendererSystem(camera);
		spriteUpdateSystem = new SpriteUpdateSystem();
		physicsSystem = new PhysicsSystem(new com.badlogic.gdx.physics.box2d.World(new Vector2(0f, -10f), true));

		world = new World();
		world.getSystemManager().setSystem(textRendererSystem);
		world.getSystemManager().setSystem(spriteRenderSystem);
		world.getSystemManager().setSystem(spriteUpdateSystem);
		world.getSystemManager().setSystem(physicsSystem);
		world.getSystemManager().initializeAll();

		EntityFactory entityFactory = new EntityFactory(world);
		entityFactory.fpsEntity( //
				new SimpleProperty<Vector2>(new Vector2(0.5f, 0.5f)), //
				new SimpleProperty<BitmapFont>(font), //
				new SimpleProperty<Vector2>(new Vector2(10, Gdx.graphics.getHeight() - 20)));

		rockTexture = new Texture(Gdx.files.internal("data/rock-512x512.png"));
		rockTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		physicsWorld = physicsSystem.getPhysicsWorld();

		PolygonShape groundPoly = new PolygonShape();
		groundPoly.setAsBox(100, 1);

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

		// createRock(rockTexture, physicsWorld, new Vector2(0, 5), new Vector2(50f, 50f));

		createArrow(new Vector2(10, 20), new Vector2(1, 1), 10f);

		// orthographicCamera.update();
		// orthographicCamera.apply(Gdx.gl10);

	}

	protected void createRock(Texture rockTexture, com.badlogic.gdx.physics.box2d.World physicsWorld, Vector2 startPosition, Vector2 startImpulse) {

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(startPosition);

		body = physicsWorld.createBody(bodyDef);
		CircleShape shape = new CircleShape();
		shape.setPosition(new Vector2(0, 0));
		shape.setRadius(2);
		body.createFixture(shape, 0.25f);
		shape.dispose();

		body.applyAngularImpulse(10f);
		// body.applyForce(new Vector2(150f, 150f), body.getTransform().getPosition());

		body.applyLinearImpulse(startImpulse, body.getTransform().getPosition());

		Entity entity = world.createEntity();

		entity.addComponent(new PhysicsComponent(new SimpleProperty<Body>(body), new SimpleProperty<PhysicsBehavior>(new PhysicsBehavior())));
		entity.addComponent(new SpatialComponent( //
				new Box2dPositionProperty(body), //
				new SimpleProperty<Vector2>(new Vector2(5f, 5f)), //
				new Box2dAngleProperty(body)));
		entity.addComponent(new SpriteComponent(new SimpleProperty<Sprite>(new Sprite(rockTexture)), new SimpleProperty<IntValue>(new IntValue(1))));

		entity.refresh();
	}

	protected void createArrow(Vector2 position, Vector2 direction, float power) {

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.bullet = true;
		bodyDef.position.set(position);

		final Body arrowBody = physicsWorld.createBody(bodyDef);

		// body.SetMassFromShapes();

		PolygonShape polygonShape = new PolygonShape();
		polygonShape.setAsBox(0.72f, 0.05f);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = polygonShape;
		fixtureDef.density = 1f;
		fixtureDef.friction = 0.8f;

		arrowBody.createFixture(fixtureDef);

		// 0.7112 meters

		MassData massData = new MassData();

		massData.center.set(0.35f, 0f);
		massData.I = 0f;
		massData.mass = 0.8f;

		arrowBody.setMassData(massData);
		arrowBody.resetMassData();

		arrowBody.setTransform(position, (float) (direction.angle() / 180f * Math.PI));

		Vector2 impulse = new Vector2(direction);
		impulse.mul(arrowBody.getMass());
		impulse.mul(power);

		Vector2 lp = arrowBody.getWorldPoint(new Vector2(0f, 0f));
		arrowBody.applyLinearImpulse(impulse, lp);

		PhysicsBehavior arrowBehavior = new ArrowPhysicsBehavior();

		arrowBody.setUserData(arrowBehavior);

		Entity entity = world.createEntity();

		entity.addComponent(new PhysicsComponent(new SimpleProperty<Body>(arrowBody), new SimpleProperty<PhysicsBehavior>(arrowBehavior)));

		entity.addComponent(new SpatialComponent( //
				new Box2dPositionProperty(arrowBody), //
				new SimpleProperty<Vector2>(new Vector2(1f, 1f)), //
				new Box2dAngleProperty(arrowBody)));
		entity.addComponent(new SpriteComponent(new SimpleProperty<Sprite>(new Sprite(rockTexture)), new SimpleProperty<IntValue>(new IntValue(1))));

		entity.refresh();

	}

	boolean wasTouched = false;

	private Vector3 p0;

	private Vector3 p1;

	@Override
	public void render(float delta) {

		camera.update();
		camera.apply(Gdx.gl10);

		Gdx.graphics.getGL10().glClear(GL10.GL_COLOR_BUFFER_BIT);

		world.loopStart();
		world.setDelta((int) (delta * 1000));

		physicsSystem.process();

		spriteRenderSystem.process();
		spriteUpdateSystem.process();
		textRendererSystem.process();

		if (Gdx.input.isTouched()) {

			if (!wasTouched) {
				p0 = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
				camera.unproject(p0);
				wasTouched = true;
			} else {
				p1 = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
				camera.unproject(p1);
			}

		} else {
			if (wasTouched) {
				System.out.println(p0);
				System.out.println(p1);

				Vector3 mul = p1.sub(p0).mul(-5);

				System.out.println(mul);

				// createRock(rockTexture, physicsWorld, new Vector2(p0.x, p0.y), new Vector2(mul.x, mul.y));

				float len = mul.len();
				mul.nor();

				createArrow(new Vector2(p0.x, p0.y), new Vector2(mul.x, mul.y), len);

				wasTouched = false;
			}
		}

		camera.update();
		camera.apply(Gdx.gl10);

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