package cd.arnett.advent.destinations.box;

import cd.arnett.advent.Advent;
import cd.arnett.advent.destinations.TeleportDestination;
import cd.arnett.advent.destinations.spot.SpotDestination;
import com.destroystokyo.paper.ParticleBuilder;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Random;

public class BoxDestination extends TeleportDestination {

    public double xScale;
    public double yScale;
    public double zScale;


    //region Initialization

    /*=================================================================================================
                        -  Initialization  -
    =================================================================================================*/

    public TeleportDestination initialize(NamespacedKey eventId, String tag, World residingWorld, double x, double y, double z, double zScale, double yScale, double xScale) {
        this.zScale = zScale;
        this.yScale = yScale;
        this.xScale = xScale;
        return super.initialize(eventId, tag, residingWorld, x, y, z);
    }

    @Override
    public TeleportDestination initialize(NamespacedKey eventId, String tag, World residingWorld, double x, double y, double z) {
        return new SpotDestination().initialize(eventId, tag, residingWorld, x, y, z);
    }


    //endregion


    //region Get

    /*=================================================================================================
                        -  Get  -
    =================================================================================================*/

    @Override
    public Location getDestination() {

        Random rand = new Random();

        double randX = x + rand.nextDouble(-.5d, .5d) * xScale;
        double randY = y + rand.nextDouble(-.5d, .5d) * yScale;
        double randZ = z + rand.nextDouble(-.5d, .5d) * zScale;

        return new Location(residingWorld, randX, randY, randZ);
    }

    @Override
    public double getVolume() {
        return xScale * yScale * zScale;
    }


    @Override
    public String getDisplayInfo() {
        return String.format("Box - X %.2f, Y %.2f, Z %.2f, V %.2f", x, y, z, getVolume());
    }

    @Override
    public NamespacedKey getTypeId() {
        return new NamespacedKey(Advent.singleton, "box");
    }

    //endregion


    //region Yaml

    /*=================================================================================================
                        -  Yaml  -
    =================================================================================================*/


    @Override
    @OverridingMethodsMustInvokeSuper
    public YamlConfiguration writeToYaml() {
        var yaml = super.writeToYaml();

        yaml.set("xscale", xScale);
        yaml.set("yscale", yScale);
        yaml.set("zscale", zScale);

        return yaml;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void readFromYaml(YamlConfiguration yaml) {
        super.readFromYaml(yaml);

        xScale = yaml.getDouble("xscale");
        yScale = yaml.getDouble("yscale");
        zScale = yaml.getDouble("zscale");
    }


    //endregion


    //region other

    /*=================================================================================================
                        -  other  -
    =================================================================================================*/


    @Override
    public void sendDisplayParticles(Player... players) {
        new ParticleBuilder(Particle.DUST)
                .location(new Location(residingWorld, x, y, z))
                .count((int)getVolume() * 2)
                .offset(xScale/2, yScale/2, zScale/2)
                .color(Color.RED)
                .receivers(players)
                .spawn();
    }

    /**
     * Check for whether a specific location is within a provided spot
     * @param spot spot to check
     * @return true if the spot is within this destination's bounds
     */
    @Override
    public boolean isWithinBounds(Location spot) {
        return  spot.getX() > x - xScale/2 && spot.getX() < x + xScale/2 &&
                spot.getY() > y - yScale/2 && spot.getY() < y + yScale/2 &&
                spot.getZ() > z - zScale/2 && spot.getZ() < z + zScale/2;
    }


    //endregion

}
