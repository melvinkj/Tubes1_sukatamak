package Models;

import Enums.*;
import java.util.*;

public class GameObject {
  public UUID id;
  public Integer size;
  public Integer speed;
  public Integer currentHeading;
  public Position position;
  public ObjectTypes gameObjectType;
  public Integer effects;
  public Integer torpedoSalvoCount;
  public Integer supernovaAvailable;
  public Integer teleporterCount;
  public Integer shieldCount;

  public GameObject(UUID id, Integer size, Integer speed, Integer currentHeading, Position position, ObjectTypes gameObjectType, Integer effects, Integer torpedoSalvoCount, Integer supernovaAvailable, Integer teleporterCount, Integer shieldCount) {
    this.id = id;
    this.size = size;
    this.speed = speed;
    this.currentHeading = currentHeading;
    this.position = position;
    this.gameObjectType = gameObjectType;
    this.effects = effects;
    this.torpedoSalvoCount = torpedoSalvoCount;
    this.supernovaAvailable = supernovaAvailable;
    this.teleporterCount = teleporterCount;
    this.shieldCount = shieldCount;
  }
  public GameObject(UUID id, Integer size, Integer speed, Integer currentHeading, Position position, ObjectTypes gameObjectType) {
    this.id = id;
    this.size = size;
    this.speed = speed;
    this.currentHeading = currentHeading;
    this.position = position;
    this.gameObjectType = gameObjectType;
    this.effects = 0;
    this.torpedoSalvoCount = 0;
    this.supernovaAvailable = 0;
    this.teleporterCount = 0;
    this.shieldCount = 0;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getSpeed() {
    return speed;
  }

  public void setSpeed(int speed) {
    this.speed = speed;
  }

  public int getHeading() {
    return this.currentHeading;
  }

  public Integer getEffects() {
       return effects;
  }

  public Integer getTorpedoSalvoCount() {
      return torpedoSalvoCount;
  }

  public Integer getTeleporterCount() {
      return teleporterCount;
  }

  public Integer getShieldCount() {
      return shieldCount;
  }

  public Integer getSupernovaAvailable() {
      return supernovaAvailable;
  }

  public Position getPosition() {
    return position;
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  public ObjectTypes getGameObjectType() {
    return gameObjectType;
  }

  public void setGameObjectType(ObjectTypes gameObjectType) {
    this.gameObjectType = gameObjectType;
  }

  public int getHeadingBetween(GameObject otherObject) {
    var direction = (int) Math.toDegrees(Math.atan2(otherObject.getPosition().y - this.getPosition().y,
            otherObject.getPosition().x - this.getPosition().x));
    return (direction + 360) % 360;
  }

  public static GameObject FromStateList(UUID id, List<Integer> stateList)
  {
    Position position = new Position(stateList.get(4), stateList.get(5));


    int effects = 0;
    int torpedoSalvoCount = 0;
    int supernovaAvailable = 0;
    int teleporterCount = 0;
    int shieldCount = 0;
    
    if (stateList.size() == 11) {
      effects = stateList.get(6);
      torpedoSalvoCount = stateList.get(7);
      supernovaAvailable = stateList.get(8);
      teleporterCount = stateList.get(9);
      shieldCount = stateList.get(10);
      
    }
    return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position, ObjectTypes.valueOf(stateList.get(3)), effects, torpedoSalvoCount, supernovaAvailable, teleporterCount, shieldCount);
  }

/*
    Afterburner = 0,
    AsteroidField = 1,
    GasCloud = 2,
    Superfood = 3,
    Shield = 4
 */
  public int[] getArrEffects() {
    int arrEffect [] = new int[5];
    int temp = this.getEffects();
    for (int i = 4; i > 0; i--)
    {
      if (temp >= Math.pow(2, i)) 
      {
        arrEffect[i] = 1;
        temp -= Math.pow(2, i);
      }
      else
      {
        arrEffect[i] = 0;
      }
    }

    return arrEffect;
  }  
}