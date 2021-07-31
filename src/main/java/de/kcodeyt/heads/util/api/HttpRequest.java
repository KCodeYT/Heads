package de.kcodeyt.heads.util.api;

import com.google.gson.Gson;
import de.kcodeyt.heads.Heads;
import lombok.RequiredArgsConstructor;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.URLEncoder.encode;

@RequiredArgsConstructor
class HttpRequest<R> {

    private static final Gson GSON = new Gson();

    private final String urlSpec;
    private final Class<R> clazz;

    R request(String data) {
        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL(this.urlSpec + encode(data, "UTF-8")).openConnection();
            connection.setRequestProperty("User-Agent", "Chrome");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            connection.connect();

            final R object;
            try(final Reader reader = new InputStreamReader(connection.getInputStream())) {
                object = GSON.fromJson(reader, this.clazz);
            }

            connection.disconnect();
            if(connection.getResponseCode() != 200)
                throw Heads.EXCEPTION;
            return object;
        } catch(Throwable cause) {
            throw Heads.EXCEPTION;
        }
    }

}
