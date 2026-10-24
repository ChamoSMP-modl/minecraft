package gg.modl.minecraft.core.impl.menus.util;

import lombok.Value;
import lombok.experimental.Accessors;

import gg.modl.minecraft.api.Account;

@Accessors(fluent = true)
public record InspectContext(Account account, int punishmentCount, int noteCount) {
}
