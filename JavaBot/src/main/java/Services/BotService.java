package Services;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import Enums.ObjectTypes;
import Enums.PlayerActions;
import Models.GameObject;
import Models.GameState;
import Models.PlayerAction;

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


    /* 
     * Fungsi isInside_NT(GameObject item) 
     * mengembalikan true jika bot berpotensi bersentuhan dengan item PADA TICK SELANJUTNYA
     * mengembalikan false jika bot tidak berpotensi bersentuhan dengan item  PADA TICK SELANJUTNYA
    */
    public boolean isInside_NT(GameObject item) {
        int safe_radius = this.getBot().getSize() + item.getSize() + item.getSpeed();
        return (getDistanceBetween(this.bot, item) < safe_radius);
    }
    public boolean isSafeToHead(int heading) {
        return isSafeToHead(heading, 1);
    }
    public boolean isSafeToHead(int heading, double sensitifity) {

        /* GETTING MAP BOUNDARIES */
        int mapCenterPoint_x = this.gameState.world.centerPoint.x;
        int mapCenterPoint_y = this.gameState.world.centerPoint.y;
        int mapRadius = this.gameState.world.radius;

        /* LISTING BIGGER PLAYER */
        var biggerPlayerList = gameState.getPlayerGameObjects()
        .stream().filter(item -> item.getSize() >= bot.getSize() && !item.id.equals(bot.id))
        .sorted(Comparator
                .comparing(item -> getDistanceBetween(bot, item)))
        .collect(Collectors.toList());

       /* LISTING SUPERNOVA BOMB */
       var supernovaBombList = gameState.getGameObjects()
       .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVABOMB)
       .sorted(Comparator
               .comparing(item -> getDistanceBetween(bot, item)))
       .collect(Collectors.toList());

        /* LISTING GASCLOUD  */
        var gasCloudList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.GASCLOUD)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        /* LISTING ASTEROID */
        var asteroidList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.ASTEROIDFIELD)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        int dummy_x = this.getBot().getPosition().x + (int) Math.cos(Math.toRadians(heading)) * this.getBot().getSpeed();
        int dummy_y = this.getBot().getPosition().y + (int) Math.sin(Math.toRadians(heading)) * this.getBot().getSpeed();
        int dummy_size = this.getBot().getSize();

        for (int i = 0; i < biggerPlayerList.size(); i++)
        {
            int dif_x = dummy_x - biggerPlayerList.get(i).getPosition().x;
            int dif_y = dummy_y - biggerPlayerList.get(i).getPosition().y;
            int safe_radius = dummy_size + biggerPlayerList.get(i).getSize() + biggerPlayerList.get(i).getSpeed();
            
            if (dif_x * dif_x + dif_y * dif_y <= safe_radius * safe_radius * sensitifity)
            {
                return false;
            }
        }

        for (int i = 0; i < gasCloudList.size(); i++)
        {
            int dif_x = dummy_x - gasCloudList.get(i).getPosition().x;
            int dif_y = dummy_y - gasCloudList.get(i).getPosition().y;
            int safe_radius = dummy_size + gasCloudList.get(i).getSize() + gasCloudList.get(i).getSpeed();
            
            if (dif_x * dif_x + dif_y * dif_y <= safe_radius * safe_radius * sensitifity)
            {
                return false;
            }
        }

        for (int i = 0; i < supernovaBombList.size(); i++)
        {
            int dif_x = dummy_x - supernovaBombList.get(i).getPosition().x;
            int dif_y = dummy_y - supernovaBombList.get(i).getPosition().y;
            int safe_radius = dummy_size + supernovaBombList.get(i).getSize() + supernovaBombList.get(i).getSpeed();
            
            if (dif_x * dif_x + dif_y * dif_y <= safe_radius * safe_radius * sensitifity)
            {
                return false;
            }
        }

        {
            int dif_x = dummy_x - mapCenterPoint_x;
            int dif_y = dummy_y - mapCenterPoint_y;
            int safe_radius = mapRadius - dummy_size;
            
            if (dif_x * dif_x + dif_y * dif_y > safe_radius * safe_radius / sensitifity)
            {
                return false;
            }
        }

        return true;

    }

    /* 
     * Prosedur torpedoDefense()
     * menyalakan shield jika ada torpedoSalvo yang berpotensi menabrak bot
    */
    public boolean torpedoDefense(PlayerAction playerAction) {
        // UBAH KOMPUTASI SUDUT
        if (this.bot.getShieldCount() == 0 || this.bot.getArrEffects()[4] == 1)
        {
            return false;
        }
        var torpedoList = gameState.getGameObjects()
            .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDOSALVO)
            .sorted(Comparator
                    .comparing(item -> getDistanceBetween(bot, item)))
            .collect(Collectors.toList());
        
        for (int i = 0; i < torpedoList.size(); i++){
            double hitDegree = Math.atan((this.bot.getSize() + 10) / getDistanceBetween(this.getBot(), torpedoList.get(i)) * 180 / Math.PI );

            if (isInside_NT(torpedoList.get(i)) && -45 <= torpedoList.get(i).getHeading() - torpedoList.get(i).getHeadingBetween(this.bot) && (torpedoList.get(i).getHeading() - torpedoList.get(i).getHeadingBetween(this.bot)) <= 45){
                if (this.bot.getShieldCount() == 1 && this.bot.getArrEffects()[4] == 0) { // menyalakan shield apabila tersedia dan belum menyala
                    playerAction.action = PlayerActions.ACTIVATESHIELD;
                    this.playerAction = playerAction;
                    return true;
                } else if (!(this.bot.getShieldCount() == 0 || this.bot.getArrEffects()[4] == 1)){ // menembaki torpedo yang datang
                    playerAction.heading = this.getBot().getHeadingBetween(torpedoList.get(i));
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                    return true;
                } else { 
                    // pasrah
                }
                
            } else if (-1*hitDegree <= torpedoList.get(i).getHeading() - torpedoList.get(i).getHeadingBetween(this.bot) && torpedoList.get(i).getHeading() - torpedoList.get(i).getHeadingBetween(this.bot) <= hitDegree) {
                // melarikan diri
                if (this.bot.getArrEffects()[0] == 0) { // menyalakan afterburner apabila belum menyala
                    playerAction.action = PlayerActions.STARTAFTERBURNER;
                    playerAction.heading = torpedoList.get(0).getHeading() + 90; // + 90 agar ke arah tangensial sebagai jalur melarikan diri tercepat
                    return true;
                } else {
                    playerAction.action = PlayerActions.FORWARD;
                    playerAction.heading = torpedoList.get(0).getHeading() + 90; // + 90 agar ke arah tangensial sebagai jalur melarikan diri tercepat
                    return true;
                }
            }
        }
        
        return false;
    }

    /*
     * Prosedur computeShooter()
     * menembak musuh jika masuk pada range tembakan
    //  */
    // public boolean computeShooter(PlayerAction playerAction) {
    //     return computeShooter(playerAction, 1);
    // }

    // public boolean computeShooter(PlayerAction playerAction, int accuracy) {
    //     if (this.bot.getSize() <= 5 || this.bot.getTorpedoSalvoCount() == 0)
    //     {
    //         return false;
    //     }


    //     var playerList = this.gameState.getPlayerGameObjects()
    //             .stream()
    //             .filter(item -> !item.id.equals(bot.id))
    //             .sorted(Comparator
    //                     .comparing(item -> getDistanceBetween(bot, item)))
    //             .collect(Collectors.toList());
        

    //     if (playerList.size() != 0)
    //     {  
    //         int pedospeed = 60; 

    //         for (int i = 0; i < playerList.size(); i++) 
    //         {
    //             GameObject target = playerList.get(i);
    //             double target_distance = getDistanceBetween((this.bot), target);
    //             if ((target_distance/pedospeed) < (target.getSize() / (target.getSpeed() * accuracy)))
    //             {
    //                 playerAction.heading = getHeadingBetween(target);
    //                 playerAction.action = PlayerActions.FIRETORPEDOES;
    //                 this.playerAction = playerAction;
    //                 return true;
    //             }
    //         }
    //     }

    //     else {
    //         System.out.println("computeShooter: playerList empty");
    //     }
    //     return false;

    // }
    public boolean escapse (PlayerAction playerAction) {
        var biggerPlayerList = gameState.getPlayerGameObjects()
        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER && item.getSize() > bot.getSize() )
        .sorted(Comparator
                .comparing(item -> getDistanceBetween(bot, item)))
        .collect(Collectors.toList());

        if (biggerPlayerList.size() != 0){
            System.out.println("Bigger Player");
            if (getDistanceBetween(biggerPlayerList.get(0), getBot()) <= getBot().getSize() + biggerPlayerList.get(0).getSize() + 120 ) {
                if (getBot().torpedoSalvoCount != 0 && getBot().getSize() >= 30) {
                    playerAction.heading = getHeadingBetween(biggerPlayerList.get(0));
                    System.out.println("Torpedo 2");
                    System.out.println(playerAction.heading);
                    System.out.println(getBot().getSize());
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                    return true;
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
                    return true;
                }
            } else {
                // ga nyerang
                if (getBot().effects == 1) { 
                    playerAction.action = PlayerActions.STOPAFTERBURNER;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean smallerTarget (PlayerAction playerAction) {
        var smallerPlayerList = gameState.getPlayerGameObjects()
        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER && item.getSize() < bot.getSize() )
        .sorted(Comparator
                .comparing(item -> getDistanceBetween(bot, item)))
        .collect(Collectors.toList());
        if (smallerPlayerList.size() != 0) {
            System.out.println("Smaller Player");
            if (getDistanceBetween(smallerPlayerList.get(0), getBot()) <= getBot().getSize() + smallerPlayerList.get(0).getSize() + 80){
                if (getBot().torpedoSalvoCount != 0 && getBot().getSize() >= 30) {
                    playerAction.heading = getHeadingBetween(smallerPlayerList.get(0));
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                    System.out.println("Torpedo 3");
                    System.out.println(playerAction.heading);
                    System.out.println(getBot().getSize());
                    return true; 
                }
            } else {
                
            }
        }
        return false;
    }

    
    public void unhandled(PlayerAction playerAction) {
        getCentered(playerAction, 1, true);
    }

    public boolean getCentered(PlayerAction playerAction){
        return getCentered(playerAction, 1);
    }
    public boolean getCentered(PlayerAction playerAction, int sensitifity){
        return getCentered(playerAction, sensitifity, false);
    }
    public boolean getCentered(PlayerAction playerAction, int sensitifity, boolean pass){
        double dif_x;
        double dif_y;

        int x = bot.getPosition().x;
        int y = bot.getPosition().y;
        int mapCenterPoint_x = this.gameState.world.centerPoint.x;
        int mapCenterPoint_y = this.gameState.world.centerPoint.y;
        int mapRadius = this.gameState.world.radius;

        dif_x = x - mapCenterPoint_x;
        dif_y = y - mapCenterPoint_y;
        int safe_radius = mapRadius - this.bot.size;
            
        if ((dif_x * dif_x + dif_y * dif_y <= safe_radius * safe_radius / sensitifity) || pass)
        {
            return false;
        }
        var direction = toDegrees(Math.atan2(mapCenterPoint_y - y,
        mapCenterPoint_x - x));
        direction = (direction + 360) % 360;

        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = direction;

        return true;
    }
    // public boolean getCentered(PlayerAction playerAction, int sensitifity, boolean pass){
    //     double dif_x;
    //     double dif_y;
    //     double denominator;
    //     int x = bot.getPosition().x;
    //     int y = bot.getPosition().y;
    //     int mapCenterPoint_x = this.gameState.world.centerPoint.x;
    //     int mapCenterPoint_y = this.gameState.world.centerPoint.y;
    //     int mapRadius = this.gameState.world.radius;

    //     dif_x = x - mapCenterPoint_x;
    //     dif_y = y - mapCenterPoint_y;
    //     int safe_radius = mapRadius - this.bot.size;
            
    //     if ((dif_x * dif_x + dif_y * dif_y <= safe_radius * safe_radius / sensitifity) || pass)
    //     {
    //         return false;
    //     }

    //     if (!gameState.getGameObjects().isEmpty()) 
    //     {
    //         double fx = 0;
    //         double fy = 0;
    //         denominator = Math.pow((dif_x)*(dif_x) + (dif_y)*(dif_y), 3/2);
    //         fx -= (100 * dif_x) / denominator;
    //         fy -= (100 * dif_y) / denominator;

    //         List<GameObject> obtaclesList = gameState.getGameObjects()
    //             .stream()
    //             .filter(item -> 
    //                 item.getGameObjectType() != ObjectTypes.FOOD && 
    //                 item.getGameObjectType() != ObjectTypes.SUPERFOOD &&
    //                 item.getGameObjectType() != ObjectTypes.SUPERNOVABOMB &&
    //                 item.getGameObjectType() != ObjectTypes.TORPEDOSALVO )
    //             .sorted(Comparator
    //                     .comparing(item -> getDistanceBetween(bot, item)))
    //             .collect(Collectors.toList());
    //         obtaclesList.addAll(gameState.getPlayerGameObjects()
    //             .stream().filter(item -> !item.id.equals(bot.id) && item.getSize() >= bot.getSize())
    //             .collect(Collectors.toList()));

    //         for (int i = 0; i < obtaclesList.size(); i++){
    //                 int weight;
    //                 if (obtaclesList.get(i).getGameObjectType() == ObjectTypes.PLAYER)
    //                 {
    //                     weight = -3;
    //                 }
    //                 else if (obtaclesList.get(i).getGameObjectType() == ObjectTypes.GASCLOUD)
    //                 {
    //                     weight = -2;
    //                 }
    //                 else if (obtaclesList.get(i).getGameObjectType() == ObjectTypes.ASTEROIDFIELD)
    //                 {
    //                     weight = -1;
    //                 }
    //                 else if (obtaclesList.get(i).getGameObjectType() == ObjectTypes.TORPEDOSALVO)
    //                 {
    //                     weight = -2;
    //                 }
    //                 else if (obtaclesList.get(i).getGameObjectType() == ObjectTypes.SUPERNOVABOMB)
    //                 {
    //                     weight = -2;
    //                 }
    //                 else 
    //                 {
    //                     System.out.println("getCentered: object type not found");
    //                     weight = 0;
    //                 }

    //                 dif_x = x - obtaclesList.get(i).position.x;
    //                 dif_y = y - obtaclesList.get(i).position.y;
    //                 denominator = Math.pow((dif_x)*(dif_x) + (dif_y)*(dif_y), 3/2);
    //                 fx -= weight * (obtaclesList.get(i).size * dif_x) / denominator;
    //                 fy -= weight * (obtaclesList.get(i).size * dif_y) / denominator;
    //         }
    //         playerAction.action = PlayerActions.FORWARD;
    //         playerAction.heading = toDegrees(Math.atan2(fy,fx));
    //         this.playerAction = playerAction;
    //         return true;
    //     } 
    //     else 
    //     {
    //         System.out.println("getCentered: list kosong");
    //         playerAction.action = PlayerActions.FORWARD;
    //         playerAction.heading = new Random().nextInt(360);
    //         this.playerAction = playerAction;
    //         return false;
    //     }
    // }

    /*
     * Prosedur computeNextMove()
     * menggerakan bot ke posisi optimum, jika posisi sekarang sudah optimum, bot akan berhenti
     */
    public boolean computeNextMove(PlayerAction playerAction) {
        return computeNextMove(playerAction, 1);
    }
    public boolean computeNextMove(PlayerAction playerAction, int sensitifity) {
        if (gameState.getGameObjects().isEmpty())
        {
            System.out.println("unable to compute next move: empty game objects");
            this.playerAction.action = PlayerActions.STOP;
        }

        /* LISTING FOOD DAN SUPER FOOD */
        /* SUPER FOOD DIBERI WEIGHT 2 */
        var foodList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPERFOOD)
                .sorted(Comparator
                        .comparing((item -> getDistanceBetween(bot, item))))
                .collect(Collectors.toList());

        /* LISTING SMALLER PLAYER */
        var smallerPlayerList = gameState.getPlayerGameObjects()
                .stream().filter(item -> !item.id.equals(bot.id) && item.getSize() < bot.getSize() )
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());


        /* LISTING SUPERNOVA PICKUP */
        var supernovaPickUpList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());



        for (int i = 0; i < foodList.size(); i++)
        {
            int heading = getHeadingBetween(foodList.get(i));
            if (isSafeToHead(heading, sensitifity))
            {
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = heading;
                this.playerAction = playerAction;
                return true;
            }
        }

        playerAction.action = PlayerActions.STOP;
        this.playerAction = playerAction;
        return false;
    
    }

    public boolean avoidTeleporter(PlayerAction playerAction){
        var teleporterList = gameState.getGameObjects()
        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
        .sorted(Comparator
                .comparing(item -> getDistanceBetween(bot, item)))
        .collect(Collectors.toList());

        if (teleporterList.size() == 0) {
            return false;
        }

        var nearTeleporterPlayerList = gameState.getGameObjects()
        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER && item != this.bot)
        .sorted(Comparator
                .comparing(item -> getDistanceBetween(teleporterList.get(0), item)))
        .collect(Collectors.toList());

        if (nearTeleporterPlayerList.size() == 0) {
            return false;
        }
        if (nearTeleporterPlayerList.get(0).getSize() <= this.bot.size) {
            return false;
        }  
        double hitDegree = Math.atan((this.bot.getSize() + 10) / getDistanceBetween(this.getBot(), teleporterList.get(0)) * 180 / Math.PI );
        if (-1*hitDegree <= teleporterList.get(0).getHeading() - teleporterList.get(0).getHeadingBetween(this.bot) && teleporterList.get(0).getHeading() - teleporterList.get(0).getHeadingBetween(this.bot) <= hitDegree) {
            // melarikan diri
            if (this.bot.getArrEffects()[0] == 0) { // menyalakan afterburner apabila belum menyala
                playerAction.action = PlayerActions.STARTAFTERBURNER;
                playerAction.heading = teleporterList.get(0).getHeading() + 90; // + 90 agar ke arah tangensial sebagai jalur melarikan diri tercepat
                return true;
            } else {
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = teleporterList.get(0).getHeading() + 90; // + 90 agar ke arah tangensial sebagai jalur melarikan diri tercepat
                return true;
            }
        }
        return false;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        updateSelfState();
        try {
            if (this.gameState.world.getCurrentTick() == 0)
            {
                return;
            }
        }
        catch(Exception e) {
            System.out.println("Null GAMESTATE");
            return;
        }

        boolean isExecuted;

        System.out.println("_______________________________");
        System.out.printf("Tick %d: \n", this.gameState.world.getCurrentTick());
        System.out.printf("x = %d, y = %d\n", this.bot.getPosition().x, this.bot.getPosition().y);

        isExecuted = getCentered(playerAction);
        if (isExecuted) 
        { 
            System.out.println("Action : CENTERING");
            return; 
        }

        isExecuted = computeShield(playerAction);
        if (isExecuted) 
        { 
            System.out.println("Action : SHIELD");
            return; 
        }

        isExecuted = computeShooter(playerAction, 4);
        if (isExecuted) 
        { 
            System.out.println("Action : SHOOT");
            return; 
        }

        isExecuted = computeNextMove(playerAction, 1);
        if (isExecuted) 
        { 
            System.out.println("Action : MOVE");
            return; 
        }
        unhandled(playerAction);
        System.out.println("Action : UNHANDLED");

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


// DUMP

        // /* LISTING SHIELDED PLAYER  */
        // var shieldList = gameState.getGameObjects()
        //         .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SHIELD)
        //         .sorted(Comparator
        //                 .comparing(item -> getDistanceBetween(bot, item)))
        //         .collect(Collectors.toList());

        // public int getBestAngleMove_calculus(){
        //     int angle;
        //     if (!gameState.getGameObjects().isEmpty()) 
        //     {
        //         int x = bot.getPosition().x;
        //         int y = bot.getPosition().y;
        //         List<GameObject> foodList = gameState.getGameObjects()
        //             .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
        //             .sorted(Comparator
        //                     .comparing(item -> getDistanceBetween(bot, item)))
        //             .collect(Collectors.toList());
        //         double fx = 0;
        //         double fy = 0;
        //         for (int i = 0; i < foodList.size(); i++){
        //                 double dif_x = x - foodList.get(i).position.x;
        //                 double dif_y = y - foodList.get(i).position.y;
        //                 double denominator = Math.pow((dif_x)*(dif_x) + (dif_y)*(dif_y), 2);
        //                 fx -= (foodList.get(i).size * dif_x) / denominator;
        //                 fy -= (foodList.get(i).size * dif_y) / denominator;
        //         }
        //         angle = toDegrees(Math.atan2(fy,fx));
    
        //         // ______DEBUG BLOCK_______
        //         System.out.println("___________________");
        //         System.out.print("Angle: ");
        //         System.out.println(angle);
        //         System.out.print("X: ");
        //         System.out.println(x);
        //         System.out.print("Y: ");
        //         System.out.println(y);
        //         System.out.print("fx: ");
        //         System.out.println(fx);
        //         System.out.print("fy: ");
        //         System.out.println(fy);
        //         // _________________________
        //     } 
        //     else 
        //     {
        //         System.out.println("DEBUG: no object");
        //         angle = new Random().nextInt(360);
        //     }
        //     return angle;
        // }

                // /* LISTING TELEPORTER */
        // var teleporterList = gameState.getGameObjects()
        //         .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
        //         .sorted(Comparator
        //                 .comparing(item -> getDistanceBetween(bot, item)))
        //         .collect(Collectors.toList());

        // /* LISTING WORMHOLE */
        // var wormholeList = gameState.getGameObjects()
        //         .stream().filter(item -> item.getGameObjectType() == ObjectTypes.WORMHOLE )
        //         .sorted(Comparator
        //                 .comparing(item -> getDistanceBetween(bot, item)))
        //         .collect(Collectors.toList());
        