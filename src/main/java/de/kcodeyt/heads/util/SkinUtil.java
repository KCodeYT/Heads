package de.kcodeyt.heads.util;

import cn.nukkit.Server;
import cn.nukkit.utils.SerializedImage;
import de.kcodeyt.heads.Heads;
import de.kcodeyt.heads.util.api.Mojang;
import de.kcodeyt.heads.util.api.SkinAPI;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

public class SkinUtil {

    public static final String PLACED_SKULL_GEOMETRY = "{\"format_version\":\"1.12.0\",\"minecraft:geometry\":[{\"description\":{\"identifier\":\"geometry.heads.placed\",\"texture_width\":64,\"texture_height\":64,\"visible_bounds_width\":2,\"visible_bounds_height\":1,\"visible_bounds_offset\":[0,0,0]},\"bones\":[{\"name\":\"head\",\"pivot\":[0,24,0],\"cubes\":[{\"origin\":[-4,0,-4],\"size\":[8,8,8],\"uv\":[0,0]}]},{\"name\":\"hat\",\"parent\":\"head\",\"pivot\":[0,24,0],\"cubes\":[{\"origin\":[-4,0,-4],\"size\":[8,8,8],\"inflate\":0.2,\"uv\":[32,0]}]}]}]}";
    public static final String PLACED_SKULL_GEOMETRY_NAME = "geometry.heads.placed";

    private static final Map<String, SerializedImage> SKINS = new ConcurrentHashMap<>();
    private static final PluginHolder PLUGIN_HOLDER = new PluginHolder();

    public static ScheduledFuture<SerializedImage> base64Texture(String texture) {
        if(SKINS.containsKey(texture))
            return ScheduledFuture.completed(SKINS.get(texture));
        final Heads heads = PLUGIN_HOLDER.get();
        final boolean saveSkinCache = heads != null && heads.getConfig().getBoolean("save-skin-cache");
        final String skinCacheFolderStr = saveSkinCache ? heads.getConfig().getString("skin-cache-folder") : null;
        final File skinCacheFolder = skinCacheFolderStr == null ? null : new File(skinCacheFolderStr);
        if(skinCacheFolder != null && !skinCacheFolder.exists() && !skinCacheFolder.mkdirs())
            Server.getInstance().getLogger().warning("Could not create skin cache folder!");

        return ScheduledFuture.supplyAsync(() -> {
            final String textureUrl = SkinAPI.fromBase64(texture);
            if(!textureUrl.startsWith(Mojang.TEXTURES))
                throw new CompletionException(new RuntimeException());

            final String textureId = textureUrl.substring(Mojang.TEXTURES.length());
            final File textureFile;
            if(skinCacheFolder == null || !skinCacheFolder.exists())
                textureFile = null;
            else {
                final File subFolder = new File(skinCacheFolder, textureId.toLowerCase().substring(0, 2));
                if(subFolder.exists() || subFolder.mkdirs())
                    textureFile = new File(subFolder, textureId);
                else
                    textureFile = null;
            }

            try(final InputStream inputStream = textureFile != null && textureFile.exists() ?
                    new FileInputStream(textureFile) :
                    new URL(textureUrl).openStream()) {
                final BufferedImage bufferedImage = ImageIO.read(inputStream);
                if(textureFile != null && !textureFile.exists())
                    ImageIO.write(bufferedImage, "PNG", textureFile);

                final byte[] imageData = new byte[bufferedImage.getHeight() * bufferedImage.getWidth() * 4];
                int cursor = 0;
                for(int y = 0; y < bufferedImage.getHeight(); y++) {
                    for(int x = 0; x < bufferedImage.getWidth(); x++) {
                        final int color = bufferedImage.getRGB(x, y);
                        imageData[cursor++] = (byte) ((color >> 16) & 0xFF);
                        imageData[cursor++] = (byte) ((color >> 8) & 0xFF);
                        imageData[cursor++] = (byte) (color & 0xFF);
                        imageData[cursor++] = (byte) ((color >> 24) & 0xFF);
                    }
                }

                final SerializedImage serializedImage;
                SKINS.put(texture, serializedImage = new SerializedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), imageData));
                return serializedImage;
            } catch(Throwable throwable) {
                throw new CompletionException(throwable);
            }
        });
    }

    private static final class PluginHolder {
        private Heads plugin;

        private Heads get() {
            if(this.plugin == null)
                this.plugin = (Heads) Server.getInstance().getPluginManager().getPlugins().values().stream().
                        filter(plugin -> plugin instanceof Heads).findAny().orElse(null);
            return this.plugin;
        }
    }

}
