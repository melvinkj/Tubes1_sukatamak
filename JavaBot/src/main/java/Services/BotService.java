package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

import javax.print.attribute.standard.PDLOverrideSupported;

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

    public World getWorld(){
        return gameState.world;
    }

/* -------------------------------- KODE NYA DIMULAI DARI SINI ----------------------------------------  */
    public void computeNextPlayerAction(PlayerAction playerActions) {
        
        if (!gameState.getGameObjects().isEmpty()) {
            var superNova = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVA_PICKUP)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

            var foodList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPER_FOOD)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

            var pList = gameState.getPlayerGameObjects()
                .stream().filter(item -> true)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
            
            System.out.print("Size array :");
            System.out.print(pList.size());
            System.out.println();
            
            var wormholeList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.WORMHOLE)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

            var asteroidList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.ASTEROID_FIELD)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
            
            var gasCloudList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.GAS_CLOUD)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
            
            var torpedoList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDO_SALVO)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
            
            // Kejar supernova selama masih available di map
            // if(superNovaDropped){
            //     playerAction.heading = this.getHeadingBetween(superNova.get(0));
            //     playerAction.action = PlayerActions.FORWARD;
            //     boolean afterBurnerOn = false;
            //     if(bot.getSize() > 40 && !afterBurnerOn){
            //         playerAction.action = PlayerActions.STARTAFTERBURNER;
            //         afterBurnerOn = true;
            //     }else{
            //         if(bot.getSize() <= 40){
            //             playerAction.action = PlayerActions.STOPAFTERBURNER;
            //             afterBurnerOn = false;
            //         }
            //     }
            // }
            // boolean outOfBound = false;

            // boolean safeFromTorpedo = true;
            // if(torpedoList.size() > 0){
            //     if(getDistanceBetween(bot, torpedoList.get(0)) < bot.getSize() + 70){
            //         playerAction.action = PlayerActions.ACTIVATESHIELD;
            //         safeFromTorpedo = false;
            //     }else{
            //         safeFromTorpedo = true;
            //     }
            // }else{
            //     safeFromTorpedo = true;
            // }

            // Makan dulu selama ukuran kurang dari 100
            if(bot.getSize() < 100){
                playerAction.action = PlayerActions.FORWARD;
                boolean safeToMove = true;
                
                // cari makanan paling dekat yang aman
                for(int i = 0; i < foodList.size(); i++){
                    
                    boolean safeFromPlayer, safeFromGasCloud;
                    safeFromPlayer = true;
                    safeFromGasCloud = true;
                    
                    for(int j = 0; j < pList.size(); j++){
                        if(pList.get(j).getSize() > bot.getSize()){
                            int degreeToOtherPlayer = this.getHeadingBetween(pList.get(j));
                            if(getDistanceBetween(bot,pList.get(j)) < (1 * bot.getSpeed()) + bot.getSize() + pList.get(j).getSize()){
                                playerAction.heading = 180 + degreeToOtherPlayer;
                                safeFromPlayer = false;
                                break;
                            }
                        }
                    }

                    for(int k = 0; k < gasCloudList.size(); k++){
                        if(getDistanceBetween(bot,gasCloudList.get(k)) < (1 * bot.getSpeed()) + bot.getSize()){
                            int degreeToGasCloud = this.getHeadingBetween(gasCloudList.get(k));
                            playerAction.heading = 180 + degreeToGasCloud;
                            safeFromGasCloud = false;
                            break;
                        }
                    }

                    safeToMove = safeFromPlayer && safeFromGasCloud;
                    if(safeToMove){
                        playerAction.heading = this.getHeadingBetween(foodList.get(i));
                        // if(i+1 < foodList.size()){
                        //     if(getDistanceBetween(bot,foodList.get(i)) == getDistanceBetween(bot,foodList.get(i+1))){
                        //         playerAction.heading = new Random().nextInt(360);
                        //     }
                        // }
                        break;
                    }else{
                        playerAction.heading = new Random().nextInt(360);
                    }

                }
            }else{
                playerAction.heading = this.getHeadingBetween(pList.get(1));
                playerAction.action = PlayerActions.FIRETORPEDOES;
        }
            
            }

        this.playerAction = playerAction; 
    }
}

