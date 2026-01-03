package org.craftamethyst.tritium.mixin.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.ParticleOptions;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import org.craftamethyst.tritium.random.TritiumRandomManager;
import org.craftamethyst.tritium.random.TritiumRandomTarget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Queue;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Shadow
    @Final
    private Map<ParticleRenderType, Queue<Particle>> particles;

    @Shadow
    @Final
    private Queue<Particle> particlesToAdd;

    @Inject(
            method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onCreateParticle(ParticleOptions particleData, double x, double y, double z,
                                  double xSpeed, double ySpeed, double zSpeed,
                                  CallbackInfoReturnable<Particle> cir) {
        if (!TritiumConfigBase.ParticleLimit.enableParticleLimit) {
            return;
        }

        int totalParticles = tritium$getTotalParticleCount();
        if (totalParticles >= TritiumConfigBase.ParticleLimit.maxParticles) {
            double rejectionProbability = Math.min(0.95,
                    (double) totalParticles / (TritiumConfigBase.ParticleLimit.maxParticles * 1.5));

            if (TritiumRandomManager.nextDouble(TritiumRandomTarget.PARTICLE_REJECTION, null) < rejectionProbability) {
                cir.setReturnValue(null);
                cir.cancel();
            }
        }
    }

    @Unique
    private int tritium$getTotalParticleCount() {
        int total = 0;
        for (Queue<Particle> queue : this.particles.values()) {
            total += queue.size();
        }
        total += this.particlesToAdd.size();
        return total;
    }

    @Inject(
            method = "add(Lnet/minecraft/client/particle/Particle;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAddParticle(Particle particle, CallbackInfo ci) {
        if (!TritiumConfigBase.ParticleLimit.enableParticleLimit) {
            return;
        }

        int totalParticles = tritium$getTotalParticleCount();
        if (totalParticles > TritiumConfigBase.ParticleLimit.maxParticles * 2) {
            ci.cancel();
        }
    }
}
