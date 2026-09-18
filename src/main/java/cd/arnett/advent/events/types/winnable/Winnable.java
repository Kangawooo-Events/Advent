package cd.arnett.advent.events.types.winnable;

import java.util.UUID;

public interface Winnable {

    void win(UUID... winners);

    //region Events

    /*=================================================================================================
                        -  Events  -
    =================================================================================================*/


    void onWin(UUID... winners);


    //endregion
}
