package net.caffeinemc.mods.lithium.mixin.entity.inactive_navigations;

import net.caffeinemc.mods.lithium.common.entity.NavigatingEntity;
import net.caffeinemc.mods.lithium.common.world.ServerWorldExtended;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements NavigatingEntity {
    @Unique
    private PathNavigation registeredNavigation;

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }


    @Shadow
    public abstract PathNavigation getNavigation();

    @Override
    public boolean lithium$isRegisteredToWorld() {
        return this.registeredNavigation != null;
    }

    @Override
    public void lithium$setRegisteredToWorld(PathNavigation navigation) {
        this.registeredNavigation = navigation;
    }

    @Override
    public PathNavigation lithium$getRegisteredNavigation() {
        return this.registeredNavigation;
    }

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At("RETURN"))
    private void onNavigationReplacement(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
        this.lithium$updateNavigationRegistration();
    }

    @Override
    public void lithium$updateNavigationRegistration() {
        if (this.lithium$isRegisteredToWorld()) {
            PathNavigation navigation = this.getNavigation();
            if (this.registeredNavigation != navigation) {
                ((ServerWorldExtended) this.level()).lithium$setNavigationInactive((Mob) (Object) this);
                this.registeredNavigation = navigation;

                if (navigation.getPath() != null) {
                    ((ServerWorldExtended) this.level()).lithium$setNavigationActive((Mob) (Object) this);
                }
            }
        }
    }

}
