package io.chazza.advancementapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
* Created by charliej on 14/05/2017.
*/
public class AdvancementAPI {

    private String id, title, parent, trigger, icon, description, background;
    private List<ItemStack> items;

    AdvancementAPI(String id){
        this.id = id;
        this.items = new ArrayList<>();
    }

    public String getID(){
        return id;
    }

    public String getIcon(){
        return icon;
    }
    public void setIcon(String icon){
        this.icon = icon;
    }


    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }


    public String getBackground(){
        return description;
    }
    public void setBackground(String url){
        this.background = background;
    }


    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }


    public String getParent(){
        return parent;
    }
    public void setParent(String parent){
        this.parent = parent;
    }


    public String getTrigger(){
        return trigger;
    }
    public void setTrigger(String trigger){
        this.trigger = trigger;
    }


    public List<ItemStack> getItems(){
        return items;
    }
    public void addItem(ItemStack is){
        items.add(is);
    }

    public String getJSON(){
        JSONObject json = new JSONObject();

        //
        JSONObject icon = new JSONObject();
        icon.put("item", getIcon());
        //
        JSONObject display = new JSONObject();
        display.put("icon", icon);
        display.put("title", getTitle());
        display.put("description", getDescription());
        display.put("background", getBackground());

        //
        json.put("parent", getParent());
        //
        JSONObject criteria = new JSONObject();
        JSONObject conditions = new JSONObject();
        JSONObject elytra = new JSONObject();

        JSONArray itemArray = new JSONArray();
        JSONObject itemJSON = new JSONObject();

        for(ItemStack i : getItems()){
            itemJSON.put("item", "minecraft:"+i.getType().name().toLowerCase());
            itemJSON.put("amount", i.getAmount());
            itemArray.add(itemJSON);
        }

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

    public void save() {
        File f = new File(
            Bukkit.getWorld("world").getWorldFolder().getAbsolutePath()
                + File.separator + "data"
                + File.separator + "advancements"
                + File.separator + "minecraft"
        + File.separator + "story");
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(f.getAbsolutePath() + File.separator + getID() + ".json");

            fileWriter.write(getJSON());
            fileWriter.close();

            Bukkit.getLogger().info("[AdvancementAPI] Created " + getID() + ".json.");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
