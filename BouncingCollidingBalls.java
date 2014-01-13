package MyProject1;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
//**********************************************************************************
// Name of the file: BouncingCollidingBalls.java  
// Author: Patrice M. Ngassa
// Date: February 24,2013
// Instructor: Prof. Amitava Karmaker
// Class: CMSC325
// Project #2 
// Purpose: This class will give objects physical properties so they bounce inside   
// 	    4 cubes and collide while recording their positions.	      		    
//			
//********************************************************************************
 */

public class BouncingCollidingBalls extends SimpleApplication {

   public static BouncingCollidingBalls app = new BouncingCollidingBalls();
   
  public static void main(String args[])throws Exception {
        output = new java.io.PrintWriter(file);
        app.start();
  }

  //create a new file for storing position and velocity data
    public static java.io.File file = new java.io.File("BallsPositions.txt");
    public static java.io.PrintWriter output;
  /** Prepare the Physics Application State (jBullet) */
  private BulletAppState bulletAppState;

  /** Prepare Materials */
  Material wall_mat;
  Material stone_mat;
  Material floor_mat;
  Material mat_tt;
  
  // initiate the objects for generating random numbers
    private Random posNegGenerator = new Random();
    private Random gravGenerator = new Random();
    
  // intiate the variable for storing time
    long startTime;
    long startGravTime;

  /** Prepare geometries and physical nodes for cube and cannon balls. */
 
  //private RigidBodyControl    ball_phy;
  private static final Sphere sphere;
  private RigidBodyControl    floor_phy;
  private RigidBodyControl    roof_phy;
  private RigidBodyControl    wall_phy2;
  private RigidBodyControl    wall_phy3;
  private RigidBodyControl    wall_phy4;
  private RigidBodyControl    wall_phy5;
  private static final Box    floor;
  private static final Box    roof;
  private static final Box    wall2;
  private static final Box    wall3;
  private static final Box    wall4;
  private static final Box    wall5;
  final private int NUMBALLS = 16;
  private static RigidBodyControl[] ball_phy = new RigidBodyControl[16];
   Node shootables;

  static {
    /** Initialize the cannon ball (sphere) geometry */
    sphere = new Sphere(20, 20, 0.4f, true, false);
    sphere.setTextureMode(Sphere.TextureMode.Projected);
   
    /** Initialize the cube geometry by using walls */
    floor = new Box( Vector3f.ZERO, 3f, 0.1f, 3f);
    floor.scaleTextureCoordinates(new Vector2f(3, 6));
    
    roof = new Box( Vector3f.ZERO, 3f, 0.1f, 3f);
    roof.scaleTextureCoordinates(new Vector2f(3, 6));
    
    wall2 = new Box(Vector3f.ZERO, 3f, 3f, 0.1f);
    wall2.scaleTextureCoordinates(new Vector2f(3, 6));
    
    wall3 = new Box(Vector3f.ZERO, 3f, 3f, 0.1f);
    wall3.scaleTextureCoordinates(new Vector2f(3, 6));
    
    wall4 = new Box(Vector3f.ZERO, 0.1f, 3f, 3f);
    wall4.scaleTextureCoordinates(new Vector2f(3, 6));
    
    wall5 = new Box(Vector3f.ZERO, 0.1f, 3f, 3f);
    wall5.scaleTextureCoordinates(new Vector2f(3, 6));
    
  }
     

  @Override
  public void simpleInitApp() {
    /** Set up Physics Game */
    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);
    //bulletAppState.getPhysicsSpace().enableDebug(assetManager);
    /** Configure cam to look at scene */ 
    
    cam.setLocation(new Vector3f(4.5f, 20f, 4.5f));
    cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    
    shootables = new Node("Shootables");
    rootNode.attachChild(shootables);
     
    
    /** Initialize the scene, materials, inputs, and physics space */
    initInputs();
    initMaterials();
    
    // initialize the balls by giving them physical presence and make them bouncing
    for (int i = 0; i < NUMBALLS;i++){
        ball_phy[i] = new RigidBodyControl(1f); // physical presence
        initBalls(ball_phy[i],i);
        ball_phy[i].setRestitution(1.5f);// bouncing balls
    }
    
    updatePositionDisplay();
    
    // Get the cubes together
    rootNode.attachChild(initScene(0f,0f,0f));
    rootNode.attachChild(initScene(3f,0f,0f));
    rootNode.attachChild(initScene(6f,0f,0f));
    
    
    //initCrossHairs();
    
   
  }

  /** Add InputManager action: Left click triggers shooting. */
  private void initInputs() {
    inputManager.addMapping("Exit", 
            new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    inputManager.addListener(actionListener, "Exit");
  }

  /**
   * Every time the shoot action is triggered, a new cannon ball is produced.
   * The ball is set up to fly from the camera position in the camera direction.
   */
  private ActionListener actionListener = new ActionListener() {
    public void onAction(String name, boolean keyPressed, float tpf) {
      if (name.equals("Exit") && !keyPressed) {
                 
         //  Reset results list.
        CollisionResults results = new CollisionResults();
        //  Aim the ray from cam loc to cam direction.
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        //  Collect intersections between Ray and Shootables in results list.
        shootables.collideWith(ray, results);
        //  Print the results
        output.println("----- Collisions? " + results.size() + "-----");
        for (int i = 0; i < results.size(); i++) {
          // For each hit, we know distance, impact point.
          float dist = results.getCollision(i).getDistance();
          Vector3f pt = results.getCollision(i).getContactPoint();
          output.println("* Collision #" + i);
          output.println("  Contact Point at " + pt + ", at distance " + dist + " wu away.");
          output.close();
          app.stop();
          
        }
      }
    }
  };

  /** Initialize the materials used in this scene. */
  public void initMaterials() {
    wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    TextureKey key = new TextureKey("Textures/Terrain/BrickWall/BrickWall.jpg");
    key.setGenerateMips(true);
    Texture tex = assetManager.loadTexture(key);
    wall_mat.setTexture("ColorMap", tex);

    stone_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
    key2.setGenerateMips(true);
    Texture tex2 = assetManager.loadTexture(key2);
    stone_mat.setTexture("ColorMap", tex2);

    floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    TextureKey key3 = new TextureKey("Textures/Terrain/Pond/Pond.jpg");
    key3.setGenerateMips(true);
    Texture tex3 = assetManager.loadTexture(key3);
    tex3.setWrap(Texture.WrapMode.Repeat);
    floor_mat.setTexture("ColorMap", tex3);
    
    mat_tt = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    mat_tt.setColor("Color", new ColorRGBA(0, 0, 8, 0.5f));
    mat_tt.getAdditionalRenderState().setBlendMode(BlendMode.Alpha); // activate transparency
    
    
  }

  /** Make the bottom of the cube (solid floor) and add it to the scene. */
  public Node initScene(float x, float y, float z) {
    Geometry floor_geo = new Geometry("Floor", floor);
    floor_geo.setMaterial(floor_mat);
    floor_geo.setLocalTranslation(0f+x, -0.1f+y, 0f+z);
    shootables.attachChild(floor_geo);
    /* Make the floor physical with mass 0.0f! */
    floor_phy = new RigidBodyControl(0.0f);
    floor_geo.addControl(floor_phy);
    bulletAppState.getPhysicsSpace().add(floor_phy);
    floor_phy.setRestitution(0.5f);
    
    // The roof
    Geometry roof_geo = new Geometry("Roof", roof);
    roof_geo.setMaterial(mat_tt);
    roof_geo.setQueueBucket(Bucket.Transparent);
    roof_geo.setLocalTranslation(0f+x, 4.9f+y, 0f+z);
    
    shootables.attachChild(roof_geo);
    /* Make the roof physical with mass 0.0f! */
    roof_phy = new RigidBodyControl(0.0f);
    roof_geo.addControl(roof_phy);
    bulletAppState.getPhysicsSpace().add(roof_phy);
    roof_phy.setRestitution(0.5f);
    
    
    // first wall
    Geometry wall_geo2 = new Geometry("Wall2", wall2);
    wall_geo2.setMaterial(wall_mat);
    wall_geo2.setLocalTranslation(0.1f+x, 2f+y, -3f+z);
    shootables.attachChild(wall_geo2);
    /* Make the floor physical with mass 0.0f! */
    wall_phy2 = new RigidBodyControl(0.0f);
    wall_geo2.addControl(wall_phy2);
    bulletAppState.getPhysicsSpace().add(wall_phy2);
    wall_phy2.setRestitution(0.5f);
    
    // second wall
    Geometry wall_geo3 = new Geometry("Wall3", wall3);
    wall_geo3.setMaterial(wall_mat);
    wall_geo3.setLocalTranslation(0.1f+x, 2f+y, 3f+z);
    shootables.attachChild(wall_geo3);
    /* Make the floor physical with mass 0.0f! */
    wall_phy3 = new RigidBodyControl(0.0f);
    wall_geo3.addControl(wall_phy3);
    bulletAppState.getPhysicsSpace().add(wall_phy3);
    wall_phy3.setRestitution(0.5f);
    
    // third wall
    Geometry wall_geo4 = new Geometry("Wall4", wall4);
    wall_geo4.setMaterial(wall_mat);
    wall_geo4.setLocalTranslation(3f+x, 2f+y, 0f+z);
    shootables.attachChild(wall_geo4);
    /* Make the floor physical with mass 0.0f! */
    wall_phy4 = new RigidBodyControl(0.0f);
    wall_geo4.addControl(wall_phy4);
    bulletAppState.getPhysicsSpace().add(wall_phy4);
    wall_phy4.setRestitution(0.5f);
    
    // fourth wall
    Geometry wall_geo5 = new Geometry("Wall5", wall5);
    wall_geo5.setMaterial(wall_mat);
    wall_geo5.setLocalTranslation(-3f+x, 2f+y, 0f+z);
    shootables.attachChild(wall_geo5);
    /* Make the floor physical with mass 0.0f! */
    wall_phy5 = new RigidBodyControl(0.0f);
    wall_geo5.addControl(wall_phy5);
    bulletAppState.getPhysicsSpace().add(wall_phy5);
    wall_phy5.setRestitution(0.5f);
    
    return shootables;
    
  }

 
  /* Method to generate a random number*/
    static int getRandomInteger(int aStart, int aEnd, Random random){  
        if ( aStart > aEnd ) {  
          throw new IllegalArgumentException("Start cannot exceed End.");  
        }  
        //get the range, casting to long to avoid overflow problems  
        long range = (long)aEnd - (long)aStart + 1;  
        // compute a fraction of the range, 0 <= frac < range  
        long fraction = (long)(range * random.nextDouble());  
        int randomNumber =  (int)(fraction + aStart);  
        return randomNumber; 
    }

  
  /** A plus sign used as crosshairs to help the player with aiming.*/
  protected void initCrossHairs() {
    setDisplayStatView(false);
    //guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
    BitmapText ch = new BitmapText(guiFont, false);
    ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
    ch.setText("+");        // fake crosshairs :)
    ch.setLocalTranslation( // center
      settings.getWidth() / 2,
      settings.getHeight() / 2, 0);
    guiNode.attachChild(ch);
  }

        // This displays the positions of each ball separately on the GUI
        void BallTextDisplay(RigidBodyControl b, float xPos1, float yPos1, float zPos1,int i) {
            
            guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
             BitmapText ballText = new BitmapText(guiFont, false);
             ballText.setSize(guiFont.getCharSet().getRenderedSize());
             String xPos = String.format("%5.2f", b.getPhysicsLocation().getX());
             String yPos = String.format("%5.2f", b.getPhysicsLocation().getY());
             String zPos = String.format("%5.2f", b.getPhysicsLocation().getZ());
             ballText.setText("B" + i + ": (" + xPos + ", " + yPos + ", " + zPos + ")");
             ballText.setLocalTranslation(xPos1, yPos1, zPos1);
             guiNode.attachChild(ballText);
             
     } 
     // generates a random gravity for all of the balls
    //  or for each ball separately
    public Vector3f generateRandomGravity() {
        float gravX = gravGenerator.nextInt(3);
        int posNeg = posNegGenerator.nextInt(2);
        if (posNeg == 0){
            gravX *= -1;
        }
        float gravY = gravGenerator.nextInt(3);
        posNeg = posNegGenerator.nextInt(2);
        if (posNeg == 0){
                gravY *= -1;
        }
        float gravZ = gravGenerator.nextInt(3);
        posNeg = posNegGenerator.nextInt(2);
        if (posNeg == 0){
            gravZ *= -1;
        }
        return new Vector3f( gravX, gravY, gravZ);
    }
    // The update loop for changing the text display in real time
    //  and for calling the method for recording position, 
    //  velocity, and gravity data on the current playthrough to a text file
    @Override
    public void simpleUpdate(float tpf) {
        long estimatedTime = System.currentTimeMillis() - startTime;
        long estimatedGravTime = System.currentTimeMillis() - startGravTime;
        System.out.println(estimatedTime + ", " + startTime);
        int elapsedTime = 0;
        
        // if one second has elapsed in game
        if(estimatedTime > 1000) {
            // update the gui and record position and velocity data
            //  to the text file
            updatePositionDisplay();
            
            for (int i = 0; i < ball_phy.length; i++) {
                output.println(getBallPosString(String.format("%1d", i + 1 ), ball_phy[i]));
            }
            
            startTime = System.currentTimeMillis();
            
        }
        // otherwise, only update the gui
        else {
            updatePositionDisplay();
        }
        // if at least 5 seconds has elapsed from the last time, change
        // the gravity. If randGrav true, then each ball has its own
        // gravity. Otherwise, change the world gravity.
        if(estimatedGravTime > 5000) {
            // Add the text for tracking the balls' locations to the gui
            updatePositionDisplay();
             startGravTime = System.currentTimeMillis();
        }
        
    }
    
    // set the text on the gui display
    public void updatePositionDisplay() {        
        
        // Remove the default text display from the gui.
            guiNode.detachAllChildren();
            setDisplayStatView(false);
            setDisplayFps(false);
        for (int i = 0; i < NUMBALLS; i++) {
           BallTextDisplay(ball_phy[i],465, 485 - i*25, 0,i+1); 
        }
    }
    
    
    // print the position info of the balls on the screen (not yet supported)
    String getBallPosString(String a, RigidBodyControl b)
    {
       String xPos = String.format("%5.2f", b.getPhysicsLocation().getX());
        String yPos = String.format("%5.2f", b.getPhysicsLocation().getY());
        String zPos = String.format("%5.2f", b.getPhysicsLocation().getZ());
        
       return ("Ball "+ a +" is at ("+ xPos +","+ yPos +"," + zPos+")"); 
    }
    
    void initBalls(RigidBodyControl b, int i){
        /** Create a cannon ball geometry and attach to scene graph. */
          Geometry ball_geo = new Geometry("cannon ball", sphere);
          ball_geo.setMaterial(stone_mat);
          shootables.attachChild(ball_geo);
         
          /** Position the cannon ball  */
          if (i >= 0 && i <= 3){
            ball_geo.setLocalTranslation(new Vector3f(6f, 2f, 2f));
          }
          else if(i >= 4 && i <= 7){
            ball_geo.setLocalTranslation(new Vector3f(4f, 3f, 1f));
          }
          else if(i >= 8 && i <= 11){
            ball_geo.setLocalTranslation(new Vector3f(2f, 1f, 2f));
          }
          else {
            ball_geo.setLocalTranslation(new Vector3f(-2f, 1f, 1f));
          }
         
          /** Add physical ball to physics space. */
          ball_geo.addControl(b);
          bulletAppState.getPhysicsSpace().add(b);
          
          
          /*Generate a random number*/
         Random generator = new Random();  
         generator.setSeed(System.currentTimeMillis());  

          /** Accelerate the physcial ball to shoot it. */
          b.setLinearVelocity(Vector3f.ZERO.mult(getRandomInteger(20, 60, generator)));
          b.setGravity(generateRandomGravity());
    }
}

    
    