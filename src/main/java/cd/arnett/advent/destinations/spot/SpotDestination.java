package cd.arnett.advent.destinations.spot;

import cd.arnett.advent.Advent;
import cd.arnett.advent.destinations.TeleportDestination;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.*;
import org.bukkit.entity.Player;

public class SpotDestination extends TeleportDestination {

    //region Get

    /*=================================================================================================
                        -  Get  -
    =================================================================================================*/


    @Override
    public Location getDestination() {
        return location;
    }

    @Override
    public double getVolume() {
        return 1;
    }


    @Override
    public String getDisplayInfo() {
        return String.format("Spot - X %.2f, Y %.2f, Z %.2f",
                location.getX(), location.getY(), location.getZ());
    }

    @Override
    public NamespacedKey getTypeId() {
        return new NamespacedKey(Advent.singleton, "spot");
    }

    //endregion


    //region Other

    /*=================================================================================================
                        -  Other  -
    =================================================================================================*/


    @Override
    public void sendDisplayParticles(Player... players) {
        new ParticleBuilder(Particle.DUST)
                .location(location)
                .count(5)
                .color(Color.RED)
                .receivers(players)
                .spawn();
    }

    @Override
    public boolean isWithinBounds(Location spot) {
        return spot.distance(location) < 1;
    }


    //endregion

}
