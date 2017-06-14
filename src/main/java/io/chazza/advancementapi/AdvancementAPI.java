package io.chazza.advancementapi;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by charliej on 14/05/2017.
 * Edited by GiansCode
 */

public class AdvancementAPI {

    private NamespacedKey id;
    private String parent, trigger, icon, background;
    private BaseComponent title, description;
    private FrameType frame;
    private boolean announce, shouldShowToast, shouldBeHiddenBeforeArchieved;
    private List<ItemStack> items;

    public AdvancementAPI(NamespacedKey id) {
        this.id = id;
        this.items = Lists.newArrayList();
        this.announce = true;
    }

    public String getID() {
        return id.toString();
    }

    public AdvancementAPI withIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public AdvancementAPI withDescription(String description) {
        this.description = new TextComponent(description);
        return this;
    }
    public AdvancementAPI withDescription(BaseComponent description) {
        this.description = description;
        return this;
    }

    public AdvancementAPI withBackground(String url) {
        this.background = url; //Fixed this for you, too
        return this;
    }

    public AdvancementAPI withTitle(String title) {
        this.title = new TextComponent(title);
        return this;
    }

    public AdvancementAPI withTitle(BaseComponent title) {
        this.title = title;
        return this;
    }


    public AdvancementAPI withParent(String parent) {
        this.parent = parent;
        return this;
    }

    public AdvancementAPI withTrigger(String trigger) {
        this.trigger = trigger;
        return this;
    }

    public AdvancementAPI withItem(ItemStack is) {
        items.add(is);
        return this;
    }

    public AdvancementAPI withFrame(FrameType frame) {
        this.frame = frame;
        return this;
    }

    public AdvancementAPI withAnnouncement(boolean announce) {
        this.announce = announce;
        return this;
    }

    public AdvancementAPI withToast(boolean showToast) {
        this.shouldShowToast = showToast;
        return this;
    }

    public AdvancementAPI withShouldBeHiddenBeforeArchieved(boolean shouldBeHiddenBeforeArchieved) {
        this.shouldBeHiddenBeforeArchieved = shouldBeHiddenBeforeArchieved;
        return this;
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
        display.put("announce_to_chat", getAnnouncement());
        display.put("show_toast", getToast());
        display.put("hidden", getshouldBeHiddenBeforeArchieved());


        json.put("parent", getParent());

        JSONObject criteria = new JSONObject();
        JSONObject conditions = new JSONObject();
        JSONObject elytra = new JSONObject();

        JSONArray itemArray = new JSONArray();
        JSONObject itemJSON = new JSONObject();

        for (ItemStack i : getItems()) {
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
        elytra.put("trigger", getTrigger());
        elytra.put("conditions", conditions);

        criteria.put("elytra", elytra);

        json.put("criteria", criteria);
        json.put("display", display);


        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

    public String getIcon() {
        return icon;
    }

    public BaseComponent getTitle() {
        return title;
    }

    public BaseComponent getDescription() {
        return description;
    }

    public String getBackground() {
        return background;
    }

    public FrameType getFrame() {
        return frame;
    }

    public boolean getAnnouncement() {
        return announce;
    }

    public boolean getToast() {
        return shouldShowToast;
    }
    public boolean getshouldBeHiddenBeforeArchieved() {
        return shouldBeHiddenBeforeArchieved;
    }

    public String getParent() {
        return parent;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public String getTrigger() {
        return trigger;
    }


    public enum FrameType {
        TASK("task"),
        GOAL("goal"),
        CHALLENGE("challenge");
        private String name = "task";

        private FrameType(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

}
