package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

import javax.swing.text.PlainDocument;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
    }


    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    // tambah
    public World getWorld() {
        return gameState.world;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        // playerAction.action = PlayerActions.FORWARD;
        // playerAction.heading = new Random().nextInt(360);

        if (!gameState.getGameObjects().isEmpty()) {
            System.out.println("Tes");
            var torpedoList = gameState.getGameObjects() // PERLU PERBAIKIN TYPENYA GA
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
            var teleporterList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
            var foodList = gameState.getGameObjects()
                    // .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPER_FOOD)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            var biggerPlayerList = gameState.getPlayerGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER && item.getSize() > bot.getSize() )
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            var smallerPlayerList = gameState.getPlayerGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER && item.getSize() < bot.getSize() )
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            var gasCloudList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.GAS_CLOUD)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());


            boolean actionTaken = false;
            // 1. Mengecek apakah ada serangan torpedo
            if (torpedoList.size() == 0) { // kalau sudah tidak ada torpedo, afterburner dimatikan
                // playerAction.action = PlayerActions.STOPAFTERBURNER;
            } else {
                System.out.println("Torpedo");
                // Apabila ada torpedo dalam zona tembak dan masih ada shield
                if (getDistanceBetween(torpedoList.get(0), getBot()) <= getBot().getSize() + 40 && getBot().shieldCount >= 1 && (getBot().torpedoSalvoCount == 5)) {
                    playerAction.action = PlayerActions.ACTIVATESHIELD;
                    actionTaken = true;
                } else if (getDistanceBetween(torpedoList.get(0), getBot()) <= getBot().getSize() + 60 && getBot().shieldCount == 0) {
                    playerAction.heading = getHeadingBetween(torpedoList.get(0));
                    if (getBot().torpedoSalvoCount != 0 && getBot().getSize()>= 30 ) {
                        System.out.println("Torpedo 1");
                        System.out.println(playerAction.heading);
                        System.out.println(getBot().getSize());
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        actionTaken = true;
                    } else { // AFTER BURNER
                        // playerAction.heading = playerAction.heading + 90;
                        // playerAction.action = PlayerActions.STARTAFTERBURNER;
                        // actionTaken = true;
                    } // BISA JUGA HANDLE BUAT TELEPORT
                }
            }
            //2. Mengecek apakah ada teleporter di radius
            if (teleporterList.size() != 0){
                System.out.println("Teleporter");
                if (!actionTaken && getDistanceBetween(teleporterList.get(0), getBot()) > getBot().getSize() + 45){
                    playerAction.action = PlayerActions.TELEPORT;
                    actionTaken = true;
                }
            }

            // 3. mengecek apakah bigger player terdekat berada di dalam zona tembak torpedo
            // Apabila dalam zona tembak torpedo
            if (biggerPlayerList.size() != 0){
                System.out.println("Bigger Player");
                if (!actionTaken && getDistanceBetween(biggerPlayerList.get(0), getBot()) <= getBot().getSize() + biggerPlayerList.get(0).getSize() + 120 ) {
                    if (getBot().torpedoSalvoCount != 0 && getBot().getSize() >= 30) {
                        playerAction.heading = getHeadingBetween(biggerPlayerList.get(0));
                        System.out.println("Torpedo 2");
                        System.out.println(playerAction.heading);
                        System.out.println(getBot().getSize());
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        actionTaken = true;
                    } else {
                        //kabur
                        playerAction.heading = getHeadingBetween(biggerPlayerList.get(0)) + 180;
                        System.out.println("Kabur bang");
                        System.out.println(playerAction.heading);
                        System.out.println(getBot().getSize());
                        if (getBot().effects != 1) {
                            playerAction.action = PlayerActions.STARTAFTERBURNER;
                        } else {
                            playerAction.action = PlayerActions.FORWARD;
                        }
                        actionTaken = true;
                    }
                } else {
                    // ga nyerang
                    if (getBot().effects == 1) { 
                        playerAction.action = PlayerActions.STOPAFTERBURNER;
                        actionTaken = true;
                    }
                }
            }

            // 4. Mengecek apakah smaller opponent terdekat berada di radius tembak
            if (smallerPlayerList.size() != 0) {
                System.out.println("Smaller Player");
                if (!actionTaken && getDistanceBetween(smallerPlayerList.get(0), getBot()) <= getBot().getSize() + smallerPlayerList.get(0).getSize() + 80){
                    if (getBot().torpedoSalvoCount != 0 && getBot().getSize() >= 30) {
                        playerAction.heading = getHeadingBetween(smallerPlayerList.get(0));
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        System.out.println("Torpedo 3");
                        System.out.println(playerAction.heading);
                        System.out.println(getBot().getSize());
                        actionTaken = true; 
                    }
                } else {
                    
                }
            }

            // 5. Berburu makanan
            if (!actionTaken) {
                int i = 0;
                System.out.print("Food List: ");
                System.out.println(foodList.size());
                boolean found = false;
                while(i < foodList.size() && found == false) {
                    // gas cloud consideration
                    int j;

                    for(j = 0; j < gasCloudList.size(); j++){
                        // iterate hingga akhir, apabila di tengah ketemu yg deket bahaya: break
                        // nilai j akan menjadi indikasi apakah sudah aman atau belum, kalau j nya = size, berarti aman
                        if (getDistanceBetween(gasCloudList.get(j), foodList.get(i)) <= gasCloudList.get(j).getSize() + getBot().getSize()) {
                            break;
                        }
                    }

                    if (j == gasCloudList.size()) {
                        System.out.print("Gas cloud List 1: ");
                        System.out.println(gasCloudList.size());
                        int k;
                        for(k = 0; k < biggerPlayerList.size(); k++) {
                            if (getDistanceBetween(biggerPlayerList.get(k), foodList.get(i)) <= biggerPlayerList.get(k).getSize() + getBot().getSize() + 10) {
                                break;
                            } 
                        }
                        System.out.print("bigger Player List 1: ");
                        System.out.println(biggerPlayerList.size());
                        if (k == biggerPlayerList.size()) {
                            found = true;
                            playerAction.heading = getHeadingBetween(foodList.get(i));
                            playerAction.action = PlayerActions.FORWARD;
                            actionTaken = true;

                        }
                    }
                    i++;
                }
            }
            System.out.println("Checkpoint: ");
            if (playerAction.action == PlayerActions.FORWARD) {
                GameObject center = gasCloudList.get(0) ;
                center.position = getWorld().getCenterPoint();
                if (getWorld().getRadius() <= getDistanceBetween(getBot(), center) + getBot().getSize() + 10) {
                    System.out.println("Checkpoint 1 ");
                    playerAction.heading = getHeadingBetween(center);
                } else {
                    // // safest projection
                    // System.out.println("Checkpoint 2 ");
                    // boolean safe = false ;
                    // while (!safe) {
                    //     double radians = Math.toRadians(playerAction.heading);
                    //     double sinValue = Math.sin(radians);
                    //     double cosValue = Math.cos(radians);
                    //     GameObject projection = getBot();
                    //     projection.position.setX((int) ((getBot().getSpeed() + getBot().getSize()) * cosValue) + getBot().getPosition().getX());
                    //     projection.position.setY((int) ((getBot().getSpeed() + getBot().getSize()) * sinValue) + getBot().getPosition().getY());
                     
                    //     // gas cloud consideration
                    //     int j;
                    //     for(j = 0 ; j < gasCloudList.size(); j++){
                    //         // iterate hingga akhir, apabila di tengah ketemu yg deket bahaya: break
                    //         // nilai j akan menjadi indikasi apakah sudah aman atau belum, kalau j nya = size, berarti aman
                    //         if (getDistanceBetween(gasCloudList.get(j), projection) <= gasCloudList.get(j).getSize() + getBot().getSize()) {
                    //             break;
                    //         }
                    //     }
                    //     System.out.println(j);
                    //     System.out.print("Gas cloud List 2: ");
                    //     System.out.println(gasCloudList.size());
                        
                    //     if (j == gasCloudList.size()) {
                    //         int k;
                            
                    //         for(k = 0; k < biggerPlayerList.size(); k++) {
                    //             if (getDistanceBetween(biggerPlayerList.get(k), projection) <= biggerPlayerList.get(k).getSize() + getBot().getSize() + 10) {
                    //                 playerAction.heading = getHeadingBetween(projection) + 1;
                    //                 System.out.println("Break");
                    //                 break;
                    //             } 
                    //         }
                    //         System.out.println(k);
                    //         System.out.print("bigger Player List 2: ");
                    //         System.out.println(biggerPlayerList.size());
                            
                    //         if (k == biggerPlayerList.size()) {
                    //             safe = true;
                    //             playerAction.heading = getHeadingBetween(projection);
                    //             playerAction.action = PlayerActions.FORWARD;
                    //         }
                    //     }
                    // }
                }
            }
            
        }
        System.out.println("Tes2");
        System.out.print("Action: ");
        System.out.println(playerAction.action);
        System.out.print("Heading: ");
        System.out.println(playerAction.heading);
          
        this.playerAction = playerAction;
    }


    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    private double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    private int getHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }


}
