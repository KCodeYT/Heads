package de.kcodeyt.heads.util;

import cn.nukkit.utils.SerializedImage;
import de.kcodeyt.heads.util.api.SkinAPI;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class SkinUtil {

    public static final String PLACED_SKULL_GEOMETRY = "{\"format_version\":\"1.12.0\",\"minecraft:geometry\":[{\"description\":{\"identifier\":\"geometry.heads.placed\",\"texture_width\":64,\"texture_height\":64,\"visible_bounds_width\":2,\"visible_bounds_height\":1,\"visible_bounds_offset\":[0,0,0]},\"bones\":[{\"name\":\"head\",\"pivot\":[0,24,0],\"cubes\":[{\"origin\":[-4,0,-4],\"size\":[8,8,8],\"uv\":[0,0]}]},{\"name\":\"hat\",\"parent\":\"head\",\"pivot\":[0,24,0],\"cubes\":[{\"origin\":[-4,0,-4],\"size\":[8,8,8],\"inflate\":0.2,\"uv\":[32,0]}]}]}]}";
    public static final String PLACED_SKULL_GEOMETRY_NAME = "geometry.heads.placed";

    private static final Map<String, SerializedImage> SKINS = new HashMap<>();

    public static CompletableFuture<SerializedImage> base64Texture(String texture) {
        if(SKINS.containsKey(texture))
            return CompletableFuture.completedFuture(SKINS.get(texture));
        return CompletableFuture.supplyAsync(() -> {
            try {
                final BufferedImage bufferedImage = ImageIO.read(new URL(SkinAPI.fromBase64(texture)));
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
                return new SerializedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), imageData);
            } catch(Throwable throwable) {
                throw new CompletionException(throwable);
            }
        }).whenComplete((serializedImage, throwable) -> {
            if(serializedImage != null)
                SKINS.put(texture, serializedImage);
        });
    }

}
