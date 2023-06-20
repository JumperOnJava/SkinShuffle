/*
 *
 *     Copyright (C) 2023 Calum (mineblock11), enjarai
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 */

package com.mineblock11.skinshuffle.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mineblock11.skinshuffle.SkinShuffle;
import com.mineblock11.skinshuffle.client.config.SkinShuffleConfig;
import com.mineblock11.skinshuffle.mixin.accessor.MinecraftClientAccessor;
import com.mineblock11.skinshuffle.mixin.accessor.MinecraftClientAuthAccessor;
import com.mineblock11.skinshuffle.mixin.accessor.YggdrasilUserApiServiceAccessor;
import com.mineblock11.skinshuffle.util.AuthUtil;
import com.mineblock11.skinshuffle.util.SkinCacheRegistry;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.util.UUIDTypeAdapter;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class MojangApiImpl implements MojangApi {
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    /**
     * Get the player's skin texture.
     * @return Is a default skin? Skin URL, Model Type
     * @param uuid
     */
    @Override
    public SkinQueryResult getPlayerSkinTexture(String uuid) {
        try {
            String jsonResponse = Unirest.get("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false")
                    .asString().getBody();

            if (jsonResponse.isBlank()) {
                throw new IOException("Empty response from Mojang API.");
            }

            Gson gson = new Gson();
            JsonObject object = gson.fromJson(jsonResponse, JsonObject.class);
            JsonObject textureJSON = new JsonObject();
            textureJSON.addProperty("invalid", true);

            @Nullable String textureSignature = null;
            @Nullable String textureValue = null;

            for (JsonElement properties : object.get("properties").getAsJsonArray()) {
                if (properties.getAsJsonObject().get("name").getAsString().equals("textures")) {
                    textureSignature = properties.getAsJsonObject().get("signature").getAsString();
                    textureValue = properties.getAsJsonObject().get("value").getAsString();

                    String jsonContent = new String(Base64.getDecoder().decode(textureValue), StandardCharsets.UTF_8);
                    textureJSON = gson.fromJson(jsonContent, JsonObject.class);
                    break;
                }
            }

            if (textureJSON.has("invalid")) {
                return SkinQueryResult.EMPTY_RESULT;
            }

            if (!textureJSON
                    .get("textures").getAsJsonObject()
                    .has("SKIN")
            ) {
                return SkinQueryResult.EMPTY_RESULT;
            }

            var skin = textureJSON
                    .get("textures").getAsJsonObject()
                    .get("SKIN").getAsJsonObject();

            if (skin.get("url").getAsString().equals("Steve?") || skin.get("url").getAsString().equals("Alex?")) {
                return SkinQueryResult.EMPTY_RESULT;
            }

            String skinURL = skin.get("url").getAsString();
            String modelType = "default";

            try {
                modelType = skin
                        .get("metadata").getAsJsonObject()
                        .get("model").getAsString();
            } catch (Exception ignored) {
            }

            return new SkinQueryResult(false, skinURL, modelType, textureSignature, textureValue);
        } catch (Exception e) {
            SkinShuffle.LOGGER.error(e.getMessage());
            return SkinQueryResult.EMPTY_RESULT;
        }
    }

    @Override
    public Optional<UUID> getUUIDFromUsername(String username) {
        try {
            String jsonResponse = Unirest.get("https://api.mojang.com/users/profiles/minecraft/" + username)
                    .asString().getBody();

            Gson gson = new Gson();
            JsonObject object = gson.fromJson(jsonResponse, JsonObject.class);

            if (object.has("error")) {
                throw new RuntimeException(object.get("errorMessage").getAsString());
            }

            var idString = object.get("id").getAsString();
            return Optional.of(UUIDTypeAdapter.fromString(idString));
        } catch (Exception e) {
            SkinShuffle.LOGGER.error(e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get the player's uuid using their username.
     * @return An Optional containing the UUID of the provided username, or an empty Optional if the username is invalid.
     * @param username
     */
    /**
     * Set a skin texture from a file - will use URL if file has not been modified since previous upload.
     * @param skinFile The file to upload.
     * @param model The type of skin model.
     */
    @Override
    public void setSkinTexture(File skinFile, String model) {
        var skinsetter = SkinShuffleConfig.get().getSkinSetter();
        try {
            var cachedURL = SkinCacheRegistry.getCachedUploadedSkin(skinFile);
            if (cachedURL != null) {
                skinsetter.setSkinTexture(cachedURL, model);
                return;
            }
        } catch (IOException e) {
            SkinShuffle.LOGGER.error("Failed to hash file.");
            return;
        }
        var uploadUrl = uploadSkin(skinFile,model);
        try{
            SkinCacheRegistry.saveUploadedSkin(skinFile, uploadUrl);
        }catch (Exception e){
            SkinShuffle.LOGGER.info("Failed to upload skin:");
            SkinShuffle.LOGGER.info(e.getMessage());
        }
        skinsetter.setSkinTexture(uploadUrl,model);

    }

    protected String uploadSkin(File skinFile, String model) {
        if(0==0)
            throw new RuntimeException("safe");
        UserApiService service = ((MinecraftClientAccessor) MinecraftClient.getInstance()).getUserApiService();
        if (AuthUtil.isLoggedIn()) {
            com.mojang.authlib.minecraft.client.MinecraftClient client = ((YggdrasilUserApiServiceAccessor) service).getMinecraftClient();
            String token = ((MinecraftClientAuthAccessor) client).getAccessToken();

            HttpResponse<String> response = Unirest.post("https://api.minecraftservices.com/minecraft/profile/skins")
                    .header("Authorization", "Bearer " + token)
                    .field("variant", model.equals("default") ? "classic" : "slim")
                    .field("file", skinFile)
                    .asString();
            JsonObject responseObject = GSON.fromJson(response.getBody(), JsonObject.class);
            String skinURL = responseObject
                    .get("skins").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("url").getAsString();


            if (MinecraftClient.getInstance().world != null) {
                ClientPlayNetworking.send(SkinShuffle.id("preset_changed"), PacketByteBufs.create().writeString(skinURL));
            }

            SkinShuffle.LOGGER.info("Uploaded texture: " + skinURL);
            SkinShuffle.LOGGER.info("Set player skin: " + skinURL);
            return skinURL;

        } else {
            throw new RuntimeException("Cannot connect to Mojang API.");
        }
    }
}
