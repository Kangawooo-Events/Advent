package cd.arnett.advent.events.types.seperateInventory;

import cd.arnett.advent.events.annotations.ModuleDataProvider;
import cd.arnett.advent.events.annotations.data.ItemStackArrayModuleData;
import cd.arnett.advent.events.annotations.data.UUIDHashMapModuleData;
import cd.arnett.advent.events.types.EventType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public interface SeparateInventoryModule extends EventType {

    static String previousInventoriesKey = "PlayerInventories";

    //region ModuleData

    /*=================================================================================================
                        -  ModuleData  -
    =================================================================================================*/

    @ModuleDataProvider(key = previousInventoriesKey, fileBasename = "inventories")
    @UUIDHashMapModuleData(type = ItemStackArrayModuleData.class)
    default HashMap<UUID, ItemStack[]> playerInventories()
    {
        return null;
    }


    //endregion

}
