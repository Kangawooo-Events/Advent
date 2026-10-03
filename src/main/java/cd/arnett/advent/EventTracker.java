package cd.arnett.advent;

import cd.arnett.advent.events.PlayerEventStatus;
import cd.arnett.advent.events.EventBase;
import cd.arnett.advent.events.types.LobbyEventBase;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Handles everything to do with the state of events themselves
 */
public class EventTracker {

    /**
     * Tracks the active events, the players in them and their status. here for faster lookup of player -> events
     * and events -> players without having to have it in separate locations
     */
    static HashMap<NamespacedKey, EventBase> registeredEvents = new HashMap<>();

    /**
     * Tracks what events players are playing in
     */
    static HashMap<UUID, EventBase> playingEvents = new HashMap<>();
    static EventBase hubEvent = null;
    static EventBase devEvent = null;
    static EventBase buildEvent = null;


    /**
     * Tracks players in the process of joining an event
     */
    public static HashSet<UUID> limboPlayers = new HashSet<>();


    //region Get/Set Data

    /*=================================================================================================
                        -  Get/Set Data  -
    =================================================================================================*/


    /**
     * @param playerId Player to get events of
     * @return List of events the player registered to
     */
    public static List<EventBase> getRegisteredEvents(UUID playerId)
    {
        ArrayList<EventBase> events = new ArrayList<>();

        for(var entry : registeredEvents.values())
        {
            if(entry.isRegistered(playerId)) events.add(entry);
        }

        return events;
    }

    /**
     * Provides a copy of the event column for this player, a hashmap of the events and the player's status in them
     * @param playerId Player to get events of
     * @return Set of events the player is related to
     */
    public static EventBase getPlayingEvent(UUID playerId)
    {
        return playingEvents.get(playerId);
    }

    /**
     * Sets what event a player is playing in, PURELY FOR TRACKING PURPOSES.
     * Calling this function does not actually make them join the event
     * @param playerId player to set as playing the event
     * @param base event that player is playing in
     */
    public static void setPlayingEvent(UUID playerId, EventBase base)
    {
        playingEvents.put(playerId, base);
    }

    /**
     * Clears what event a player is set as playing. This is because when a player is in limbo they should
     * not be set as playing an event
     * @param playerId player
     */
    public static void clearPlayingEvent(UUID playerId)
    {
        playingEvents.remove(playerId);
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
        return registeredEvents.get(eventKey).getRegisteredPlayers();
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


    //endregion



    //region Registration

    /*=================================================================================================
                        -  Registration  -
    =================================================================================================*/

    /**
     * Registers an event and loads saved yaml from it. Event is then loaded later onServerLoad
     * @param event  event to register
     */
    public static void registerEvent(EventBase event) {

        NamespacedKey eventKey = event.eventKey();
        registeredEvents.put(eventKey, event);
    }

    /**
     * Loads registered events separate from registration so other plugins have time to setup worlds and such
     */
    static void loadRegisteredEvents()
    {
        registeredEvents.forEach((eventKey, event) -> {
            //get the path to the Menus directory
            Path eventDataPath = Advent.singleton.getDataFolder()
                    .toPath()
                    .resolve("Events")
                    .resolve(eventKey.getNamespace())
                    .resolve(eventKey.getKey())
                    .resolve("event.yaml");

            //read existing data
            if(Files.exists(eventDataPath))
            {
                event.readFromYaml(YamlConfiguration.loadConfiguration(eventDataPath.toFile()));
            }


            //start event if set to autostart
            if(event.autoOpen())
            {
                event.openEvent();
            }
        });

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
        getRegisteredEvents(id).forEach(event -> event.revokeRegistration(id));
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



    //region Hub

    /*=================================================================================================
                        -  Hub  -
    =================================================================================================*/


    public static EventBase getHubEvent()
    {
        return hubEvent;
    }

    public static void registerHubEvent(LobbyEventBase event)
    {
        registerEvent(event);
        hubEvent = event;
    }


    //endregion


    //region Dev

    /*=================================================================================================
                        -  Dev  -
    =================================================================================================*/


    public static EventBase getDevEvent()
    {
        return devEvent;
    }

    public static void registerDevEvent(LobbyEventBase event)
    {
        registerEvent(event);
        devEvent = event;
    }


    //endregion


    //region Build

    /*=================================================================================================
                        -  Build  -
    =================================================================================================*/


    public static EventBase getBuildEvent()
    {
        return buildEvent;
    }

    public static void registerBuildEvent(EventBase event)
    {
        registerEvent(event);
        buildEvent = event;
    }


    //endregion


    //region YAML

    /*=================================================================================================
                        -  YAML  -
    =================================================================================================*/

    /**
     * Saves data to plugin data file in async
     */
    public static void saveData()
    {
        CompletableFuture<Void> saveFuture = new CompletableFuture<>();

        saveFuture.thenRunAsync(() -> {
            Advent.logger.info("Saving Events");

            //get the path of the data folder
            File dataFolder = Advent.singleton.getDataFolder();
            Path dataFolderPath = dataFolder.toPath();

            //get the path to the Menus directory
            Path eventFolder = dataFolderPath.resolve("Events");

            //write each row to its own folder yaml
            registeredEvents.values().forEach((event) -> {
                NamespacedKey eventKey = event.eventKey();
                Path eventFilePath = eventFolder.resolve(eventKey.getNamespace())
                        .resolve(eventKey.getKey())
                        .resolve("event.yaml");


                YamlConfiguration yaml = event.writeToYaml();


                //make sure the Yaml file exists
                if(!Files.exists(eventFilePath.getParent()))
                {
                    //create the file
                    try {
                        Files.createDirectories(eventFilePath.getParent());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
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

    /**
     * Saves data to plugin data file in
     */
    public static void saveDataNow()
    {
        Advent.logger.info("Saving Events");

        //get the path of the data folder
        File dataFolder = Advent.singleton.getDataFolder();
        Path dataFolderPath = dataFolder.toPath();

        //get the path to the Menus directory
        Path eventFolder = dataFolderPath.resolve("Events");

        //write each row to its own folder yaml
        registeredEvents.values().forEach((event) -> {
            NamespacedKey eventKey = event.eventKey();
            Path eventFilePath = eventFolder.resolve(eventKey.getNamespace())
                    .resolve(eventKey.getKey())
                    .resolve("event.yaml");


            YamlConfiguration yaml = event.writeToYaml();


            //make sure the Yaml file exists
            if(!Files.exists(eventFilePath.getParent()))
            {
                //create the file
                try {
                    Files.createDirectories(eventFilePath.getParent());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
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
    }

    /**
     * Saves data for a single event
     * @param event Event to save for
     */
    public static void saveSingleEvent(EventBase event)
    {
        //get the path of the data folder
        File dataFolder = Advent.singleton.getDataFolder();
        Path dataFolderPath = dataFolder.toPath();

        //get the path to the Menus directory
        NamespacedKey eventKey = event.eventKey();
        Path eventFolderPath = dataFolderPath.resolve("Events")
                .resolve(eventKey.getNamespace())
                .resolve(eventKey.getKey());

        //make sure the directory exists
        if(!Files.exists(eventFolderPath))
        {
            //create the file
            try {
                Files.createDirectories(eventFolderPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        YamlConfiguration yaml = event.writeToYaml();

        Path eventFilePath = eventFolderPath.resolve("event.yaml");

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
    }

    //endregion


}
