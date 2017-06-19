package kleyba.planets.threaded;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.Random;

public class Main extends Application
{

  private Group root = new Group();
  private BorderPane boarder = new BorderPane();
  private VBox buttonBox = new VBox();

  private Button pauseButton = new Button("Pause");
  private Button startButton = new Button("Start");
  private Button resetButton = new Button("Reset");

  private TextField numPlanetsField = new TextField("100");
  private Label numPlanetsLabel = new Label("N-bodies");
  private TextField gFactorField = new TextField("2");
  private Label gFactorLabel = new Label("G-factor");
  private TextField fidelityField = new TextField("400");
  private Label fidelityFieldLabel = new Label("Fidelity");
  private TextField velocityField = new TextField("1");
  private Label velocityFieldLabel = new Label("Vel-Range");
  private TextField massField = new TextField("4");
  private Label massFieldLabel = new Label("Mass-Range");
  private TextField timeField = new TextField("1");
  private Label timeFieldLabel = new Label("Time-const");

  private ToggleButton toggleRotateButton = new ToggleButton("Rotate");

  private PointLight centerLight = new PointLight();
  private PointLight centerTopLight = new PointLight();
  private PointLight centerBotLight = new PointLight();
  private AmbientLight aLight = new AmbientLight();
  private Group sphereGroup = new Group();
  private SubScene scene3D;
  private Camera camera = new PerspectiveCamera(true);
  private Planet[] planets;
  private Sphere[] spheres;
  private PhongMaterial planetMat = new PhongMaterial();

  private boolean go = false;
  private boolean reset = false;
  private boolean rotate = false;

  private double zoomLevel = -1000;
  private int numPlanets = 100;
  private double gFactor = 2;
  private double fidelity = 400;
  private double massRange = 4;
  private double velRange = 1;
  private double timeConst = 1;


  private PlanetWorker worker1;
  private PlanetWorker worker2;
  private PlanetWorker worker3;
  private PlanetWorker worker4;

  public static void main(String[] args)
  {
    launch(args);
  }

  /**
   * Override start method for JavaFX application
   * @param primaryStage
   */
  @Override
  public void start(Stage primaryStage)
  {
    //Setting up the display, gui, and visualization
    planetMat.setSpecularColor(Color.BEIGE);
    planetMat.setSpecularPower(2);
    planetMat.setDiffuseColor(Color.rgb(255,0,255));
    generatePlanets(numPlanets);

    camera.setFarClip(4000);
    camera.setTranslateZ(zoomLevel);

    sphereGroup.getChildren().addAll(spheres);

    centerLight.setColor(Color.WHITE);
    centerBotLight.setColor(Color.WHITE);
    centerBotLight.setTranslateZ(-20);
    centerTopLight.setColor(Color.WHITE);
    centerTopLight.setTranslateZ(20);
    aLight.setColor(Color.rgb(200,10,255,0.2));
    root.getChildren().addAll(sphereGroup, centerLight, centerTopLight, centerBotLight, aLight);

    scene3D = new SubScene(root, 800, 800, true, SceneAntialiasing.BALANCED );
    scene3D.setFill(Color.rgb(30,10,10));
    scene3D.setCamera(camera);

    ButtonListener bl = new ButtonListener();
    ZoomListener zl = new ZoomListener();

    scene3D.setOnScroll(zl);

    pauseButton.setPrefWidth(64);
    pauseButton.setOnAction(bl);
    pauseButton.setStyle("-fx-base: #83ada3");

    startButton.setPrefWidth(64);
    startButton.setOnAction(bl);
    startButton.setStyle("-fx-base: #83ada3");

    resetButton.setPrefWidth(64);
    resetButton.setOnAction(bl);
    resetButton.setStyle("-fx-base: #83ada3");

    toggleRotateButton.setPrefWidth(64);
    toggleRotateButton.setOnAction(bl);
    toggleRotateButton.setStyle("-fx-base: #83ada3");

    numPlanetsField.setPrefWidth(64);
    gFactorField.setPrefWidth(64);
    fidelityField.setPrefWidth(64);
    velocityField.setPrefWidth(64);
    massField.setPrefWidth(64);
    timeField.setPrefWidth(64);

    buttonBox.getChildren().addAll(pauseButton, startButton, resetButton,
            toggleRotateButton, numPlanetsLabel, numPlanetsField, gFactorLabel,
            gFactorField, fidelityFieldLabel, fidelityField, velocityFieldLabel, velocityField,
            massFieldLabel, massField, timeFieldLabel, timeField);

    boarder.setCenter(scene3D);
    scene3D.heightProperty().bind(boarder.heightProperty());
    scene3D.widthProperty().bind(boarder.widthProperty());
    boarder.setLeft(buttonBox);

    Scene scene = new Scene(boarder, 800 , 800, true);

    //Assigning spheres to initial planet position
    for (int i = 0; i < planets.length; i++)
    {
      spheres[i].setTranslateX(planets[i].getX());
      spheres[i].setTranslateY(planets[i].getY());
      spheres[i].setTranslateZ(planets[i].getZ());
    }

    primaryStage.setScene(scene);
    primaryStage.show();

    startWorkers(4);

    //Ensuring closed threads
    primaryStage.setOnCloseRequest(
            new EventHandler<WindowEvent>()
            {
              @Override
              public void handle(WindowEvent event)
              {

                while(worker1.isAlive() || worker2.isAlive() || worker3.isAlive() || worker4.isAlive())
                {
                  worker1.setGo(false);
                  worker1.setEnd(true);

                  worker2.setGo(false);
                  worker2.setEnd(true);

                  worker3.setGo(false);
                  worker3.setEnd(true);

                  worker4.setGo(false);
                  worker4.setEnd(true);

                  try
                  {
                    worker1.join();
                    worker2.join();
                    worker3.join();
                    worker4.join();
                  } catch (InterruptedException e)
                  {
                  }

                  try
                  {
                    Thread.sleep(50);
                  } catch (InterruptedException e) {}

                }
              }
            }
    );

    //Main gui loop
    AnimationTimer planetAnim = new AnimationTimer()
   {
     @Override
     public void handle(long now)
     {

       if(reset)
       {
         resetPlanets();
       }

       worker1.setGo(go);
       worker2.setGo(go);
       worker3.setGo(go);
       worker4.setGo(go);



       for (int i = 0; i < planets.length; i++)
       {
         spheres[i].setTranslateX(planets[i].getX());
         spheres[i].setTranslateY(planets[i].getY());
         spheres[i].setTranslateZ(planets[i].getZ());
       }

       if(rotate)
       {
         root.getTransforms().addAll(
                 new Rotate(0.2,0,0,0,new Point3D(1,1,0).normalize())
         );
       }

       camera.setTranslateZ(zoomLevel);
       try
       {
         numPlanets = Integer.parseInt(numPlanetsField.getText());
         gFactor = Double.parseDouble(gFactorField.getText());
         fidelity = Double.parseDouble(fidelityField.getText());
         massRange = Double.parseDouble(massField.getText());
         velRange = Double.parseDouble(velocityField.getText());
         timeConst = Double.parseDouble(timeField.getText());
       }
       catch (Exception e)
       {}

     }

   };

   planetAnim.start();

  }

  // Generates the planets for the planets array
  private void generatePlanets(int numPlanets)
  {
    planets = new Planet[numPlanets];
    spheres = new Sphere[numPlanets];
    Random rand = new Random();

    for(int i = 0; i < numPlanets; i++)
    {
      double rx = (rand.nextDouble()*640) - 320;
      double ry = (rand.nextDouble()*640) - 320;
      double rz = (rand.nextDouble()*100) - 50;
      double rm = (rand.nextDouble()*massRange);
      double rr = rm;
      double rxV = (rand.nextDouble()*velRange) - velRange/2;
      double ryV = (rand.nextDouble()*velRange) - velRange/2;
      double rzV = (rand.nextDouble()*velRange) - velRange/2;
      double G = gFactor;
      planets[i] = new Planet(rr, rx, ry, rz, rm, rxV, ryV, rzV, G, fidelity, timeConst);
      spheres[i] = new Sphere(rr);
      spheres[i].setMaterial(planetMat);
    }
  }

  // Resets the simulation
  private void resetPlanets()
  {
    worker1.setGo(false);
    worker2.setGo(false);
    worker3.setGo(false);
    worker4.setGo(false);
    sphereGroup.getChildren().removeAll(spheres);
    generatePlanets(numPlanets);
    worker1.setPlanets(planets);
    worker2.setPlanets(planets);
    worker1.setGo(true);
    worker2.setGo(true);
    worker3.setPlanets(planets);
    worker4.setPlanets(planets);
    worker3.setGo(true);
    worker4.setGo(true);
    for (int i = 0; i < planets.length; i++)
    {
      spheres[i].setTranslateX(planets[i].getX());
      spheres[i].setTranslateY(planets[i].getY());
      spheres[i].setTranslateZ(planets[i].getZ());
    }
    sphereGroup.getChildren().addAll(spheres);
    reset = false;
  }

  //Starts worker threads
  private void startWorkers(int numWorkers)
  {
    worker1 = new PlanetWorker(planets);
    worker2 = new PlanetWorker(planets);
    worker3 = new PlanetWorker(planets);
    worker4 = new PlanetWorker(planets);
    worker1.start();
    worker2.start();
    worker3.start();
    worker4.start();
  }

  //ends worker threads
  private void endWorkers()
  {
    worker1.setGo(false);
    worker1.setEnd(true);
    try
    {
      worker1.join();
    }catch (InterruptedException e) {}

    worker2.setGo(false);
    worker2.setEnd(true);
    try
    {
      worker2.join();
    }catch (InterruptedException e) {}

    worker3.setGo(false);
    worker3.setEnd(true);
    try
    {
      worker3.join();
    }catch (InterruptedException e) {}

    worker4.setGo(false);
    worker4.setEnd(true);
    try
    {
      worker4.join();
    }catch (InterruptedException e) {}
  }

  //Button Listener for all the gui buttons
  class ButtonListener implements EventHandler<ActionEvent>
  {

    @Override
    public void handle(ActionEvent event)
    {
      if(event.getSource() == pauseButton)
      {
        go = false;
      }
      if(event.getSource() == startButton)
      {
        go = true;
      }
      if(event.getSource() == resetButton)
      {
        reset = true;
      }
      if(event.getSource() == toggleRotateButton)
      {
        rotate = !rotate;
      }
    }
  }

  //Listener for zooming
  class ZoomListener implements EventHandler<ScrollEvent>
  {

    @Override
    public void handle(ScrollEvent event)
    {
      zoomLevel = zoomLevel + event.getDeltaY();
    }
  }

}
