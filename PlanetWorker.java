package kleyba.planets.threaded;

/**
 * @author Kirtus Leyba
 * Class used for multiple threading of an N-body gravity simulation
 */
public class PlanetWorker extends Thread
{
  private Planet[] planets;
  private Planet currentPlanet;
  private boolean go = false;
  private boolean reachedEnd = false;
  private boolean end = false;
  private boolean moving = false;
  private boolean switched = false;

  /**
   * Planet worker constructor
   * @param planets planet array
   */
  PlanetWorker(Planet[] planets)
  {
    this.planets = planets;
  }

  /**
   * sets the end field for ending thread operation
   * @param e end boolean
   */
  void setEnd(boolean e)
  {
    this.end = e;
  }

  private void findFreePlanet(Planet[] planets)
  {
    for(Planet p: planets)
    {
      if(p.shouldWork())
      {
        this.currentPlanet = p;
        break;
      }
    }

    if(currentPlanet != null)
    {
      if(!currentPlanet.shouldWork())
      {
        currentPlanet = null;
        reachedEnd = true;
      }
    }
    else
    {
      reachedEnd = true;
    }
  }

  /**
   * sets the go field
   * @param go go boolean
   */
  void setGo(boolean go)
  {
    this.go = go;
  }

  /**
   * assigns planet array
   * @param planets planet array
   */
  void setPlanets(Planet[] planets)
  {
    this.planets = planets;
  }

  public boolean getReachedEnd()
  {
    return reachedEnd;
  }
  public void setReachedEnd(boolean rE)
  {
    this.reachedEnd = rE;
    go = true;
  }

  /**
   * Overriden run method for thread
   */
  @Override
  public void run()
  {
    while(!end)
    {
      if(go && !moving)
      {
        findFreePlanet(planets);
        if(currentPlanet!=null)
        {
          currentPlanet.setCalculating(true);
          currentPlanet.calcVelocity(planets);
          currentPlanet.setFinished(true);
          currentPlanet.setCalculating(false);
        }
        else
        {
          moving = true;
          resetPlanetWorking(planets);
        }
      }
      else if(go && moving)
      {
        findFreePlanet(planets);
        if(currentPlanet!=null)
        {
          currentPlanet.setCalculating(true);
          currentPlanet.updatePosition();
          currentPlanet.setFinished(true);
          currentPlanet.setCalculating(false);
        }
        else
        {
          moving = false;
          resetPlanetWorking(planets);
        }
      }
      else if(!moving)
      {
        try
        {
          sleep(20);
        }catch(InterruptedException e) {}
      }
    }
  }

  private void resetPlanetWorking(Planet[] planets)
  {
    for(Planet p: planets)
    {
      p.setFinished(false);
      p.setCalculating(false);
    }
  }


  /**
   * Setters and getters for Planet worker below
   */
  public Planet[] getPlanets()
  {
    return planets;
  }

  public boolean getMoving()
  {
    return moving;
  }

  public void setSwitched(boolean s)
  {
    switched = s;
  }


}
