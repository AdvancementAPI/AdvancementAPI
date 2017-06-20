package src;

import org.bukkit.Material;
import src.AdvancementAPI.*;
import src.AdvancementAPI;

/**
 * Created by ryan9 on 20/06/2017.
 */
public class AdvancementTest {
    //TODO JUnit maybe?
    public AdvancementTest(){
        AdvancementAPI api=new AdvancementAPI();
        String test;

        String expectedResult="";
       test= api.icon(Material.DIAMOND).addTrigger(new Trigger(TriggerType.BREWED_POTION, "test").
               addCondition(new Condition("potion", "minecraft:long_invisibility"))).background("minecraft:textures/blocks/gold_block.png")
               .description("Hello there. Make animals have sex and you get me").getJSON();
        //TODO Proper boolean comparison test without need of human
       boolean isCorrect=test.equals(expectedResult);
        System.out.println("Is this correct?");
        System.out.println(test);
        System.out.println("Expected: ");
        System.out.println(expectedResult);

    }
    public static void main(String[] args){
        new AdvancementTest();



    }
}


