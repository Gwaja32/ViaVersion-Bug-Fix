package kr.haruserver.viaversionbugfix.mixin;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public class FabricParticleFixerMixin {

    @Shadow
    public void send(Packet<?> packet) {
        // Original Method Call
    }

    // Flags for Infinite Loop Protection
    @Unique
    private static final ThreadLocal<Boolean> IS_FIXING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        // If it's a new packet we're sending,
        // it goes through without interception
        if (IS_FIXING.get()) return;

        if (packet instanceof ClientboundLevelParticlesPacket oldPacket) {
            ParticleOptions cleanParameters = getCleanParticleEffect(oldPacket);

            try {
                IS_FIXING.set(true);

                ClientboundLevelParticlesPacket cleanPacket = new ClientboundLevelParticlesPacket(
                        cleanParameters,
                        oldPacket.isOverrideLimiter(),
                        oldPacket.alwaysShow(),
                        oldPacket.getX(),
                        oldPacket.getY(),
                        oldPacket.getZ(),
                        oldPacket.getXDist(),
                        oldPacket.getYDist(),
                        oldPacket.getZDist(),
                        oldPacket.getMaxSpeed(),
                        oldPacket.getCount()
                );

                this.send(cleanPacket);
                ci.cancel();
            } finally {
                IS_FIXING.set(false);
            }
        }
    }

    @Unique
    private static ParticleOptions getCleanParticleEffect(ClientboundLevelParticlesPacket oldPacket) {
        ParticleOptions parameters = oldPacket.getParticle();
        ParticleOptions cleanParameters = parameters;

        if (parameters instanceof BlockParticleOption blockEffect) {
            cleanParameters = new BlockParticleOption(blockEffect.getType(), blockEffect.getState());
        }
        return cleanParameters;
    }
}