package gg.modl.minecraft.core.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplayServiceTest {
    @Test
    void shouldAttemptCaptureForColdUnknownTargetWhenBridgeIsConnected() {
        UUID targetUuid = UUID.randomUUID();
        StatusReplayService replayService = new StatusReplayService(ReplayCaptureStatus.NO_ACTIVE_RECORDING);

        assertTrue(replayService.shouldAttemptCapture(targetUuid));
    }

    @Test
    void shouldNotAttemptCaptureForHardUnavailableReplayStatuses() {
        UUID targetUuid = UUID.randomUUID();

        assertFalse(new StatusReplayService(ReplayCaptureStatus.NO_BRIDGE_CONNECTED).shouldAttemptCapture(targetUuid));
    }

    private record StatusReplayService(ReplayCaptureStatus status) implements ReplayService {
        @Override
        public CompletableFuture<ReplayCaptureResult> captureReplayResult(UUID targetUuid, String targetName) {
            return CompletableFuture.completedFuture(ReplayCaptureResult.noActiveRecording());
        }

        @Override
        public ReplayCaptureStatus getReplayStatus(UUID playerUuid) {
            return status;
        }
    }
}