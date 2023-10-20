package dev.theagameplayer.puresuffering.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.theagameplayer.puresuffering.world.entity.PSInvasionMob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Mob;

@Mixin(Mob.class)
public final class MobMixin implements PSInvasionMob {
	private static final EntityDataAccessor<Integer> PS_HYPER_CHARGE = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.INT);

	@Inject(at = @At("RETURN"), method = "defineSynchedData()V")
	private final void defineSynchedData(final CallbackInfo callbackIn) {
		((Mob)(Object)this).getEntityData().define(PS_HYPER_CHARGE, 0);
	}

	@Inject(at = @At("HEAD"), method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
	private final void addAdditionalSaveData(final CompoundTag nbtIn, final CallbackInfo callbackIn) {
		nbtIn.putInt(HYPER_CHARGE, this.psGetHyperCharge());
	}

	@Inject(at = @At("HEAD"), method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V")
	private final void readAdditionalSaveData(final CompoundTag nbtIn, final CallbackInfo callbackIn) {
		this.psSetHyperCharge(nbtIn.getInt(HYPER_CHARGE));
	}

	@Override
	public final int psGetHyperCharge() {
		return ((Mob)(Object)this).getEntityData().get(PS_HYPER_CHARGE);
	}

	@Override
	public final void psSetHyperCharge(final int hyperChargeIn) {
		((Mob)(Object)this).getEntityData().set(PS_HYPER_CHARGE, hyperChargeIn);
	}
}
