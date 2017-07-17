package io.chazza.advancementapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

public class AdvancementAPI {

    private static final Gson gson = new Gson();

    private NamespacedKey id;
    private String parent, icon, background;
    private TextComponent title, description;
    private FrameType frame;
    private boolean announce = true, toast = true, hidden = true;
    private int counter = 1;

    private Set<Trigger.TriggerBuilder> triggers;

    private AdvancementAPI(NamespacedKey id, String parent, String icon, String background, TextComponent title, TextComponent description, FrameType frame, boolean announce, boolean toast, boolean hidden, int counter, Set<Trigger.TriggerBuilder> triggers) {
        this.id = id;
        this.parent = parent;
        this.icon = icon;
        this.background = background;
        this.title = title;
        this.description = description;
        this.frame = frame;
        this.announce = announce;
        this.toast = toast;
        this.hidden = hidden;
        this.counter = counter;
        this.triggers = triggers;
    }


    public static AdvancementAPIBuilder builder(NamespacedKey id) {
        return new AdvancementAPIBuilder().id(id);
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

        for (Trigger.TriggerBuilder triggerBuilder : getTriggers()) {
            Trigger trigger = triggerBuilder.build();
            criteria.add(trigger.name, trigger.toJsonObject());
        }

        json.add("criteria", criteria);
        json.add("display", display);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(json);
    }

    public String getIcon() {
        return this.icon;
    }

    public static JsonElement getJsonFromComponent(TextComponent textComponent) {
        return gson.fromJson(ComponentSerializer.toString(textComponent), JsonElement.class);

    }

    public TextComponent getTitle() {
        return this.title;
    }

    public TextComponent getDescription() {
        return this.description;
    }

    public String getBackground() {
        return this.background;
    }

    public FrameType getFrame() {
        return this.frame;
    }

    public String getParent() {
        return this.parent;
    }

    public Set<Trigger.TriggerBuilder> getTriggers() {
        return this.triggers;
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

    public NamespacedKey getId() {
        return this.id;
    }

    public boolean isAnnounce() {
        return this.announce;
    }

    public boolean isToast() {
        return this.toast;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public int getCounter() {
        return this.counter;
    }

    public static class AdvancementAPIBuilder {
        private NamespacedKey id;
        private String parent;
        private String icon;
        private String background;
        private TextComponent title;
        private TextComponent description;
        private FrameType frame;
        private boolean announce;
        private boolean toast;
        private boolean hidden;
        private int counter;
        private ArrayList<Trigger.TriggerBuilder> triggers;

        AdvancementAPIBuilder() {
        }

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


        public AdvancementAPIBuilder id(NamespacedKey id) {
            this.id = id;
            return this;
        }

        public AdvancementAPIBuilder parent(String parent) {
            this.parent = parent;
            return this;
        }

        public AdvancementAPIBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public AdvancementAPIBuilder background(String background) {
            this.background = background;
            return this;
        }

        public AdvancementAPIBuilder frame(FrameType frame) {
            this.frame = frame;
            return this;
        }

        public AdvancementAPIBuilder announce(boolean announce) {
            this.announce = announce;
            return this;
        }

        public AdvancementAPIBuilder toast(boolean toast) {
            this.toast = toast;
            return this;
        }

        public AdvancementAPIBuilder hidden(boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        public AdvancementAPIBuilder counter(int counter) {
            this.counter = counter;
            return this;
        }

        public AdvancementAPIBuilder trigger(Trigger.TriggerBuilder trigger) {
            if (this.triggers == null)
                this.triggers = new ArrayList<Trigger.TriggerBuilder>();
            this.triggers.add(trigger);
            return this;
        }

        public AdvancementAPIBuilder triggers(Collection<? extends Trigger.TriggerBuilder> triggers) {
            if (this.triggers == null)
                this.triggers = new ArrayList<Trigger.TriggerBuilder>();
            this.triggers.addAll(triggers);
            return this;
        }

        public AdvancementAPIBuilder clearTriggers() {
            if (this.triggers != null)
                this.triggers.clear();

            return this;
        }

        public AdvancementAPI build() {
            Set<Trigger.TriggerBuilder> triggers;
            switch (this.triggers == null ? 0 : this.triggers.size()) {
                case 0:
                    triggers = java.util.Collections.singleton(Trigger.builder(Trigger.TriggerType.IMPOSSIBLE, "default"));    
                    break;
                case 1:
                    triggers = java.util.Collections.singleton(this.triggers.get(0));
                    break;
                default:
                    triggers = new java.util.LinkedHashSet<Trigger.TriggerBuilder>(this.triggers.size() < 1073741824 ? 1 + this.triggers.size() + (this.triggers.size() - 3) / 3 : Integer.MAX_VALUE);
                    triggers.addAll(this.triggers);
                    triggers = java.util.Collections.unmodifiableSet(triggers);
            }

            return new AdvancementAPI(id, parent, icon, background, title, description, frame, announce, toast, hidden, counter, triggers);
        }

        public String toString() {
            return "io.chazza.advancementapi.AdvancementAPI.AdvancementAPIBuilder(id=" + this.id + ", parent=" + this.parent + ", icon=" + this.icon + ", background=" + this.background + ", title=" + this.title + ", description=" + this.description + ", frame=" + this.frame + ", announce=" + this.announce + ", toast=" + this.toast + ", hidden=" + this.hidden + ", counter=" + this.counter + ", triggers=" + this.triggers + ")";
        }
    }

}
