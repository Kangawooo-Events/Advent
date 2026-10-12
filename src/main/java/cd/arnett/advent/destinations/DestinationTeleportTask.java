package cd.arnett.advent.destinations;

import cd.arnett.advent.Advent;
import cd.arnett.advent.DestinationManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks capacity data for teleportation destinations to enable continued teleportation of players without
 * compromising the capacity limit
 */
public class DestinationTeleportTask {

    NamespacedKey eventId;
    String tag;

    ConcurrentHashMap<TeleportDestination, Integer> modifiableDestinationCapacities = new ConcurrentHashMap<>();
    ArrayList<TeleportDestination> uncappedDestinations = new ArrayList<>();
    AtomicInteger totalCapacity = new AtomicInteger(0);


    public DestinationTeleportTask(NamespacedKey eventId, String tag)
    {
        this.eventId = eventId;
        this.tag = tag;
        reloadDestinations();
    }

    public DestinationTeleportTask(ConfigurationSection config)
    {
        eventId = NamespacedKey.fromString(config.getString("eventId"));
        tag = config.getString("tag");
        var capacitySection = config.getConfigurationSection("capacity-map");

        AtomicInteger total = new AtomicInteger(0);
        var capacities = capacitySection.getValues(false);

        DestinationManager.getDestinations(eventId, tag).forEach((destination) -> {

            UUID id = destination.getId();

            if(capacities.containsKey(id.toString()))
            {
                int cap = (int)capacities.getOrDefault(id, -1);

                if(destination.capacity < 0)
                {
                    uncappedDestinations.add(destination);
                }
                else
                {
                    total.addAndGet(cap);
                    modifiableDestinationCapacities.put(destination, cap);
                }
            }
            else
            {
                int cap = destination.getCapacity();
                if(cap < 0)
                {
                    uncappedDestinations.add(destination);
                }
                else
                {
                    total.addAndGet(cap);
                    modifiableDestinationCapacities.put(destination, cap);
                }
            }

        });
    }


    public ConfigurationSection writeToYaml()
    {
        var config = new YamlConfiguration();

        config.set("eventId", eventId.toString());
        config.set("tag", tag);

        var capacitySection = config.createSection("capacity-map");
        getCapacityMap().forEach((id, cap) -> {
            capacitySection.set(id.toString(), cap);
        });

        return config;
    }

    /**
     * Refreshes the destinations stored and clears tracked capacities
     */
    public void reloadDestinations()
    {
        modifiableDestinationCapacities.clear();
        uncappedDestinations.clear();

        var destinations = DestinationManager.getDestinations(eventId, tag);

        //did not find destinations
        if(destinations == null)
            return;

        //calculate total available slots
        for(var destination : destinations)
        {
            int capacity = destination.getCapacity();

            if(capacity <= 0)
            {
                uncappedDestinations.add(destination);
                continue;
            }

            modifiableDestinationCapacities.put(destination, capacity);
            totalCapacity.addAndGet(capacity);
        }
    }

    /**
     * Teleports players asynchronously
     * @param entities Entities to teleport
     */
    public List<CompletableFuture<Boolean>> teleportAsync(Entity... entities)
    {
        ArrayList<CompletableFuture<Boolean>> teleportFutures = new ArrayList<>();

        for (Entity entity : entities) {
            teleportFutures.add(teleportAsync(entity));
        }

        return teleportFutures;
    }

    public CompletableFuture<Boolean> teleportAsync(Entity entity)
    {
        return entity.teleportAsync(popTeleportDestination(), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    /**
     * Pops a teleport destination from capacities while taking up a capacity slot
     * @return Teleport location
     */
    public Location popTeleportDestination()
    {
        int rollSize = Math.max(totalCapacity.get(), 1);

        //batch out teleports
        var rand = new Random();
        int roll = rand.nextInt(rollSize);

        for(var entry : modifiableDestinationCapacities.entrySet()) {
            roll -= entry.getValue();
            if(roll <= 0)
            {
                //teleport and decrement capacity
                var dest = entry.getKey().getSafeDestination();
                modifiableDestinationCapacities.put(entry.getKey(), entry.getValue() - 1);
                totalCapacity.decrementAndGet();
                return dest;
            }
        }

        //roll greater than capacity
        if(!uncappedDestinations.isEmpty())
        {
            var dest = uncappedDestinations.get(
                    rand.nextInt(uncappedDestinations.size())
            ).getSafeDestination();
            return dest;
        }
        else
        {
            //this is only true if the loop can't find a spot
            if(modifiableDestinationCapacities.isEmpty())
            {
                Advent.logger.warning("No " + tag + " destination has been set in " + eventId + " to teleport to");
                var future = new CompletableFuture<Boolean>();
                future.complete(false);
                return null;
            }

            Advent.logger.warning("No uncapped destination to send overflow of players, Sending to capped destination");
            var dest = modifiableDestinationCapacities.keySet().iterator().next().getDestination();
            return dest;
        }
    }

    /**
     * Gets a teleport destination from capacities
     * @return Teleport location
     */
    public Location getTeleportDestination()
    {
        int rollSize = Math.max(totalCapacity.get(), 1);

        //batch out teleports
        var rand = new Random();
        int roll = rand.nextInt(rollSize);

        for(var entry : modifiableDestinationCapacities.entrySet()) {
            roll -= entry.getValue();
            if(roll <= 0)
            {
                //teleport and decrement capacity
                var dest = entry.getKey().getSafeDestination();
                return dest;
            }
        }

        //roll greater than capacity
        if(!uncappedDestinations.isEmpty())
        {
            var dest = uncappedDestinations.get(
                    rand.nextInt(uncappedDestinations.size())
            ).getSafeDestination();
            return dest;
        }
        else
        {
            //this is only true if the loop can't find a spot
            if(modifiableDestinationCapacities.isEmpty())
            {
                Advent.logger.warning("No " + tag + " destination has been set in " + eventId + " to teleport to");
                var future = new CompletableFuture<Boolean>();
                future.complete(false);
                return null;
            }

            Advent.logger.warning("No uncapped destination to send overflow of players, Sending to capped destination");
            var dest = modifiableDestinationCapacities.keySet().iterator().next().getDestination();
            return dest;
        }
    }

    /**
     * @return number of destinations
     */
    public int getDestinationCount()
    {
        AtomicInteger total = new AtomicInteger();
        modifiableDestinationCapacities.forEach(
                (teleportDestination, integer) -> total.getAndIncrement()
        );
        uncappedDestinations.forEach(
                (teleportDestination) -> total.getAndIncrement()
        );
        return total.get();
    }

    /**
     * @return A map of the IDs of the teleport destinations and their capacities
     */
    public HashMap<UUID, Integer> getCapacityMap()
    {
        HashMap<UUID, Integer> map = new HashMap<>();

        modifiableDestinationCapacities.forEach((teleportDestination, capRemaining) ->
                map.put(teleportDestination.getId(), capRemaining)
        );
        uncappedDestinations.forEach((teleportDestination) ->
                map.put(teleportDestination.getId(), -1)
        );

        return map;
    }

    public NamespacedKey getEventId() {
        return eventId;
    }

    public String getTag() {
        return tag;
    }

    public int getTotalCapacity() {
        return totalCapacity.get();
    }
}
