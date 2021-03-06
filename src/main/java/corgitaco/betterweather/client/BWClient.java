package corgitaco.betterweather.client;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.weatherevents.AcidRain;
import corgitaco.betterweather.weatherevents.Blizzard;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;

public class BWClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(BWClient::clientTickEvent);

        ClientSidePacketRegistry.INSTANCE.register(BetterWeather.BW_WEATHER_PACKET,
                (packetContext, attachedData) -> {
                    boolean readBlizzard = attachedData.readBoolean();
                    boolean readAcidRain = attachedData.readBoolean();

                    packetContext.getTaskQueue().execute(() -> {
                        BetterWeather.BetterWeatherEvents.weatherData.setBlizzard(readBlizzard);
                        BetterWeather.BetterWeatherEvents.weatherData.setAcidRain(readAcidRain);


                    });
                });
    }

    public static final ResourceLocation RAIN_TEXTURE = new ResourceLocation("textures/environment/rain.png");
    public static final ResourceLocation ACID_RAIN_TEXTURE = new ResourceLocation(BetterWeather.MOD_ID, "textures/environment/acid_rain.png");

    static int idx = 0;

    public static void clientTickEvent(Minecraft minecraft) {
        if (minecraft.level != null) {
            BetterWeather.BetterWeatherEvents.setWeatherData(minecraft.level);
            if (minecraft.level.getLevelData().isRaining() && BetterWeather.BetterWeatherEvents.weatherData.isAcidRain()) {

                if (BetterWeather.BW_CONFIG.acid_rain.client.smokeParticles)
                    AcidRain.addAcidRainParticles(minecraft.gameRenderer.getMainCamera(), minecraft, minecraft.levelRenderer);

                if (LevelRenderer.RAIN_LOCATION != ACID_RAIN_TEXTURE && BetterWeather.BetterWeatherEvents.weatherData.isAcidRain())
                    LevelRenderer.RAIN_LOCATION = ACID_RAIN_TEXTURE;
                else if (LevelRenderer.RAIN_LOCATION != RAIN_TEXTURE && !BetterWeather.BetterWeatherEvents.weatherData.isAcidRain())
                    LevelRenderer.RAIN_LOCATION = RAIN_TEXTURE;
            }

            if (minecraft.level.getLevelData().isRaining() && BetterWeather.BetterWeatherEvents.weatherData.isBlizzard()) {
                minecraft.levelRenderer.lastViewDistance = BetterWeather.BW_CONFIG.blizzard.client.forcedBlizzardRenderDistance;
                idx = 0;
            }
            if (minecraft.levelRenderer.lastViewDistance != minecraft.options.renderDistance && !BetterWeather.BetterWeatherEvents.weatherData.isBlizzard() && idx == 0) {
                minecraft.levelRenderer.lastViewDistance = minecraft.options.renderDistance;
                idx++;
            }
            Blizzard.blizzardSoundHandler(minecraft, minecraft.gameRenderer.getMainCamera());
        }
    }

}