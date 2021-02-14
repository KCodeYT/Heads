package de.kcodeyt.heads.util.api;

import cn.nukkit.utils.SerializedImage;
import com.google.gson.Gson;
import de.kcodeyt.heads.util.ScheduledFuture;
import de.kcodeyt.heads.util.SkinUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class SkinAPI {

    private static final Gson GSON = new Gson();

    private static final Pattern UUID_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    private static final Map<String, String> UUID_CACHE = new ConcurrentHashMap<>();
    private static final List<SkinData> SKIN_CACHE = new CopyOnWriteArrayList<>();

    public static ScheduledFuture<SkinResponse> getSkin(String name) {
        return ScheduledFuture.supplyAsync(() -> {
            String uniqueId;
            String playerName;
            String foundName = null;
            for(String uuidKey : UUID_CACHE.keySet()) {
                if(uuidKey.equalsIgnoreCase(name))
                    foundName = uuidKey;
            }
            if(foundName == null) {
                try {
                    final Map<String, String> profileData = GSON.<Map<String, String>>fromJson(httpRequest(Mojang.API + URLEncoder.encode(name, "UTF-8")), Map.class);
                    if(profileData.isEmpty())
                        return SkinResponse.NOT_FOUND;
                    uniqueId = UUID_PATTERN.matcher(profileData.get("id")).replaceFirst("$1-$2-$3-$4-$5");
                    playerName = profileData.get("name");
                    UUID_CACHE.put(playerName, uniqueId);
                } catch(Throwable throwable) {
                    throw new CompletionException(throwable);
                }
            } else
                uniqueId = UUID_CACHE.get(playerName = foundName);

            SkinData skin;
            if((skin = SKIN_CACHE.stream().filter(skinData -> skinData.getSkinOwnerName().equalsIgnoreCase(playerName) || skinData.getSkinOwnerUniqueId().equals(uniqueId)).findAny().orElse(null)) == null) {
                try {
                    final Map<String, Object> profileData = GSON.<Map<String, Object>>fromJson(httpRequest(Mojang.SESSION_SERVER + URLEncoder.encode(uniqueId.replace("-", ""), "UTF-8")), Map.class);
                    if(profileData.isEmpty())
                        return SkinResponse.NOT_FOUND;

                    final String texture = shrinkBase64((String) ((Map<?, ?>) ((List<?>) profileData.get("properties")).get(0)).get("value"));
                    SKIN_CACHE.removeIf(skinData -> skinData.getTexture().equalsIgnoreCase(texture));

                    skin = SkinData.builder().
                            texture(texture).
                            skinOwnerName(playerName).
                            skinOwnerUniqueId(uniqueId).
                            serializedImage(getSkinByTexture(texture).join()).
                            build();
                    SKIN_CACHE.add(skin);
                } catch(Throwable throwable) {
                    throw new CompletionException(throwable);
                }
            }

            return SkinResponse.of(skin);
        });
    }

    private static String shrinkBase64(String base64Texture) {
        final Map<String, Object> shrunkMap = Collections.singletonMap("textures", Collections.singletonMap("SKIN", Collections.singletonMap("url", fromBase64(base64Texture))));
        return Base64.getEncoder().encodeToString(GSON.toJson(shrunkMap).getBytes(StandardCharsets.UTF_8));
    }

    public static String fromBase64(String base64Texture) {
        return (String) ((Map<?, ?>) ((Map<?, ?>) GSON.fromJson(new String(Base64.getDecoder().decode(base64Texture)), Map.class).get("textures")).get("SKIN")).get("url");
    }

    private static String httpRequest(String urlSpec) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) new URL(urlSpec).openConnection();
        connection.setRequestProperty("User-Agent", "Chrome");

        String content = "{}";
        if(connection.getResponseCode() == 200) {
            try(final InputStream inputStream = connection.getInputStream()) {
                final StringBuilder builder = new StringBuilder();
                final byte[] bytes = new byte[1024 * 1024];
                for(int read; (read = inputStream.read(bytes)) > 0; )
                    builder.append(new String(Arrays.copyOf(bytes, read), StandardCharsets.UTF_8));
                content = builder.toString();
            }
        }

        connection.disconnect();
        return content;
    }

    public static ScheduledFuture<SerializedImage> getSkinByTexture(String texture) {
        final SkinData skin;
        if((skin = SKIN_CACHE.stream().filter(skinData -> skinData.getTexture().equalsIgnoreCase(texture)).findAny().orElse(null)) == null)
            return SkinUtil.base64Texture(texture);
        return ScheduledFuture.completed(skin.getSerializedImage());
    }

}
