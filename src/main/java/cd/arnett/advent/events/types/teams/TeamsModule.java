package cd.arnett.advent.events.types.teams;

import cd.arnett.advent.events.annotations.OnInitialize;
import cd.arnett.advent.events.annotations.data.PlayerIdArrayList;
import cd.arnett.advent.events.types.EventType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface TeamsModule extends EventType {

    List<EventTeamDefinition> teamDefinitions();

    //region Initialization

    /*=================================================================================================
                        -  Initialization  -
    =================================================================================================*/

    @OnInitialize
    default void teamRegistration()
    {
        var teams = teamDefinitions();

        teams.forEach(team -> {
            getEvent().registerAutomatedData(
                    PlayerIdArrayList.class,
                    team.name() + "TeamPlayers",
                    new ArrayList<UUID>()
            );
        });

    }


    //endregion


}
