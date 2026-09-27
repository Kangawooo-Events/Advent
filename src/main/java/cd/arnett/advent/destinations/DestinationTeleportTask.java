package cd.arnett.advent.destinations;

import cd.arnett.advent.Advent;
import cd.arnett.advent.DestinationManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks capacity data for teleportation destinations to enable continued teleportation of players without
 * compromising the capacity limit
 */
public class DestinationTeleportTask {

    NamespacedKey eventId;
    String tag;

    HashMap<TeleportDestination, Integer> modifiableDestinationCapacities = new HashMap<>();
    ArrayList<TeleportDestination> uncappedDestinations = new ArrayList<>();
    int totalCapacity = 0;


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
                int cap = (int)capacities.get(id);

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
            totalCapacity += capacity;
        }
    }

    /**
     * Teleports players asynchronously
     * @param entities Entities to teleport
     */
    public void teleportAsync(Entity... entities)
    {
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger rollSize = new AtomicInteger(Math.max(totalCapacity, entities.length));
        //batch out teleports
        Bukkit.getScheduler().runTaskTimerAsynchronously(Advent.singleton, (task) -> {
            try {

                var rand = new Random();

                for (int i = 0; i < 10 && (counter.get() < entities.length); i++) {
                    Entity entity = entities[counter.get()];
                    counter.getAndIncrement();

                    int roll = rand.nextInt(rollSize.get());

                    for(var entry : modifiableDestinationCapacities.entrySet()) {
                        roll -= entry.getValue();
                        if(roll < 0)
                        {
                            //teleport and decrement capacity
                            entity.teleportAsync(entry.getKey().getSafeDestination());
                            modifiableDestinationCapacities.put(entry.getKey(), entry.getValue() - 1);
                            totalCapacity--;
                            break;
                        }
                    }

                    //if we found a destination then don't go to uncapped
                    if(roll < 0)
                    {
                        return;
                    }

                    //roll greater than capacity
                    if(!uncappedDestinations.isEmpty())
                    {
                        entity.teleportAsync(
                                uncappedDestinations.get(
                                        rand.nextInt(uncappedDestinations.size())
                                ).getSafeDestination()
                        );
                    }
                    else
                    {

                        if(modifiableDestinationCapacities.isEmpty())
                        {
                            Advent.logger.warning("NOWHERE TO TELEPORT PLAYERS, Like at all :(");
                            return;
                        }

                        Advent.logger.warning("NOWHERE TO TELEPORT PLAYERS, Sending to capped destination");
                        entity.teleportAsync(
                                modifiableDestinationCapacities.keySet().iterator().next().getDestination()
                        );
                    }
                    rollSize.getAndDecrement();
                }

                if(counter.get() >= entities.length)
                    task.cancel();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }, 0, 1);

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
        return totalCapacity;
    }
}
