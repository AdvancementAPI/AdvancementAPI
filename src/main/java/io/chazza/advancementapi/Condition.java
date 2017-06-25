package io.chazza.advancementapi;

import com.google.gson.JsonObject;
import org.bukkit.inventory.ItemStack;

/**
 * Created by ysl3000
 */ //BEGIN CLASSES
public class Condition {
    protected String name;
    protected JsonObject set;

    private Condition(String name, JsonObject set) {
        this.name = name;
        this.set = set;
    }

    public static ConditionBuilder builder(String name, JsonObject itemStack) {
        return new Condition.ConditionBuilder().name(name).set(itemStack);
    }

    public static ConditionBuilder builder(String name, ItemStack itemStack) {
        return Condition.builder(name,convertItemToJSON(itemStack));
    }


    //BEGIN UTIL
    private static JsonObject convertItemToJSON(ItemStack item) {
        JsonObject itemJSON = new JsonObject();
        itemJSON.addProperty("item", "minecraft:" + item.getType().name().toLowerCase());
        itemJSON.addProperty("amount", item.getAmount());
        itemJSON.addProperty("data", item.getData().getData());
        return itemJSON;
    }



    public static class ConditionBuilder {
        private String name;
        private JsonObject set;

        ConditionBuilder() {
        }

        public Condition.ConditionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Condition.ConditionBuilder set(JsonObject set) {
            this.set = set;
            return this;
        }

        public Condition build() {
            return new Condition(name, set);
        }

        public String toString() {
            return "io.chazza.advancementapi.Condition.ConditionBuilder(name=" + this.name + ", set=" + this.set + ")";
        }
    }
}
