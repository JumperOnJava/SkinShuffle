package com.mineblock11.skinshuffle.client.skinsetter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

import java.util.Objects;

public class SkinsRestorerSetter implements SkinSetter {

    @Override
    public void setSkinTexture(String skinUrl, String model) {
        //super.setSkinTexture(skinUrl,model);
        try{
            install(skinUrl,model);
        }
        catch (Exception e){
            MinecraftClient.getInstance()
                    .getToastManager()
                    .add(SystemToast.create(MinecraftClient.getInstance(),
                    SystemToast.Type.PACK_LOAD_FAILURE,
                    Text.translatable("skinshuffle.serverplugin.toast.title"),
                    Text.translatable("skinshuffle.serverplugin.toast.message")));
        }
    }

    private void install(String skinUrl, String model) {
        Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).sendChatCommand("skin url %s %s".formatted(skinUrl, model.equals("default") ? "CLASSIC" : "SLIM"));
    }
}
