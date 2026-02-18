package kr.haruserver.viaversionbugfix.mixin;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
public class FabricParticleFixerMixin {

    @Shadow
    public void sendPacket(Packet<?> packet) {
        // Original Method Call
    }

    // Flags for Infinite Loop Protection
    @Unique
    private static final ThreadLocal<Boolean> IS_FIXING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "sendPacket(Lnet/minecraft/network/packet/Packet;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        // If it's a new packet we're sending,
        // it goes through without interception
        if (IS_FIXING.get()) return;

        if (packet instanceof ParticleS2CPacket oldPacket) {
            ParticleEffect cleanParameters = getCleanParticleEffect(oldPacket);

            try {
                IS_FIXING.set(true);

                ParticleS2CPacket cleanPacket = new ParticleS2CPacket(
                        cleanParameters,
                        oldPacket.shouldForceSpawn(),
                        oldPacket.isImportant(),
                        oldPacket.getX(),
                        oldPacket.getY(),
                        oldPacket.getZ(),
                        oldPacket.getOffsetX(),
                        oldPacket.getOffsetY(),
                        oldPacket.getOffsetZ(),
                        oldPacket.getSpeed(),
                        oldPacket.getCount()
                );

                this.sendPacket(cleanPacket);
                ci.cancel();
            } finally {
                IS_FIXING.set(false);
            }
        }
    }

    @Unique
    private static ParticleEffect getCleanParticleEffect(ParticleS2CPacket oldPacket) {
        ParticleEffect parameters = oldPacket.getParameters();
        ParticleEffect cleanParameters = parameters;

        if (parameters instanceof BlockStateParticleEffect blockEffect) {
            cleanParameters = new BlockStateParticleEffect(blockEffect.getType(), blockEffect.getBlockState());
        }
        return cleanParameters;
    }
}