package cd.arnett.advent.events.types.teams;

import cd.arnett.advent.events.annotations.OnInitialize;
import cd.arnett.advent.events.annotations.data.HashSetModuleData;
import cd.arnett.advent.events.annotations.data.UUIDModuleData;
import cd.arnett.advent.events.types.EventType;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
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
                    new HashSetModuleData()
                    {
                        @Override
                        public Class<? extends Annotation> annotationType() {
                            return HashSetModuleData.class;
                        }

                        @Override
                        public Class<? extends Annotation> value() {
                            return UUIDModuleData.class;
                        }
                    },
                    team.name() + "TeamPlayers",
                    new HashSet<UUID>()
            );
        });

    }


    //endregion


}
