package com.mineblock11.skinshuffle.api;

import com.google.gson.JsonObject;
import com.mineblock11.skinshuffle.SkinShuffle;
import com.mineblock11.skinshuffle.client.config.SkinShuffleConfig;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.io.File;


public class MineSkinUpload extends MojangApiImpl {

    @Override
    public String uploadSkin(File skinFile,String model){
        var apikey = SkinShuffleConfig.get().apiKey;

        var slimHand = model == "slim";
        var requestBody = new JsonObject();
        HttpResponse<String> response = Unirest.post("https://api.mineskin.org/generate/upload?key="+apikey)
                .header("User-Agent", "SkinShuffle/1.0")
                .field("variant", model.equals("default") ? "classic" : "slim")
                .field("file", skinFile)
                .field("visibility","1")
                .field("name",skinFile.toString())
                .asString();
        SkinShuffle.LOGGER.info(response.getBody());
        JsonObject responseObject = GSON.fromJson(response.getBody(), JsonObject.class);
        var url = responseObject.get("data").getAsJsonObject().get("texture").getAsJsonObject().get("url").getAsString();
        return url;
    }
}
