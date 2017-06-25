package io.chazza.advancementapi;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * Created by ysl3000
 */
public class Trigger {
    protected TriggerType type;
    protected String name;
    protected Set<Condition.ConditionBuilder> conditions;

    private Trigger(TriggerType type, String name, Set<Condition.ConditionBuilder> conditions) {
        this.type = type;
        this.name = name;
        this.conditions = conditions;
    }

    public static TriggerBuilder builder(TriggerType type, String name) {
        return new TriggerBuilder().type(type).name(name);
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

    public static class TriggerBuilder {
        private TriggerType type;
        private String name;
        private ArrayList<Condition.ConditionBuilder> conditions;

        TriggerBuilder() {
        }

        public Trigger.TriggerBuilder type(TriggerType type) {
            this.type = type;
            return this;
        }

        public Trigger.TriggerBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Trigger.TriggerBuilder condition(Condition.ConditionBuilder condition) {
            if (this.conditions == null)
                this.conditions = new ArrayList<Condition.ConditionBuilder>();
            this.conditions.add(condition);
            return this;
        }

        public Trigger.TriggerBuilder conditions(Collection<? extends Condition.ConditionBuilder> conditions) {
            if (this.conditions == null)
                this.conditions = new ArrayList<Condition.ConditionBuilder>();
            this.conditions.addAll(conditions);
            return this;
        }

        public Trigger.TriggerBuilder clearConditions() {
            if (this.conditions != null)
                this.conditions.clear();

            return this;
        }

        public Trigger build() {
            Set<Condition.ConditionBuilder> conditions;
            switch (this.conditions == null ? 0 : this.conditions.size()) {
                case 0:
                    conditions = java.util.Collections.emptySet();
                    break;
                case 1:
                    conditions = java.util.Collections.singleton(this.conditions.get(0));
                    break;
                default:
                    conditions = new java.util.LinkedHashSet<Condition.ConditionBuilder>(this.conditions.size() < 1073741824 ? 1 + this.conditions.size() + (this.conditions.size() - 3) / 3 : Integer.MAX_VALUE);
                    conditions.addAll(this.conditions);
                    conditions = java.util.Collections.unmodifiableSet(conditions);
            }

            return new Trigger(type, name, conditions);
        }

        public String toString() {
            return "io.chazza.advancementapi.Trigger.TriggerBuilder(type=" + this.type + ", name=" + this.name + ", conditions=" + this.conditions + ")";
        }
    }
}
