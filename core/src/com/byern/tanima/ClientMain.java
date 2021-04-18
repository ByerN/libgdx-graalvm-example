package com.byern.tanima;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.Curvature;
import com.bitfire.utils.ShaderLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClientMain extends ApplicationAdapter implements InputProcessor, ControllerListener {

  Integer w;
  Integer h;
  SpriteBatch batch;
  Texture img;
  Sound sound;
  Music music;
  SchedulerCaller soundScheduler;
  SchedulerCaller musicPositionCheckerScheduler;
  SchedulerCaller box2dObjectsCheckerScheduler;
  SchedulerCaller controllersScheduler;
  SchedulerCaller pingScheduler;
  World world;
  RayHandler rayHandler;
  Box2DDebugRenderer debugRenderer;
  Camera camera;
  FreeTypeFontGenerator generator;
  List<Fixture> box2dObjects = new ArrayList<>();
  BitmapFont font12;
  PostProcessor postProcessor;
  Optional<KryonetClientServer> maybeKryonet;
  List<BallPainter> ballPainters;

  public ClientMain(Optional<KryonetClientServer.ClientServerConfig> maybeConfig) {
    maybeKryonet = maybeConfig.map(c -> {
      try {
        return new KryonetClientServer(c);
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
        throw new IllegalStateException(e);
      }
    });
  }

  public static class SchedulerCaller {

    private Long lastTimeUsed = 0L;
    private Boolean initialized = false;

    private final Integer threshold;
    private final Runnable runnable;
    private final Boolean onlyOnce;

    public SchedulerCaller(Integer threshold, Runnable runnable) {
      this.threshold = threshold;
      this.runnable = runnable;
      this.onlyOnce = false;
    }

    public SchedulerCaller(Runnable runnable) {
      this.threshold = 0;
      this.runnable = runnable;
      this.onlyOnce = true;
    }

    public void call() {
      long now = System.currentTimeMillis();
      if ((!initialized || !onlyOnce) && (now - lastTimeUsed > threshold)) {
        lastTimeUsed = now;
        runnable.run();
      }
      initialized = true;
    }

  }

  @Override
  public void create() {
    w = Gdx.graphics.getWidth();
    h = Gdx.graphics.getHeight();
    ballPainters = IntStream.range(0, 1).mapToObj(
       i -> new BallPainter(i, w, h, maybeKryonet)
    ).collect(Collectors.toList());
    generator = new FreeTypeFontGenerator(Gdx.files.internal("Pacifico.ttf"));
    FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
    parameter.size = 62;
    parameter.color = Color.BLACK;
    font12 = generator.generateFont(parameter); // font size 12 pixels
    Box2D.init();
    batch = new SpriteBatch();
    img = new Texture("badlogic.jpg");
    sound = Gdx.audio.newSound(Gdx.files.internal("wand1.wav"));
    music = Gdx.audio.newMusic(Gdx.files.internal("all.mp3"));
   // music.play();
    music.setPosition(0.5f);
    soundScheduler = new SchedulerCaller(5000, new Runnable() {
      @Override
      public void run() {
       // sound.play();
      }
    });
    musicPositionCheckerScheduler = new SchedulerCaller(2000, new Runnable() {
      @Override
      public void run() {
        System.out.println("music.isPlaying(): " + music.isPlaying());
        System.out.println("music.getPosition(): " + music.getPosition());
      }
    });
    box2dObjectsCheckerScheduler = new SchedulerCaller(2000, new Runnable() {
      @Override
      public void run() {
        for (Fixture fixture : box2dObjects) {
          System.out.println("box2d fixture.getBody().getPosition()" + fixture.getBody().getPosition().x + " " + fixture.getBody().getPosition().y);
        }

      }
    });
    controllersScheduler = new SchedulerCaller(2000, new Runnable() {
      @Override
      public void run() {
        for (Controller controller : Controllers.getControllers()) {
          System.out.println("Controller: " + controller.getName());
        }
      }
    });
    pingScheduler = new SchedulerCaller(2000, () -> {
      System.out.println("'Hi' sent");
      maybeKryonet.ifPresent(kryonet -> kryonet.send("Hi"));
    });
    System.out.println("music.isPlaying(): " + music.isPlaying());
    Gdx.input.setInputProcessor(this);
    Controllers.addListener(this);
    world = new World(new Vector2(0, -10), true);
    rayHandler = new RayHandler(world);
    debugRenderer = new Box2DDebugRenderer();
    camera = new OrthographicCamera(w, h);
    camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
    camera.update();
    new PointLight(rayHandler, 1000, new Color(1, 1, 1, 1), 1000, w / 2, h / 2);

    ShaderLoader.BasePath = "./shaders/";
    postProcessor = new PostProcessor( false, false, true );
    Curvature curvature = new Curvature();

    postProcessor.addEffect( curvature );
  }

  @Override
  public void render() {
    camera.update();
    Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    postProcessor.capture();
    batch.begin();
    batch.draw(img, 0, 0);
    ballPainters.forEach(b -> b.paint(batch));
    batch.end();

    postProcessor.render();

    soundScheduler.call();
    musicPositionCheckerScheduler.call();
    pingScheduler.call();
    box2dObjectsCheckerScheduler.call();
    debugRenderer.render(world, camera.combined);
    world.step(1 / 60f, 6, 2);
    rayHandler.setCombinedMatrix((OrthographicCamera) camera);
    rayHandler.updateAndRender();
    batch.begin();
    font12.draw(batch, "Hello Native!", w/2, h/2);
    batch.end();
  }

  @Override
  public void dispose() {
    maybeKryonet.ifPresent(KryonetClientServer::dispose);
    postProcessor.dispose();
    batch.dispose();
    img.dispose();
    sound.dispose();
    generator.dispose();
    music.dispose();
    world.dispose();
    rayHandler.dispose();
    for (Fixture fixture : box2dObjects) {
      fixture.getShape().dispose();
    }
  }


  @Override
  public boolean keyDown(int i) {
    System.out.println("keyDown " + i);
    return true;
  }

  @Override
  public boolean keyUp(int i) {
    System.out.println("keyUp " + i);
    return true;
  }

  @Override
  public boolean keyTyped(char c) {
    System.out.println("keyTyped " + c);
    return true;
  }

  @Override
  public boolean touchDown(int i, int i1, int i2, int i3) {
    System.out.println("touchDown " + i + " " + i1 + " " + i2 + " " + i3);
    createBox2dBody(i, (int) (h - i1));
    return true;
  }

  @Override
  public boolean touchUp(int i, int i1, int i2, int i3) {
    System.out.println("touchUp " + i + " " + i1 + " " + i2 + " " + i3);
    return true;
  }

  @Override
  public boolean touchDragged(int i, int i1, int i2) {
    System.out.println("touchDragged " + i + " " + i1 + " " + i2);
    return true;
  }

  @Override
  public boolean mouseMoved(int i, int i1) {
    //System.out.println("mouseMoved " + i + " " + i1 );
    return true;
  }

  @Override
  public boolean scrolled(float v, float v1) {
    System.out.println("scrolled " + v + " " + v1);
    return true;
  }


  @Override
  public void connected(Controller controller) {
    System.out.println("Controller connected: " + controller.getName());
  }

  @Override
  public void disconnected(Controller controller) {
    System.out.println("Controller disconnected: " + controller.getName());

  }

  @Override
  public boolean buttonDown(Controller controller, int buttonCode) {
    System.out.println("Controller buttonDown: " + buttonCode);

    return true;
  }

  @Override
  public boolean buttonUp(Controller controller, int buttonCode) {
    System.out.println("Controller buttonUp: " + buttonCode);
    return true;
  }

  @Override
  public boolean axisMoved(Controller controller, int axisCode, float value) {
    System.out.println("Controller axisMoved: " + axisCode + " " + value);
    return true;
  }


  public void createBox2dBody(int x, int y) {
    //https://github.com/libgdx/libgdx/wiki/Box2d
    // First we create a body definition
    BodyDef bodyDef = new BodyDef();
// We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
    bodyDef.type = BodyDef.BodyType.DynamicBody;
// Set our body's starting position in the world
    bodyDef.position.set(x, y);

// Create our body in the world using our body definition
    Body body = world.createBody(bodyDef);

// Create a circle shape and set its radius to 6
    CircleShape circle = new CircleShape();
    circle.setRadius(6f);

// Create a fixture definition to apply our shape to
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = circle;
    fixtureDef.density = 0.5f;
    fixtureDef.friction = 0.4f;
    fixtureDef.restitution = 0.6f; // Make it bounce a little bit

// Create our fixture and attach it to the body
    Fixture fixture = body.createFixture(fixtureDef);
    box2dObjects.add(fixture);
    System.out.println("Body created at " + x + " " + y);
// Remember to dispose of any shapes after you're done with them!
// BodyDef and FixtureDef don't need disposing, but shapes do.
    //circle.dispose();
  }
}
