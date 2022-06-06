/*
 * Copyright 2022 KCodeYT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kcodeyt.heads.util.api;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.URLEncoder.encode;

@RequiredArgsConstructor
final class HttpRequest<R> {

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

            if(connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) {
                connection.disconnect();
                throw new RuntimeException("HTTP error: " + connection.getResponseCode());
            }

            final R object;
            try(final Reader reader = new InputStreamReader(connection.getInputStream())) {
                object = GSON.fromJson(reader, this.clazz);
            }

            connection.disconnect();
            return object;
        } catch(Throwable cause) {
            throw new RuntimeException("Failed to request " + this.urlSpec + " with data " + data, cause);
        }
    }

}
