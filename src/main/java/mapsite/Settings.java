package mapsite;

import necesse.engine.GameLog;
import necesse.engine.commands.CommandLog;
import necesse.engine.modLoader.ModSettings;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

public class Settings extends ModSettings {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    protected @interface DefaultValue {
        int value();
    }

    @DefaultValue(8080)
    public static int webappPort;

    @DefaultValue(1000)
    public static int playerUpdateRateMs;

    @DefaultValue(5000)
    public static int mapUpdateRateMs;

    @DefaultValue(100)
    public static int mapChunkFetchRateMs;

    @DefaultValue(128)
    public static int mapChunkSize;

    @DefaultValue(10000)
    public static int mapChunkUpdateRateMs;

    @Override
    public void addSaveData(SaveData save) {
        for (Field field : Settings.class.getDeclaredFields()) {
            try {
                save.addInt(field.getName(), field.getInt(null));
            } catch (IllegalAccessException e) {
                GameLog.warn.println("MapSite: Error saving " + field.getName());
            }
        }
    }

    @Override
    public void applyLoadData(LoadData save) {
        for (Field field : Settings.class.getDeclaredFields()) {
            try {
                field.set(null, field.getAnnotation(DefaultValue.class).value());
            } catch (IllegalAccessException e) {
                GameLog.warn.println("MapSite: Error setting " + field.getName());
            }
        }
    }

    public static void printToLog(CommandLog log) {
        log.add("MapSite Settings");
        log.add("================");
        for (Field field : Settings.class.getDeclaredFields()) {
            try {
                log.add(field.getName() + ": " + field.get(null));
            } catch (IllegalAccessException e) {
                log.add("Error fetching " + field.getName());
            }
        }
        log.add("================");
    }

    private static Field getField(String option) {
        for (Field field : Settings.class.getDeclaredFields()) {
            if (field.getName().equals(option)) {
                return field;
            }
        }
        return null;
    }

    public static boolean hasOption(String option) {
        return getField(option) != null;
    }

    public static void logOptionValue(String option, CommandLog log) {
        Field field = getField(option);
        if (field == null) {
            log.add("Unknown option: " + option);
            return;
        }
        try {
            log.add(option + ": " + field.get(null));
        } catch (IllegalAccessException e) {
            log.add("Error fetching " + option);
        }
    }

    public static void setOptionAndLog(String option, String value, CommandLog log) {
        Field field = getField(option);
        if (field == null) {
            log.add("Unknown option: " + option);
            return;
        }
        Object parsedValue;
        try {
            parsedValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.add("Expected int value, got: " + value);
            return;
        }
        try {
            field.set(null, parsedValue);
            log.add("Set " + option + " to " + value);
            System.out.println(Settings.webappPort);
        } catch (IllegalAccessException e) {
            log.add("Error setting " + option + " to " + value);
        }
    }
}
