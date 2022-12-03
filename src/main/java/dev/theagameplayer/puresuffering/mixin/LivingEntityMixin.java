package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.theagameplayer.puresuffering.world.entity.PSHyperCharge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public final class LivingEntityMixin implements PSHyperCharge {
	private static final EntityDataAccessor<Integer> PS_HYPER_CHARGE = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
	
	@Inject(at = @At("RETURN"), method = "defineSynchedData()V")
	private final void defineSynchedData(final CallbackInfo callbackIn) {
		final LivingEntity self = (LivingEntity)(Object)this;
		self.getEntityData().define(PS_HYPER_CHARGE, 0);
	}

	@Inject(at = @At("HEAD"), method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
	private final void addAdditionalSaveData(final CompoundTag nbtIn, final CallbackInfo callbackIn) {
		nbtIn.putInt("HyperCharge", this.psGetHyperCharge());
	}

	@Inject(at = @At("HEAD"), method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
	private final void readAdditionalSaveData(final CompoundTag nbtIn, final CallbackInfo callbackIn) {
		this.psSetHyperCharge(nbtIn.getInt("HyperCharge"));
	}

	@Override
	public final int psGetHyperCharge() {
		final LivingEntity self = (LivingEntity)(Object)this;
		return self.getEntityData().get(PS_HYPER_CHARGE);
	}

	@Override
	public final void psSetHyperCharge(final int hyperChargeIn) {
		final LivingEntity self = (LivingEntity)(Object)this;
		self.getEntityData().set(PS_HYPER_CHARGE, hyperChargeIn);
	}
}
