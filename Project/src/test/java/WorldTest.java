import org.junit.jupiter.api.Test;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

    @Test
    void showMap() {
        Player player = new Player ();
        player.setName("Bilbo");
        player.reset();
        Monster monster = new Monster();
        monster.setName("Slime");
        monster.setDamage(1);
        monster.setMaxHealth(10);
        monster.reset();
        Map map = new Map();
        map.setDimensions();
        Item item = new Item();
        World world = new World(map,player,monster,item);
        Scanner scan = new Scanner(System.in);

        int newPlayerX = 2;
        int newPlayerY = 2;

        if (map.isTraversable(newPlayerX,newPlayerY)) {
            monster.chase(player.getPosX(),player.getPosY(),map);//monsters chase
            player.setPosX(newPlayerX); //update player's position if move is valid
            player.setPosY(newPlayerY);
        } else {
            //if the move is invalid, monster chase player from player current place
            monster.chase(player.getPosX(),player.getPosY(),map);
        }

        System.out.println("player x: "+player.getPosX());
        System.out.println("player y: "+player.getPosY());
        System.out.println("monster x: "+monster.getPosX());
        System.out.println("monster y: "+monster.getPosY());
        //showMap();
    }
}