package com.mineblock11.skinshuffle.api;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

public interface MojangApi {
    SkinQueryResult getPlayerSkinTexture(String uuid);

    Optional<UUID> getUUIDFromUsername(String username);

    void setSkinTexture(File skinFile, String model);
}
