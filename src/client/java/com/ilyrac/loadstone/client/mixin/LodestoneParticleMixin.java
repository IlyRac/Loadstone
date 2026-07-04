package com.ilyrac.loadstone.client.mixin;

import com.ilyrac.loadstone.client.ClientLoaderCache;
import com.ilyrac.loadstone.client.config.LoadstoneConfig;
import com.ilyrac.loadstone.loader.LoaderTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Block.class)
public class LodestoneParticleMixin {

    @Inject(method = "animateTick", at = @At("HEAD"))
    private void onAnimateTick(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        // 1. Instantly exit if this isn't a Lodestone block
        if (!state.is(Blocks.LODESTONE)) return;

        // 2. Only run on the client side
        if (!level.isClientSide()) return;

        // 3. Check config for Ambient Particle mode
        LoadstoneConfig.AmbientMode mode = LoadstoneConfig.getInstance().ambientParticles;
        if (mode == LoadstoneConfig.AmbientMode.OFF) return;

        // 4. Safely check your cache
        Optional<LoaderTier> activeTier = ClientLoaderCache.get(pos);
        if (activeTier.isEmpty()) return;

        // 5. Adjust spawn frequency and density modifiers based on setting
        int baseCountModifier = 1;

        if (mode == LoadstoneConfig.AmbientMode.LOW) {
            // Lower rate: 10% chance per tick instead of 20%, and half the density
            if (random.nextInt(10) != 0) return;
            baseCountModifier = 2; // Divide loop count by 2
        } else {
            // HIGH mode: 40% chance per random tick check for a heavier constant hum
            if (random.nextInt(5) > 1) return;
        }

        LoaderTier tier = activeTier.get();

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        switch (tier) {
            case IRON -> {
                int count = 6 / baseCountModifier;
                for (int i = 0; i < count; i++) {
                    double offsetX = (random.nextDouble() - 0.5) * 0.6;
                    double offsetY = (random.nextDouble() - 0.5) * 0.2;
                    double offsetZ = (random.nextDouble() - 0.5) * 0.6;

                    double velX = (random.nextDouble() - 0.5) * 0.08;
                    double velY = random.nextDouble() * 0.03 + 0.02;
                    double velZ = (random.nextDouble() - 0.5) * 0.08;

                    level.addParticle(ParticleTypes.SMOKE, x + offsetX, y + offsetY, z + offsetZ, velX, velY, velZ);
                }
            }
            case DIAMOND -> {
                int count = 8 / baseCountModifier;
                for (int i = 0; i < count; i++) {
                    double offsetX = (random.nextDouble() - 0.5) * 0.7;
                    double offsetY = (random.nextDouble() - 0.5) * 0.3;
                    double offsetZ = (random.nextDouble() - 0.5) * 0.7;

                    double velX = (random.nextDouble() - 0.5) * 0.12;
                    double velY = (random.nextDouble() - 0.5) * 0.05 + 0.04;
                    double velZ = (random.nextDouble() - 0.5) * 0.12;

                    level.addParticle(ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS, x + offsetX, y + offsetY, z + offsetZ, velX, velY, velZ);
                }
            }
            case NETHERITE -> {
                int count = 8 / baseCountModifier;
                for (int i = 0; i < count; i++) {
                    double offsetX = (random.nextDouble() - 0.5) * 0.7;
                    double offsetY = (random.nextDouble() - 0.5) * 0.3;
                    double offsetZ = (random.nextDouble() - 0.5) * 0.7;

                    double velX = (random.nextDouble() - 0.5) * 0.15;
                    double velY = random.nextDouble() * 0.05 + 0.02;
                    double velZ = (random.nextDouble() - 0.5) * 0.15;

                    level.addParticle(
                            PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F),
                            x + offsetX, y + offsetY, z + offsetZ,
                            velX, velY, velZ
                    );
                }
            }
        }
    }
}