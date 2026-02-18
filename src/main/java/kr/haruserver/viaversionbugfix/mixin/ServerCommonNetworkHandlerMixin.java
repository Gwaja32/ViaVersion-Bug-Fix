package kr.haruserver.viaversionbugfix.mixin;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.*;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin {

    private static final java.util.Set<ParticleType<?>> SAFE_PARTICLES = java.util.Set.of(
            ParticleTypes.BLOCK, ParticleTypes.BLOCK_CRUMBLE, ParticleTypes.BLOCK_MARKER,
            ParticleTypes.DUST, ParticleTypes.DUST_COLOR_TRANSITION, ParticleTypes.DUST_PILLAR,
            ParticleTypes.ENTITY_EFFECT, ParticleTypes.ITEM, ParticleTypes.SCULK_CHARGE,
            ParticleTypes.SHRIEK, ParticleTypes.TINTED_LEAVES, ParticleTypes.VIBRATION,
            ParticleTypes.FALLING_DUST, ParticleTypes.EFFECT, ParticleTypes.FLASH,
            ParticleTypes.INSTANT_EFFECT, ParticleTypes.TRAIL
    );

    @Inject(method = "sendPacket(Lnet/minecraft/network/packet/Packet;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void particleFixer$preventParticlePacketToViaVersionClient(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ParticleS2CPacket particlePacket) {
            ParticleEffect particleEffect = particlePacket.getParameters();
            ParticleType<?> type = particleEffect.getType();

            if (particleEffect instanceof BlockStateParticleEffect) {
                if (isSafeParticle(type)) {
                    return;
                }
                ci.cancel();
            }
        }
    }

    @Unique
    private boolean isSafeParticle(ParticleType<?> type) {
        return SAFE_PARTICLES.contains(type);
    }
}
