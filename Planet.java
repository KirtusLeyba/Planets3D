package kleyba.planets.threaded;

/**
 * @author Kirtus Leyba
 */
public class Planet
{

  private double radius;
  private double x;
  private double y;
  private double z;
  private double mass;
  private double xVelocity;
  private double yVelocity;
  private double zVelocity;
  private double G;
  private double fidelity;
  private double timeConstant;
  private boolean isCalculating = false;
  private boolean isFinished = false;

  /**
   * Constructor of planet object
   * @param radius radius of planet
   * @param x location of planet
   * @param y location of planet
   * @param mass of planet
   */
  Planet(double radius, double x, double y, double z,
                double mass, double xVelocity, double yVelocity,
                double zVelocity, double G, double fidelity, double timeConstant)
  {
    this.radius = radius;
    this.x = x;
    this.y = y;
    this.z = z;
    this.mass = mass;
    this.xVelocity = xVelocity;
    this.yVelocity = yVelocity;
    this.zVelocity = zVelocity;
    this.G = G;
    this.fidelity = fidelity;
    this.timeConstant = timeConstant;
  }

  /**
   * Updates the position of the planet, based on the current velocity and timeConstant
   */
  void updatePosition()
  {
    x = x + xVelocity*timeConstant;
    y = y + yVelocity*timeConstant;
    z = z + zVelocity*timeConstant;
  }

  /**
   * Uses an array of planets to determine the velocity
   * @param planets an array of planets
   */
  void calcVelocity(Planet[] planets)
  {
    double xForce = 0;
    double yForce = 0;
    double zForce = 0;
    double xAccel = 0;
    double yAccel = 0;
    double zAccel = 0;
    double dist = 0;
    double theta = 0;
    double phi = 0;

    for(Planet p: planets)
    {

      dist = distToPlanet(p);
      theta = Math.atan2(p.getY() - y, p.getX() - x);
      phi = Math.acos((p.getZ() - z)/dist);


      if(!p.equals(this))
      {
        if(dist > radius && dist < fidelity)
        {
          xForce = xForce + Math.sin(phi) * Math.cos(theta) * (p.getMass() * G/((dist)*(dist)));
          yForce = yForce + Math.sin(phi) * Math.sin(theta) * (p.getMass() * G/((dist)*(dist)));
          zForce = zForce + Math.cos(phi) * (p.getMass() * G/((dist)*(dist)));
        }
      }
    }

    xAccel = xForce/mass;
    yAccel = yForce/mass;
    zAccel = zForce/mass;

    xVelocity = xVelocity + xAccel;
    yVelocity = yVelocity + yAccel;
    zVelocity = zVelocity + zAccel;

  }

  private double distToPlanet(Planet p)
  {
    return Math.sqrt((p.getX() - x)*(p.getX() - x) + (p.getY() - y)*(p.getY() - y) + (p.getZ() - z)*(p.getZ() - z));
  }

  synchronized boolean shouldWork()
  {
    return (!isCalculating) && (!isFinished);
  }


  /**
   * Setters and Getters below
   */
  public double getxVelocity()
  {
    return xVelocity;
  }

  public void setxVelocity(double xVelocity)
  {
    this.xVelocity = xVelocity;
  }

  public double getyVelocity()
  {
    return yVelocity;
  }

  double getZ()
  {
    return z;
  }

  public void setZ(double z)
  {
    this.z = z;
  }

  public double getzVelocity()
  {
    return zVelocity;
  }

  public void setzVelocity(double zVelocity)
  {
    this.zVelocity = zVelocity;
  }

  public void setyVelocity(double yVelocity)
  {
    this.yVelocity = yVelocity;
  }

  public double getRadius()
  {
    return radius;
  }

  public void setRadius(double radius)
  {
    this.radius = radius;
  }

  double getX()
  {
    return x;
  }

  public void setX(double x)
  {
    this.x = x;
  }

  double getY()
  {
    return y;
  }

  public void setY(double y)
  {
    this.y = y;
  }

  private double getMass()
  {
    return mass;
  }

  public void setMass(double mass)
  {
    this.mass = mass;
  }

  public boolean getCalculating()
  {
    return isCalculating;
  }
  synchronized void setCalculating(boolean calc)
  {
    isCalculating = calc;
  }
  synchronized void setFinished(boolean f)
  {
    isFinished = f;
  }
}
