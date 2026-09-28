package cd.arnett.advent.events.modules.playerState;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class PlayerState {

    ItemStack[] content = new ItemStack[0];
    HashMap<Attribute, Pair<Double, Collection<AttributeModifier>>> attributes = new HashMap<>();
    double health;
    double absorption;
    double saturation;
    double exp;
    int level;
    int saturationRegen;
    int starvationRate;
    int air;
    int maxAir;
    int fireTicks;
    int freezeTicks;
    Location respawn;
    Collection<PotionEffect> effects = List.of();

    public PlayerState()
    {
        health = 20;
        absorption = 0;
        saturation = 5;
        saturationRegen = 10;
        starvationRate = 80;
        exp = 0;
        level = 0;
        maxAir = 300;
        air = maxAir;
        fireTicks = 0;
        freezeTicks = 0;
        respawn = null;
        effects = List.of();
        content = new ItemStack[0];
    }

    public PlayerState(Player player) {

        //read attributes
        for (Attribute value : Attribute.values()) {
            var attribute = player.getAttribute(value);
            if(attribute == null) continue;
            attributes.put(attribute.getAttribute(), Pair.of(attribute.getBaseValue(), attribute.getModifiers()));
        }


        //read current stats
        effects = player.getActivePotionEffects();
        health = player.getHealth();
        air = player.getRemainingAir();
        maxAir = player.getMaximumAir();
        saturation = player.getSaturation();
        saturationRegen = player.getSaturatedRegenRate();
        starvationRate = player.getStarvationRate();
        exp = player.getExp();
        absorption = player.getAbsorptionAmount();
        fireTicks = player.getFireTicks();
        freezeTicks = player.getFreezeTicks();
        level = player.getLevel();
        content = player.getInventory().getContents();
        respawn = player.getRespawnLocation();
    }

    public PlayerState(ConfigurationSection config)
    {
        health = config.getDouble("health");
        absorption = config.getDouble("absorption");
        saturation = config.getDouble("food");
        saturationRegen = config.getInt("saturationRegen");
        starvationRate = config.getInt("starvationRate");
        exp = config.getDouble("exp");
        level = config.getInt("level");
        air = config.getInt("air");
        maxAir = config.getInt("maxAir");
        fireTicks = config.getInt("fireTicks");
        freezeTicks = config.getInt("freezeTicks");
        respawn = config.getLocation("respawn");
        effects = (List<PotionEffect>)config.getList("effects");
        content = config.getList("content").toArray(ItemStack[]::new);

        for (Attribute attribute : Attribute.values()) {
            String name = attribute.name();
            var section = config.getConfigurationSection(name);
            if(section != null)
            {
                //we have data stored for this section
                attributes.put(
                        attribute,
                        Pair.of(
                                section.getDouble(".base-value"),
                                (List<AttributeModifier>)section.getList("modifier-value")
                        )
                );
            }
        }
    }

    public void applyToPlayer(Player applicant)
    {
        //add attributes
        for (Attribute attribute : Attribute.values()) {
            var saved = attributes.get(attribute);

            AttributeInstance instance = applicant.getAttribute(attribute);

            if(instance == null)
                continue;

            if(saved == null)
            {
                switch (attribute)
                {
                    case Attribute.GENERIC_MOVEMENT_SPEED -> applicant.setWalkSpeed(0.2f);
                    case Attribute.GENERIC_FLYING_SPEED -> instance.setBaseValue(0.1f);
                    default -> instance.setBaseValue(instance.getDefaultValue());
                }
                continue;
            }

            instance.setBaseValue(saved.getLeft());
            instance.getModifiers().forEach(instance::removeModifier);
            var list = saved.getRight();
            if(list != null)
                list.forEach(instance::addModifier);
        }

        //reset potion effects
        applicant.getActivePotionEffects().forEach(effect -> applicant.removePotionEffect(effect.getType()));

        //add potion effects
        effects.forEach(applicant::addPotionEffect);

        applicant.setHealth(health);
        applicant.setSaturatedRegenRate(saturationRegen);
        applicant.setStarvationRate(starvationRate);
        applicant.setAbsorptionAmount(absorption);
        applicant.setSaturation((float)saturation);
        applicant.setLevel(level);
        applicant.setExp((float)exp);
        applicant.setMaximumAir(maxAir);
        applicant.setRemainingAir(air);
        applicant.setFireTicks(fireTicks);
        applicant.setFreezeTicks(freezeTicks);
        applicant.getInventory().setContents(content);

        if(respawn == null)
        {
            applicant.setRespawnLocation(null);
        }
        else
        {
            BlockData respawnBlockData = respawn.getBlock().getBlockData();
            if(respawnBlockData instanceof Bed ||
                    respawnBlockData instanceof RespawnAnchor anchor && anchor.getCharges() > 0)
                applicant.setRespawnLocation(respawn);
            else
                applicant.setRespawnLocation(null);
        }


        applicant.getPersistentDataContainer().set(
                new NamespacedKey("session", "session"),
                PersistentDataType.STRING,
                UUID.randomUUID().toString()
        );
    }

    public ConfigurationSection writeToYaml()
    {
        YamlConfiguration config = new YamlConfiguration();

        config.set("air", air);
        config.set("maxAir", maxAir);
        config.set("health", health);
        config.set("exp", exp);
        config.set("food", saturation);
        config.set("absorption", absorption);
        config.set("fireTicks", fireTicks);
        config.set("freezeTicks", freezeTicks);
        config.set("saturationRegen", saturationRegen);
        config.set("starvationRate", starvationRate);
        config.set("level", level);
        config.set("respawn", respawn);
        config.set("effects", new ArrayList<>(effects));
        config.set("content", Arrays.asList(content));

        attributes.forEach((attribute, pair) -> {
            config.set(
                    attribute.name() + ".base-value", pair.getLeft()
            );

            config.set(
                    attribute.name() + ".modifier-value", pair.getRight()
            );
        });

        return config;
    }

    //region Get/Set Properties

    /*=================================================================================================
                        -  Get/Set Properties  -
    =================================================================================================*/

    public ItemStack[] getContent() {
        return content;
    }

    public PlayerState setContent(ItemStack[] content) {
        this.content = content;
        return this;
    }

    public HashMap<Attribute, Pair<Double, Collection<AttributeModifier>>> getAttributes() {
        return attributes;
    }

    public PlayerState setAttributes(HashMap<Attribute, Pair<Double, Collection<AttributeModifier>>> attributes) {
        this.attributes = attributes;
        return this;
    }

    public double getHealth() {
        return health;
    }

    public PlayerState setHealth(double health) {
        this.health = health;
        return this;
    }

    public double getAbsorption() {
        return absorption;
    }

    public PlayerState setAbsorption(double absorption) {
        this.absorption = absorption;
        return this;
    }

    public double getSaturation() {
        return saturation;
    }

    public PlayerState setSaturation(double saturation) {
        this.saturation = saturation;
        return this;
    }

    public double getExp() {
        return exp;
    }

    public PlayerState setExp(double exp) {
        this.exp = exp;
        return this;
    }

    public int getLevel() {
        return level;
    }

    public PlayerState setLevel(int level) {
        this.level = level;
        return this;
    }

    public int getSaturationRegen() {
        return saturationRegen;
    }

    public PlayerState setSaturationRegen(int saturationRegen) {
        this.saturationRegen = saturationRegen;
        return this;
    }

    public int getStarvationRate() {
        return starvationRate;
    }

    public PlayerState setStarvationRate(int starvationRate) {
        this.starvationRate = starvationRate;
        return this;
    }

    public int getAir() {
        return air;
    }

    public PlayerState setAir(int air) {
        this.air = air;
        return this;
    }

    public int getMaxAir() {
        return maxAir;
    }

    public PlayerState setMaxAir(int maxAir) {
        this.maxAir = maxAir;
        return this;
    }

    public int getFireTicks() {
        return fireTicks;
    }

    public PlayerState setFireTicks(int fireTicks) {
        this.fireTicks = fireTicks;
        return this;
    }

    public int getFreezeTicks() {
        return freezeTicks;
    }

    public PlayerState setFreezeTicks(int freezeTicks) {
        this.freezeTicks = freezeTicks;
        return this;
    }

    public Location getRespawn() {
        return respawn;
    }

    public PlayerState setRespawn(Location respawn) {
        this.respawn = respawn;
        return this;
    }

    public Collection<PotionEffect> getEffects() {
        return effects;
    }

    public PlayerState setEffects(Collection<PotionEffect> effects) {
        this.effects = effects;
        return this;
    }


    //endregion
}
