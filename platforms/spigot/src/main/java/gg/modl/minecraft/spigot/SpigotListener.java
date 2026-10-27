package gg.modl.minecraft.spigot;

import gg.modl.minecraft.api.http.ModlHttpClient;
import gg.modl.minecraft.api.http.PanelUnavailableException;
import gg.modl.minecraft.api.http.request.PlayerLoginRequest;
import gg.modl.minecraft.core.HttpClientHolder;
import gg.modl.minecraft.core.boot.StartupClient;
import gg.modl.minecraft.core.cache.Cache;
import gg.modl.minecraft.core.cache.LoginCache;
import gg.modl.minecraft.core.chat.ChatService;
import gg.modl.minecraft.core.chat.CommandInterceptService;
import gg.modl.minecraft.core.integration.iplookup.IpEnrichmentService;
import gg.modl.minecraft.core.integration.iplookup.PendingIpLookupService;
import gg.modl.minecraft.core.integration.mojang.MojangProfiles;
import gg.modl.minecraft.core.integration.mojang.WebPlayer;
import gg.modl.minecraft.core.locale.LocaleManager;
import gg.modl.minecraft.core.login.LoginPipeline;
import gg.modl.minecraft.core.login.LoginService;
import gg.modl.minecraft.core.service.ChatMessageCache;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class SpigotListener implements Listener {
    private static final long PRE_LOGIN_TIMEOUT_SECONDS = 10;

    private final SpigotPlatform platform;
    private final Cache cache;
    private final HttpClientHolder httpClientHolder;
    private final ChatMessageCache chatMessageCache;
    private final LocaleManager localeManager;
    private final LoginPipeline loginPipeline;
    private final ChatService chatService;
    private final CommandInterceptService commandInterceptService;
    private final IpEnrichmentService ipEnrichmentService;
    private final PendingIpLookupService pendingIpLookupService;

    private ModlHttpClient getHttpClient() {
        return httpClientHolder.client();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String ipAddress = event.getAddress().getHostAddress();

        LoginCache.CachedLoginResult cached = loginPipeline.loginCache().getCachedLoginResult(event.getUniqueId());
        if (cached != null) {
            platform.getLogger().debug("Using cached login result for " + event.getName());
            loginPipeline.loginCache().storePreLoginResult(event.getUniqueId(),
                    new LoginCache.PreLoginResult(cached.response(), cached.ipInfo(), cached.skinHash()));
            handleBanStatus(event);
            return;
        }

        CompletableFuture<Map<String, Object>> ipInfoFuture = ipEnrichmentService.getIpInfo(ipAddress);
        CompletableFuture<WebPlayer> webPlayerFuture = MojangProfiles.client().get(event.getUniqueId());

        CompletableFuture<Void> combinedFuture = ipInfoFuture
                .thenCombine(webPlayerFuture, (ipInfo, webPlayer) -> {
                    String skinHash = (webPlayer != null && webPlayer.valid()) ? webPlayer.skin() : null;
                    PlayerLoginRequest request = new PlayerLoginRequest(
                            event.getUniqueId().toString(), event.getName(),
                            ipAddress, skinHash, platform.getServerName(), ipInfo,
                            StartupClient.getServerInstanceId());
                    return new Object[]{request, ipInfo, skinHash};
                })
                .thenCompose(data -> {
                    PlayerLoginRequest request = (PlayerLoginRequest) data[0];
                    @SuppressWarnings("unchecked")
                    Map<String, Object> ipInfo = (Map<String, Object>) data[1];
                    String skinHash = (String) data[2];
                    return getHttpClient().playerLogin(request)
                            .thenAccept(response -> {
                                loginPipeline.loginCache().cacheLoginResult(event.getUniqueId(), response, ipInfo, skinHash);
                                loginPipeline.loginCache().storePreLoginResult(event.getUniqueId(),
                                        new LoginCache.PreLoginResult(response, ipInfo, skinHash));
                                pendingIpLookupService.handlePendingIpLookups(response, event.getUniqueId().toString(), ipAddress, CompletableFuture.completedFuture(ipInfo));
                            });
                })
                .exceptionally(throwable -> {
                    platform.getLogger().warning("Failed to check punishments for " + event.getName() + ": " + throwable.getMessage());
                    Exception error = throwable instanceof Exception ? (Exception) throwable : new RuntimeException(throwable);
                    loginPipeline.loginCache().storePreLoginResult(event.getUniqueId(), new LoginCache.PreLoginResult(error));
                    return null;
                });

        try {
            combinedFuture.get(PRE_LOGIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            platform.getLogger().warning("Async pre-login timed out for " + event.getName() + ": " + e.getMessage());
            loginPipeline.loginCache().storePreLoginResult(event.getUniqueId(), new LoginCache.PreLoginResult(e));
        }

        handleBanStatus(event);
    }

    public void handleBanStatus(AsyncPlayerPreLoginEvent event) {
        String playerName = event.getName();
        UUID playerUuid = event.getUniqueId();

        LoginCache.PreLoginResult preLoginResult = loginPipeline.loginCache().getAndRemovePreLoginResult(playerUuid);

        if (preLoginResult == null) {
            platform.getLogger().warning("No pre-login result found for " + playerName + " - blocking login for safety");
            denyLoginUnverified(event);
            return;
        }

        if (preLoginResult.hasError()) {
            if (preLoginResult.getError() instanceof PanelUnavailableException) {
                platform.getLogger().warning("Panel 502 during login check for " + playerName + " - blocking login for safety");
            }

            // Yeah, this is a big wall of text
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    MiniMessage.miniMessage().deserialize(
                            Objects.requireNonNull(
                                    loginPipeline.loginService().denialMessage(
                                            loginPipeline.loginService().handleLoginError(
                                                    preLoginResult.getError()
                                            )
                                    )
                            )
                    )
            );
            return;
        }

        if (!preLoginResult.isSuccess()) {
            platform.getLogger().warning("Invalid pre-login result for " + playerName + " - blocking login for safety");
            denyLoginUnverified(event);
            return;
        }

        LoginService.LoginResult result = loginPipeline.loginService().processLoginResponse(
                preLoginResult.getResponse(), playerUuid);

        String denialMessage = loginPipeline.loginService().denialMessage(result);
        if (denialMessage != null) {
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    MiniMessage.miniMessage().deserialize(denialMessage)
            );
        }
    }

    private void denyLoginUnverified(AsyncPlayerPreLoginEvent event) {
        event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                MiniMessage.miniMessage().deserialize(localeManager.getMessage("api_errors.ban_check_failed"))
        );
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        loginPipeline.playerSessionService().handlePlayerJoin(uuid, event.getPlayer().getName());
        cacheSkinTexture(uuid);
        chatMessageCache.updatePlayerServer(platform.getServerName(), uuid.toString());

        LoginCache.CachedLoginResult cachedResult = loginPipeline.loginCache().getCachedLoginResult(uuid);
        loginPipeline.loginService().cacheLoginData(uuid, cachedResult != null ? cachedResult.response() : null);
    }

    private void cacheSkinTexture(UUID uuid) {
        String nativeTexture = platform.getPlayerSkinTexture(uuid);
        if (nativeTexture != null) {
            cache.cacheSkinTexture(uuid, nativeTexture);
            return;
        }
        MojangProfiles.client().get(uuid)
                .thenAccept(webPlayer -> {
                    if (webPlayer != null && webPlayer.valid() && webPlayer.textureValue() != null)
                        cache.cacheSkinTexture(uuid, webPlayer.textureValue());
                })
                .exceptionally(_ -> null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        loginPipeline.playerSessionService().handlePlayerDisconnect(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncChatEvent event) {
        ChatService.Result result = chatService.handleChat(
                event.getPlayer().getUniqueId(), event.getPlayer().getName(), MiniMessage.miniMessage().serialize(event.message()),
                platform.getServerName(),
                msg -> event.getPlayer().sendMessage(msg));
        if (result == ChatService.Result.CANCELLED) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        CommandInterceptService.CommandResult result = commandInterceptService.handleCommand(
                event.getPlayer().getUniqueId(), event.getPlayer().getName(),
                event.getMessage(), platform.getServerName());

        if (result != CommandInterceptService.CommandResult.ALLOWED) {
            event.setCancelled(true);
            event.getPlayer().sendRichMessage(Objects.requireNonNull(commandInterceptService.getBlockMessage(
                    result, event.getPlayer().getUniqueId())));
        }
    }

}
