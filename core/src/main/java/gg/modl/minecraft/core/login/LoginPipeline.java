package gg.modl.minecraft.core.login;

import gg.modl.minecraft.core.cache.LoginCache;
import gg.modl.minecraft.core.session.PlayerSessionService;

public record LoginPipeline(LoginService loginService, LoginCache loginCache,
                            PlayerSessionService playerSessionService) {
}
