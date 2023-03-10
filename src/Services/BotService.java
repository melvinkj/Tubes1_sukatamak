package Services;

import java.lang.Math;
import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

import javax.lang.model.util.ElementScanner14;
import javax.swing.text.Position;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

    List<UUID[]> listOfKnownTeleporter = new ArrayList<>();
    double accuracy = 3;

    public void mappingTeleporter() {
        List<GameObject> teleporterList = gameState.getGameObjects()
            .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
            .sorted(Comparator
                    .comparing(item -> getDistanceBetween(bot, item)))
            .collect(Collectors.toList());
        
        for (int i = 0; i < teleporterList.size(); i++)
        {
            // Check apakah teleport tersebut sudah di map pada list;
            boolean listed = false;
            for (int j = 0; j < listOfKnownTeleporter.size(); j++)
            {
                if (listOfKnownTeleporter.get(j)[0].equals(teleporterList.get(i).getId()))
                {
                    listed = true;
                    break;
                }
                // jika belum di list maka di map
                else 
                {
                    continue;
                }
            }
            if (!listed)
            {   
                int idx = i;
                GameObject nearestPlayer = gameState.getPlayerGameObjects()
                    .stream()
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(item, teleporterList.get(idx))))
                    .collect(Collectors.toList()).get(0);
                listOfKnownTeleporter.add(new UUID[] {(teleporterList.get(i).getId()), (nearestPlayer.getId())});
            }
        }
    }

    public UUID identifyTeleporter(GameObject object){
        UUID teleport_id = object.getId();

        for (int j = 0; j < listOfKnownTeleporter.size(); j++)
        {
            if (listOfKnownTeleporter.get(j)[0].equals(teleport_id))
            {
                return listOfKnownTeleporter.get(j)[1];
            }
            else 
            {
                continue;
            }
        }
        return null;
    }

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

    /* 
     * Fungsi isSafeToHead(GameObject item) 
     * mengembalikan true jika hasil proyeksi bot dengan heading tersebut aman dari obtacles, 
     * mengembalikan false jika tidak,
     *
     * obtacles yang dijadikan konsiderasi: map boundaries, bigger player, supernova bomb, dan gas cloud
     */
    public boolean isSafeToHead(int heading) {
        return isSafeToHead(heading, 1);
    }
    public boolean isSafeToHead(int heading, double sensitivity) {
        heading = heading % 360; 
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

        int dummy_x = this.getBot().getPosition().x + (int) Math.cos(Math.toRadians(heading)) * this.getBot().getSpeed();
        int dummy_y = this.getBot().getPosition().y + (int) Math.sin(Math.toRadians(heading)) * this.getBot().getSpeed();
        int dummy_size = this.getBot().getSize();

        for (int i = 0; i < biggerPlayerList.size(); i++)
        {
            int dif_x = dummy_x - biggerPlayerList.get(i).getPosition().x;
            int dif_y = dummy_y - biggerPlayerList.get(i).getPosition().y;
            int safe_radius = dummy_size + biggerPlayerList.get(i).getSize() + biggerPlayerList.get(i).getSpeed();
            
            if (dif_x * dif_x + dif_y * dif_y <= safe_radius * safe_radius * sensitivity)
            {
                return false;
            }
        }

        for (int i = 0; i < gasCloudList.size(); i++)
        {
            int dif_x = dummy_x - gasCloudList.get(i).getPosition().x;
            int dif_y = dummy_y - gasCloudList.get(i).getPosition().y;
            int safe_radius = dummy_size + gasCloudList.get(i).getSize() + gasCloudList.get(i).getSpeed();
            
            if (dif_x * dif_x + dif_y * dif_y <= safe_radius * safe_radius * sensitivity)
            {
                return false;
            }
        }

        for (int i = 0; i < supernovaBombList.size(); i++)
        {
            int dif_x = dummy_x - supernovaBombList.get(i).getPosition().x;
            int dif_y = dummy_y - supernovaBombList.get(i).getPosition().y;
            int safe_radius = dummy_size + supernovaBombList.get(i).getSize() + supernovaBombList.get(i).getSpeed();
            
            if (dif_x * dif_x + dif_y * dif_y <= safe_radius * safe_radius * sensitivity)
            {
                return false;
            }
        }

        {
            int dif_x = dummy_x - mapCenterPoint_x;
            int dif_y = dummy_y - mapCenterPoint_y;
            int safe_radius = mapRadius - dummy_size;
            
            if (dif_x * dif_x + dif_y * dif_y > safe_radius * safe_radius / sensitivity)
            {
                return false;
            }
        }

        return true;

    }

    /* 
     * Fungsi isCoordinatSafe(GameObject item) 
     * mengembalikan true jika hasil proyeksi bot  aman dari obtacles, 
     * mengembalikan false jika tidak,
     *
     * obtacles yang dijadikan konsiderasi: map boundaries, bigger player, supernova bomb, dan gas cloud
     */

    public boolean isCoordinateSafe(int x, int y, double sensitivity) {
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

        int dummy_x = x;
        int dummy_y = y;
        int dummy_size = this.getBot().getSize();

        for (int i = 0; i < biggerPlayerList.size(); i++)
        {
            int dif_x = dummy_x - biggerPlayerList.get(i).getPosition().x;
            int dif_y = dummy_y - biggerPlayerList.get(i).getPosition().y;
            int safe_radius = dummy_size + biggerPlayerList.get(i).getSize() + biggerPlayerList.get(i).getSpeed();
            
            if (dif_x * dif_x + dif_y * dif_y <= safe_radius * safe_radius * sensitivity)
            {
                return false;
            }
        }

        for (int i = 0; i < gasCloudList.size(); i++)
        {
            int dif_x = dummy_x - gasCloudList.get(i).getPosition().x;
            int dif_y = dummy_y - gasCloudList.get(i).getPosition().y;
            int safe_radius = dummy_size + gasCloudList.get(i).getSize() + gasCloudList.get(i).getSpeed();
            
            if (dif_x * dif_x + dif_y * dif_y <= safe_radius * safe_radius * sensitivity)
            {
                return false;
            }
        }

        for (int i = 0; i < supernovaBombList.size(); i++)
        {
            int dif_x = dummy_x - supernovaBombList.get(i).getPosition().x;
            int dif_y = dummy_y - supernovaBombList.get(i).getPosition().y;
            int safe_radius = dummy_size + supernovaBombList.get(i).getSize() + supernovaBombList.get(i).getSpeed();
            
            if (dif_x * dif_x + dif_y * dif_y <= safe_radius * safe_radius * sensitivity)
            {
                return false;
            }
        }

        {
            int dif_x = dummy_x - mapCenterPoint_x;
            int dif_y = dummy_y - mapCenterPoint_y;
            int safe_radius = mapRadius - dummy_size;
            
            if (dif_x * dif_x + dif_y * dif_y > safe_radius * safe_radius / sensitivity)
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
     * Prosedur escape
     * kabur dari musuh terdekat jika harus
     */
    public boolean escape (PlayerAction playerAction) {
        var biggerPlayerList = gameState.getPlayerGameObjects()
        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER && item.getSize() > bot.getSize() )
        .sorted(Comparator
                .comparing(item -> getDistanceBetween(bot, item)))
        .collect(Collectors.toList());

        if (biggerPlayerList.size() != 0){
            System.out.println("Bigger Player");
            if (getDistanceBetween(biggerPlayerList.get(0), getBot())/60 <= biggerPlayerList.get(0).getSize()/(biggerPlayerList.get(0).getSpeed() * accuracy)) {
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

    /* 
     * Prosedur smaller target
     * menyerang target kecil terdekat yang berada pada lintasan
    */
    public boolean smallerTarget (PlayerAction playerAction) {
        var smallerPlayerList = gameState.getPlayerGameObjects()
        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER && item.getSize() < bot.getSize() )
        .sorted(Comparator
                .comparing(item -> getDistanceBetween(bot, item)))
        .collect(Collectors.toList());
        if (smallerPlayerList.size() != 0) {
            System.out.println("Smaller Player");
            if (getDistanceBetween(smallerPlayerList.get(0), getBot())/60 <= smallerPlayerList.get(0).getSize()/(smallerPlayerList.get(0).getSpeed() * accuracy)){
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

    /* 
     * prosedur getCentered
     * membawa bot ke tengah saat out of bound
     */
    public boolean getCentered(PlayerAction playerAction){
        return getCentered(playerAction, 1);
    }
    public boolean getCentered(PlayerAction playerAction, double sensitivity){
        return getCentered(playerAction, sensitivity, false);
    }
    public boolean getCentered(PlayerAction playerAction, double sensitivity, boolean pass){
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
            
        if ((dif_x * dif_x + dif_y * dif_y <= safe_radius * safe_radius / sensitivity) || pass)
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

    /*
     * prosedur tryTeleport
     * mencoba teleport jika menguntungkan (memakan player) dan aman
     */
    public boolean tryTeleport(PlayerAction playerAction){
        var teleporterList = gameState.getGameObjects()
            .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER && item.getId().equals(identifyTeleporter(item)))
            .collect(Collectors.toList());

        if (teleporterList.size() == 0)
        {
            return false;
        }
        
        var smallerPlayerList = this.gameState.getPlayerGameObjects()
            .stream()
            .filter(item -> !item.id.equals(bot.id) && item.getSize() < this.bot.getSize()) 
            .sorted(Comparator
                    .comparing(item -> getDistanceBetween(this.bot, item)))
            .collect(Collectors.toList());

        int x = teleporterList.get(0).getPosition().x;
        int y = teleporterList.get(0).getPosition().y;

        // Ada player yang dapat dimakan
        for (int i = 0; i < smallerPlayerList.size(); i++)
        {
            int target_x = (smallerPlayerList.get(i).getPosition().getX());
            int target_y = (smallerPlayerList.get(i).getPosition().getY());
            int radius = smallerPlayerList.get(i).getSize() + this.bot.getSize();
            if (
                (x - target_x) *  (x - target_x) + (y - target_y) + (y - target_y) < radius * radius
            )
            {
                playerAction.action = PlayerActions.TELEPORT;
                return true;
            }
            
        }
        // Tidak ada player yang dapat dimakan
        return false;
    }

    /*
     * Prosedur gotoCollectable()
     * menggerakan bot ke posisi optimum, jika posisi sekarang sudah optimum, bot akan berhenti
     */

    public boolean gotoCollectable(PlayerAction playerAction) {
        return gotoCollectable(playerAction, 1);
    }
    public boolean gotoCollectable(PlayerAction playerAction, double sensitivity) {
        if (gameState.getGameObjects().isEmpty())
        {
            System.out.println("unable to compute next move: empty game objects");
            this.playerAction.action = PlayerActions.STOP;
        }

        /* Collectable weigth */
        int superFoodWeight = 2;
        int foodWeight = 1;
        int supernovaPickupWeight = 2;

        /* LISTING FOOD DAN SUPER FOOD */
        /* SUPER FOOD DIBERI WEIGHT 2 */
        var collectableList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPERFOOD || item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP)
                .sorted(Comparator
                        .comparing((item -> {
                            if (item.getGameObjectType() == ObjectTypes.FOOD)
                            {
                                return getDistanceBetween(this.bot, item) * foodWeight;
                            }
                            else if (item.getGameObjectType() == ObjectTypes.SUPERFOOD)
                            {
                                return getDistanceBetween(this.bot, item) * superFoodWeight;
                            }
                            else
                            {
                                return getDistanceBetween(this.bot, item) * supernovaPickupWeight;
                            }
                        })))
                .collect(Collectors.toList());

        for (int i = 0; i < collectableList.size(); i++)
        {
            int heading = getHeadingBetween(collectableList.get(i));
            if (isSafeToHead(heading, sensitivity))
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
    /* 
     * prosedur avoidTeleporter()
     * menghindari teleport musuh jika itu milik yang lebih besar
     */
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
    private boolean isIn(int x, int y, int object_x, int object_y, int radius) {
        return ((x - object_x) * (x - object_x) + (y - object_y) * (y - object_y)) <= radius * radius;
    }

    /* 
     * Fungsi getSafeHeadingBetween 
     * mengembalikan heading yang aman menuju object (jika ada),
     * mengembalikan -1 jika tidak ada
    */
    private int getSafeHeadingBetween(GameObject object) {
        int initialHeading = getHeadingBetween(object);
        int deflect = 0;
        
        while (deflect < 180)
        {
            if (isSafeToHead(initialHeading + deflect))
            {
                return initialHeading + deflect;
            }
            else if (isSafeToHead(initialHeading - deflect))
            {
                return initialHeading - deflect;
            }
            else 
            {
                deflect++;
            }
        }
        return -1;
    }

    public boolean hunting(PlayerAction playerAction) {
        /* listing bigger player dan smaller player */
        var playerList = this.gameState.getPlayerGameObjects()
                .stream()
                .filter(item -> !item.id.equals(bot.id) && item.getSize() < this.bot.getSize()) 
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(this.bot, item)))
                .collect(Collectors.toList());
        for (int i = 0; i < playerList.size(); i++)
        {   
            var target = playerList.get(i);
            if (target.getSize() >= this.bot.getSize())
            {
                // jika dapat menembak, tembak
                if (this.bot.getTeleporterCount() > 0 &&
                    getDistanceBetween(this.bot, target)/20 < target.getSize()/(target.getSpeed() * accuracy))
                
                {
                    // CATATAN: mungkin bisa didiversifikasi
                    playerAction.heading = getHeadingBetween(target);
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                }
                // jika tidak bisa menembak, abaikan
                else
                {   
                    continue;
                }
            }
            else
            {   
                if (this.bot.getTeleporterCount() > 0 &&
                    getDistanceBetween(this.bot, target)/20 < target.getSize()/(target.getSpeed() * accuracy)) 
                {
                    playerAction.heading = getHeadingBetween(target);
                    playerAction.action = PlayerActions.FIRETELEPORT;
                }
                else if (this.bot.getTorpedoSalvoCount() > 0 &&
                    getDistanceBetween(this.bot, target)/60 < target.getSize()/(target.getSpeed() * accuracy)) 
                {
                    playerAction.heading = getHeadingBetween(target);
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                }
                else 
                {
                    playerAction.heading = getSafeHeadingBetween(target);
                    if (playerAction.heading != -1) {
                        playerAction.action = PlayerActions.FORWARD;
                    }
                    else 
                    {
                        playerAction.action = PlayerActions.STOP;
                        System.out.println("Parkir..");
                        return false;
                    }
                }
            }

            this.playerAction = playerAction;
            return true;
        }
        

        return true;
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
        mappingTeleporter();

        System.out.println("_______________________________");
        System.out.printf("Tick %d: \n", this.gameState.world.getCurrentTick());
        System.out.printf("x = %d, y = %d\n", this.bot.getPosition().x, this.bot.getPosition().y);

        isExecuted = tryTeleport(playerAction);
        if (isExecuted) 
        { 
            System.out.println("Action : teleporting..");
            return; 
        }

        isExecuted = torpedoDefense(playerAction);
        if (isExecuted) 
        { 
            System.out.println("Action : defensing against torpedo");
            return; 
        }

        isExecuted = avoidTeleporter(playerAction);
        if (isExecuted) 
        { 
            System.out.println("Action : avoiding teleporter");
            return; 
        }

        isExecuted = escape(playerAction);
        if (isExecuted) 
        { 
            System.out.println("Action : escape");
            return; 
        }

        isExecuted = smallerTarget(playerAction);
        if (isExecuted) 
        { 
            System.out.println("Action : shooting smaller target");
            return; 
        }

        isExecuted = getCentered(playerAction, 1.1);
        if (isExecuted) 
        { 
            System.out.println("Action : centering");
            return; 
        }

        isExecuted = gotoCollectable(playerAction, 1.1);
        if (isExecuted) 
        { 
            System.out.println("Action : going to collectable..");
            return; 
        }

        isExecuted = hunting(playerAction);
        if (isExecuted) 
        { 
            System.out.println("Action : hunting");
            return; 
        }
    
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);
        this.playerAction = playerAction;
        System.out.println("Action : tolong!");
        return;
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
