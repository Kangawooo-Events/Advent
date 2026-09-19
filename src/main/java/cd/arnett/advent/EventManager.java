package cd.arnett.advent;

import cd.arnett.advent.events.PlayerEventStatus;
import cd.arnett.advent.events.EventBase;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Handles everything to do with the state of events themselves
 */
public class EventManager {

    /**
     * Tracks the active events, the players in them and their status. here for faster lookup of player -> events
     * and events -> players without having to have it in separate locations
     */
    static HashMap<NamespacedKey, EventBase> registeredEvents = new HashMap<>();


    //region Get Data

    /*=================================================================================================
                        -  Get Data  -
    =================================================================================================*/


    /**
     * Provides a copy of the event column for this player, a hashmap of the events and the player's status in them
     * @param playerId Player to get events of
     * @return copy of Map of events the player is related to
     */
    public static Set<EventBase> getPlayerEvents(UUID playerId)
    {
        return registeredEvents.values().stream().filter(
                eventBase -> eventBase.isRegistered(playerId)
        ).collect(Collectors.toSet());
    }


    /**
     * Provides a copy of the player row for this event, a hashmap of the players and the player's status in them
     * @param eventKey Event key to get data of
     * @return copy of map of players and their status in the event
     */
    public static HashMap<UUID, PlayerEventStatus> getEventPlayerStatusMap(NamespacedKey eventKey)
    {
        return registeredEvents.get(eventKey).getPlayerStatusMap();
    }

    /**
     * Provides a copy of the players for this event
     * @param eventKey Event key to get data of
     * @return copy of set of players
     */
    public static HashSet<UUID> getRegisteredEventPlayers(NamespacedKey eventKey)
    {
        return registeredEvents.get(eventKey).getPlayers();
    }


    /**
     * @return Provides a list of all the active events
     */
    public static List<NamespacedKey> getEventNames()
    {
        return List.copyOf(registeredEvents.values().stream().map(EventBase::eventKey).toList());
    }

    /**
     * @return Returns an event by key, NULL if not registered
     */
    public static EventBase getEvent(NamespacedKey key)
    {
        for (var event : registeredEvents.values())
        {
            if(event.eventKey().equals(key))
                return event;
        }

        return null;
    }

    /**
     * @return All events registered
     */
    public static List<EventBase> getEvents()
    {
        return List.copyOf(registeredEvents.values());
    }

    //region Overloads

    //----------= Overloads =-----------------------------------------------------------------------------

    /**
     * Provides a list of the events a player is related to by the provided status filter
     * @param playerId Player to get events of
     * @return Copy of Map of events the player is related to
     */
    public Set<EventBase> getPlayerEvents(UUID playerId, PlayerEventStatus... filter)
    {
        return registeredEvents.values().stream().filter(
                eventBase -> eventBase.isRegistered(playerId, filter)
        ).collect(Collectors.toSet());
    }

    //endregion

    //endregion



    //region Registration

    /*=================================================================================================
                        -  Registration  -
    =================================================================================================*/

    /**
     * Registers an event and loads saved yaml from it
     * @param event  event to register
     * @param listeners Listeners to register along with it, DO NOT ALSO REGISTER IN ON ENABLE
     */
    public static void registerEvent(EventBase event, Listener... listeners) {

        NamespacedKey eventKey = event.eventKey();
        registeredEvents.put(eventKey, event);

        Advent.logger.info("Reading Event Data for: " + eventKey);

        //get the path to the Menus directory
        Path eventDataPath = Advent.singleton.getDataFolder()
                .toPath()
                .resolve("Events")
                .resolve(eventKey.getNamespace())
                .resolve(eventKey.getKey())
                .resolve("event.yaml");

        //no data to read
        if(!Files.exists(eventDataPath))
        {
            return;
        }

        //read existing data
        else
        {
            event.readFromYaml(YamlConfiguration.loadConfiguration(eventDataPath.toFile()));
        }

        //register listeners;
        for (Listener listener : listeners)
        {
            Advent.registerListener(listener);
        }
    }


    //endregion


    //region Player Registration

    /*=================================================================================================
                        -  Player Registration  -
    =================================================================================================*/


    /**
     * Enables a player to be able to join an event
     * @param eventKey Key of event to register to
     * @param id Id of player to register
     */
    public static void registerForEvent(NamespacedKey eventKey, UUID id)
    {
        registeredEvents.get(eventKey).register(id);
    }

    /**
     * Revokes all registration from event for given player
     * @param id Id of player to revoke
     */
    public static void revokeRegistrations(UUID id)
    {
        getPlayerEvents(id).forEach(event -> event.revokeRegistration(id));
    }

    /**
     * Revokes a players registration from an event
     * @param eventKey Key of event to revoke registration from
     * @param id Id of player to revoke
     */
    public static void revokeRegistration(NamespacedKey eventKey, UUID id)
    {
        registeredEvents.get(eventKey).register(id);
    }





    //region Overrides

    //----------= Overrides =-----------------------------------------------------------------------------


    /**
     * Enables a player to be able to join an event
     * @param eventKey Key of event to register to
     * @param player Player to register
     */
    public static void registerForEvent(NamespacedKey eventKey, OfflinePlayer player)
    {
        registerForEvent(eventKey, player.getUniqueId());
    }

    /**
     * Revokes a players registration from an event
     * @param eventKey Key of event to revoke registration from
     * @param player Player to revoke
     */
    public static void revokeRegistration(NamespacedKey eventKey, OfflinePlayer player)
    {
        revokeRegistration(eventKey, player.getUniqueId());
    }

    /**
     * Revokes all registration from event for given player
     * @param player Player to revoke
     */
    public static void revokeRegistrations(OfflinePlayer player)
    {
        revokeRegistrations(player.getUniqueId());
    }

    //endregion

    //endregion


    //region Controlling Events

    /*=================================================================================================
                        -  Controlling Events  -
    =================================================================================================*/


    public static void startEvent()
    {

    }


    //endregion


    //region YAML

    /*=================================================================================================
                        -  YAML  -
    =================================================================================================*/

    /**
     * Saves data to plugin data file
     */
    public void writeData()
    {
        CompletableFuture<Void> saveFuture = new CompletableFuture<>();

        saveFuture.thenRunAsync(() -> {
            Advent.logger.info("Saving Events");

            //get the path of the data folder
            File dataFolder = Advent.singleton.getDataFolder();
            Path dataFolderPath = dataFolder.toPath();

            //get the path to the Menus directory
            Path destFolderPath = dataFolderPath.resolve("Events");

            //make sure that menus directory exists
            if(!Files.exists(destFolderPath))
            {
                try {
                    Files.createDirectories(destFolderPath);
                } catch (IOException e) {
                    Advent.logger.warning("Unable to save events!");
                    throw new RuntimeException(e);
                }
            }

            //write each row to its own folder yaml
            registeredEvents.values().forEach((event) -> {
                NamespacedKey eventKey = event.eventKey();
                Path eventFilePath = destFolderPath.resolve(eventKey.getNamespace())
                                                    .resolve(eventKey.getKey())
                                                    .resolve("event.yaml");


                YamlConfiguration yaml = event.writeToYaml();


                //make sure the Yaml file exists
                if(!Files.exists(eventFilePath))
                {
                    //create the file
                    try {
                        Files.createFile(eventFilePath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                //save to file
                try {
                    yaml.save(eventFilePath.toFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    //endregion



}
