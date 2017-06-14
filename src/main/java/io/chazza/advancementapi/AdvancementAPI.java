package io.chazza.advancementapi;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created by charliej on 14/05/2017.
 * Edited by GiansCode
 */

@Builder(builderMethodName = "hiddenBuilder")
public class AdvancementAPI {

    @Getter
    private NamespacedKey id;
    @Getter
    private String parent, trigger, icon, background;
    @Getter
    private BaseComponent title, description;
    @Getter
    private FrameType frame;
    @Builder.Default
    @Getter
    private boolean announce = true, shouldShowToast = true, shouldBeHiddenBeforeArchieved = true;
    @Singular
    private List<ItemStack> items;


    public static AdvancementAPIBuilder builder(NamespacedKey id){
        return AdvancementAPI.hiddenBuilder().id(id);
    }


    public void save(String world) {
        this.save(Bukkit.getWorld(world));
    }

    public void save(World world) {


        File dir = new File(world.getWorldFolder(), "data" + File.separator + "advancements"
                + File.separator + id.getNamespace());

        if (dir.mkdirs()) {
            File file = new File(dir.getPath() + File.separator + id.getKey() + ".json");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(getJSON());
                Bukkit.getLogger().info("[AdvancementAPI] Created " + id.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public String getJSON() {
        JSONObject json = new JSONObject();


        JSONObject icon = new JSONObject();
        icon.put("item", getIcon());

        JSONObject display = new JSONObject();
        display.put("icon", icon);
        display.put("title", getTitle());
        display.put("description", getDescription());
        display.put("background", getBackground());
        display.put("frame", frame.toString());
        display.put("announce_to_chat", announce);
        display.put("show_toast", shouldShowToast);
        display.put("hidden", shouldBeHiddenBeforeArchieved);


        json.put("parent", parent);

        JSONObject criteria = new JSONObject();
        JSONObject conditions = new JSONObject();
        JSONObject elytra = new JSONObject();

        JSONArray itemArray = new JSONArray();
        JSONObject itemJSON = new JSONObject();

        for (ItemStack i : items) {
            itemJSON.put("item", "minecraft:" + i.getType().name().toLowerCase());
            itemJSON.put("amount", i.getAmount());
            itemArray.add(itemJSON);
        }

        /**
         * TODO
         * define each criteria, for each criteria in list,
         * add items, trigger and conditions
         */

        conditions.put("items", itemArray);
        elytra.put("trigger", trigger);
        elytra.put("conditions", conditions);

        criteria.put("elytra", elytra);

        json.put("criteria", criteria);
        json.put("display", display);


        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }


    @SuppressWarnings("deprecation")
    public AdvancementAPI add() {
        try {
            Bukkit.getUnsafe().loadAdvancement(id, getJSON());
            Bukkit.getLogger().info("Successfully registered advancement.");
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().info("Error registering advancement. It seems to already exist!");
        }
        return this;
    }

    @SuppressWarnings("deprecation")
    public AdvancementAPI remove() {
        Bukkit.getUnsafe().removeAdvancement(id);
        return this;
    }


    public AdvancementAPI show(JavaPlugin plugin,Player... players) {
        add();
        grant(players);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            revoke(players);
            remove();
        }, 20L);
        return this;
    }

    public AdvancementAPI grant(Player... players) {
        Advancement advancement = getAdvancement();
        for (Player player : players) {
            if (!player.getAdvancementProgress(advancement).isDone()) {
                Collection<String> remainingCriteria = player.getAdvancementProgress(advancement).getRemainingCriteria();
                Bukkit.getLogger().info(remainingCriteria.toString());
                for (String remainingCriterion : remainingCriteria) player.getAdvancementProgress(getAdvancement())
                        .awardCriteria(remainingCriterion);
            }
        }
        return this;
    }

    public AdvancementAPI revoke(Player... players) {
        Advancement advancement = getAdvancement();
        for (Player player : players) {
            if (player.getAdvancementProgress(advancement).isDone()) {
                Collection<String> awardedCriteria = player.getAdvancementProgress(advancement).getAwardedCriteria();
                Bukkit.getLogger().info(awardedCriteria.toString());
                for (String awardedCriterion : awardedCriteria) player.getAdvancementProgress(getAdvancement())
                        .revokeCriteria(awardedCriterion);
            }
        }
        return this;
    }

    public Advancement getAdvancement() {
        return Bukkit.getAdvancement(id);
    }



    public enum FrameType {
        TASK("task"),
        GOAL("goal"),
        CHALLENGE("challenge");
        private String name;

        FrameType(String name) {
            this.name = name;
        }

        public static FrameType RANDOM(){
            FrameType[] frameTypes = FrameType.values();
            return frameTypes[(int)(Math.random()*(frameTypes.length-1))];
        }


        public String toString() {
            return name;
        }
    }

}
