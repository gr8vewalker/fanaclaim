package me.ahmetflix.claim.message;

import me.ahmetflix.claim.FanaClaim;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class Messages {

    public static final Message DAYS = new Message("<days> gün");
    public static final Message HOURS = new Message("<hours> saat");
    public static final Message MINUTES = new Message("<minutes> dakika");
    public static final Message SECONDS = new Message("<seconds> saniye");
    public static final Message FULL_TIME_FORMAT = new Message("<days> <hours> <minutes> <seconds>");
    public static final Message UNTIL_CLAIM_ENDS = new Message("<player> adlı oyuncunun claiminin bitmesine <remaining> var. Koordinatlar: <world> <x> <y> <z>");
    public static final Message CLAIM_END = new Message("<player> adlı oyuncunun <world> <x> <y> <z> koordinatlarındaki claimi bitti!");
    public static final Message CLAIM_DELETED = new Message("<red>Claimin silindi!");
    public static final Message CLAIM_EXTENDED = new Message("<green>Claimin süresi uzatıldı!");

    public static final Message CANT_BREAK = new Message("<red>Bu claimde blok kıramazsın!");
    public static final Message CANT_PLACE = new Message("<red>Bu claime blok koyamazsın!");
    public static final Message CANT_PVP = new Message("<red>Bu claimde pvp yasaklı!");
    public static final Message CANT_PULL = new Message("<red>Bu claimde olta ile canlı çekmek yasaklı!");
    public static final Message CANT_OPEN_CONTAINER = new Message("<red>Bu claimde saklama bloklarını açamazsın!");
    public static final Message CANT_USE = new Message("<red>Bu claimde bu bloğu kullanamazsın!");
    public static final Message CANT_USE_COMMAND = new Message("<red>Bu claimde bu komutu kullanamazsın!");
    public static final Message NOT_OWNER = new Message("<red>Bu claimin sahibi değilsin!");
    public static final Message NO_CLAIM = new Message("<red>Bu bölgede bir claim yok!");
    public static final Message NOT_ENOUGH_MONEY = new Message("<red>Yeterli bakiyen yok!");

    public static final Message INTERNAL_ERROR = new Message("<red>Bir hata oluştu!");

    public static final Message FLAG_BUILD = new Message("Blok Kırma/Koyma");
    public static final Message FLAG_BUILD_HOPPERS = new Message("Huni Kırma/Koyma");
    public static final Message FLAG_BREAK_BEACON = new Message("Fener Kırma");
    public static final Message FLAG_BREAK_SPAWNER = new Message("Spawner Kırma");
    public static final Message FLAG_PLACE_SPAWNER = new Message("Spawner Koyma");
    public static final Message FLAG_PLACE_FLUID = new Message("Lav/Su Koyma");
    public static final Message FLAG_PLACE_ENTITIES = new Message("Tekne/Vagon Koyma");
    public static final Message FLAG_USE_ROD_ON_ENTITIES = new Message("Olta İle Canlı Çekme");
    public static final Message FLAG_USE_DOORS = new Message("Kapı Kullanma");
    public static final Message FLAG_USE_SETHOME = new Message("/sethome Kullanma");
    public static final Message FLAG_OPEN_CONTAINERS = new Message("Envanter Bloklarını Açma");
    public static final Message FLAG_TRIGGER_REDSTONE = new Message("Redstone Devrelerini Tetikleme");

    public static void load() {
        try {
            FanaClaim plugin = FanaClaim.getInstance();
            File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
            YamlConfiguration messagesYaml;
            if (!messagesFile.exists()) {
                messagesYaml = getYamlConfig(messagesFile);
                for (Field field : Messages.class.getDeclaredFields()) {
                    if (field.getType() == Message.class) {
                        Message message = (Message) field.get(null);
                        messagesYaml.set(field.getName(), message.getBase());
                    }
                }
                messagesYaml.save(messagesFile);
            }
            messagesYaml = getYamlConfig(messagesFile);
            for (Field field : Messages.class.getDeclaredFields()) {
                if (field.getType() == Message.class) {
                    if (!messagesYaml.contains(field.getName())) {
                        // insert default message
                        messagesYaml.set(field.getName(), ((Message) field.get(null)).getBase());
                        continue;
                    }
                    String base = messagesYaml.getString(field.getName());
                    Message message = (Message) field.get(null);
                    message.base(base);
                }
            }
            messagesYaml.save(messagesFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static YamlConfiguration getYamlConfig(File messagesFile) {
        if (!messagesFile.exists()) {
            try {
                messagesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(messagesFile);
    }

}
