package com.mineblock11.skinshuffle.mixin;

import com.mineblock11.skinshuffle.compat.ServerSideSkinCommand.CommandCheck;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class CommandCheckMixin {
    @Inject(method = "onCommandTree",at = @At(value = "TAIL"))
    public void checkForSkinPlugins(CommandTreeS2CPacket packet, CallbackInfo ci){
        CommandCheck.getCommandCheck().onConnected();
    }
}
