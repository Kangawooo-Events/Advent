package cd.arnett.advent.destinations.box;

import cd.arnett.advent.Advent;
import cd.arnett.advent.destinations.TeleportDestination;
import cd.arnett.advent.destinations.box.command.CreateBoxDestinationCommand;
import cd.arnett.advent.destinations.spot.SpotDestination;
import cd.arnett.caddamands.cattamands.cattamand.Cattamand;
import com.destroystokyo.paper.ParticleBuilder;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
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

    public TeleportDestination initialize(NamespacedKey eventId, String tag, Location spot, double zScale, double yScale, double xScale) {
        this.zScale = zScale;
        this.yScale = yScale;
        this.xScale = xScale;
        return super.initialize(eventId, tag, spot);
    }

    @Override
    public TeleportDestination initialize(NamespacedKey eventId, String tag, Location spot) {
        return new SpotDestination().initialize(eventId, tag, spot);
    }


    //endregion


    //region Get

    /*=================================================================================================
                        -  Get  -
    =================================================================================================*/

    @Override
    public Location getDestination() {

        Random rand = new Random();

        double randX = location.getX() + rand.nextDouble(-.5d, .5d) * xScale;
        double randY = location.getY() + rand.nextDouble(-.5d, .5d) * yScale;
        double randZ = location.getZ() + rand.nextDouble(-.5d, .5d) * zScale;

        return new Location(location.getWorld(), randX, randY, randZ);
    }

    @Override
    public double getVolume() {
        return xScale * yScale * zScale;
    }


    @Override
    public String getDisplayInfo() {
        return String.format("Box - X %.2f, Y %.2f, Z %.2f, V %.2f",
                location.getX(), location.getY(), location.getZ(), getVolume());
    }

    @Override
    public Cattamand creationCommand() {
        return new CreateBoxDestinationCommand();
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
    public TeleportDestination readFromYaml(ConfigurationSection yaml) {

        xScale = yaml.getDouble("xscale");
        yScale = yaml.getDouble("yscale");
        zScale = yaml.getDouble("zscale");
        return super.readFromYaml(yaml);
    }


    //endregion


    //region other

    /*=================================================================================================
                        -  other  -
    =================================================================================================*/


    @Override
    public void sendDisplayParticles(Player... players) {
        for (int i = 0; i < Math.min((int)getVolume() * 2, 50); i++) {
            new ParticleBuilder(Particle.DUST)
                    .location(getDestination())
                    .count(1)
                    .color(color)
                    .receivers(players)
                    .extra(0)
                    .spawn();
        }

        //region Corner Particles

        //----------= Corner Particles =-----------------------------------------------------------------------------

        var cornerParticle = new ParticleBuilder(Particle.DUST)
                .location(location)
                .count(5)
                .color(color)
                .extra(0)
                .receivers(players);

        double halfXScale = xScale/2;
        double halfYScale = yScale/2;
        double halfZScale = zScale/2;

        cornerParticle.location(getCenterWithOffset(-halfXScale, -halfYScale, -halfZScale)).spawn();
        cornerParticle.location(getCenterWithOffset(-halfXScale, -halfYScale, halfZScale)).spawn();
        cornerParticle.location(getCenterWithOffset(-halfXScale, halfYScale, -halfZScale)).spawn();
        cornerParticle.location(getCenterWithOffset(-halfXScale, halfYScale, halfZScale)).spawn();
        cornerParticle.location(getCenterWithOffset(halfXScale, -halfYScale, -halfZScale)).spawn();
        cornerParticle.location(getCenterWithOffset(halfXScale, -halfYScale, halfZScale)).spawn();
        cornerParticle.location(getCenterWithOffset(halfXScale, halfYScale, -halfZScale)).spawn();
        cornerParticle.location(getCenterWithOffset(halfXScale, halfYScale, halfZScale)).spawn();

        //endregion
    }

    /**
     * Check for whether a specific location is within a provided spot
     * @param spot spot to check
     * @return true if the spot is within this destination's bounds
     */
    @Override
    public boolean isWithinBounds(Location spot) {
        return  spot.getX() > location.getX() - xScale/2 && spot.getX() < location.getX() + xScale/2 &&
                spot.getY() > location.getY() - yScale/2 && spot.getY() < location.getY() + yScale/2 &&
                spot.getZ() > location.getZ() - zScale/2 && spot.getZ() < location.getZ() + zScale/2;
    }


    //endregion

}
