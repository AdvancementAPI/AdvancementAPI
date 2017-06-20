package io.chazza.advancementapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * Created by charliej on 14/05/2017.
 * Edited by GiansCode
 */

@Builder(builderMethodName = "hiddenBuilder")
public class AdvancementAPI {

    @Getter
    private NamespacedKey id;
    @Builder.Default
    @Getter
    private String parent, icon, background;
    @Getter
    private BaseComponent title, description;
    @Getter
    private FrameType frame;
    @Builder.Default
    @Getter
    private boolean announce = true, toast = true, hidden = true;

    @Builder.Default
    @Getter
    private int counter = 1;

    @Singular
    @Getter
    private Set<Trigger.TriggerBuilder> triggers;


    public static AdvancementAPIBuilder builder(NamespacedKey id) {
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
        display.put("frame", getFrame().toString());
        display.put("announce_to_chat", announce);
        display.put("show_toast", toast);
        display.put("hidden", hidden);

        json.put("parent", getParent());

        JSONObject criteria = new JSONObject();


        //Changed to normal comment as JavaDocs are not displayed here @PROgrm_JARvis
        /*
         * Define each criteria, for each criteria in list,
         * add items, trigger and conditions
         */

        if (getTriggers().isEmpty())
            triggers.add(Trigger.builder(Trigger.TriggerType.IMPOSSIBLE, "default"));

        for (Trigger.TriggerBuilder triggerBuilder : getTriggers()) {
            Trigger trigger = triggerBuilder.build();
            criteria.put(trigger.name, trigger.toJsonObject());
        }

        json.put("criteria", criteria);
        json.put("display", display);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(json);
    }

    public AdvancementAPI show(JavaPlugin plugin, Player... players) {
        add();
        grant(players);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            revoke(players);
            remove();
        }, 20L);
        return this;
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

    public AdvancementAPI grant(Player... players) {
        Advancement advancement = getAdvancement();
        for (Player player : players) {
            if (!player.getAdvancementProgress(advancement).isDone()) {
                Collection<String> remainingCriteria = player.getAdvancementProgress(advancement).getRemainingCriteria();
                Bukkit.getLogger().info(remainingCriteria.toString());
                for (String remainingCriterion : remainingCriteria)
                    player.getAdvancementProgress(getAdvancement())
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
                for (String awardedCriterion : awardedCriteria)
                    player.getAdvancementProgress(getAdvancement())
                            .revokeCriteria(awardedCriterion);
            }
        }
        return this;
    }

    @SuppressWarnings("deprecation")
    public AdvancementAPI remove() {
        Bukkit.getUnsafe().removeAdvancement(id);
        return this;
    }

    public Advancement getAdvancement() {
        return Bukkit.getAdvancement(id);
    }

    public boolean counterUp(Player player) {
        String criteriaString = null;
        for (String criteria : getAdvancement().getCriteria()) {
            if (player.getAdvancementProgress(getAdvancement()).getDateAwarded(criteria) != null) {
                criteriaString = criteria;
            } else {
                break;
            }
        }
        if (criteriaString == null) return false;
        player.getAdvancementProgress(getAdvancement()).awardCriteria(criteriaString);
        return true;
    }

    public boolean counterDown(Player player) {
        String criteriaString = null;
        for (String criteria : getAdvancement().getCriteria()) {
            if (player.getAdvancementProgress(getAdvancement()).getDateAwarded(criteria) != null) {
                criteriaString = criteria;
            } else {
                break;
            }
        }
        if (criteriaString == null) return false;
        player.getAdvancementProgress(getAdvancement()).revokeCriteria(criteriaString);
        return true;
    }

    public void counterReset(Player player) {
        for (String criteria : getAdvancement().getCriteria()) {
            if (player.getAdvancementProgress(getAdvancement()).getDateAwarded(criteria) != null) {
                player.getAdvancementProgress(getAdvancement()).revokeCriteria(criteria);
            }
        }
    }


    public enum FrameType {
        TASK("task"),
        GOAL("goal"),
        CHALLENGE("challenge");
        private String name;

        FrameType(String name) {
            this.name = name;
        }

        public static FrameType RANDOM() {
            FrameType[] frameTypes = FrameType.values();
            return frameTypes[(int) (Math.random() * (frameTypes.length - 1))];
        }


        public String toString() {
            return name;
        }
    }


    //BEGIN CLASSES
    @Builder(builderMethodName = "hiddenbuilder")
    public static class Condition {
        protected String name;
        protected JSONObject set;

        public static Condition.ConditionBuilder builder(String name, JSONObject itemStack) {
            return Condition.hiddenbuilder().name(name).set(itemStack);
        }

        public static Condition.ConditionBuilder builder(String name, ItemStack itemStack) {
            return Condition.hiddenbuilder().name(name).set(convertItemToJSON(itemStack));
        }


        //BEGIN UTIL
        private static JSONObject convertItemToJSON(ItemStack item) {
            JSONObject itemJSON = new JSONObject();
            itemJSON.put("item", "minecraft:" + item.getType().name().toLowerCase());
            itemJSON.put("amount", item.getAmount());
            itemJSON.put("data", item.getData().getData());
            return itemJSON;
        }
    }

    @Builder(builderMethodName = "hiddenbuilder")
    public static class Trigger {
        protected TriggerType type;
        protected String name;
        @Singular
        protected Set<Condition.ConditionBuilder> conditions;

        public static Trigger.TriggerBuilder builder(TriggerType type, String name) {
            return Trigger.hiddenbuilder().type(type).name(name);
        }

        public JSONObject toJsonObject() {

            JSONObject triggerObj = new JSONObject();

            final JSONObject advConditions = new JSONObject();
            // that doesn't fit in here
            triggerObj.put("trigger", "minecraft:" + this.type.toString().toLowerCase());
            this.conditions.forEach(conditionBuilder -> {

                Condition condition = conditionBuilder.build();
                advConditions.put(condition.name, condition.set);
            });
            if (!this.conditions.isEmpty())
                triggerObj.put("conditions", advConditions);


            return triggerObj;

        }


        public static enum TriggerType {
            ARBITRARY_PLAYER_TICK,
            BRED_ANIMALS,
            BREWED_POTION,
            CHANGED_DIMENSION,
            CONSTRUCT_BEACON,
            CONSUME_ITEM,
            CURED_ZOMBIE_VILLAGER,
            ENCHANTED_ITEM,
            ENTER_BLOCK,
            ENTITY_HURT_PLAYER,
            ENTITY_KILLED_PLAYER,
            IMPOSSIBLE,
            INVENTORY_CHANGED,
            ITEM_DURABILITY_CHANGED,
            LEVITATION,
            LOCATION,
            PLACED_BLOCK,
            PLAYER_HURT_ENTITY,
            PLAYER_KILLED_ENTITY,
            RECIPE_UNLOCKED,
            SLEPT_IN_BED,
            SUMMONED_ENTITY,
            TAME_ANIMAL,
            TICK,
            USED_ENDER_EYE,
            VILLAGER_TRADE
        }

    }


}
