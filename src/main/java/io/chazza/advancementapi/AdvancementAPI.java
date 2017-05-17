package io.chazza.advancementapi;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
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

    private String id, title, parent, trigger, icon, description, background, frame;
    private boolean announce;
    private List<ItemStack> items;

    public AdvancementAPI(String id) {
        this.id = id;
        this.items = Lists.newArrayList();
        this.announce = true;
    }

    public AdvancementAPI withIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public AdvancementAPI withDescription(String description) {
        this.description = description;
        return this;
    }

    public AdvancementAPI withBackground(String url) {
        this.background = url; //Fixed this for you, too
        return this;
    }

    public AdvancementAPI withTitle(String title) {
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

    public AdvancementAPI withFrame(String frame) {
        this.frame = frame;
        return this;
    }

    public AdvancementAPI withAnnouncement(boolean announce) {
        this.announce = announce;
        return this;
    }

    public void save(String world) {

        File f = new File(
                Bukkit.getWorld(world).getWorldFolder().getAbsolutePath()
                        + File.separator + "data"
                        + File.separator + "advancements"
                        + File.separator + "minecraft"
                        + File.separator + "story");

        if (f.mkdirs()) {
            FileWriter fileWriter;
            try {
                fileWriter = new FileWriter(f.getAbsolutePath() + File.separator + getID() + ".json");

                fileWriter.write(getJSON());
                fileWriter.close();

                // Bukkit.getLogger().info("[io.chazza.advancementapi.AdvancementAPI] Created " + getID() + ".json.");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public String getID() {
        return id;
    }

    public String getJSON() {
        JSONObject json = new JSONObject();


        JSONObject icon = new JSONObject();
        icon.put("item", getIcon());

        JSONObject display = new JSONObject();
        display.put("icon", icon);
        display.put("title", getTitle());
        display.put("description", getDescription());
        display.put("background", getBackground());
        display.put("frame", getFrame());
        display.put("announce_to_chat", getAnnouncement());


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

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getBackground() {
        return background;
    }

    public String getFrame() {
        return frame;
    }

    public boolean getAnnouncement() {
        return announce;
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
}
