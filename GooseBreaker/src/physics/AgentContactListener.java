package physics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;

import tessellation.IceChunk;

/**
 * 
 * Collision callback that makes note of all collisions involving some Agent.
 * @author Jack
 *
 */
public class AgentContactListener implements ContactListener {

  public Set<Fixture> bumpedChunks;
  public Map<Body, Body> bumpedFood;
  
  
  public AgentContactListener() {
    bumpedChunks = new HashSet<Fixture>();
    bumpedFood = new HashMap<Body, Body>();
    
  }
  
  @Override
  public void beginContact(Contact contact) {
    Fixture a = contact.getFixtureA();
    Fixture b = contact.getFixtureB();
    Fixture agentFixture = null;
    Fixture nonAgentFixture = null;
    if (a.getBody().getUserData() instanceof Agent) {
      agentFixture = a;
      nonAgentFixture = b;
    } else if (b.getBody().getUserData() instanceof Agent) {
      agentFixture = b;
      nonAgentFixture = a;
    } else {
      return;
    }
    
    if (nonAgentFixture.getUserData() instanceof IceChunk) {
      bumpedChunks.add(nonAgentFixture);
    } else if (nonAgentFixture.getBody().getUserData() instanceof FoodPiece) {
      bumpedFood.put(nonAgentFixture.getBody(), agentFixture.getBody());
    }
  }

  public void clear() {
    bumpedChunks.clear();
    bumpedFood.clear();
  }
  

  @Override
  public void endContact(Contact contact) {
    // TODO Auto-generated method stub
    
  }


  @Override
  public void preSolve(Contact contact, Manifold oldManifold) {
    // TODO Auto-generated method stub

  }


  @Override
  public void postSolve(Contact contact, ContactImpulse impulse) {
    // TODO Auto-generated method stub

  }

}
