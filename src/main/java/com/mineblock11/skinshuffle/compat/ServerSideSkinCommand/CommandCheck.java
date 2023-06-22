package com.mineblock11.skinshuffle.compat.ServerSideSkinCommand;

import com.mineblock11.skinshuffle.SkinShuffle;
import com.mineblock11.skinshuffle.client.config.SkinShuffleConfig;
import com.mineblock11.skinshuffle.client.skinsetter.MojangSkinSetter;
import com.mineblock11.skinshuffle.client.skinsetter.SkinSetter;
import com.mineblock11.skinshuffle.client.skinsetter.SkinsRestorerSetter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.Suggestions;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CommandCheck{
    private static SkinSetter currentSetter = new MojangSkinSetter();
    public static SkinSetter getCurrentSetter() {
        return currentSetter;
    }
    public static void setCurrentSetter(SkinSetter currentSetter) {
        CommandCheck.currentSetter = currentSetter;
    }

    private static CommandCheck commandCheck = new CommandCheck();
    public static void setCommandCheck(CommandCheck commandCheck) {
        CommandCheck.commandCheck = commandCheck;
    }
    public static CommandCheck getCommandCheck() {
        return commandCheck;
    }

   private CommandCheck(){};

    public void onConnected()
    {
        var client = MinecraftClient.getInstance();
        var networkHandler = client.getNetworkHandler();
        try {
            var dispatcher = networkHandler.getCommandDispatcher();
            var source = new ClientCommandSource(client.getNetworkHandler(), client);
            checkSkinsRestorer(dispatcher, source,() -> currentSetter = new SkinsRestorerSetter());


        } catch (Exception e) {
            //SkinShuffle.LOGGER.info(e.toString());
            throw new RuntimeException(e);
        }
    }
    public void checkSkinsRestorer(CommandDispatcher<CommandSource> dispatcher,ClientCommandSource commandSource,Runnable callback) throws ExecutionException, InterruptedException {
        CompletableFuture.runAsync(()->{
            try {

                Thread.sleep(5000);
                var parse = dispatcher.parse("skin url check.png ", commandSource);
                var suggestions = dispatcher.getCompletionSuggestions(parse).get();

                SkinShuffle.LOGGER.info(suggestions.toString());
                if (suggestions.getList().size() > 0) {
                    callback.run();
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
    }
}
