package io.chazza.advancementapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * @author charliej - the very API
 * @author DiscowZombie - adopting for Builder-Pattern
 * @author 2008Choco - NamespacedKey support
 * @author GiansCode - small but useful changes
 * @author Ste3et_C0st - add/take advancement logic
 * @author PROgrammer_JARvis - rework and combining
 * @author ysl3000 - useful advice and bug-tracking at PullRequests/ JUnit-Tests, full Builder-Pattern support, Lombok
 */

@Builder(builderMethodName = "hiddenBuilder")
public class AdvancementAPI {

    private static final Gson gson = new Gson();

    @Getter
    private NamespacedKey id;
    @Builder.Default
    @Getter
    private String parent, icon, background;
    @Getter
    private TextComponent title, description;
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

    @Deprecated
    public void save(String world) {
        this.save(Bukkit.getWorld(world));
    }

    @Deprecated
    public void save(World world) {


        File file = new File(world.getWorldFolder(), "data" + File.separator + "advancements"
                + File.separator + id.getNamespace() + File.separator + id.getKey() + ".json");

        File dir = file.getParentFile();

        if (dir.mkdirs() || dir.exists()) {

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(getJSON());
                Bukkit.getLogger().info("[AdvancementAPI] Created " + id.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getJSON() {
        JsonObject json = new JsonObject();

        JsonObject icon = new JsonObject();
        icon.addProperty("item", getIcon());

        JsonObject display = new JsonObject();
        display.add("icon", icon);
        display.add("title", getJsonFromComponent(getTitle()));
        display.add("description", getJsonFromComponent(getDescription()));
        display.addProperty("background", getBackground());
        display.addProperty("frame", getFrame().toString());
        display.addProperty("announce_to_chat", announce);
        display.addProperty("show_toast", toast);
        display.addProperty("hidden", hidden);

        json.addProperty("parent", getParent());

        JsonObject criteria = new JsonObject();


        //Changed to normal comment as JavaDocs are not displayed here @PROgrm_JARvis
        /*
         * Define each criteria, for each criteria in list,
         * add items, trigger and conditions
         */

        if (getTriggers().isEmpty())
            triggers.add(Trigger.builder(Trigger.TriggerType.IMPOSSIBLE, "default"));

        for (Trigger.TriggerBuilder triggerBuilder : getTriggers()) {
            Trigger trigger = triggerBuilder.build();
            criteria.add(trigger.name, trigger.toJsonObject());
        }

        json.add("criteria", criteria);
        json.add("display", display);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(json);
    }

    public static JsonElement getJsonFromComponent(TextComponent textComponent) {
        return gson.fromJson(ComponentSerializer.toString(textComponent), JsonElement.class);

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

        public static FrameType getFromString(String frameType) {
            if (frameType.equalsIgnoreCase("random")) return FrameType.RANDOM();
            else try {
                return FrameType.valueOf(frameType);
            } catch (EnumConstantNotPresentException e) {
                Bukkit.getLogger().info("[AdvancementAPI] Unknown FrameType given. Using default (TASK)");
                return FrameType.TASK;
            }
        }

        public static FrameType RANDOM() {
            FrameType[] frameTypes = FrameType.values();
            return frameTypes[(int) (Math.random() * (frameTypes.length - 1))];
        }

        public String toString() {
            return name;
        }
    }

    public static class AdvancementAPIBuilder {
        public AdvancementAPIBuilder title(String title) {

            this.title = new TextComponent(title);

            return this;
        }

        public AdvancementAPIBuilder title(TextComponent title) {
            this.title = title;

            return this;
        }

        public AdvancementAPIBuilder description(String description) {
            this.description = new TextComponent(description);
            return this;
        }

        public AdvancementAPIBuilder description(TextComponent description) {
            this.description = description;
            return this;
        }


    }

    //BEGIN CLASSES
    @Builder(builderMethodName = "hiddenbuilder")
    public static class Condition {
        protected String name;
        protected JsonObject set;

        public static Condition.ConditionBuilder builder(String name, JsonObject itemStack) {
            return Condition.hiddenbuilder().name(name).set(itemStack);
        }

        public static Condition.ConditionBuilder builder(String name, ItemStack itemStack) {
            return Condition.hiddenbuilder().name(name).set(convertItemToJSON(itemStack));
        }


        //BEGIN UTIL
        private static JsonObject convertItemToJSON(ItemStack item) {
            JsonObject itemJSON = new JsonObject();
            itemJSON.addProperty("item", "minecraft:" + item.getType().name().toLowerCase());
            itemJSON.addProperty("amount", item.getAmount());
            itemJSON.addProperty("data", item.getData().getData());
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

        public JsonObject toJsonObject() {

            JsonObject triggerObj = new JsonObject();

            final JsonObject advConditions = new JsonObject();
            triggerObj.addProperty("trigger", "minecraft:" + this.type.toString().toLowerCase());
            this.conditions.forEach(conditionBuilder -> {

                Condition condition = conditionBuilder.build();
                advConditions.add(condition.name, condition.set);
            });
            if (!this.conditions.isEmpty())
                triggerObj.add("conditions", advConditions);


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
