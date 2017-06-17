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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * test
 * @author charliej - the very API
 * @author DiscowZombie - adopting for Builder-Pattern
 * @author Ste3et_C0st - add/take advancement logic
 * @author PROgrammer_JARvis - rework and combining
 * @author ysl3000 - useful advice and bug-tracking at PullRequests
 */
public class AdvancementAPI {

    private NamespacedKey id;
    private int counter = 1;
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

    private AdvancementAPI(NamespacedKey id) {
        this.id = id;
    }

    public static List<AdvancementAPI> advancements = new ArrayList<>();

    public static List<AdvancementAPI> getAdvancements() {
        return advancements;
    }

    public static AdvancementAPI build(NamespacedKey id) {
        AdvancementAPI advancement = new AdvancementAPI(id);
        advancements.add(advancement);
        return advancement;
    }

    public static AdvancementAPI build(JavaPlugin plugin, String id) {
        return build(new NamespacedKey(plugin, id));
    }

    public AdvancementAPI build() {
        advancements.add(this);
        return this;
    }

    public AdvancementAPI unbuild() {
        remove();
        AdvancementAPI.advancements.remove(this);
        return this;
    }

    public String getID() {
        return id.toString();
    }

    public String getIcon() {
        return icon;
    }

    public AdvancementAPI icon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AdvancementAPI description(String description) {
        this.description = description;
        return this;
    }

    public String getBackground() {
        return background;
    }

    public AdvancementAPI background(String url) {
        this.background = url;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public AdvancementAPI title(String title) {
        this.title = title;
        return this;
    }

    public String getParent() {
        return parent;
    }

    public AdvancementAPI parent(String parent) {
        this.parent = parent;
        return this;
    }

    public String getTrigger() {
        return trigger;
    }

    public AdvancementAPI trigger(String trigger) {
        this.trigger = trigger;
        return this;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public AdvancementAPI addItem(ItemStack itemStack) {
        items.add(itemStack);
        return this;
    }

    public FrameType getFrame() {
        return frame;
    }

    public AdvancementAPI frame(FrameType frame) {
        this.frame = frame;
        return this;
    }

    public boolean getAnnouncement(){
        return announce;
    }

    public AdvancementAPI announcement(boolean announce){
        this.announce = announce;
        return this;
    }

    public boolean getToast(){
        return toast;
    }

    public AdvancementAPI toast(boolean toast){
        this.toast = toast;
        return this;
    }
    
    public AdvancementAPI counter(int i){
    	this.counter = i;
    	return this;
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
        
        for(int i = 0; i<counter; i++){
        	JSONObject triggerObj = new JSONObject();
        	triggerObj.put("trigger", this.trigger);
       	 	conditions.put("items", itemArray);
       	 	triggerObj.put("conditions", conditions);
            criteria.put("elytra" + i, triggerObj);
        }

        json.put("criteria", criteria);
        json.put("display", display);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
            Bukkit.getLogger().info("[AdvancementAPI] Created " + id.toString());
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    
    
    public boolean counterUp(Player player){
    	String criteriaString = null;
    	for(String criteria : getAdvancement().getCriteria()){
    		if(player.getAdvancementProgress(getAdvancement()).getDateAwarded(criteria) != null){
    			criteriaString = criteria;
    		}else{
    			break;
    		}
    	}
    	if(criteriaString == null) return false;
    	player.getAdvancementProgress(getAdvancement()).awardCriteria(criteriaString);
    	return true;
    }
    
    public boolean counterDown(Player player){
    	String criteriaString = null;
    	for(String criteria : getAdvancement().getCriteria()){
    		if(player.getAdvancementProgress(getAdvancement()).getDateAwarded(criteria) != null){
    			criteriaString = criteria;
    		}else{
    			break;
    		}
    	}
    	if(criteriaString == null) return false;
    	player.getAdvancementProgress(getAdvancement()).revokeCriteria(criteriaString);
    	return true;
    }
    
    public void counterReset(Player player){
    	for(String criteria : getAdvancement().getCriteria()){
    		if(player.getAdvancementProgress(getAdvancement()).getDateAwarded(criteria) != null){
    			player.getAdvancementProgress(getAdvancement()).revokeCriteria(criteria);
    		}
    	}
    }

    @SuppressWarnings("deprecation")
    public AdvancementAPI add() {
        try {
            Bukkit.getUnsafe().loadAdvancement(id, getJSON());
            Bukkit.getLogger().info("[AdvancementAPI] Successfully registered advancement");
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().info("[AdvancementAPI] Error registering advancement. It seems to already exist");
        }
        return this;
    }

    @SuppressWarnings("deprecation")
    public AdvancementAPI remove() {
        Bukkit.getUnsafe().removeAdvancement(id);
        return this;
    }

    public AdvancementAPI show(JavaPlugin plugin, final Player... players) {
        add();
        grant(players);
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                revoke(players);
                remove();
            }
        }, 20L);
        return this;
    }

    public AdvancementAPI grant(Player... players) {
        Advancement advancement = getAdvancement();
        for (Player player : players) {
            if (!player.getAdvancementProgress(advancement).isDone()) {
                Collection<String> remainingCriteria = player.getAdvancementProgress(advancement).getRemainingCriteria();
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

    @Override
    public String toString() {
        return "Advancement(" + id + "|" + this.title + ")";
    }
}