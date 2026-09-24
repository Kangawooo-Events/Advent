package cd.arnett.advent.destinations;

import cd.arnett.advent.Advent;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public abstract class TeleportDestination {

    protected Location location;
    protected NamespacedKey eventId;
    protected String tag;
    protected Color color = Color.RED;
    protected int capacity = -1;


    //region Initialization

    /*=================================================================================================
                        -  Initialization  -
    =================================================================================================*/


    public TeleportDestination initialize(NamespacedKey eventId, String tag, Location spot) {
        this.eventId = eventId;
        this.tag = tag;
        this.location = spot;
        return this;
    }


    public TeleportDestination initialize(YamlConfiguration yaml) {
        readFromYaml(yaml);
        return this;
    }

    //endregion


    //region Optional Properties

    /*=================================================================================================
                        -  Optional Properties  -
    =================================================================================================*/


    /**
     * Sets the color particles will display as
     * @param color Color to set to
     * @return this object
     */
    public TeleportDestination setColor(Color color)
    {
        this.color = color;
        return this;
    }

    /**
     * Sets the max number of players who can teleport to this location
     * @param capacity Capacity to set to
     * @return this object
     */
    public TeleportDestination setCapacity(int capacity)
    {
        this.capacity = capacity;
        return this;
    }


    //endregion


    //region Getters

    /*=================================================================================================
                        -  Getters  -
    =================================================================================================*/


    public Location getLocation() {
        return location;
    }

    public NamespacedKey getEventId() {
        return eventId;
    }

    public String getTag() {
        return tag;
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


    /**
     * @return a copy of center location with an offset specified
     */
    public Location getCenterWithOffset(double x, double y, double z)
    {
        return new Location(location.getWorld(), location.getX() - x, location.getY() - y, location.getZ() - z);
    }

    /**
     * @return capacity of this destination
     */
    public int getCapacity()
    {
        return capacity;
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

    /**
     * Determines whether a spot is within the bounds of the destination (ideally with a bit of padding for thin one)
     * @param spot Spot to check
     * @return Whether the spot is in bounds
     */
    public abstract boolean isWithinBounds(Location spot);

    /**
     * @return String which describes this destination
     */
    public abstract String getDisplayInfo();

    /**
     * @return The Creation command for this destination type.<br>
     * Arguments available from previous segments in the command tree are:<br>
     *     - NamespacedKey "event", the event Id specified for this destination <br>
     *     - String "tag", the tag specified for this destination <br>
     *     - BlockPosition "position", the position specified for this destination
     *         which takes the BlockPosition to resolve <br>
     *
     */
    public abstract Cattamand creationCommand();

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
        yaml.set("location", location);
        yaml.set("eventid", eventId.toString());
        yaml.set("tag", tag);
        yaml.set("color", color);
        yaml.set("capacity", capacity);

        return yaml;
    }

    /**
     * Reads data in from Yaml
     */
    @OverridingMethodsMustInvokeSuper
    public TeleportDestination readFromYaml(ConfigurationSection yaml)
    {
        try {
            eventId = NamespacedKey.fromString(yaml.getString("eventid"));
            tag = yaml.getString("tag");
            location = yaml.getLocation("location");
            color = yaml.getColor("color");
            capacity = yaml.getInt("capacity");
        }
        catch (Exception e)
        {
            Advent.logger.warning("UNABLE TO READ IN DESTINATION");
            e.printStackTrace();
        }
        return this;
    }

    /**
     * @return Type of
     */
    public abstract NamespacedKey getTypeId();

    //endregion

}
