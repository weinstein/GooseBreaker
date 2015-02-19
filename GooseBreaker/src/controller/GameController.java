package controller;

import graphics.Animation;
import graphics.CameraController;
import graphics.GameRenderer;
import graphics.ParticleEffect;
import graphics.ParticleJet;
import graphics.StaticAnimation;
import graphics.TrajectoryTween;
import io.IceWorldLoader;
import io.LevelIO;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.MassData;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;
import org.jbox2d.dynamics.Body;

import physics.Agent;
import physics.BreadBall;
import physics.FlyingPieceOfFood;
import physics.FoodPiece;
import physics.FreeSpace;
import physics.Goose;
import physics.IceWorld;
import physics.LinearTrajectory;
import physics.ThrownTrajectory;
import physics.Trajectory;
import sound.AudioController;
import tessellation.IceSheet;
import util.PhysicsUtil;
import util.RandomUtil;

/**
 * The game controller handles some processing between the input layer and the
 * physics world layer, for example, handling bread which is in-flight after the
 * user flings it, but has not yet entered the physics world.
 * 
 * The controller is abstracted so that different types of controllers can
 * implement different kinds of game rules.
 * 
 * @author Jack
 *
 */
public abstract class GameController {
  private static final float POINTS_PER_FOOD_MASS_UNIT = 5;
  private static final int POINTS_PER_ICE_BREAK = 1;
  public static final int POINTS_PER_ICE_SHEET = 100;
  private static final float MAX_TRAJECTORY_ERROR = 0.01f;
  
  private IceWorld gameWorld;
  private int score;
  private List<FlyingPieceOfFood> foodsInFlight;
  private List<ParticleEffect> particles;
  private List<Vec2> snowflakes;
  private float xMinBound;
  private float xMaxBound;
  private float yMinBound;
  private float yMaxBound;
  private GameRenderer renderer;
  private AudioController audio;
  private String userMessage;
  private long userMessageHideTimeMs;
  private long lastStepTimeMillis;
  
  public GameController(IceWorld world, GameRenderer renderer, AudioController audio) {
    this.gameWorld = world;
    this.gameWorld.setController(this);
    this.renderer = renderer;
    this.audio = audio;
    this.score = 0;
    this.foodsInFlight = new ArrayList<FlyingPieceOfFood>();
    this.particles = new ArrayList<ParticleEffect>();
    
    this.xMinBound = this.yMinBound = Float.MIN_VALUE;
    this.xMaxBound = this.yMaxBound = Float.MAX_VALUE;
    
    this.lastStepTimeMillis = System.currentTimeMillis();
    this.snowflakes = new ArrayList<Vec2>();
  }
  
  private void initSnowFlakes(int numflakes) {
    float maxw = this.renderer.getCamera().getScreenWidth() / CameraController.MIN_ZOOM_FACTOR;
    float maxh = this.renderer.getCamera().getScreenHeight() / CameraController.MIN_ZOOM_FACTOR;
    float minx = this.renderer.getCamera().getPanBoundLowerX() - maxw/2;
    float miny = this.renderer.getCamera().getPanBoundLowerY() - maxh/2;
    float maxx = this.renderer.getCamera().getPanBoundUpperX() + maxw/2;
    float maxy = this.renderer.getCamera().getPanBoundUpperY() + maxh/2;
    
    for (int i = 0; i < numflakes; ++i) {
      float x = (float) RandomUtil.Uniform(minx, maxx);
      float y = (float) RandomUtil.Uniform(miny, maxy);
      this.snowflakes.add(new Vec2(x, y));
      System.out.println("Add new flake (" + x + "," + y + ")");
    }
  }
  
  /**
   * Set the world boundaries. Don't allow bread to be thrown outside these
   * boundaries. This should correspond to the boundary walls in the physics
   * world.
   * TODO: let the camera access this to implement pan boundaries.
   * @param xmin
   * @param xmax
   * @param ymin
   * @param ymax
   */
  public void setBoundaries(float xmin, float xmax, float ymin, float ymax) {
    this.xMinBound = xmin;
    this.xMaxBound = xmax;
    this.yMinBound = ymin;
    this.yMaxBound = ymax;
    this.getRenderer().getCamera().setPanBoundaries(xmin, ymin, xmax, ymax);
  }
  
  /**
   * Set the world boundaries.
   * See setBoundaries(float xmin, float xmax, float ymin, float ymax).
   * @param bounds
   */
  public void setBoundaries(AABB bounds) {
    this.setBoundaries(
        bounds.lowerBound.x, bounds.upperBound.x,
        bounds.lowerBound.y, bounds.upperBound.y);
  }
  
  /**
   * Get the current game score. Scoring may be implementation-specific.
   * @return
   */
  public int getScore() {
    return this.score;
  }
  
  public void addScore(int ds) {
    this.score += ds;
  }
  
  public void resetScore() {
    this.score = 0;
  }
  
  /**
   * Get the physics game world being controlled.
   * @return
   */
  public IceWorld getGameWorld() {
    return this.gameWorld;
  }
  
  
  public GameRenderer getRenderer() {
    return this.renderer;
  }
  
  
  public AudioController getAudio() {
    return this.audio;
  }
  
  public int getNontrivialIceSheetCount() {
    int chunks = 0;
    for (Body b : this.getGameWorld().getIceSheetBodies()) {
      IceSheet sheet = (IceSheet) b.getUserData();
      if (sheet.computeArea() > sheet.getMaxArea()) {
        ++chunks;
      }
    }
    return chunks;
  }
  
  /**
   * Get pieces flying through the air but not yet dropped into the physics
   * world.
   * @return
   */
  public List<FlyingPieceOfFood> getPiecesOfFoodInFlight() {
    return this.foodsInFlight;
  }
  
  
  public List<ParticleEffect> getParticles() {
    return this.particles;
  }
  
  /**
   * End the flight for a piece of food and put it into the world.
   * @param foodFlight
   */
  private void dropFlyingFoodIntoWorld(FlyingPieceOfFood foodFlight) {
    FoodPiece food = foodFlight.getPieceOfFood();
    Vec2 pos = foodFlight.getPos();
    float angle = foodFlight.getAngle();
    if (this.isInBounds(pos)) {
      Body foodBody = this.getGameWorld().addFoodPieceBodyAt(food, pos, angle);
      this.onFoodFallsIntoWorld(food, pos);
    }
  }
  
  private void timeStepFlyingFood(float dt) {
    for (int i = this.foodsInFlight.size()-1; i >= 0; --i) {
      FlyingPieceOfFood foodFlight = this.foodsInFlight.get(i);
      if (foodFlight.isFinished()) {
        dropFlyingFoodIntoWorld(foodFlight);
        this.foodsInFlight.remove(i);
      } else {
        foodFlight.step(dt);
      }
    }
  }
  
  private void timeStepParticles(float dt) {
    for (int i = this.particles.size()-1; i >= 0; --i) {
      ParticleEffect part = this.particles.get(i);
      if (part.isFinished()) {
        this.particles.remove(i);
      } else {
        part.step(dt);
      }
    }
  }
  
  /**
   * Step the flying food, and step the physics world.
   * @param dt
   * @param velIters
   * @param posIters
   */
  private void timeStep(float dt, int velIters, int posIters) {
    timeStepFlyingFood(dt);
    timeStepParticles(dt);
    this.gameWorld.step(dt, velIters, posIters);
    this.audio.step();
  }
  
  public void step(float rate, int vItersPerSec, int posItersPerSec) {
    long now = System.currentTimeMillis();
    float dt = (now - this.lastStepTimeMillis) / 1000.0f;
    this.lastStepTimeMillis = now;
    int vIters = (int) (vItersPerSec * dt);
    int pIters = (int) (posItersPerSec * dt);
    
    this.renderer.getCamera().unshake();
    this.timeStep(rate * dt, vIters, pIters);
  }
  
  /**
   * Get the end-of-the-game bonus points. These should be added to the score
   * exactly once.
   * @return
   */
  public int getGameEndBonus() {
    return GameController.POINTS_PER_ICE_SHEET * this.getNontrivialIceSheetCount();
  }
  
  /**
   * True iff (x,y) is within the boundaries.
   * @param x World x coordinate
   * @param y World y coordinate
   * @return
   */
  public boolean isInBounds(float x, float y) {
    return x >= this.xMinBound && x <= this.xMaxBound &&
        y >= this.yMinBound && y <= this.yMaxBound;
  }
  
  /**
   * Same as isInBound(pos.x, pos.y)
   * @param pos
   * @return
   */
  public boolean isInBounds(Vec2 pos) {
    return this.isInBounds(pos.x, pos.y);
  }
  
  /**
   * Throw food from the given initial position with the given initial velocity.
   * @param food
   * @param pos
   * @param vel
   */
  public void throwFood(FoodPiece food, Vec3 pos, Vec3 vel) {
    ThrownTrajectory trajectory = new ThrownTrajectory(pos, vel, FreeSpace.GRAVITY, FreeSpace.AIR_RESISTANCE);
    float timeOfFlight = trajectory.getTimeOfFlightApprox(GameController.MAX_TRAJECTORY_ERROR);
    TrajectoryTween tween = new TrajectoryTween(trajectory, 0, timeOfFlight);
    this.foodsInFlight.add(new FlyingPieceOfFood(food, tween));
    this.audio.playAudio(AudioController.Clip.WOOSH);
  }
  
  /**
   * Throw food from the given initial (x0, y0) position, with the given initial
   * speed and initial velocity direction given by xyAngle and zAngle. Throw
   * from height 0.
   * @param food
   * @param x0
   * @param y0
   * @param xyAngle
   * @param zAngle
   * @param speed
   */
  public void throwFood(FoodPiece food, float x0, float y0, float xyAngle, float zAngle, float speed) {
    Vec3 pos = new Vec3(x0, y0, 0);
    float xyProj = (float) (speed * Math.cos(zAngle));
    float vx = (float) (Math.cos(xyAngle) * xyProj);
    float vy = (float) (Math.sin(xyAngle) * xyProj);
    float vz = (float) (speed * Math.sin(zAngle));
    Vec3 vel = new Vec3(vx, vy, vz);
    this.throwFood(food, pos, vel);
  }
  
  /**
   * Same as throwFood, but create a BreadBall of the given size to throw.
   * @param size
   * @param x0
   * @param y0
   * @param xyAngle
   * @param zAngle
   * @param speed
   */
  public void throwBreadBall(float size, float x0, float y0, float xyAngle, float zAngle, float speed) {
    this.throwFood(new BreadBall(size), x0, y0, xyAngle, zAngle, speed);
  }
  
  
  /**
   * Callback called by the physics world whenever an Agent destroys an IceChunk.
   * @param pos
   */
  public void onAgentBreaksIce(Vec2 pos) {
    this.addScore(GameController.POINTS_PER_ICE_BREAK);
    this.audio.playAudio(AudioController.Clip.ICE_CRUNCH);
    
    Animation iceAnim = new StaticAnimation("ice_particle", 0);
    ParticleJet iceJet = new ParticleJet(iceAnim, pos);
    iceJet.setAngle(0, 6.28f);
    iceJet.setAngularVelocity(-3.14f, 3.14f);
    iceJet.setLifetime(0.1f, 0.3f);
    iceJet.setMagnitude(0.3f, 0.4f);
    iceJet.setSize(2f, 2f);
    for (int i = 0; i < 2; ++i) {
      this.particles.add(iceJet.generate());
    }
    //this.addParticleJet(iceAnim, pos, 1, 3, 1, 3, 2, 0, 3.14f, 0.2f, 0.4f);
  }
  
  
  public void onAgentActs(Agent agent) {
    if (agent instanceof Goose) {
      Goose goose = (Goose) agent;
      if (goose.isFlapping()) {
        Animation splashAnim = new StaticAnimation("water_particle", 0);
        ParticleJet waterJet = new ParticleJet(splashAnim, goose.getBody().getPosition());
        waterJet.setAngle(goose.getBody().getAngle() + 3.14f, 3.14f);
        waterJet.setLifetime(0.2f, 0.4f);
        waterJet.setMagnitude(1, 1.5f);
        waterJet.setRadius(0.5f, 1);
        waterJet.setSize(2, 3);
        this.particles.add(waterJet.generate());
        //this.addParticleJet(splashAnim, goose.getBody().getPosition(), 1f, 2f, 1f, 2f, 1, goose.getBody().getAngle() + 3.14f, 0.75f, 0.2f, 2);
      }
    }
  }
  
  /**
   * Callback called by the physics world whenever an Agent eats a FoodPiece.
   * @param agent
   * @param food
   */
  public void onAgentEatsFood(Agent agent, FoodPiece food) {
    MassData md = new MassData();
    food.getShape().computeMass(md, food.getDensity());
    this.addScore((int)(GameController.POINTS_PER_FOOD_MASS_UNIT * md.mass));
    if (RandomUtil.Uniform(0, 1) < 0.2) {
      this.audio.playAudio(AudioController.Clip.GOOSE_GROWL);
    } else {
      int clipID = RandomUtil.UniformInt(AudioController.Clip.GOOSE_HONK1.ordinal(), AudioController.Clip.GOOSE_HONK4.ordinal() + 1);
      this.audio.playAudio(AudioController.Clip.values()[clipID]);
    }
    
    Animation crumbAnim = new StaticAnimation("bread_particle", 0);
    Vec2 source = agent.getBody().getPosition();
    ParticleJet breadJet = new ParticleJet(crumbAnim, source);
    breadJet.setAngle(0, 6.28f);
    breadJet.setAngularVelocity(-6.28f, 6.28f);
    breadJet.setLifetime(0.1f, 0.5f);
    breadJet.setMagnitude(4, 5);
    breadJet.setRadius(0, 1);
    breadJet.setSize(0.1f, 2);
    for (int i = 0; i < 30; ++i) {
      this.particles.add(breadJet.generate());
    }
    //this.addParticleJet(crumbAnim, source, 0.2f, 1, 0.2f, 1, 10, 0, 3.14f, 0.2f, 5);
    //this.addParticleJet(crumbAnim, source, 0.2f, 1, 0.2f, 1, 10, 0, 3.14f, 0.4f, 4);
    //this.addParticleJet(crumbAnim, source, 0.2f, 1, 0.2f, 1, 10, 0, 3.14f, 0.6f, 4);
  }
  
  /**
   * Callback called by this controller whenever a FlyingPieceOfFood drops into
   * the physics world at the given world coordinates.
   * @param food
   * @param pos
   */
  public void onFoodFallsIntoWorld(FoodPiece food, Vec2 pos) {
    this.audio.playAudio(AudioController.Clip.PLUNK);
    
    Animation splashAnim = new StaticAnimation("water_particle", 0);
    ParticleJet waterJet = new ParticleJet(splashAnim, pos);
    waterJet.setLifetime(0.3f, 0.4f);
    waterJet.setMagnitude(2, 2.5f);
    waterJet.setRadius(0.2f, 0.5f);
    waterJet.setSize(2, 3);
    
    waterJet.setAngle(-0.57f, 1.54f);
    for (int i = 0; i < 2; ++i) {
      this.particles.add(waterJet.generate());
    }
    waterJet.setAngle(3.14f + 0.57f, 1.54f);
    for (int i = 0; i < 2; ++i) {
      this.particles.add(waterJet.generate());
    }
    //this.addParticleJet(splashAnim, pos, 1f, 2f, 1f, 2f, 2, 0, 1.2f, 0.35f, 2);
    //this.addParticleJet(splashAnim, pos, 1f, 2f, -2f, -1f, 2, 3.14f, 1.2f, 0.35f, 2);
  }
  
  /**
   * True iff the game has ended. Implementation specific.
   * @return
   */
  public abstract boolean hasGameEnded();
  
  /**
   * True iff the user has enough bread for a new BreadBall of the given size.
   * Implementation specific.
   * @param size
   * @return
   */
  public abstract boolean hasEnoughBreadFor(float size);

  public float getBreadSize() {
    return 1.5f;
  }
  
  
  public void renderGameElements() {
    this.renderer.renderGameElements(this);
  }
  
  
  public void renderUiElements() {
    this.renderer.renderUiElements(this);
    
    if (this.userMessage != null) {
      
      if (System.currentTimeMillis() > this.userMessageHideTimeMs) {
        this.hideUserMessage();
      }
    }
  }
  
  
  public void render() {
    this.renderGameElements();
    this.renderUiElements();
  }
  
  
  public void initFromLevelIO(LevelIO level) {
    IceWorldLoader worldLoader = new IceWorldLoader();
    worldLoader.loadFromProto(level, this.gameWorld);
    this.setBoundaries(level.getBounds());
  }
  

  public void showUserMessage(String msg, long timeMs) {
    this.userMessage = msg;
    this.userMessageHideTimeMs = System.currentTimeMillis() + timeMs;
  }
  
  public void hideUserMessage() {
    this.userMessage = null;
  }
  
  public String getUserMessageOrNull() {
    if (System.currentTimeMillis() > this.userMessageHideTimeMs) {
      this.hideUserMessage();
    }
    return this.userMessage;
  }
  
  public void setViewportSize(int w, int h) {
    AABB worldBounds = PhysicsUtil.getWorldShapesAABB(this.getGameWorld());
    System.out.println("x: " + worldBounds.lowerBound.x + " - " + worldBounds.upperBound.x);
    System.out.println("y: " + worldBounds.lowerBound.y + " - " + worldBounds.upperBound.y);
    this.renderer.getCamera().setViewportCenter(worldBounds.getCenter().x, worldBounds.getCenter().y);
    this.renderer.getCamera().setScreenDimensions(w, h);
    this.renderer.getCamera().setViewportScale(
        worldBounds.upperBound.x - worldBounds.lowerBound.x,
        worldBounds.upperBound.y - worldBounds.lowerBound.y);
    
    initSnowFlakes(100);
  }
  
  public void translateViewport(float dx, float dy) {
    float worldDx = this.renderer.getCamera().scaleFromViewport(dx);
    float worldDy = this.renderer.getCamera().scaleFromViewport(dy);
    this.renderer.getCamera().translateViewport(worldDx, worldDy);
  }
  
  public void scaleViewport(float scaleFactor) {
    this.renderer.getCamera().zoomViewport(scaleFactor);
  }
  
  public void fling(float viewportX, float viewportY, float viewportVelX, float viewportVelY) {
    float flingStartX = this.renderer.getCamera().transformFromViewportX(viewportX);
    float flingStartY = this.renderer.getCamera().transformFromViewportY(this.renderer.getCamera().getScreenHeight());
    float xyAngle = (float) Math.atan2(viewportVelY, viewportVelX);
    float zAngleDegrees = 40.0f;
    float zAngle = (float) (zAngleDegrees * Math.PI / 180);
    float breadSize = this.getBreadSize();
    float viewportVel = (float) Math.pow(viewportVelX * viewportVelX + viewportVelY * viewportVelY, 0.5) / 10;
    System.out.println("fling v: " + viewportVel);
    float maxDist = this.renderer.getCamera().scaleFromViewport(this.renderer.getCamera().getScreenHeight());
    float maxVel = (float) Math.sqrt(FreeSpace.GRAVITY * maxDist / Math.sin(2 * zAngle)) * 10/9;
    float throwVel = Math.min(this.renderer.getCamera().scaleFromViewport(viewportVel), maxVel);
    if (this.hasEnoughBreadFor(breadSize)) {
      this.throwBreadBall(breadSize, flingStartX, flingStartY, xyAngle, zAngle, throwVel);
    }
  }

  public List<Vec2> getSnowflakes() {
    return this.snowflakes;
  }
}
