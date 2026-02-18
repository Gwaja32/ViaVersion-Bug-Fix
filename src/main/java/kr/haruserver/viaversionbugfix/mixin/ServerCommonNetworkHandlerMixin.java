package kr.haruserver.viaversionbugfix.mixin;

import com.viaversion.viaversion.api.Via;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin {
    @Unique
    private static final int PROBLEMATIC_PROTOCOL_ID = 773;

    @Inject(method = "sendPacket(Lnet/minecraft/network/packet/Packet;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void particleFixer$preventParticlePacketToViaVersionClient(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ParticleS2CPacket particlePacket) {
            ParticleEffect particleEffect = particlePacket.getParameters();

            if (particleEffect instanceof BlockStateParticleEffect) {
                ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler)(Object)this;

                if (Via.getAPI() == null || handler.player == null) {
                    return;
                }
                int clientVersion = Via.getAPI().getPlayerVersion(handler.player.getUuid());

                if (clientVersion != PROBLEMATIC_PROTOCOL_ID) {
                    ci.cancel();
                }
            }
        }
    }
}
