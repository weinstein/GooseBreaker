package graphics;

import org.jbox2d.collision.AABB;

import util.RandomUtil;

/**
 * The camera controller handles transforming to/from screen coordinates, with
 * methods for panning, zooming, and shaking the camera.
 * @author Jack
 *
 */
public class CameraController {
  private float screenWidth;
  private float screenHeight;
  private float viewportCenterX;
  private float viewportCenterY;
  private float shakeX;
  private float shakeY;
  private float zoomFactor;
  private float lowerBoundX, lowerBoundY, upperBoundX, upperBoundY;
  public static final float MAX_ZOOM_FACTOR = 16.0f;
  public static final float MIN_ZOOM_FACTOR = 8.0f;
  
  public CameraController() {
    this.screenWidth = 0;
    this.screenHeight = 0;
    this.viewportCenterX = 0;
    this.viewportCenterY = 0;
    this.shakeX = 0;
    this.shakeY = 0;
    this.lowerBoundX = Float.MIN_VALUE;
    this.lowerBoundY = Float.MIN_VALUE;
    this.upperBoundX = Float.MAX_VALUE;
    this.upperBoundY = Float.MAX_VALUE;
    this.setZoomFactor(0);
  }
  
  public float getScreenWidth() {
    return this.screenWidth;
  }
  
  public float getScreenHeight() {
    return this.screenHeight;
  }
  
  public void setPanBoundaries(float lx, float ly, float ux, float uy) {
    this.lowerBoundX = lx;
    this.lowerBoundY = ly;
    this.upperBoundX = ux;
    this.upperBoundY = uy;
    this.setViewportCenter(this.viewportCenterX, this.viewportCenterY);
  }
  
  public float getPanBoundUpperX() {
    return this.upperBoundX;
  }
  
  public float getPanBoundLowerX() {
    return this.lowerBoundX;
  }
  
  public float getPanBoundUpperY() {
    return this.upperBoundY;
  }
  
  public float getPanBoundLowerY() {
    return this.lowerBoundY;
  }
  
  public void setScreenDimensions(float w, float h) {
    this.screenWidth = w;
    this.screenHeight = h;
  }
  
  /**
   * Zoom the viewport to fit w and h in the viewport (in world coordinates).
   * @param w
   * @param h
   */
  public void setViewportScale(float w, float h) {
    this.setZoomFactor(Math.min(this.screenWidth / w, this.screenHeight / h));
  }
  
  /**
   * Set the center of the camera in world coordinates.
   * @param worldX
   * @param worldY
   */
  public void setViewportCenter(float worldX, float worldY) {
    this.viewportCenterX = Math.min(Math.max(worldX, this.lowerBoundX), this.upperBoundX);
    this.viewportCenterY = Math.min(Math.max(worldY, this.lowerBoundY), this.upperBoundY);
  }
  
  /**
   * Pan the camera by dx, dy in world coordinates.
   * @param dx
   * @param dy
   */
  public void translateViewport(float dx, float dy) {
    this.setViewportCenter(this.viewportCenterX + dx, this.viewportCenterY + dy);
  }
  
  /**
   * Zoom in or out by the given zoom factor.
   * @param factor
   */
  public void zoomViewport(float factor) {
    this.setZoomFactor(this.zoomFactor * factor);
  }
  
  /**
   * Return a length scaled from world coords to screen coords.
   * @param len
   * @return
   */
  public float scaleForViewport(float len) {
    return len * this.zoomFactor;
  }
  
  /**
   * Return a length scaled from screen coords to world coords.
   * @param len
   * @return
   */
  public float scaleFromViewport(float len) {
    return len / this.zoomFactor;
  }
  
  /**
   * Helper method, modifies the zoom factor but ensures that
   * MIN_ZOOM_FACTOR <= zoom <= MAX_ZOOM_FACTOR
   * @param factor
   */
  private void setZoomFactor(float factor) {
    this.zoomFactor = Math.max(
        CameraController.MIN_ZOOM_FACTOR,
        Math.min(CameraController.MAX_ZOOM_FACTOR, factor));
  }
  
  /**
   * Take an x coordinate from screen coords to world coords.
   * @param x
   * @return
   */
  public float transformFromViewportX(float x) {
    return (x - this.screenWidth / 2.0f - this.shakeX) / this.zoomFactor + this.viewportCenterX;
  }
  
  /**
   * Take a y coordinate from screen coords to world coords.
   * @param y
   * @return
   */
  public float transformFromViewportY(float y) {
    return (y - this.screenHeight / 2.0f - this.shakeY) / this.zoomFactor + this.viewportCenterY;
  }
  
  /**
   * Take an x coordinate from world coords to screen coords.
   * @param x
   * @return
   */
  public float transformForViewportX(float x) {
    return (x - this.viewportCenterX) * this.zoomFactor + this.screenWidth / 2.0f + this.shakeX;
  }
  
  /**
   * Take a y coordinate from world coords to screen coords.
   * @param y
   * @return
   */
  public float transformForViewportY(float y) {
    return (y - this.viewportCenterY) * this.zoomFactor + this.screenHeight / 2.0f + this.shakeY;
  }
  
  /**
   * Shake the camera by a random amount of the given magnitude.
   * @param magnitude Maximum screen shake distance as a percentage of the
   * screen size.
   */
  public void shake(float magnitude) {
    this.shakeX = (float) RandomUtil.Uniform(-magnitude * this.screenWidth/2.0, magnitude * this.screenWidth/2.0);
    this.shakeY = (float) RandomUtil.Uniform(-magnitude * this.screenHeight/2.0, magnitude * this.screenHeight/2.0);
  }
  
  /**
   * Undo any shaking.
   */
  public void unshake() {
    this.shakeX = 0;
    this.shakeY = 0;
  }

  public void setPanBoundaries(AABB bounds) {
    this.setPanBoundaries(
        bounds.lowerBound.x,
        bounds.lowerBound.y,
        bounds.upperBound.x,
        bounds.upperBound.y);
  }

  public float getWorldCenterX() {
    return this.viewportCenterX;
  }
  
  public float getWorldCenterY() {
    return this.viewportCenterY;
  }
  
  public float getZoomFactor() {
    return this.zoomFactor;
  }
}
