package gg.modl.minecraft.bridge.staffmode;

import java.util.List;

public record ScoreboardContent(String title, List<Line> lines) {
    public record Line(String text, int score) {
    }
}
