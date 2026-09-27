package cd.arnett.advent.events;

import cd.arnett.advent.destinations.DestinationTeleportTask;
import cd.arnett.advent.events.annotations.data.*;
import cd.arnett.advent.events.modules.playerState.PlayerState;
import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class AutomaticModuleDataManager {

    @FunctionalInterface
    interface QuadConsumer<A, B, C, D> {
        void accept(A a, B b, C c, D d);
    }

    /**
     * Map of writing functions for each tracked annotation type
     */
    public static HashMap<Class<? extends Annotation>, QuadConsumer<Annotation, ConfigurationSection, String, Object>> writingMethods = new HashMap<>();

    /**
     * Map of reading functions for each tracked annotation type
     */
    public static HashMap<Class<? extends Annotation>, TriFunction<Annotation, ConfigurationSection, String, ?>> readingMethods = new HashMap<>();

    static
    {
        //reading
        readingMethods.put(
                IntegerModuleData.class, (ann, yaml, key)
                        -> yaml.getInt(key)
        );
        readingMethods.put(
                BooleanModuleData.class, (ann, yaml, key)
                        -> yaml.getBoolean(key)
        );
        readingMethods.put(
                FloatModuleData.class, (ann, yaml, key)
                        -> (float)yaml.getDouble(key)
        );
        readingMethods.put(
                DoubleModuleData.class, (ann, yaml, key)
                        -> yaml.getDouble(key)
        );
        readingMethods.put(
                StringModuleData.class, (ann, yaml, key)
                        -> yaml.getString(key)
        );
        readingMethods.put(
                LocationModuleData.class, (ann, yaml, key)
                        -> yaml.getLocation(key)
        );
        readingMethods.put(
                UUIDModuleData.class, (ann, yaml, key)
                        -> {
                    String value = yaml.getString(key);
                    if (value == null)
                        return null;

                    return UUID.fromString(value);
                }
        );
        readingMethods.put(
                NamespacedKeyModuleData.class, (ann, yaml, key)
                        -> {
                    String value = yaml.getString(key);
                    if (value == null)
                        return null;

                    return NamespacedKey.fromString(value);
                }
        );
        readingMethods.put(
                ItemStackArrayModuleData.class, (ann, yaml, key)
                        -> {
                    List<ItemStack> content = (List<ItemStack>) yaml.getList(key);
                    if(content == null)
                        return new ItemStack[0];
                    return content.toArray(ItemStack[]::new);
                }
        );
        readingMethods.put(
                DestinationTeleportTaskModuleData.class, (ann, yaml, key)
                        -> new DestinationTeleportTask(yaml.getConfigurationSection(key))
        );
        readingMethods.put(
                PlayerStateModuleData.class, (ann, yaml, key)
                        -> new PlayerState(yaml.getConfigurationSection(key))
        );




        //writing
        writingMethods.put(
                IntegerModuleData.class, (ann, yaml, key, value)
                        -> yaml.set(key, value)
        );
        writingMethods.put(
                BooleanModuleData.class, (ann, yaml, key, value)
                        -> yaml.set(key, value)
        );
        writingMethods.put(
                FloatModuleData.class, (ann, yaml, key, value)
                        -> yaml.set(key, value)
        );
        writingMethods.put(
                DoubleModuleData.class, (ann, yaml, key, value)
                        -> yaml.set(key, value)
        );
        writingMethods.put(
                StringModuleData.class, (ann, yaml, key, value)
                        -> yaml.set(key, value)
        );
        writingMethods.put(
                UUIDModuleData.class, (ann, yaml, key, value)
                        -> yaml.set(key, value.toString())
        );
        writingMethods.put(
                NamespacedKeyModuleData.class, (ann, yaml, key, value)
                        -> yaml.set(key, value.toString())
        );
        writingMethods.put(
                LocationModuleData.class, (ann, yaml, key, value)
                        -> yaml.set(key, value)
        );
        writingMethods.put(
                ItemStackArrayModuleData.class, (ann, yaml, key, value)
                        -> {
                    yaml.set(key, value);
                }
        );
        writingMethods.put(
                DestinationTeleportTaskModuleData.class, (ann, yaml, key, value)
                        -> {
                    var task = (DestinationTeleportTask)value;
                    yaml.set(key, task.writeToYaml());
                }
        );
        writingMethods.put(
                PlayerStateModuleData.class, (ann, yaml, key, value)
                        -> {
                    var task = (PlayerState)value;
                    yaml.set(key, task.writeToYaml());
                }
        );



        //region HashSet

        //----------= HashSet =-----------------------------------------------------------------------------


        readingMethods.put(
                HashSetModuleData.class, (ann, yaml, key)
                        -> {
                    HashSet<Object> set = new HashSet<>();
                    ConfigurationSection section = yaml.getConfigurationSection(key);
                    if(section == null)
                        return null;

                    section.getValues(false).forEach((id, value) -> {
                        set.add(
                                readingMethods.get(
                                        ((HashSetModuleData)ann).type()
                                ).apply(null, section, id)
                        );
                    });
                    return set;
                }
        );

        writingMethods.put(
                HashSetModuleData.class, (ann, yaml, key, value)
                        -> {
                    ConfigurationSection section = yaml.createSection(key);
                    int i = 0;

                    for (Object entry : (HashSet<Object>) value)
                    {
                        writingMethods.get(((HashSetModuleData)ann).type()).accept(
                                null, section, Integer.toString(i), entry
                        );
                        i++;
                    }
                }
        );


        //endregion


        //region UUID HashMap

        //----------= UUID HashMap =-----------------------------------------------------------------------------


        readingMethods.put(
                UUIDHashMapModuleData.class, (ann, yaml, key)
                        -> {
                    HashMap<UUID, Object> map = new HashMap<>();
                    ConfigurationSection section = yaml.getConfigurationSection(key);
                    if(section == null)
                        return null;

                    section.getValues(false).forEach((id, value) -> {
                        map.put(
                                UUID.fromString(id),
                                readingMethods.get(
                                        ((UUIDHashMapModuleData)ann).type()
                                ).apply(null, section, id)
                        );
                    });
                    return map;
                }
        );

        writingMethods.put(
                UUIDHashMapModuleData.class, (ann, yaml, key, value)
                        -> {
                    ConfigurationSection mapSection = yaml.createSection(key);
                    ((HashMap<UUID, Object>) value).forEach((id, obj) -> {
                        writingMethods.get(((UUIDHashMapModuleData)ann).type()).accept(null, mapSection, id.toString(), obj);
                    });
                }
        );


        //endregion


        //region String HashMap

        //----------= String HashMap =-----------------------------------------------------------------------------


        readingMethods.put(
                StringHashMapModuleData.class, (ann, yaml, key)
                        -> {
                    HashMap<String, Object> map = new HashMap<>();
                    ConfigurationSection section = yaml.getConfigurationSection(key);
                    if(section == null)
                        return null;

                    section.getValues(false).forEach((id, value) -> {
                        map.put(
                                id,
                                readingMethods.get(
                                        ((UUIDHashMapModuleData)ann).type()
                                ).apply(null, section, id)
                        );
                    });
                    return map;
                }
        );


        writingMethods.put(
                StringHashMapModuleData.class, (ann, yaml, key, value)
                        -> {
                    ConfigurationSection mapSection = yaml.createSection(key);
                    ((HashMap<String, Object>) value).forEach((id, obj) -> {
                        writingMethods.get(((StringHashMapModuleData)ann).type()).accept(null, mapSection, id, obj);
                    });
                }
        );


        //endregion


        //region NamespacedKey HashMap

        //----------= NamespacedKey HashMap =-----------------------------------------------------------------------------


        readingMethods.put(
                NamespacedKeyHashMapModuleData.class, (ann, yaml, key)
                        -> {
                    HashMap<NamespacedKey, Object> map = new HashMap<>();
                    ConfigurationSection section = yaml.getConfigurationSection(key);
                    if(section == null)
                        return null;

                    section.getValues(false).forEach((id, value) -> {
                        map.put(
                                NamespacedKey.fromString(id),
                                readingMethods.get(
                                        ((NamespacedKeyHashMapModuleData)ann).type()
                                ).apply(null, section, id)
                        );
                    });
                    return map;
                }
        );


        writingMethods.put(
                StringHashMapModuleData.class, (ann, yaml, key, value)
                        -> {
                    ConfigurationSection mapSection = yaml.createSection(key);
                    ((HashMap<NamespacedKey, Object>) value).forEach((id, obj) -> {
                        writingMethods.get(((NamespacedKeyHashMapModuleData)ann).type()).accept(null, mapSection, id.toString(), obj);
                    });
                }
        );


        //endregion
    }

    //endregion


}
