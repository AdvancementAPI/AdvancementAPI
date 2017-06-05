import io.chazza.advancementapi.AdvancementAPI;
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

        AdvancementAPI api = new AdvancementAPI(new NamespacedKey("test", "addiction"))
                .withTitle("Addiction!")
                .withDescription("Eat an Apple!")
                .withIcon("minecraft:golden_apple")
                .withTrigger("minecraft:consume_item")
                .withShouldBeHiddenBeforeArchieved(false)
                .withToast(false)
                .withBackground("minecraft:textures/gui/advancements/backgrounds/stone.png")
                .withItem(new ItemStack(Material.APPLE, 1)).withFrame(AdvancementAPI.FrameType.GOAL);
        api.save(worldName);


    }

}
