package com.mineblock11.skinshuffle.client.skinsetter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mineblock11.skinshuffle.SkinShuffle;
import com.mineblock11.skinshuffle.mixin.accessor.MinecraftClientAccessor;
import com.mineblock11.skinshuffle.mixin.accessor.MinecraftClientAuthAccessor;
import com.mineblock11.skinshuffle.mixin.accessor.YggdrasilUserApiServiceAccessor;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilUserApiService;
import kong.unirest.Unirest;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;

public class MojangSkinSetter implements SkinSetter {
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    /**
     * Set the player's skin texture from a URL.
     * @param skinURL The URL of the skin texture.
     * @param model The skin model type.
     */
    @Override
    public void setSkinTexture(String skinURL, String model) {
        if(0==0)
            throw new RuntimeException("safe");
        UserApiService service = ((MinecraftClientAccessor) MinecraftClient.getInstance()).getUserApiService();

        if (service instanceof YggdrasilUserApiService apiService) {
            try {
                com.mojang.authlib.minecraft.client.MinecraftClient client = ((YggdrasilUserApiServiceAccessor) apiService).getMinecraftClient();
                String token = ((MinecraftClientAuthAccessor) client).getAccessToken();

                JsonObject obj = new JsonObject();
                obj.addProperty("variant", model.equals("default") ? "classic" : "slim");
                obj.addProperty("url", skinURL);
                var result = Unirest.post("https://api.minecraftservices.com/minecraft/profile/skins")
                        .body(GSON.toJson(obj))
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token).asString().getBody();
                SkinShuffle.LOGGER.info("Set player skin: " + skinURL);
            } catch (Exception e) {
                throw new RuntimeException("Cannot connect to Mojang API.", e);
            }
        } else {
            throw new RuntimeException("Cannot connect to Mojang API - offline mode is active.");
        }

        if(MinecraftClient.getInstance().world != null) {
            ClientPlayNetworking.send(SkinShuffle.id("preset_changed"), PacketByteBufs.empty());
        }
    }

}
