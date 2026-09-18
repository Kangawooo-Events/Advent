package cd.arnett.advent.destinations;

import cd.arnett.advent.Advent;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public abstract class TeleportDestination {

    protected World residingWorld;
    protected NamespacedKey eventId;
    protected String tag;

    protected double x;
    protected double y;
    protected double z;


    //region Initialization

    /*=================================================================================================
                        -  Initialization  -
    =================================================================================================*/


    public TeleportDestination initialize(NamespacedKey eventId, String tag, World residingWorld, double x, double y, double z) {
        this.eventId = eventId;
        this.tag = tag;
        this.residingWorld = residingWorld;
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }


    public TeleportDestination initialize(YamlConfiguration yaml) {
        readFromYaml(yaml);
        return this;
    }

    //endregion


    //region Getters

    /*=================================================================================================
                        -  Getters  -
    =================================================================================================*/


    public World getResidingWorld() {
        return residingWorld;
    }

    public NamespacedKey getEventId() {
        return eventId;
    }

    public String getTag() {
        return tag;
    }
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }


    /**
     * @return A location in this object's bounds which is safe to teleport a survival player to. Will check 20 random
     * spots, if not found a safe one will just teleport in unsafe spot
     */
    public Location getSafeDestination()
    {
        for (int i = 0; i < 20; i++) {
            Location destination = getDestination();

            if(safeLocation(destination))
                return destination;
        }

        return getDestination();
    }

    /**
     * @return A number of locations in this object's bounds which is safe to teleport a survival player to
     * @param amount Amount of locations to get
     */
    public ArrayList<Location> getSafeDestinations(int amount) {
        ArrayList<Location> locations = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            locations.add(getSafeDestination());
        }

        return locations;
    }

    /**
     * @return A location in this object's bounds
     */
    public abstract Location getDestination();

    /**
     * @return A number of locations in this object's bounds
     * @param amount Amount of locations to get
     */
    public ArrayList<Location> getDestinations(int amount) {
        ArrayList<Location> locations = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            locations.add(getDestination());
        }

        return locations;
    }

    //endregion


    //region Safety

    /*=================================================================================================
                        -  Safety  -
    =================================================================================================*/

    /**
     * Returns whether a location is safe to teleport to
     * @param spot Location to check (checks below and above)
     * @return true if this is safe to teleport to
     */
    public boolean safeLocation(Location spot)
    {
        Block feetSpace = spot.getBlock();
        Block groundBlock = feetSpace.getRelative(0, -1, 0);
        Block headSpace = feetSpace.getRelative(0, 1, 0);

        //check that feet and head can pass
        return (groundBlock.isSolid() && isSafelyPassable(feetSpace) && isSafelyPassable(headSpace));
    }

    /**
     * Checks if a block is safe to walk though
     */
    boolean isSafelyPassable(Block block)
    {
        Material type = block.getType();

        switch (type)
        {
            case LAVA -> {return false;}
            case WATER -> {return false;}
            case FIRE -> {return false;}
            case SOUL_FIRE -> {return false;}
            case WITHER_ROSE -> {return false;}
        }

        return true;
    }


    //endregion


    //region Abstract methods

    /*=================================================================================================
                        -  Abstract methods  -
    =================================================================================================*/

    /**
     * @return Volume of the shape
     */
    public abstract double getVolume();

    /**
     * Sends particle effects displaying the volume of the shape to players
     */
    public abstract void sendDisplayParticles(Player... players);

    public abstract boolean isWithinBounds(Location spot);

    public abstract String getDisplayInfo();

    //endregion


    //region Yaml

    /*=================================================================================================
                        -  Yaml  -
    =================================================================================================*/

    /**
     * Saves to yaml
     */
    @OverridingMethodsMustInvokeSuper
    public YamlConfiguration writeToYaml() {
        var yaml = new YamlConfiguration();

        yaml.set("type", getTypeId().toString());
        yaml.set("x", x);
        yaml.set("y", y);
        yaml.set("z", z);
        yaml.set("eventid", eventId.toString());
        yaml.set("tag", tag);
        yaml.set("world", residingWorld.toString());

        return yaml;
    }

    /**
     * Reads data in from Yaml
     */
    @OverridingMethodsMustInvokeSuper
    public void readFromYaml(YamlConfiguration yaml)
    {
        try {
            x = yaml.getDouble("x");
            y = yaml.getDouble("y");
            z = yaml.getDouble("z");
            eventId = NamespacedKey.fromString(yaml.getString("eventid"));
            tag = yaml.getString("tag");
            residingWorld = Bukkit.getWorld(yaml.getString("world"));
        }
        catch (Exception e)
        {
            Advent.logger.warning("UNABLE TO READ IN DESTINATION");
            e.printStackTrace();
        }
    }

    /**
     * @return Type of
     */
    public abstract NamespacedKey getTypeId();

    //endregion

}
