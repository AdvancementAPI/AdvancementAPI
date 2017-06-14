package io.chazza.advancementapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.Bukkit;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import com.google.common.collect.Lists;

/**
 * Rewritten by PROgrammer_JARvis
 * Originally Created by charliej on 14/05/2017.
 * Lately modified by DiscowZombie on 5/06/2017.
 */

//Renamed into MessengerAdvancement as AdvancementAPI seems to be a better name for some Singleton @PROgrm_JARvis
public class MessengerAdvancement {

    private NamespacedKey id;
    private String
            title = "Untitled",
            parent,
            trigger = "minecraft:impossible",
            icon = "minecraft:golden_apple",
            description = "no description",
            background = "minecraft:textures/gui/advancements/backgrounds/stone.png";
    private boolean announce = false, toast = true;
    private FrameType frame = FrameType.TASK;
    private List<ItemStack> items = Lists.newArrayList();

    public MessengerAdvancement(NamespacedKey id) {
        this.id = id;
    }

    public MessengerAdvancement(JavaPlugin plugin, String id) {
        this.id = new NamespacedKey(plugin, id);
    }

    public String getID() {
        return id.toString();
    }

    public String getIcon() {
        return icon;
    }

    public MessengerAdvancement withIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public MessengerAdvancement withDescription(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public MessengerAdvancement withBackground(String url) {
        this.background = url;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MessengerAdvancement withTitle(String title) {
        this.title = title;
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getParent() {
        return parent;
    }

    public MessengerAdvancement withParent(String parent) {
        this.parent = parent;
        return this;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getTrigger() {
        return trigger;
    }

    public MessengerAdvancement withTrigger(String trigger) {
        this.trigger = trigger;
        return this;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public MessengerAdvancement withItem(ItemStack itemStack) {
        items.add(itemStack);
        return this;
    }

    public void addItem(ItemStack itemStack) {
        items.add(itemStack);
    }

    public FrameType getFrame() {
        return frame;
    }

    public MessengerAdvancement withFrame(FrameType frame) {
        this.frame = frame;
        return this;
    }

    public void setFrame(FrameType frame) {
        this.frame = frame;
    }

    public boolean getAnnouncement(){
        return announce;
    }

    public MessengerAdvancement withAnnouncement(boolean announce){
        this.announce = announce;
        return this;
    }

    public void setAnnounce(boolean announce) {
        this.announce = announce;
    }

    public boolean getToast(){
        return toast;
    }

    public MessengerAdvancement withToast(boolean toast){
        this.toast = toast;
        return this;
    }

    public void setToast(boolean toast) {
        this.toast = toast;
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
        display.put("announce_to_chat", getAnnouncement());
        display.put("show_toast", getToast());

        json.put("parent", getParent());

        JSONObject criteria = new JSONObject();
        JSONObject conditions = new JSONObject();
        JSONObject elytra = new JSONObject();

        JSONArray itemArray = new JSONArray();
        JSONObject itemJSON = new JSONObject();

        for(ItemStack itemStack : getItems()) {
            itemJSON.put("item", "minecraft:"+ itemStack.getType().name().toLowerCase());
            itemJSON.put("amount", itemStack.getAmount());
            itemArray.add(itemJSON);
        }

	//Changed to normal comment as JavaDocs are not displayed here @PROgrm_JARvis
        /*
         * Define each criteria, for each criteria in list,
         * add items, trigger and conditions
         */

        conditions.put("items", itemArray);
        elytra.put("trigger", getTrigger());
        elytra.put("conditions", conditions);

        criteria.put("elytra", elytra);

        json.put("criteria", criteria);
        json.put("display", display);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Bukkit.getLogger().info(gson.toJson(json));

        return gson.toJson(json);
    }
	
    @Deprecated
    public void save(String world) {
        this.save(Bukkit.getWorld(world));
    }

    @Deprecated
    public void save(World world) {
        try {
            Files.createDirectories(Paths.get(world.getWorldFolder() + File.separator + "data" + File.separator + "advancements"
                    + File.separator + id.getNamespace()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        File file = new File(world.getWorldFolder() + File.separator + "data" + File.separator + "advancements"
                + File.separator + id.getNamespace() + File.separator + id.getKey() + ".json");
        try{
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(getJSON());
            writer.flush();
            writer.close();
            Bukkit.getLogger().info("[MessengerAdvancement] Created " + id.toString());
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public MessengerAdvancement add() {
        try {
            Bukkit.getUnsafe().loadAdvancement(id, getJSON());
            Bukkit.getLogger().info("Successfully registered advancement.");
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().info("Error registering advancement. It seems to already exist!");
        }
        return this;
    }

    @SuppressWarnings("deprecation")
    public MessengerAdvancement remove() {
        Bukkit.getUnsafe().removeAdvancement(id);
        return this;
    }

    public MessengerAdvancement show(Player... players) {
        add();
        grant(players);
        Bukkit.getScheduler().runTaskLater(ContentMakersPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                revoke(players);
                remove();
            }
        }, 20L);
        return this;
    }

    public MessengerAdvancement grant(Player... players) {
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

    public MessengerAdvancement revoke(Player... players) {
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

        private String name = "task";

        FrameType(String name){
            this.name = name;
        }

        public FrameType RANDOM() {
            FrameType[] frameTypes = FrameType.values();
            return frameTypes[(int)(Math.random()*(frameTypes.length-1))];
        }

        public String toString(){
            return name;
        }
    }
}
