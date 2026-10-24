package gg.modl.minecraft.api.http.response;

import gg.modl.minecraft.api.PunishmentTypeClassifier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PunishmentTypesResponse extends StatusResponse {
    private List<PunishmentTypeData> data;
    private int status;

    public record PunishmentTypeData(String name, String category, String staffDescription, String playerDescription,
                                     Object durations, Object points, Integer customPoints, Boolean canBeAltBlocking,
                                     Boolean canBeStatWiping, Boolean singleSeverityPunishment,
                                     Boolean permanentUntilSkinChange, Boolean permanentUntilUsernameChange, int id,
                                     int ordinal, boolean isCustomizable) {
        public boolean isKick() {
            return "KICK".equalsIgnoreCase(category) || ordinal == PunishmentTypeClassifier.ORDINAL_KICK;
        }

        public boolean isBan() {
            return "BAN".equalsIgnoreCase(category);
        }

        public boolean isMute() {
            return "MUTE".equalsIgnoreCase(category);
        }
    }

    @SuppressWarnings("all")
    public int getStatus() {
        return status;
    }
}
