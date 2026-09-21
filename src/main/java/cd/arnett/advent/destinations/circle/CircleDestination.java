package cd.arnett.advent.destinations.circle;

import cd.arnett.advent.Advent;
import cd.arnett.advent.destinations.TeleportDestination;
import cd.arnett.advent.destinations.spot.SpotDestination;
import com.destroystokyo.paper.ParticleBuilder;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class CircleDestination extends TeleportDestination {

    public double radius;
    public boolean fill;


    //region Initalization

    /*=================================================================================================
                        -  Initalization  -
    =================================================================================================*/


    public TeleportDestination initialize(NamespacedKey eventId, String tag, Location spot, double radius, boolean fill) {
        this.fill = fill;
        this.radius = radius;
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

        //code from https://stackoverflow.com/questions/51770447/draw-random-dots-inside-a-circle
        double a = Math.random() * (2 * Math.PI);

        double r;

        if(fill)
        {
            r = radius * Math.sqrt(Math.random());
        }
        else
        {
            r = radius;
        }

        double randX = r * Math.cos(a) + radius;
        double randZ = r * Math.sin(a) + radius;

        return new Location(location.getWorld(), randX, location.y(), randZ);
    }

    @Override
    public double getVolume() {
        return Math.PI * radius * radius;
    }

    @Override
    public String getDisplayInfo() {
        return String.format("Circle - X %.2f, Y %.2f, Z %.2f, V %.2f",
                location.getX(), location.getY(), location.getZ());
    }

    @Override
    public NamespacedKey getTypeId() {
        return new NamespacedKey(Advent.singleton, "circle");
    }

    //endregion


    //region YAML

    /*=================================================================================================
                        -  YAML  -
    =================================================================================================*/


    @Override
    @OverridingMethodsMustInvokeSuper
    public YamlConfiguration writeToYaml() {
        var yaml = super.writeToYaml();

        yaml.set("radius", radius);
        yaml.set("fill", fill);

        return yaml;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void readFromYaml(YamlConfiguration yaml) {
        super.readFromYaml(yaml);

        radius = yaml.getDouble("radius");
        fill = yaml.getBoolean("fill");
    }



    //endregion


    //region Other

    /*=================================================================================================
                        -  Other  -
    =================================================================================================*/


    @Override
    public void sendDisplayParticles(Player... players) {
        for (int i = 0; i < (int)getVolume()*2; i++) {
            new ParticleBuilder(Particle.DUST)
                    .location(getDestination())
                    .count(1)
                    .color(Color.RED)
                    .receivers(players)
                    .spawn();
        }
    }


    @Override
    public boolean isWithinBounds(Location spot) {

        if(spot.getWorld() != location.getWorld())
            return false;

        double diffX = Math.abs(location.getX() - spot.getX());
        double diffY = Math.abs(location.getY() - spot.getY());
        double diffZ = Math.abs(location.getZ() - spot.getZ());

        if(diffY > 1)
            return false;

        double radiusDifference = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffZ, 2)) - radius;

        return (fill && radiusDifference <= 0) || (!fill && radiusDifference <= 0.5 && radiusDifference >= -.5f);
    }


    //endregion

}
