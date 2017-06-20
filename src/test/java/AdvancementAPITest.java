import io.chazza.advancementapi.AdvancementAPI;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

/**
 * Created by ysl3000
 */
public class AdvancementAPITest {


    private final String worldName = "world";

    @Before
    public void setUp() {
        World world = mock(World.class);


        Mockito.when(world.getWorldFolder()).thenReturn(new File(worldName));

        Server server = mock(Server.class);
        Mockito.when(server.getLogger()).thenReturn(Logger.getAnonymousLogger());
        Mockito.when(server.getWorld("world")).thenReturn(world);
        Mockito.when(server.getVersion()).thenReturn("AdvancementTestMocking");
        Mockito.when(server.getVersion()).thenReturn("AdvancementTestMocking");
        Bukkit.setServer(server);
    }


    @Test
    public void createAndSave() {

        AdvancementAPI advancementAPI = AdvancementAPI.builder(new NamespacedKey("test", "addiction"))
                .title(new TextComponent("Addiction!"))
                .description(new TextComponent("Eat an Apple"))
                .icon("minecraft:golden_apple")
                .trigger(AdvancementAPI.Trigger.builder(AdvancementAPI.Trigger.TriggerType.CONSUME_ITEM, "test").condition(AdvancementAPI.Condition.builder("potion", new ItemStack(Material.APPLE, 1))))
                .hidden(false)
                .toast(false)
                .background("minecraft:textures/gui/advancements/backgrounds/stone.png")
                .frame(AdvancementAPI.FrameType.GOAL)
                .build();

        System.out.println(advancementAPI.getJSON());
        advancementAPI.save(worldName);

    }

}
