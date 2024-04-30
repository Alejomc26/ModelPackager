package io.github.alejomc.resourcepack;

import java.util.Base64;

public record Model(String outputJson, String base64String, String modelName) {

    public byte[] base64Bytes() {
        return Base64.getDecoder().decode(base64String);
    }

}
