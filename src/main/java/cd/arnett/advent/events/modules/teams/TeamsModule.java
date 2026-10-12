package cd.arnett.advent.events.modules.teams;

import cd.arnett.advent.events.annotations.ModuleDataProvider;
import cd.arnett.advent.events.annotations.OnInitialize;
import cd.arnett.advent.events.annotations.data.HashSetModuleData;
import cd.arnett.advent.events.annotations.data.UUIDModuleData;
import cd.arnett.advent.events.modules.EventModule;
import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.entity.Player;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public interface TeamsModule extends EventModule {

    String teamNameSuffix =  "-TeamPlayers";



    //region Module Data

    /*=================================================================================================
                        -  Module Data  -
    =================================================================================================*/

    @OnInitialize
    default void teamRegistration()
    {
        var teams = teamDefinitions();

        teams.forEach(team -> {
            getEvent().registerAutomatedData(
                    new HashSetModuleData()
                    {
                        @Override
                        public Class<? extends Annotation> type() {
                            return UUIDModuleData.class;
                        }

                        @Override
                        public Class<? extends Annotation> annotationType() {
                            return HashSetModuleData.class;
                        }

                    },
                    new ModuleDataProvider() {

                        @Override
                        public Class<? extends Annotation> annotationType() {
                            return ModuleDataProvider.class;
                        }

                        @Override
                        public String key() {
                            return team.name() + teamNameSuffix;
                        }

                        @Override
                        public String fileBasename() {
                            return "Teams";
                        }
                    },
                    new HashSet<UUID>()
            );
        });

    }

    /**
     * Returns the team list for the specified team name
     */
    default HashSet<UUID> getTeamPlayers(String teamName)
    {
        return getModuleData(teamName + teamNameSuffix);
    }

    //endregion


    //region Abstract Properties
    
    /*=================================================================================================
                        -  Abstract Properties  -
    =================================================================================================*/

    /**
     * @return List of teams
     */
    List<EventTeamDefinition> teamDefinitions();
    
    
    //endregion
    
    
    //region Queries
    
    /*=================================================================================================
                        -  Queries  -
    =================================================================================================*/

    /**
     * Checks whether a player is in a team
     * @param teamName Team to check
     * @param id Player ID to check
     * @return Whether they are in that team
     */
    default boolean isInTeam(String teamName, UUID id)
    {
        return getTeamPlayers(teamName).contains(id);
    }
    
    
    //region Overloads
    
    //----------= Overloads =-----------------------------------------------------------------------------

    /**
     * Checks whether a player is in a team
     * @param teamName Team to check
     * @param profile Player profile to check
     * @return Whether they are in that team
     */
    default boolean isInTeam(String teamName, PlayerProfile profile)
    {
        return isInTeam(teamName, profile.getId());
    }
    
    /**
     * Checks whether a player is in a team
     * @param teamName Team to check
     * @param player Player to check
     * @return Whether they are in that team
     */
    default boolean isInTeam(String teamName, Player player)
    {
        return isInTeam(teamName, player.getPlayerProfile());
    }

    //endregion
    
    //endregion
    
    
    //region Info
    
    /*=================================================================================================
                        -  Info  -
    =================================================================================================*/
    
    
    
    
    
    //endregion


    //region Controls

    /*=================================================================================================
                        -  Controls  -
    =================================================================================================*/





    //endregion
}
