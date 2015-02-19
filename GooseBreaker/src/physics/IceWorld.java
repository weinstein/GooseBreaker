package physics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.JointEdge;
import org.jbox2d.dynamics.joints.WeldJointDef;
import org.jbox2d.particle.ParticleDef;
import org.jbox2d.particle.ParticleGroupDef;

import tessellation.IceChunk;
import tessellation.IceSheet;
import controller.GameController;

public class IceWorld extends World {
  private AgentContactListener agentContactListener;
  private GameController controller;
  private Collection<Body> iceSheetBodies;
  private Collection<Body> agentBodies;
  private boolean hasAnyAgentActed;
  private Collection<Body> foodBodies;
  private Map<Body, Collection<Fixture>> bumpedChunks;
  private ArrayList<IceChunk> pendingDelete;
  private ArrayList<Fixture> fixturesPendingDelete;
  
  public IceWorld() {
    super(new Vec2());  // no gravity!
    agentContactListener = new AgentContactListener();
    this.setContactListener(agentContactListener);
    this.iceSheetBodies = new HashSet<Body>();
    this.agentBodies = new HashSet<Body>();
    this.foodBodies = new HashSet<Body>();
    this.bumpedChunks = new HashMap<Body, Collection<Fixture>>();
    this.pendingDelete = new ArrayList<IceChunk>();
    this.fixturesPendingDelete = new ArrayList<Fixture>();
    this.hasAnyAgentActed = false;
  }
  
  public void setController(GameController controller) {
    this.controller = controller;
  }
  
  /**
   * Add a Body for the IceSheet to this world, and also add the new Body to
   * the list of Bodies for IceSheets.
   * @param sheet
   * @return
   */
  public Body addIceSheet(IceSheet sheet) {
    Body b = sheet.addToWorldAsBody(this);
    b.setUserData(sheet);
    iceSheetBodies.add(b);
    return b;
  }
  
  
  /**
   * Get the Bodies representing IceSheets in the world.
   * @return
   */
  public Collection<Body> getIceSheetBodies() {
    return iceSheetBodies;
  }
  
  
  /**
   * Add a Body for the Agent to this world, and also add the new Body to the
   * list of Bodies for Agents.
   * @param agent
   * @return
   */
  public Body addAgent(Agent agent) {
    Body b = agent.addToWorldAsBody(this);
    b.setUserData(agent);
    agentBodies.add(b);
    return b;
  }
  
  
  /**
   * Get the Bodies representing Agents in the world.
   * @return
   */
  public Collection<Body> getAgentBodies() {
    return agentBodies;
  }
  
  /**
   * True iff any of the agents have acted during the last step.
   * @return
   */
  public boolean hasAnyAgentActed() {
    return this.hasAnyAgentActed;
  }
  
  /**
   * Add a Body for the FoodPiece to this world, and also add the new Body to
   * the list of Bodies for FoodPieces.
   * @param food
   * @return
   */
  public Body addFoodPieceBody(FoodPiece food) {
    Body b = food.attachToWorldAsBody(this);
    b.setUserData(food);
    foodBodies.add(b);
    return b;
  }
  
  
  /**
   * Helper method which checks if a food body is sitting on top of ice. If it
   * is, the food becomes welded to the ice.
   * This should be called once after a food piece is (re)added to the world.
   * @param foodBody
   */
  private void maybeWeldFoodToIce(Body foodBody) {
    Vec2 worldPos = foodBody.getPosition();
    for (Body iceBody : this.getIceSheetBodies()) {
      IceSheet sheet = (IceSheet) iceBody.getUserData();
      for (IceChunk chunk : sheet.getIceChunks()) {
        if (chunk.getTriangle().testPoint(iceBody.getTransform(), worldPos)) {
          WeldJointDef jd = new WeldJointDef();
          jd.bodyA = foodBody;
          jd.bodyB = iceBody;
          jd.referenceAngle = iceBody.getAngle() - foodBody.getAngle();
          jd.localAnchorA.set(0, 0);
          jd.localAnchorB.set(iceBody.getLocalPoint(worldPos));
          jd.collideConnected = false;
          this.createJoint(jd);
          return;
        }
      }
    }
  }
  
  
  /**
   * Same as addFoodPieceBody, but place the body at the given position and
   * weld it to the ice at that position (if any).
   * @param food
   * @param worldPos
   * @return
   */
  public Body addFoodPieceBodyAt(FoodPiece food, Vec2 worldPos, float angle) {
    Body b = addFoodPieceBody(food);
    b.setTransform(worldPos, angle);
    maybeWeldFoodToIce(b);
    return b;
  }

  /**
   * Get all the Bodies representing pieces of food in the world.
   * @return
   */
  public Collection<Body> getFoodPieceBodies() {
    return foodBodies;
  }
  
  
  /**
   * Helper method for processing the results of agentContactListener each step,
   * and destroying/splitting any chunks of ice that get bumped into.
   */
  private void processBumpedIceChunks() {
    // The gotcha here is that if we immediately re-add an ice sheet body, then
    // the fixtures we've yet to process will become invalid/destroyed.
    // Instead, for each bumped fixture, we'll handle one body at a time by
    // first splitting all the chunks that are too big, and then removing all
    // the chunks that are small enough all at once, and then updating the sheet
    // body/bodies.
    
    // First step: collect all the bumped chunks by their attached body.
    this.bumpedChunks.clear();
    for (Fixture chunkFixture : agentContactListener.bumpedChunks) {
      Body chunkBody = chunkFixture.getBody();
      
      Collection<Fixture> chunksForBody = bumpedChunks.get(chunkBody);
      if (chunksForBody == null) {
        chunksForBody = new ArrayList<Fixture>();
        bumpedChunks.put(chunkBody, chunksForBody);
      }
      chunksForBody.add(chunkFixture);
      // chunkBody is going to be destroyed one way or another.
      // Disable collisions so that when we re-add bodies, we don't hold
      // references to these bodies.
      chunkBody.setActive(false);
    }
    // We don't want to clear the list of chunks to process after calling
    // super.step(), since super.step() may call collision callbacks again for
    // the updated bodies and add to the list of chunks.
    agentContactListener.bumpedChunks.clear();
    
    // Step two: for each body, split everything we can, and collect all the
    // chunks we plan to delete.
    for (Entry<Body, Collection<Fixture>> e : bumpedChunks.entrySet()) {
      Body chunkBody = e.getKey();
      IceSheet sheet = (IceSheet) chunkBody.getUserData();
      this.pendingDelete.clear();
      this.fixturesPendingDelete.clear();
      for (Fixture chunkFixture : e.getValue()) {
        IceChunk chunk = (IceChunk) chunkFixture.getUserData();
        if (chunk.getTriangle().getArea() < sheet.getMaxArea()) {
          pendingDelete.add(chunk);
          fixturesPendingDelete.add(chunkFixture);
          if (this.controller != null) {
            this.controller.onAgentBreaksIce(chunkBody.getWorldPoint(chunk.getTriangle().m_centroid));
          }
        } else {
          this.updateIceSheetBodyWithBinaryDivision(chunkBody, chunkFixture);
        }
      }
      // Step two part two: if we want to delete chunks for the body, delete
      // them all at once and readd the body. Otherwise, just readd the body.
      if (pendingDelete.isEmpty()) {
        chunkBody.setActive(true);
        super.step(0, 0, 0);
      } else {
        ArrayList<IceChunk> prevPerimChunks = sheet.getBareIceChunksCopy();
        ArrayList<IceSheet> newSheets = sheet.removeIceChunks(pendingDelete);
        if (newSheets.isEmpty()) {
          this.destroyBody(chunkBody);
          this.iceSheetBodies.remove(chunkBody);
          super.step(0, 0, 0);
        } else if (newSheets.size() == 1 && newSheets.get(0) == sheet) {
          for (Fixture f : fixturesPendingDelete) {
            chunkBody.destroyFixture(f);
          }
          for (IceChunk newlyBare : sheet.getBareIceChunks()) {
            if (prevPerimChunks.contains(newlyBare)) {
              continue;
            }
            chunkBody.createFixture(newlyBare.getTriangle(), IceSheet.DENSITY).setUserData(newlyBare);
          }
          
          if (chunkBody.getFixtureList() == null) {
            this.destroyBody(chunkBody);
            this.iceSheetBodies.remove(chunkBody);
          } else {
            chunkBody.setActive(true);
          }
          super.step(0, 0, 0);
        } else {
          this.removeBodyToCreateNewSheets(chunkBody, newSheets);
        }
      }
    }
  }
  
  
  private void updateIceSheetBodyWithBinaryDivision(Body chunkBody,
      Fixture chunkFixture) {
    IceSheet sheet = (IceSheet) chunkBody.getUserData();
    IceChunk chunk = (IceChunk) chunkFixture.getUserData();
    ArrayList<IceChunk> newChunks = sheet.randomBinaryDivision(chunk);
    chunkBody.destroyFixture(chunkFixture);
    for (IceChunk newChunk : newChunks) {
      if (newChunk.hasBareEdge()) {
        chunkBody.createFixture(newChunk.getTriangle(), IceSheet.DENSITY).setUserData(newChunk);
      }
    }
    //super.step(0, 0, 0);
  }

  /**
   * Same as above, but removes pieces of food from the world when they are
   * bumped.
   */
  private void processBumpedFoodPieces() {
    for (Map.Entry<Body, Body> foodAgent : agentContactListener.bumpedFood.entrySet()) {
      Body foodBody = foodAgent.getKey();
      FoodPiece food = (FoodPiece) foodBody.getUserData();
      Body agentBody = foodAgent.getValue();
      Agent agent = (Agent) agentBody.getUserData();
      this.destroyBody(foodBody);
      this.foodBodies.remove(foodBody);
      super.step(0, 0, 0);
      
      if (this.controller != null) {
        this.controller.onAgentEatsFood(agent, food);
      }
    }
    agentContactListener.bumpedFood.clear();
  }
  
  
  private boolean stepAllAgents(float dt) {
    boolean hasActed = false;
    for (Body b : this.agentBodies) {
      Agent agent = (Agent) b.getUserData();
      if (agent.step(dt, b)) {
        hasActed = true;
        if (this.controller != null) {
          this.controller.onAgentActs(agent);
        }
      }
    }
    return hasActed;
  }
  
  
  /**
   * The world step pulverizes any IceChunk/FoodPiece which collided with an
   * Agent since the last call to step(), and calls step() for each Body
   * belonging to an Agent in addition to the normal physics solver super.step()
   */
  @Override
  public void step(float dt, int velocityIterations, int positionIterations) {
    super.step(dt, velocityIterations, positionIterations);
    
    processBumpedIceChunks();
    processBumpedFoodPieces();
    
    this.hasAnyAgentActed = stepAllAgents(dt);
  }
  


  /**
   * Handle transferring linear and angular velocity to split sheets properly.
   * Also creates new bodies for the new sheets, and destroys the old body.
   * Also handle re-welding pieces of food to new ice, if appropriate.
   * 
   * @param oldBody
   *          Old body which was split to create the new sheets.
   * @param newSheets
   *          The new sheets split off from the old body.
   */
  private void removeBodyToCreateNewSheets(Body oldBody, Collection<IceSheet> newSheets) {
    Vec2 v0 = oldBody.getLinearVelocity();
    float omega0 = oldBody.getAngularVelocity();
    Vec2 x0 = oldBody.getPosition();
    float theta0 = oldBody.getAngle();
    Vec2 centerOfMass = oldBody.getWorldCenter();
    
    Collection<Body> attachedFoodBodies = new LinkedList<Body>();
    for (JointEdge j = oldBody.getJointList(); j != null; j = j.next) {
      Body otherBody = j.other;
      if (otherBody.getUserData() instanceof FoodPiece) {
        attachedFoodBodies.add(otherBody);
      }
    }

    this.iceSheetBodies.remove(oldBody);
    this.destroyBody(oldBody);
    for (IceSheet newSheet : newSheets) {
      Body b = addIceSheet(newSheet);
      b.setTransform(x0, theta0);
      Vec2 newCenterOfMass = b.getWorldCenter();
      Vec2 r = newCenterOfMass.sub(centerOfMass);
      Vec2 linearVelocityFromAngular = new Vec2(-r.y * omega0, +r.x * omega0);
      b.setLinearVelocity(v0.add(linearVelocityFromAngular));
      b.setAngularVelocity(omega0);
    }
    
    for (Body foodBody : attachedFoodBodies) {
      this.maybeWeldFoodToIce(foodBody);
    }
    
    super.step(0, 0, 0);
  }


  /**
   * Given a world point, find a Fixture that contains the world point. If there
   * is more than one such fixture, return one of them arbitrarily. If there is
   * no such fixture, return null. Just a dumb linear search. testPoint() seems
   * fast enough that this doesn't really slow things down.
   * 
   * @param pt
   *          World point to query for a Fixture
   * @return A Fixture containing the point, or null if there are none that do.
   */
  public Fixture getFixtureAt(Vec2 pt) {
    for (Body b = this.getBodyList(); b != null; b = b.getNext()) {
      for (Fixture f = b.getFixtureList(); f != null; f = f.getNext()) {
        if (f.testPoint(pt)) {
          return f;
        }
      }
    }
    return null;
  }

  public Body addFoodPieceBodyAt(FoodPiece food, Vec2 pos) {
    return this.addFoodPieceBodyAt(food, pos, 0);
  }
  
  
  /**
   * Helper method for removing at the given position an IceChunk which is part
   * of a super chunk that hasn't been fully resolved yet.
   * @param f The super chunk's Fixture
   * @param pos Position in world coordinates.
   */
  /*
  private void resolveAndRemoveIceChunk(Fixture f, Vec2 pos) {
    Body b = f.getBody();
    IceSheet sheet = (IceSheet) b.getUserData();
    IceChunk superChunk = (IceChunk) f.getUserData();
    IceChunk removeChunk = sheet.resolveIceChunk(superChunk, b.getLocalPoint(pos));
    if (removeChunk.hasBareEdge()) {
      this.removeBodyToCreateNewSheets(b, sheet.removeIceChunk(removeChunk));
    } else {
      this.updateIceSheetBody(b);
    }
  }*/


  /**
   * Remove the first n ice chunk encountered on the segment from pt1 to pt2.
   * 
   * @param pt1
   * @param pt2
   */
  /*
  public void removeIceChunksThrough(Vec2 pt1, Vec2 pt2, int n) {
    GetFirstIceChunkFixtureCallback cb = new GetFirstIceChunkFixtureCallback();
    for (int i = 0; i < n; ++i) {
      this.raycast(cb, pt1, pt2);
      if (cb.getFraction() > 0 && cb.getFraction() < 1) {
        resolveAndRemoveIceChunk(cb.getResult(), cb.getPoint());
        cb.clear();
      } else {
        break;
      }
    }
  }*/
}
