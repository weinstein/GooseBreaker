package graphics;

import java.util.List;

import org.jbox2d.collision.AABB;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import physics.BreadBall;
import physics.FlyingPieceOfFood;
import physics.FoodPiece;
import tessellation.IceSheet;
import util.PhysicsUtil;
import util.RandomUtil;
import controller.GameController;

/**
 * The GameRenderer abstracts rendering game elements on a screen using a
 * camera controller.
 * @author Jack
 */
public abstract class GameRenderer {
    private CameraController camera;
    private KeyframeFactory<?> factory;
    
    public GameRenderer(KeyframeFactory<?> keyframeFactory) {
      this.camera = new CameraController();
      this.factory = keyframeFactory;
    }
    
    public GameRenderer(CameraController cam, KeyframeFactory<?> keyframeFactory) {
      this.camera = cam;
      this.factory = keyframeFactory;
    }
    
    
    public KeyframeFactory<?> getKeyframeFactory() {
      return this.factory;
    }
    
    
    public CameraController getCamera() {
      return this.camera;
    }
    
    /**
     * Set the current drawing color to be used for drawing text and polygons.
     * Color format is implementation-specific.
     * @param color
     */
    public abstract void setColor(int color);
    
    /**
     * Return the color to use for filling ice sheet polygons.
     * @return
     */
    public abstract int getIceSheetFillColor();
    
    /**
     * Return the color to use for outlining ice sheet polygons.
     * @return
     */
    public abstract int getIceSheetBorderColor();
    
    /**
     * Return the color to use for general text.
     * @return
     */
    public abstract int getTextColor();
    
    /**
     * Return a bread-y color.
     * @return
     */
    public abstract int getBreadColor();
    

    public abstract int getWaterColor();
    
    
    public abstract int getSkyColor();
    
    /**
     * Clear the current screen.
     */
    public abstract void clearScreen();
    
    /**
     * Draw the outline of a polygon defined by the list of vertices.
     * @param pts Vertices, in screen coordinates.
     */
    public abstract void drawPolygon(List<Vec2> pts);
    
    /**
     * Draw a filled polygon defined by the list of vertices.
     * @param pts Vertices, in screen coordinates.
     */
    public abstract void fillPolygon(List<Vec2> pts);
    
    /**
     * Draw a keyframe animation on the screen, after shifting, scaling, and
     * rotating.
     * @param x Upper-left corner x in screen coordinates.
     * @param y Upper-left corner y in screen coordinates.
     * @param w Image width in screen coordinates.
     * @param h Image height in screen coordiantes.
     * @param angle Angle to rotate the image in radians.
     * @param animation Keyframe animation to display.
     */
    public abstract void drawAnimationOnScreen(float x, float y, float w, float h, float angle, Animation animation);
    
    /**
     * Draw text right-aligned with the given lower-right corner screen
     * coordinates and font size.
     * @param text
     * @param x
     * @param y
     * @param size
     */
    public abstract void drawTextRightAligned(String text, float x, float y, float size);
    
    /**
     * Draw text left-aligned with the given lower-left corner screen
     * coordinates and font size.
     * @param text
     * @param x
     * @param y
     * @param size
     */
    public abstract void drawTextLeftAligned(String text, float x, float y, float size);
    
    /**
     * Render all of the game elements.
     * @param controller The controller controlling a game to render.
     */
    public void renderGameElements(GameController controller) {
      this.clearScreen();
      this.drawBackground();
      
      for (Body b : controller.getGameWorld().getIceSheetBodies()) {
        this.drawIceSheetBody(b);
      }
      
      for (ParticleEffect part : controller.getParticles()) {
        this.drawParticle(part);
      }
      for (Body b : controller.getGameWorld().getFoodPieceBodies()) {
        this.drawAnimatedBody(b);
      }
      for (Body b : controller.getGameWorld().getAgentBodies()) {
        AnimatedBody bodyAnim = (AnimatedBody) b.getUserData();
        bodyAnim.setBody(b);
        this.drawAnimatedBody(b);
      }
      for (FlyingPieceOfFood flyingFood : controller.getPiecesOfFoodInFlight()) {
        this.drawParticle(flyingFood);
      }
      
      this.drawSnowflakes(controller.getSnowflakes());
    }

    public void maybeDrawUserMessage(String msgOrNull) {
      if (msgOrNull != null) {
        float textSize = this.getCamera().getScreenHeight() / 24.0f;
        int margin = (int) (textSize / 2);
        this.drawMessage(msgOrNull, textSize, 4 * margin, margin);
      }
    }
    
    public void renderUiElements(GameController controller) {
      this.drawScore(
          controller.getScore(),
          controller.getNontrivialIceSheetCount());
      
      this.maybeDrawUserMessage(controller.getUserMessageOrNull());
      
    }
    
    /**
     * Draw a count of the supply of bread.
     * @param supply
     */
    public void drawFoodSupply(int supply, FoodPiece prototype) {
      float textSize = this.getCamera().getScreenHeight() / 16.0f;
      this.drawAnimationOnScreen(0, 0, textSize, textSize, 0, prototype);
      
      this.setColor(this.getBreadColor());
      this.drawTextLeftAligned(GameRenderer.getCounterString(supply, 2), textSize, textSize, textSize);
    }
    
    
    public void drawAnimatedBody(Body b) {
      this.drawAnimatedBody(b, (Animation) b.getUserData());
    }
    
    
    /**
     * Draw a keyframe animation for a Body.
     * @param b
     * @param animation
     */
    public void drawAnimatedBody(Body b, Animation animation) {
      AABB bodyAABB = PhysicsUtil.getBodyShapesAABB(b);
      float w = this.camera.scaleForViewport(bodyAABB.upperBound.x - bodyAABB.lowerBound.x);
      float h = this.camera.scaleForViewport(bodyAABB.upperBound.y - bodyAABB.lowerBound.y);
      Vec2 localPos = new Vec2(bodyAABB.lowerBound.x, bodyAABB.lowerBound.y);
      Vec2 worldPos = b.getWorldPoint(localPos);
      float screenX = this.camera.transformForViewportX(worldPos.x);
      float screenY = this.camera.transformForViewportY(worldPos.y);
      
      this.drawAnimationOnScreen(screenX, screenY, w, h, b.getAngle(), animation);
    }
    
    /**
     * Apply a transform to a list of points in-place
     * @param xf
     * @param pts
     */
    public static void transformPoints(Transform xf, List<Vec2> pts) {
      for (int i = 0; i < pts.size(); ++i) {
        pts.set(i, Transform.mul(xf, pts.get(i)));
      }
    }
    
    
    /**
     * Take a list of points from world coordinates to screen coordinates in-place.
     * @param pts
     */
    public void transformPointsForViewport(List<Vec2> pts) {
      for (int i = 0; i < pts.size(); ++i) {
        Vec2 pt = pts.get(i);
        float tx = this.camera.transformForViewportX(pt.x);
        float ty = this.camera.transformForViewportY(pt.y);
        pt.set(tx, ty);
      }
    }
    
    
    /**
     * Draw an IceSheet on the current canvas using the given border/fill colors.
     * @param b
     * @param borderColor
     * @param fillColor
     */
    public void drawIceSheetBody(Body b) {
      IceSheet sheet = (IceSheet) b.getUserData();
      List<Vec2> perimeter = sheet.getBareEdgePerimeterCopy();
      transformPoints(b.getTransform(), perimeter);
      transformPointsForViewport(perimeter);
      
      this.setColor(this.getIceSheetFillColor());
      fillPolygon(perimeter);
      this.setColor(this.getIceSheetBorderColor());
      drawPolygon(perimeter);
    }
    
    public void drawParticle(ParticleEffect particle) {
      float screenWidth = this.camera.scaleForViewport(particle.getWidth());
      float screenHeight = this.camera.scaleForViewport(particle.getHeight());
      Transform xf = new Transform();
      xf.set(particle.getPos(), particle.getAngle());
      Vec2 worldCorner = Transform.mul(xf, new Vec2(-particle.getWidth()/2, -particle.getHeight()/2));
      float screenX = this.camera.transformForViewportX(worldCorner.x);
      float screenY = this.camera.transformForViewportY(worldCorner.y);
      this.drawAnimationOnScreen(
          screenX, screenY, screenWidth, screenHeight,
          particle.getAngle(), particle.getAnimation());
    }
    
    public static String getCounterString(int num, int len) {
      StringBuilder builder = new StringBuilder(len);
      int numDigits = (num > 0 ? (int) (Math.log10(num) + 1) : 0);
      for (int i = 0; i < len - numDigits; ++i) {
        builder.append('0');
      }
      if (numDigits > 0) {
        builder.append(num);
      }
      return builder.toString();
    }
    
    /**
     * Draw the current game score, and the current number of pieces of ice.
     * @param score
     * @param numChunks
     */
    public void drawScore(int score, int numChunks) {
      this.setColor(this.getTextColor());
      
      float textSize = this.camera.getScreenHeight() / 16.0f;
      this.drawTextRightAligned(GameRenderer.getCounterString(score, 6), this.camera.getScreenWidth(), textSize, textSize);
      
      float chunksTxtSize = textSize / 2;
      this.drawTextRightAligned("Ice Pieces: " + GameRenderer.getCounterString(numChunks, 3), this.camera.getScreenWidth(), textSize + chunksTxtSize, chunksTxtSize);
    }

    public void drawTimer(int remainingSec) {
      float textSize = this.camera.getScreenHeight() / 16.0f;
      this.setColor(this.getTextColor());
      this.drawTextLeftAligned(GameRenderer.getCounterString(remainingSec, 3), 0, textSize * 2, textSize);
    }

    public abstract int getTextBackgroundColor();

    public abstract void drawRect(int x, int y, int w, int h);
    

    public void drawMessage(String msg, float size, int scrnMargin, int txtMargin) {
      this.setColor(this.getTextBackgroundColor());
      this.drawRect(scrnMargin, scrnMargin, (int) this.getCamera().getScreenWidth() - 2 * scrnMargin, (int) this.getCamera().getScreenHeight() - 2 * scrnMargin);
      this.setColor(this.getTextColor());
      this.drawTextLeftAligned(msg, scrnMargin + txtMargin, this.getCamera().getScreenHeight() / 2 + size / 2, size);
    }
    
    
    private void drawBackground() {
      this.setColor(this.getSkyColor());
      this.drawRect(0, 0, (int) this.getCamera().getScreenWidth(), (int) this.getCamera().getScreenHeight());
      
      Animation landscapeAnim = new StaticAnimation("landscape", 0);
      float w = this.getCamera().scaleForViewport(200.0f);
      float h = w;
      float lowerBoundY = this.getCamera().getPanBoundLowerY();
      float upperBoundY = this.getCamera().getPanBoundUpperY();
      float curY = this.getCamera().getWorldCenterY();
      float horizon = h * 1 / 2 * (upperBoundY - curY) / (upperBoundY - lowerBoundY) - h * 1 / 2;
      
      float levelCenterX = this.getCamera().getPanBoundUpperX()/2 + this.getCamera().getPanBoundLowerX()/2;
      float xStart = this.getCamera().transformForViewportX(levelCenterX);
      float xDist = this.getCamera().scaleForViewport(this.getCamera().getPanBoundUpperX() - levelCenterX) + this.getCamera().getScreenWidth()/2;
      for (float x = 0; x < xDist; x += w) {
        this.drawAnimationOnScreen(xStart + x, horizon + h, w, -h, 0, landscapeAnim);
      }
      for (float x = w; x - w < xDist; x += w) {
        this.drawAnimationOnScreen(xStart - x, horizon + h, w, -h, 0, landscapeAnim);
      }
      
      this.setColor(this.getWaterColor());
      this.drawRect(0, (int) horizon, (int) this.getCamera().getScreenWidth(), (int) (this.getCamera().getScreenHeight() - horizon + 1));
    }
    
    
    private void drawSnowflakes(List<Vec2> snowflakes) {
      float maxw = this.getCamera().getScreenWidth() / CameraController.MIN_ZOOM_FACTOR;
      float maxh = this.getCamera().getScreenHeight() / CameraController.MIN_ZOOM_FACTOR;
      float minx = this.getCamera().getPanBoundLowerX() - maxw/2;
      float miny = this.getCamera().getPanBoundLowerY() - maxh/2;
      float maxx = this.getCamera().getPanBoundUpperX() + maxw/2;
      float maxy = this.getCamera().getPanBoundUpperY() + maxh/2;
      
      Animation flakeAnim = new StaticAnimation("snowflake_particle", 0);
      for (Vec2 flake : snowflakes) {
        if (flake.x > maxx) {
          flake.x -= (maxx - minx);
        } else if (flake.x < minx) {
          flake.x += (maxx - minx);
        }
        if (flake.y > maxy) {
          flake.y -= (maxy - miny);
        }
        
        float x = this.getCamera().transformForViewportX(flake.x);
        float y = this.getCamera().transformForViewportY(flake.y);
        float size = 16 - 8 * (y / this.getCamera().getScreenHeight());
        this.drawAnimationOnScreen(x - size/2, y - size/2, size, size, 0, flakeAnim);
        
        flake.x += RandomUtil.Uniform(-0.1, 0.1);
        flake.y += RandomUtil.Uniform(0, 0.2);
      }
    }
}
