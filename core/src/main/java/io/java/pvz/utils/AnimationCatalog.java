package io.java.pvz.utils;

import io.java.pvz.models.entities.SunType;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class AnimationCatalog {
    private AnimationCatalog() {}

    public static final class EntityAnimation {
        public final String name;
        public final String path;
        private final Map<String, Float> clips;

        private EntityAnimation(String name, String path, Map<String, Float> clips) {
            this.name = name;
            this.path = path;
            this.clips = Collections.unmodifiableMap(clips);
        }

        public boolean hasClip(String clipName) {
            return clips.containsKey(clipName);
        }

        public float getDuration(String clipName) {
            Float d = clips.get(clipName);
            return d != null ? d : 0f;
        }

        public Set<String> getClipNames() {
            return clips.keySet();
        }

        @Override
        public String toString() {
            return name + " [" + path + "] clips=" + clips.keySet();
        }
    }

    private static final Map<String, EntityAnimation> PLANTS = new HashMap<>();
    private static final Map<ZombieType, EntityAnimation> ZOMBIES = new HashMap<>();
    private static final Map<String, EntityAnimation> MOWERS = new HashMap<>();
    private static final Map<SunType, EntityAnimation> SUNS = new HashMap<>();

    static {
        registerPlants(PLANTS);
        registerZombies(ZOMBIES);
        registerMowers(MOWERS);
        registerSuns(SUNS);
    }

    private static void registerPlants(Map<String, EntityAnimation> table) {
        register(table, "APPEASEMINT", "768/INITIAL/EMPOWERMINTS/PLANT/APPEASEMINT/APPEASEMINT.PAM", "intro", 2.2333f, "loop", 1.4333f, "outro", 0.8333f);
        register(table, "ARMAMINT", "768/INITIAL/EMPOWERMINTS/PLANT/ARMAMINT/ARMAMINT.PAM", "intro", 2.9667f, "loop", 3.3333f, "outro", 1.4f);
        register(table, "ARMAMINT_EXPLOSION", "768/INITIAL/EMPOWERMINTS/PLANT/ARMAMINT_EXPLOSION/ARMAMINT_EXPLOSION.PAM", "animation", 0.8333f);
        register(table, "ARMAMINT_PROJECTILE", "768/INITIAL/EMPOWERMINTS/PLANT/ARMAMINT_PROJECTILE/ARMAMINT_PROJECTILE.PAM", "animation", 0.1f);
        register(table, "BOMBARDMINT", "768/INITIAL/EMPOWERMINTS/PLANT/BOMBARDMINT/BOMBARDMINT.PAM", "intro", 1.1667f, "loop", 3.3333f, "outro", 1.5f);
        register(table, "BONKCHOY", "768/INITIAL/PLANT/BONKCHOY/BONKCHOY.PAM", "idle", 1.0f, "idle2", 1.0f, "idle3", 1.0333f, "attack", 0.3333f, "attack2", 0.3333f, "attack3", 0.6667f, "attack4", 0.5f, "attack5", 0.5f, "plantfood_on", 1.0f, "plantfood", 1.0f, "plantfood_off", 0.3333f, "water", 1.7333f);
        register(table, "BOWLINGBULB", "768/FULL/PLANT/BOWLINGBULB/BOWLINGBULB.PAM", "idle", 2.6667f, "special", 2.6667f, "special2", 2.6667f, "special3", 2.6667f, "reload", 0.8333f, "reload2", 0.8f, "reload3", 0.8667f, "plantfood_on", 1.2333f, "plantfood_idle", 2.0f, "plantfood1", 0.8f, "plantfood2", 0.7667f, "plantfood3", 0.9f, "water", 2.7f);
        register(table, "CABBAGEPULT", "768/INITIAL/PLANT/CABBAGEPULT/CABBAGEPULT.PAM", "idle", 4.0f, "idle2", 4.0f, "attack", 1.6667f, "plantfood", 2.0f, "water", 2.0333f);
        register(table, "CACTUS", "768/INITIAL/PLANT/CACTUS/CACTUS.PAM", "idle", 2.6667f, "idle2", 2.6667f, "idle3", 1.3333f, "attack", 1.0333f, "plantfood", 0.8667f, "down", 1.0667f, "down_idle", 0.2333f, "down_attack", 0.5667f, "up", 1.0667f, "idle_plantfood", 2.6667f, "idle_plantfood2", 2.6667f, "idle_plantfood3", 1.3333f, "attack_plantfood", 1.0333f, "down_plantfood", 1.0667f, "down_idle_plantfood", 1.6667f, "down_attack_plantfood", 0.5667f, "up_plantfood", 1.0667f, "water", 2.9667f, "up_stretch", 0.8333f, "attack_stretch", 1.1667f, "down_stretch", 0.8333f);
        register(table, "CAULIPOWER", "768/INITIAL/PLANT/CAULIPOWER/CAULIPOWER.PAM", "idle1_1", 4.0f, "idle2_1", 4.0f, "idle3_1", 4.0f, "idle4_1", 4.0f, "attack", 1.7333f, "plantfood_start", 0.3333f, "plantfood_loop", 0.4333f, "plantfood_loop2", 0.4333f, "plantfood_end", 0.3333f, "water", 4.0f);
        register(table, "CHERRYBOMB", "768/FULL/PLANT/CHERRYBOMB/CHERRYBOMB.PAM", "idle", 1.0f, "attack", 0.7f, "water", 1.6667f);
        register(table, "CHOMPER", "768/INITIAL/PLANT/CHOMPER/CHOMPER.PAM", "idle", 2.0f, "idle2", 2.0f, "idle3", 2.3667f, "idle4", 3.0667f, "bite", 0.7333f, "bite_end", 0.8f, "special", 1.9667f, "special_idle", 0.9333f, "special_end", 3.1667f, "plantfood_on", 1.0f, "plantfood", 0.9667f, "plantfood_off", 1.9667f, "plantfood_burp", 1.0333f, "plantfood_burp_end", 0.7f, "water", 2.5f);
        register(table, "CITRON", "768/FULL/PLANT/CITRON/CITRON.PAM", "charge", 7.0f, "idle", 0.9f, "idle2", 3.3333f, "attack", 1.3f, "plantfood", 1.3667f, "recovery", 1.4333f, "water", 1.6f);
        register(table, "DOOMSHROOM", "768/FULL/PLANT/DOOMSHROOM/DOOMSHROOM.PAM", "stage1_spawn", 1.0f, "stage1_idle", 2.0f, "stage1_explode", 2.0f, "stage1_transform", 2.0f, "stage2_idle", 2.0f, "stage2_idle2", 2.0f, "stage2_explode", 2.0f, "stage2_transform", 2.0f, "stage3_idle", 2.0f, "stage3_idle2", 2.0f, "stage3_explode", 3.3333f, "stage3_explode_short", 3.3333f, "water", 2.0f);
        register(table, "ELECTRICBLUEBERRY", "768/INITIAL/PLANT/ELECTRICBLUEBERRY/ELECTRICBLUEBERRY.PAM", "idle1_1", 2.6667f, "idle1_2", 2.0f, "idle2_1", 2.6667f, "idle2_2", 2.6667f, "idle2_3", 2.6667f, "idle2_4", 2.6667f, "idle3_1", 1.9333f, "idle3_2", 1.9333f, "idle3_3", 1.9333f, "idle4_1", 1.3333f, "idle4_2", 1.3333f, "idle4_3", 1.3333f, "attack", 1.6667f, "plantfood", 2.0f, "water", 1.5333f);
        register(table, "ENCHANTMINT", "768/INITIAL/EMPOWERMINTS/PLANT/ENCHANTMINT/ENCHANTMINT.PAM", "intro", 1.8f, "loop", 3.3333f, "outro", 1.8f);
        register(table, "ENDURIAN", "768/FULL/PLANT/ENDURIAN/ENDURIAN.PAM", "idle", 9.0f, "idle2", 10.0f, "attack_start", 0.5f, "attack_loop", 0.3333f, "attack_end", 0.5333f, "damage", 3.3333f, "attack_start_damage", 0.5f, "attack_loop_damage", 0.3333f, "attack_end_damage", 0.5333f, "damage2", 3.3333f, "attack_start_damage2", 0.5f, "attack_loop_damage2", 0.3333f, "attack_end_damage2", 0.5333f, "damage3", 6.6667f, "attack_start_damage3", 0.5f, "attack_loop_damage3", 0.3333f, "attack_end_damage3", 0.5333f, "plantfood_on", 0.5667f, "water", 1.4667f);
        register(table, "ENFORCEMINT", "768/INITIAL/EMPOWERMINTS/PLANT/ENFORCEMINT/ENFORCEMINT.PAM", "intro", 2.8333f, "loop", 3.3667f, "outro", 2.1667f);
        register(table, "ENLIGHTENMINT", "768/INITIAL/EMPOWERMINTS/PLANT/ENLIGHTENMINT/ENLIGHTENMINT.PAM", "intro", 3.2667f, "loop", 3.3333f, "outro", 1.2333f);
        register(table, "EXPLODEONUT", "768/INITIAL/PLANT/EXPLODEONUT/EXPLODEONUT.PAM", "idle", 1.6667f, "idle2", 1.6667f, "idle3", 1.6667f, "damage", 3.5f, "damage2", 3.3333f, "damage3", 0.3f, "plantfood_on", 0.6667f, "plantfood", 0.1667f, "plantfood2", 0.1333f, "plantfood3", 1.6f, "plantfood_off", 0.2667f, "water", 2.0333f);
        register(table, "FIREPEASHOOTER", "768/INITIAL/PLANT/FIREPEASHOOTER/FIREPEASHOOTER.PAM", "idle", 1.0333f, "idle2", 1.0333f, "attack", 1.0333f, "plantfood", 0.7f, "plantfood_loop", 0.7f, "plantfood_end", 0.5667f, "water", 4.0f);
        register(table, "FUMESHROOM", "768/INITIAL/PLANT/FUMESHROOM/FUMESHROOM.PAM", "idle", 2.9f, "idle2", 2.9f, "special", 1.8f, "plantfood", 5.3333f, "water", 2.7f);
        register(table, "GARLIC", "768/FULL/PLANT/GARLIC/GARLIC.PAM", "idle", 1.6667f, "idle2", 3.2333f, "idle_damage", 1.1333f, "idle2_damage", 2.3333f, "idle_damage2", 1.0f, "idle2_damage2", 1.6333f, "plantfood", 3.0333f, "water", 1.7f);
        register(table, "GOLDBLOOM", "768/INITIAL/PLANT/GOLDBLOOM/GOLDBLOOM.PAM", "idle", 1.6667f, "idle2", 1.6667f, "idle3", 2.8667f, "attack", 2.4f);
        register(table, "GOOPEASHOOTER", "768/INITIAL/PLANT/GOOPEASHOOTER/GOOPEASHOOTER.PAM", "idle", 1.3333f, "idle2", 1.3333f, "idle3", 2.1333f, "attack", 1.6f, "plantfood", 2.3f, "water", 2.1333f);
        register(table, "GRAPESHOT", "768/INITIAL/PLANT/GRAPESHOT/GRAPESHOT.PAM", "idle", 4.0f, "attack", 1.6667f, "attack_t2", 1.6667f, "attack_t3", 1.6667f);
        register(table, "GRAVEBUSTER", "768/INITIAL/PLANT/GRAVEBUSTER/GRAVEBUSTER.PAM", "attack", 1.0f, "attack1", 0.6667f, "water", 1.8667f);
        register(table, "HOTPOTATO", "768/FULL/PLANT/HOTPOTATO/HOTPOTATO.PAM", "idle", 3.3333f, "idle2", 3.3333f, "attack", 4.6667f);
        register(table, "HYPNOSHROOM", "768/INITIAL/PLANT/HYPNOSHROOM/HYPNOSHROOM.PAM", "idle", 4.0f, "idle2", 4.0f, "plantfood_on", 1.2667f, "plantfood", 4.0333f, "water", 4.0f);
        register(table, "ICEBURG", "768/INITIAL/PLANT/ICEBURG/ICEBURG.PAM", "idle", 3.9667f, "attack", 1.5f, "plantfood", 1.5f, "water", 2.0333f);
        register(table, "ICESHROOM", "768/FULL/PLANT/ICESHROOM/ICESHROOM.PAM", "idle", 0.9667f, "idle2", 1.9333f, "attack", 1.1667f, "plantfood", 1.6667f, "water", 1.9333f);
        register(table, "IMITATER", "768/INITIAL/PLANT/IMITATER/IMITATER.PAM", "idle", 2.1667f, "idle2", 3.7667f, "attack", 1.4667f, "water", 2.0333f);
        register(table, "JALAPENO", "768/INITIAL/PLANT/JALAPENO/JALAPENO.PAM", "idle", 0.6667f, "attack", 0.6667f, "water", 1.6667f);
        register(table, "KERNELPULT", "768/INITIAL/PLANT/KERNALPULT/KERNALPULT.PAM", "idle", 2.0f, "attack", 1.8667f, "attack2", 1.8333f, "plantfood", 1.5667f, "water", 2.6f);
        register(table, "KIWIBEAST", "768/INITIAL/PLANT/KIWIBEAST/KIWIBEAST.PAM", "idle_stage1_", 1.0f, "idle_stage1_2", 1.0f, "idle_stage1_3", 1.7667f, "attack_stage1", 1.4333f, "water", 2.0333f, "growth_stage1", 0.7667f, "growth_stage1_2", 0.6333f, "idle_stage2_", 1.3333f, "idle_stage2_2", 1.3333f, "idle_stage2_3", 1.8333f, "attack_stage2", 1.4667f, "growth_stage2", 0.8f, "idle_stage3_", 1.6667f, "idle_stage3_2", 1.6667f, "idle_stage3_3", 1.5667f, "attack_stage3", 1.5333f, "plantfood_stage3", 2.9f);
        register(table, "LILYPAD", "768/FULL/PLANT/LILYPAD/LILYPAD.PAM", "idle", 0.0667f, "idle2", 0.3f, "idle3", 2.1667f, "idle4", 3.1f, "idle5", 3.2333f, "plantfood", 2.1667f, "water", 3.5333f);
        register(table, "MAGNETSHROOM", "768/FULL/PLANT/MAGNETSHROOM/MAGNETSHROOM.PAM", "idle", 2.6333f, "idle2", 2.6333f, "idle3", 2.6333f, "busy", 2.6333f, "special", 0.6667f, "catch", 1.3f, "plantfood_on", 0.4f, "plantfood_collection", 1.4667f, "plantfood", 0.3667f, "plantfood_off", 0.6f, "water", 1.7f);
        register(table, "MEGAGATLING", "768/INITIAL/PLANT/MEGAGATLING/MEGAGATLING.PAM", "idle", 1.0333f, "attack", 1.0333f, "plantfood", 0.3333f, "idle_stage2", 1.0333f, "attack_stage2", 1.0333f, "water", 2.3f);
        register(table, "MELONPULT", "768/INITIAL/PLANT/MELONPULT/MELONPULT.PAM", "idle", 1.3667f, "attack", 1.9667f, "plantfood", 2.9f, "water", 1.7667f);
        register(table, "PEAPOD", "768/FULL/PLANT/PEAPOD/PEAPOD.PAM", "idle", 1.0f, "idle2", 1.0f, "idle3", 1.0f, "idle4", 1.0f, "idle5", 1.0f, "attack", 1.0333f, "attack 2", 1.0f, "attack 3", 1.0f, "attack 4", 1.0f, "attack 5", 1.0f, "plantfood_on", 1.5f, "plantfood", 5.2667f, "plantfood_off", 0.8f, "water", 2.4f);
        register(table, "PEASHOOTER", "768/INITIAL/PLANT/PEASHOOTER/PEASHOOTER.PAM", "idle", 1.0333f, "attack", 1.0333f, "plantfood", 0.4333f, "water", 2.3f);
        register(table, "PEPPERPULT", "768/FULL/PLANT/PEPPERPULT/PEPPERPULT.PAM", "idle", 3.3333f, "idle2", 3.3333f, "idle3", 2.2667f, "attack", 2.0f, "plantfood", 3.6333f, "water", 6.9f);
        register(table, "PHATBEETS", "768/FULL/PLANT/PHATBEETS/PHATBEETS.PAM", "idle", 0.6667f, "idle2", 0.6667f, "attack", 0.9f, "plantfood", 2.0f, "water", 2.1667f);
        register(table, "POTATOMINE", "768/INITIAL/PLANT/POTATOMINE/POTATOMINE.PAM", "plant", 0.9667f, "plant_idle", 0.0667f, "recover", 0.8333f, "idle", 1.0f, "idle2", 1.0333f, "attack", 0.6667f, "plantfood_on", 0.6f, "plantfood", 0.5f, "plantfood_off", 0.4333f, "plantfood2", 0.3667f, "plantfood3", 1.0333f, "water", 2.0333f);
        register(table, "PRIMAL_PEASHOOTER", "768/FULL/PLANT/PRIMAL_PEASHOOTER/PRIMAL_PEASHOOTER.PAM", "idle", 1.0333f, "idle2", 1.0333f, "attack", 1.0333f, "plantfood", 0.4333f, "water", 2.4333f);
        register(table, "PRIMAL_POTATOMINE", "768/FULL/PLANT/PRIMAL_POTATOMINE/PRIMAL_POTATOMINE.PAM", "plant", 0.9667f, "plant_idle", 0.0667f, "recover", 0.8333f, "idle", 1.0f, "idle2", 1.0f, "attack", 0.6667f, "plantfood_on", 0.5333f, "plantfood", 0.5f, "plantfood_off", 0.5333f, "plantfood2", 0.6667f, "plantfood3", 0.0667f, "water", 1.7f);
        register(table, "PRIMAL_SUNFLOWER", "768/FULL/PLANT/PRIMAL_SUNFLOWER/PRIMAL_SUNFLOWER.PAM", "idle", 1.5667f, "idle2", 1.5667f, "special", 1.7f, "plantfood_on", 0.6f, "plantfood", 0.7333f, "plantfood_off", 0.6333f, "water", 1.7f);
        register(table, "PUFFSHROOM", "768/INITIAL/PLANT/PUFFSHROOM/PUFFSHROOM.PAM", "idle_stage1", 1.0f, "idle2_stage1", 1.0f, "special_stage1", 0.8f, "idle_stage2", 1.0f, "idle2_stage2", 1.0f, "special_stage2", 0.8f, "idle_stage3", 1.0f, "idle2_stage3", 1.0f, "special_stage3", 0.8f, "idle_stage4", 0.8f, "plantfood_on", 0.6667f, "plantfood", 0.1f, "plantfood_off", 0.7667f, "water", 2.0333f);
        register(table, "PUMPKIN", "768/INITIAL/PLANT/PUMPKIN/PUMPKIN.PAM", "idle", 0.6667f, "idle2", 0.6667f, "idle3", 0.6667f, "idle_plantfood", 0.6667f, "idle_plantfood2", 0.6667f, "idle_plantfood3", 0.6667f, "idle_plantfood4", 0.6667f);
        register(table, "REINFORCEMINT", "768/INITIAL/EMPOWERMINTS/PLANT/REINFORCEMINT/REINFORCEMINT.PAM", "intro", 2.0f, "loop", 10.0f, "outro", 1.2f);
        register(table, "REPEATER", "768/INITIAL/PLANT/REPEATER/REPEATER.PAM", "idle", 1.0f, "attack", 1.0f, "plantfood", 0.4f, "plantfood2", 2.4333f, "water", 1.8333f);
        register(table, "ROTORUTABAGA", "768/FULL/PLANT/ROTORUTABAGA/ROTORUTABAGA.PAM", "idle", 2.5333f, "idle2", 2.5333f, "attack", 1.7333f, "plantfood_on", 3.2333f, "water", 3.3667f);
        register(table, "SEASHROOM", "768/FULL/PLANT/SEASHROOM/SEASHROOM.PAM", "idle", 1.0667f, "idle2", 2.1333f, "attack", 1.0667f, "pf", 1.6333f, "water", 2.1333f, "death", 2.0f);
        register(table, "SNOWPEA", "768/INITIAL/PLANT/SNOWPEA/SNOWPEA.PAM", "idle", 1.0f, "attack", 1.4333f, "plantfood_on", 1.1f, "plantfood", 0.4333f, "plantfood_off", 0.3f, "water", 1.7f);
        register(table, "SPEARMINT", "768/INITIAL/EMPOWERMINTS/PLANT/SPEARMINT/SPEARMINT.PAM", "intro", 1.4667f, "loop", 1.3667f, "outro", 2.0333f);
        register(table, "SPLITPEA", "768/FULL/PLANT/SPLITPEA/SPLITPEA.PAM", "idle", 1.0f, "attack", 0.9667f, "attack2", 1.0f, "attack3", 1.0f, "plantfood", 0.4667f, "water", 2.1f);
        register(table, "SQUASH", "768/INITIAL/PLANT/SQUASH/SQUASH.PAM", "idle", 1.9667f, "turn", 1.2f, "size_up", 1.2f, "jump_up_right", 0.8f, "jump_up_left", 0.8f, "jump_down_right", 0.8f, "jump_down_left", 0.8f, "plantfood_jump_down_right", 0.7333f, "plantfood_jump_down_left", 0.7f, "water", 2.3667f);
        register(table, "STARFRUIT", "768/INITIAL/PLANT/STARFRUIT/STARFRUIT.PAM", "idle", 1.0333f, "idle2", 1.0f, "attack", 1.0f, "plantfood_on", 0.4f, "plantfood", 0.3333f, "plantfood_off", 0.3f, "water", 2.0333f);
        register(table, "SUNBEAN", "768/FULL/PLANT/SUNBEAN/SUNBEAN.PAM", "idle", 3.4f, "idle2", 3.4f, "plantfood_on", 1.0333f, "plantfood", 1.3333f, "water", 2.7667f);
        register(table, "SUNFLOWER", "768/INITIAL/PLANT/SUNFLOWER/SUNFLOWER.PAM", "idle", 2.0f, "special", 2.0f, "plantfood_on", 0.5f, "plantfood", 0.5f, "plantfood_off", 0.5f, "water", 2.1667f);
        register(table, "SUNFLOWER_TWIN", "768/INITIAL/PLANT/SUNFLOWER_TWIN/SUNFLOWER_TWIN.PAM", "idle", 2.0f, "special", 1.5f, "plantfood_on", 0.5f, "plantfood", 0.5f, "plantfood_off", 0.6667f, "water", 2.0f);
        register(table, "SUNSHROOM", "768/FULL/PLANT/SUNSHROOM/SUNSHROOM.PAM", "idle_stage1", 0.6667f, "idle2_stage1", 0.6667f, "special_stage1", 1.8333f, "plantfood_stage1", 2.2f, "growth_stage1", 1.3333f, "idle_stage2", 1.3333f, "idle2_stage2", 1.3333f, "special_stage2", 1.8333f, "plantfood_stage2", 2.2f, "growth_stage2", 1.3333f, "idle_stage3", 1.7f, "idle2_stage3", 1.7f, "special_stage3", 1.9333f, "plantfood_stage3", 2.5333f, "water", 2.9333f);
        register(table, "SWEETPOTATO", "768/INITIAL/PLANT/SWEETPOTATO/SWEETPOTATO.PAM", "idle", 2.0f, "idle2", 2.0f, "idle_damage", 2.0f, "idle2_damage", 2.0f, "idle_damage2", 2.0f, "idle2_damage2", 2.0f, "idle_damage3", 2.0f, "idle2_damage3", 2.0f, "plantfood", 1.2333f, "water", 2.8f);
        register(table, "TALLNUT", "768/FULL/PLANT/TALLNUT/TALLNUT.PAM", "idle", 10.0f, "damage", 10.0f, "damage2", 10.0f, "water", 2.2f);
        register(table, "TANGLEKELP", "768/FULL/PLANT/TANGLEKELP/TANGLEKELP.PAM", "idle", 1.7333f, "idle2", 1.7333f, "idle3", 1.7333f, "attack_submerge", 2.1333f, "attack", 2.4667f, "attack_emerge", 1.8333f, "plantfood_on", 2.0f, "plantfood", 2.1f, "plantfood_off", 0.6667f, "water", 1.7333f, "zen_idle", 1.7333f);
        register(table, "THREEPEATER", "768/INITIAL/PLANT/THREEPEATER/THREEPEATER.PAM", "idle", 1.0f, "attack", 1.0f, "plantfood", 0.2333f, "water", 1.7f);
        register(table, "TORCHWOOD", "768/INITIAL/PLANT/TORCHWOOD/TORCHWOOD.PAM", "idle", 0.6667f, "idle2", 0.6667f, "plantfood_on", 0.6667f, "plantfood", 0.7f, "plantfood_on_t2", 0.6667f, "plantfood_t2", 0.7f, "explosion", 1.7333f, "water", 3.0f);
        register(table, "WALLNUT", "768/INITIAL/PLANT/WALLNUT/WALLNUT.PAM", "idle", 9.0f, "idle2", 10.0f, "damage", 3.3333f, "damage2", 3.3333f, "damage3", 6.6667f, "plantfood_on", 0.6667f, "plantfood", 0.1667f, "plantfood2", 0.1333f, "plantfood3", 0.2f, "plantfood_off", 0.2667f, "water", 2.0333f);
        register(table, "WASABIWHIP", "768/INITIAL/PLANT/WASABIWHIP/WASABIWHIP.PAM", "idle", 1.3f, "idle2", 1.3f, "idle3", 1.7333f, "attack", 0.6667f, "attack2", 0.5667f, "attack3", 0.8333f, "attack4", 0.6667f, "attack5", 0.6667f, "plantfood_on", 0.3667f, "plantfood", 0.1333f, "plantfood_off", 1.1f, "water", 4.6333f);
        register(table, "WINTERMELON", "768/FULL/PLANT/WINTERMELON/WINTERMELON.PAM", "idle", 1.3667f, "attack", 2.1667f, "plantfood", 2.9333f, "water", 1.7667f);
    }

    private static void registerZombies(Map<ZombieType, EntityAnimation> table) {
        registerZombie(table, ZombieType.NORMAL, "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM", "idle", 2.1f, "walk", 3.0f, "eat", 8.6333f, "die", 1.6f, "particles", 0.0333f);
        registerZombie(table, ZombieType.CONE, "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM", "idle", 2.1f, "walk", 3.0f, "eat", 8.6333f, "die", 1.6f, "particles", 0.0333f);
        registerZombie(table, ZombieType.BUCKET, "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM", "idle", 2.1f, "walk", 3.0f, "eat", 8.6333f, "die", 1.6f, "particles", 0.0333f);

        registerZombie(table, ZombieType.BRICK, "768/FULL/ZOMBIE/ZOMBIE_PIRATE_BASIC_BRICK/ZOMBIE_PIRATE_BASIC_BRICK.PAM", "idle", 2.0f, "walk", 2.0f, "eat", 4.1f, "die", 1.8f, "particles", 0.0333f);
        registerZombie(table, ZombieType.DARK_ARMOR, "768/FULL/ZOMBIE/ZOMBIE_DARK_BASIC/ZOMBIE_DARK_BASIC.PAM", "idle", 2.1f, "walk", 3.0f, "eat", 8.6333f, "die", 1.8333f, "particles", 0.0333f);

        registerZombie(table, ZombieType.GARGANTUAR, "768/FULL/ZOMBIE/ZOMBIE_SUMMER_GARGANTUAR/ZOMBIE_SUMMER_GARGANTUAR.PAM", "idle", 2.0333f, "walk", 2.3667f, "eat", 1.2667f, "smash_left", 1.7667f, "fire", 0.9667f, "cannon_fire", 0.5667f, "die", 2.6333f, "particles", 0.0333f);
        registerZombie(table, ZombieType.IMP, "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL_IMP/ZOMBIE_TUTORIAL_IMP.PAM", "idle", 2.0f, "walk", 2.0f, "eat", 4.2667f, "die", 1.2f, "particles", 0.0333f, "land", 1.0f, "fly", 0.0333f);
        registerZombie(table, ZombieType.ALL_STAR, "768/FULL/ZOMBIE/ZOMBIE_MODERN_ALLSTAR/ZOMBIE_MODERN_ALLSTAR.PAM", "idle", 3.3333f, "walk", 3.0f, "eat", 8.6333f, "run", 0.6667f, "tackle", 1.3f, "kick", 1.6f, "die", 1.8333f, "particles", 0.0333f);
        registerZombie(table, ZombieType.ARCADE, "768/FULL/ZOMBIE/ZOMBIE_80S_ARCADE/ZOMBIE_80S_ARCADE.PAM", "idle", 3.3333f, "walk", 4.0333f, "eat", 8.4667f, "push", 4.0333f, "die", 3.5f, "particles", 0.0333f);
        registerZombie(table, ZombieType.JANE, "768/FULL/ZOMBIE/ZOMBIE_LOSTCITY_JANE/ZOMBIE_LOSTCITY_JANE.PAM", "idle", 3.3333f, "walk", 3.0f, "eat", 8.6333f, "die", 1.8333f, "particles", 0.0333f);
        registerZombie(table, ZombieType.CRYSTAL_SKULL, "768/FULL/ZOMBIE/ZOMBIE_LOSTCITY_CRYSTALSKULL/ZOMBIE_LOSTCITY_CRYSTALSKULL.PAM", "idle", 3.3333f, "walk", 4.0f, "eat", 3.3333f, "power_up", 0.6667f, "power", 1.0f, "power_down", 1.2667f, "attack", 1.9667f, "die", 2.1667f, "particles", 0.0333f);
        registerZombie(table, ZombieType.PROSPECTOR, "768/FULL/ZOMBIE/ZOMBIE_PROSPECTOR/ZOMBIE_PROSPECTOR.PAM", "idle", 2.3667f, "walk", 1.9667f, "eat", 6.8f, "die", 2.2667f, "particles", 0.0333f, "blastoff", 0.2667f, "fly", 0.7f, "land", 0.3333f);
        registerZombie(table, ZombieType.PIANIST, "768/FULL/ZOMBIE/PIANO/PIANO.PAM", "idle", 2.0f, "play", 1.0f, "damage", 1.0f, "play2", 1.0f, "die", 3.0f, "particles", 0.0333f);
        registerZombie(table, ZombieType.NEWSPAPER, "768/FULL/ZOMBIE/ZOMBIE_MODERN_NEWSPAPER/ZOMBIE_MODERN_NEWSPAPER.PAM", "idle_newspaper", 2.1333f, "walk_newspaper", 3.0f, "walk", 3.0f, "eat_newspaper", 8.6333f, "newspaper_defeat", 1.4f, "eat", 8.6333f, "die", 1.8333f, "particles", 0.0333f);
        registerZombie(table, ZombieType.BARREL_ROLLER, "768/FULL/ZOMBIE/ZOMBIE_PIRATE_BARREL_PUSHER/ZOMBIE_PIRATE_BARREL_PUSHER.PAM", "walk", 2.9667f, "idle", 3.6f, "eat", 4.1f, "die", 2.0f, "particles", 0.0333f, "walk2", 2.9667f, "idle2", 2.2667f, "eat2", 4.1f, "die2", 2.0f);

        //Egypt
        registerZombie(table, ZombieType.RA, "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_RA/ZOMBIE_EGYPT_RA.PAM", "idle", 2.8667f, "walk", 4.0f, "eat", 3.3333f, "power_up", 0.6667f, "power", 1.0f, "power_down", 1.2667f, "die", 2.1667f, "particles", 0.0333f);
        registerZombie(table, ZombieType.EXPLORER, "768/INITIAL/ZOMBIE/ZOMBIE_EXPLORER/ZOMBIE_EXPLORER.PAM", "idle", 2.1333f, "walk", 2.0f, "eat", 6.3333f, "die", 3.7333f, "particles", 0.0333f);
        registerZombie(table, ZombieType.TOMB_RAISER, "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_TOMBRAISER/ZOMBIE_EGYPT_TOMBRAISER.PAM", "idle", 2.0f, "walk", 2.0f, "power", 3.0f, "eat", 4.1f, "die", 1.8f, "particles", 0.0333f);

        //Frozen Caves
        registerZombie(table, ZombieType.DODO, "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_DODORIDER/ZOMBIE_ICEAGE_DODORIDER.PAM", "idle", 1.6667f, "idle2", 1.6667f, "idle3", 2.8667f, "walk", 3.0f, "eat", 3.3667f, "fly_start", 0.9667f, "fly_loop", 2.6667f, "fly_end", 1.5f, "die", 5.7f);
        registerZombie(table, ZombieType.HUNTER, "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_HUNTER/ZOMBIE_ICEAGE_HUNTER.PAM", "idle", 2.1f, "walk", 3.0f, "eat", 4.0333f, "die", 2.1667f, "throw", 2.1f, "particles", 0.0333f);
        registerZombie(table, ZombieType.TROGLOBITE, "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_TROGLOBITE/ZOMBIE_ICEAGE_TROGLOBITE.PAM", "idle", 3.3333f, "walk", 4.0333f, "eat", 8.4667f, "push", 4.0333f, "die", 3.5f, "particles", 0.0333f);

        //Big Wave Beach
        registerZombie(table, ZombieType.FISHERMAN, "768/FULL/ZOMBIE/ZOMBIE_BEACH_FISHERMAN/ZOMBIE_BEACH_FISHERMAN.PAM", "intro", 1.6333f, "idle", 2.1f, "cast", 1.2667f, "cast_loop", 0.0333f, "reel", 1.4667f, "toss", 2.4333f, "die", 3.4f, "die2", 7.8f, "particles", 0.0333f);
        registerZombie(table, ZombieType.OCTOPUS, "768/FULL/ZOMBIE/ZOMBIE_BEACH_OCTOPUS/ZOMBIE_BEACH_OCTOPUS.PAM", "idle", 3.3333f, "idle2", 3.3333f, "idle3", 3.3333f, "idle4", 3.3333f, "idle5", 3.3333f, "walk", 4.0333f, "toss", 3.0667f, "eat", 8.4667f, "die", 3.5f, "particles", 0.0333f);
        registerZombie(table, ZombieType.SNORKEL, "768/FULL/ZOMBIE/ZOMBIE_BEACH_SNORKELER/ZOMBIE_BEACH_SNORKELER.PAM", "idle", 2.3667f, "walk", 1.9667f, "eat", 6.8f, "die", 2.2667f, "particles", 0.0333f);

        //Dark Ages
        registerZombie(table, ZombieType.JUGGLER, "768/FULL/ZOMBIE/ZOMBIE_DARK_JESTER/ZOMBIE_DARK_JESTER.PAM", "idle", 2.0333f, "walk", 2.0333f, "spinup", 0.8667f, "spin", 1.2f, "spindown", 0.5f, "spin_walk", 1.4667f, "eat", 8.3667f, "die", 1.8333f, "particles", 0.0333f);
        registerZombie(table, ZombieType.WIZARD, "768/FULL/ZOMBIE/ZOMBIE_DARK_WIZARD/ZOMBIE_DARK_WIZARD.PAM", "idle", 2.8667f, "walk", 3.0f, "eat", 8.6333f, "sheep", 2.3f, "die", 2.1667f, "particles", 0.0333f);
        registerZombie(table, ZombieType.KING, "768/FULL/ZOMBIE/ZOMBIE_DARK_KING/ZOMBIE_DARK_KING.PAM", "intro", 3.2333f, "idle", 4.0f, "idle2", 2.9333f, "special", 4.0f, "die", 3.1667f, "particles", 0.0333f);
        registerZombie(table, ZombieType.IMP_DRAGON, "768/FULL/ZOMBIE/ZOMBIE_DARK_IMP_DRAGON/ZOMBIE_DARK_IMP_DRAGON.PAM", "idle", 2.0f, "walk", 2.0f, "eat", 4.2667f, "die", 1.2f, "particles", 0.0333f, "land", 1.0f, "fly", 0.0333f, "transition", 1.4f);
    }
    private static void registerMowers(Map<String, EntityAnimation> table) {
        register(table, "MOWER_BEACH", "768/FULL/MOWERS/MOWER_BEACH/MOWER_BEACH.PAM",
            "idle", 0.33f,
            "transition", 0.33f,
            "attack", 0.37f);
        register(table, "MOWER_DARK", "768/FULL/MOWERS/MOWER_DARK/MOWER_DARK.PAM",
            "idle", 0.30f,
            "transition", 0.37f,
            "attack", 0.60f);
        register(table, "MOWER_EGYPT", "768/INITIAL/MOWERS/MOWER_EGYPT/MOWER_EGYPT.PAM",
            "idle", 0.3f,
            "transition", 0.27f,
            "attack", 0.4f);
        register(table, "MOWER_ICEAGE", "768/FULL/MOWERS/MOWER_ICEAGE/MOWER_ICEAGE.PAM",
            "idle", 2.0f,
            "transition", 0.40f,
            "attack", 0.37f);
        register(table, "MOWER_WILDWEST", "768/FULL/MOWERS/MOWER_WILDWEST/MOWER_WILDWEST.PAM",
            "idle", 0.3f,
            "transition", 0.3f,
            "attack", 0.4f);
    }

    private static void register(Map<String, EntityAnimation> table, String name, String path, Object... clipPairs) {
        Map<String, Float> clips = new LinkedHashMap<>();
        for (int i = 0; i < clipPairs.length; i += 2) {
            clips.put((String) clipPairs[i], (Float) clipPairs[i + 1]);
        }
        table.put(name, new EntityAnimation(name, path, clips));
    }

    private static void registerSuns(Map<SunType, EntityAnimation> table) {
        String defaultSunPath = "768/FULL/EFFECTS/SUN/SUN.PAM";

        registerSun(table, SunType.NORMAL_SUN, defaultSunPath, "animation", 1.0f, "transition_red", 0.5333f, "red", 1.0f);
        registerSun(table, SunType.TINY_SUN, defaultSunPath, "animation", 1.0f, "transition_red", 0.5333f, "red", 1.0f);
        registerSun(table, SunType.LARGE_SUN, defaultSunPath, "animation", 1.0f, "transition_red", 0.5333f, "red", 1.0f);
        registerSun(table, SunType.HUGE_SUN, defaultSunPath, "animation", 1.0f, "transition_red", 0.5333f, "red", 1.0f);
        registerSun(table, SunType.SPECIAL_SUN, defaultSunPath, "animation", 1.0f, "transition_red", 0.5333f, "red", 1.0f);

        registerSun(table, SunType.RADIOACTIVE_SUN, defaultSunPath, "animation", 1.0f);
    }

    private static void registerSun(Map<SunType, EntityAnimation> table, SunType type, String path, Object... clipPairs) {
        if (type == null) return;
        Map<String, Float> clips = new LinkedHashMap<>();
        for (int i = 0; i < clipPairs.length; i += 2) {
            clips.put((String) clipPairs[i], (Float) clipPairs[i + 1]);
        }
        table.put(type, new EntityAnimation(type.name(), path, clips));
    }

    public static EntityAnimation getSunAnimation(SunType type) {
        return type == null ? null : SUNS.get(type);
    }

    private static void registerZombie(Map<ZombieType, EntityAnimation> table, ZombieType type, String path, Object... clipPairs) {
        if (type == null) return;
        Map<String, Float> clips = new LinkedHashMap<>();
        for (int i = 0; i < clipPairs.length; i += 2) {
            clips.put((String) clipPairs[i], (Float) clipPairs[i + 1]);
        }
        table.put(type, new EntityAnimation(type.name(), path, clips));
    }

    public static EntityAnimation getPlantAnimation(String exactName) {
        return exactName == null ? null : PLANTS.get(exactName.toUpperCase());
    }

    public static EntityAnimation getPlantAnimation(Plant plant) {
        return getPlantAnimation(UiFactory.getAtlasName(plant));
    }

    public static boolean hasPlantAnimation(String exactName) {
        return getPlantAnimation(exactName) != null;
    }

    public static EntityAnimation getZombieAnimation(ZombieType type) {
        return type == null ? null : ZOMBIES.get(type);
    }

    public static EntityAnimation getZombieAnimation(Zombie zombie) {
        if (zombie == null) return null;
        return getZombieAnimation(zombie.getType());
    }

    public static EntityAnimation getMowerAnimation(String animationName) {
        return animationName == null ? null : MOWERS.get(animationName.toUpperCase());
    }

    public static boolean hasZombieAnimation(ZombieType type) {
        return getZombieAnimation(type) != null;
    }

    public static Collection<EntityAnimation> allPlantAnimations() {
        return Collections.unmodifiableCollection(PLANTS.values());
    }

    public static Collection<EntityAnimation> allZombieAnimations() {
        return Collections.unmodifiableCollection(ZOMBIES.values());
    }
}
