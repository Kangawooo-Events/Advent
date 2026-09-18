package cd.arnett.advent;

import cd.arnett.advent.destinations.TeleportDestination;
import com.google.common.collect.HashBasedTable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Manages everything to do with the teleporting players to/from events
 */
public class TeleportationManager {

    /**
     * Map of all the teleport destinations for each event
     */
    static HashBasedTable<NamespacedKey, String, ArrayList<TeleportDestination>> eventDestinations = HashBasedTable.create();

    static HashMap<NamespacedKey, Class<? extends TeleportDestination>> destinationTypes = new HashMap<>();

    static Set<UUID> playersInDevView = new HashSet<>();
    static BukkitTask displayTask;


    //region Modify Data

    /*=================================================================================================
                        -  Modify Data  -
    =================================================================================================*/

    /**
     * Registers a destination to the Event Destinations table
     * @param destination Destination being registered
     */
    public static void addDestination(TeleportDestination destination)
    {
        //add to list or create one if not there
        eventDestinations.column(destination.getTag()).computeIfAbsent(
                destination.getEventId(),
                (k -> new ArrayList<>())
        ).add(destination);
    }

    /**
     * Removes the specified destinations from the Event Destinations table
     * @param destination Destination being removed
     */
    public static void removeDestination(TeleportDestination destination)
    {
        //add to list or create one if not there
        eventDestinations.row(destination.getEventId()).forEach((tag, destinations)
                -> destinations.removeIf(dest -> dest == destination));
    }

    /**
     * Removes all teleport destinations by location and tags sent
     * @param spot Spot to remove at
     * @param eventId Event Id of destinations we are removing
     * @param tag Tag of destinations we are removing
     * @return Display info String from the destination removed
     */
    public static String removeDestinations(Location spot, NamespacedKey eventId, String tag)
    {
        var destinations = eventDestinations.get(eventId, tag);

        TeleportDestination min = null;
        double minVolume = Double.MAX_VALUE;

        //if there are no destinations for this category
        if(destinations == null || destinations.isEmpty())
            return "No Destinations to Destroy";


        for(var teleportDestination : destinations)
        {
            if(teleportDestination.isWithinBounds(spot))
            {
                if(min == null || minVolume > teleportDestination.getVolume())
                {
                    min = teleportDestination;
                    minVolume = teleportDestination.getVolume();
                }
            }
        }

        //if we didn't find a destination we were in
        if(min == null)
            return "No Destinations to Destroy";

        destinations.remove(min);
        return min.getDisplayInfo();
    }

    /**
     * Gives a list copy of Teleport destinations by both a namespaced keys and tags
     * @param eventId Event Id of destinations we are getting
     * @param tag Tag of destinations we are getting
     * @return
     */
    public static List<TeleportDestination> getDestinations(NamespacedKey eventId, String tag)
    {
        return eventDestinations.get(eventId, tag);
    }


    /**
     * Gives a map copy of Teleport destinations by both a namespaced keys and tags
     * @param eventId Event Id of destinations we are getting
     * @return
     */
    public static Map<String, ArrayList<TeleportDestination>> getDestinations(NamespacedKey eventId)
    {
        return Map.copyOf(eventDestinations.row(eventId));
    }


    //endregion


    //region Teleportation

    /*=================================================================================================
                        -  Teleportation  -
    =================================================================================================*/


    /**
     * Teleports the provided players to random destinations under the given event and tags
     * @param eventID Event destinations to search
     * @param destinationTag Tag of the destinations we want
     * @param players players to be teleported
     * @return True if destinations were found, Teleports *may still fail but there were targets to teleport to
     */
    public static boolean teleportToDestination(NamespacedKey eventID, String destinationTag, Player... players)
    {
        var destinations = eventDestinations.get(eventID, destinationTag);

        //did not find destinations
        if(destinations == null)
            return false;

        AtomicInteger counter = new AtomicInteger(0);

        //batch out teleports
        Bukkit.getScheduler().runTaskTimerAsynchronously(Advent.singleton, () -> {
            var rand = new Random();

            for (int i = 0; i < 10 && (counter.incrementAndGet() < players.length); i++) {
                Player player = players[counter.get()];

                player.teleportAsync(
                        destinations.get(
                                rand.nextInt(destinations.size())
                        ).getSafeDestination()
                );
            }
        }, 0, 1);

        //found destinations
        return true;
    }



    //endregion


    //region Displaying

    /*=================================================================================================
                        -  Displaying  -
    =================================================================================================*/


    /**
     * Starts the display task used to show admins the area teleport destinations cover
     */
    public static void startDisplayTask()
    {
        if(displayTask != null)
            return;

        displayTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Advent.singleton, () -> {
            eventDestinations.values().forEach(
                    list -> list.forEach(TeleportDestination::sendDisplayParticles)
            );
        }, 0, 10);
    }

    /**
     * Adds a player to dev view so they can see teleport locations
     * @param player player to add
     */
    public static void addToDevView(UUID player)
    {
        playersInDevView.add(player);
    }

    /**
     * Removes a player to dev view so they can't see teleport locations
     * @param player player to add
     */
    public static void removeFromDevView(UUID player)
    {
        playersInDevView.remove(player);
    }


    //endregion


    //region Registration

    /*=================================================================================================
                        -  Registration  -
    =================================================================================================*/


    public static void registerType(Class<? extends TeleportDestination> type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        TeleportDestination dest = type.getConstructor().newInstance();
        destinationTypes.put(dest.getTypeId(), type);
    }


    //endregion


    //region Read / Write

    /*=================================================================================================
                        -  Read / Write  -
    =================================================================================================*/


    /**
     * Saves data to file
     */
    public static void writeData() throws IOException {

        var saveFuture = new CompletableFuture<Void>();

        //save in async
        saveFuture.thenRunAsync(() -> {
            Advent.logger.info("Saving Destinations");

            //get the path of the data folder
            File dataFolder = Advent.singleton.getDataFolder();
            Path dataFolderPath = dataFolder.toPath();

            //get the path to the Menus directory
            Path destFolderPath = dataFolderPath.resolve("Destinations");

            //make sure that menus directory exists
            if(!Files.exists(destFolderPath))
            {
                try {
                    Files.createDirectories(destFolderPath);
                } catch (IOException e) {
                    Advent.logger.warning("Unable to save destinations!");
                    throw new RuntimeException(e);
                }
            }

            eventDestinations.rowMap().forEach((row, values) -> {
                Path destinationFolderPath = destFolderPath.resolve(row.getNamespace()).resolve(row.getKey());

                //go though each entry and create its folder + contents
                values.forEach((tag, dests) -> {

                    //get the paths and yaml setup
                    Path tagPath = destinationFolderPath.resolve(tag).resolve("destinations.yaml");
                    YamlConfiguration yaml = new YamlConfiguration();

                    //add each entry
                    for (int i = 0; i < dests.size(); i++) {
                        yaml.set(Integer.toString(i), dests.get(i).writeToYaml());
                    }


                    //make sure the Yaml file exists
                    if(!Files.exists(tagPath))
                    {
                        //create the file
                        try {
                            Files.createFile(tagPath);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    //save to file
                    try {
                        yaml.save(tagPath.toFile());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        });
    }


    /**
     * reads in data from file, DO NOT CALL PLEASE THANK YOU
     */
    public static void readData() throws IOException {

        Advent.logger.info("Reading Destinations");

        //get the path of the data folder
        File dataFolder = Advent.singleton.getDataFolder();
        Path dataFolderPath = dataFolder.toPath();

        //get the path to the Menus directory
        Path destFolderPath = dataFolderPath.resolve("Destinations");
        //make sure that menus directory exists
        if(!Files.exists(destFolderPath))
        {
            Files.createDirectories(destFolderPath);
        }

        //go through all destinations.yaml(s) and initialize them
        Files.walk(
                dataFolderPath
        ).filter(
                path -> path.getFileName().equals("destinations.yaml")

        ).forEach(path -> {

            var yaml = YamlConfiguration.loadConfiguration(path.toFile());
            var typeKey = NamespacedKey.fromString(yaml.getString("type"));
            var type = destinationTypes.get(typeKey);

            if(type == null)
            {
                Advent.logger.warning("Unable to read destinations.yaml at " + path + " No registered type: " + typeKey);
                return;
            }

            try {
                type.getConstructor().newInstance().readFromYaml(yaml);
            } catch (Exception e) {
                Advent.logger.warning("Unable to load destinations.yaml at " + path + " No registered type: " + typeKey);
            }
        });
    }

    //endregion
}
