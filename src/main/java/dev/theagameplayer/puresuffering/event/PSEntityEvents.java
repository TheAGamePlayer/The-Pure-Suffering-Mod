package dev.theagameplayer.puresuffering.event;

import dev.theagameplayer.puresuffering.config.PSConfigValues;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Vex;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.eventbus.api.Event.Result;

public final class PSEntityEvents {
	public static final void joinLevel(final EntityJoinLevelEvent eventIn) {
		if (eventIn.getEntity() instanceof TamableAnimal) {
			final TamableAnimal tameableEntity = (TamableAnimal)eventIn.getEntity();
			if (tameableEntity.getOwner() != null && tameableEntity.getOwner().getPersistentData().contains("AntiGrief")) {
				tameableEntity.getPersistentData().putBoolean("AntiGrief", tameableEntity.getOwner().getPersistentData().getBoolean("AntiGrief"));
			}
		} else if (PSConfigValues.common.weakenedVexes && eventIn.getEntity() instanceof Vex) {
			final Vex vexEntity = (Vex)eventIn.getEntity();
			if (vexEntity.getOwner() != null && vexEntity.getOwner().getPersistentData().contains("InvasionMob")) {
				vexEntity.setLimitedLife(25 + eventIn.getLevel().getRandom().nextInt(65)); //Attempt to fix lag & spawn camping with vexes
			}
		}
	}

	public static final void mobGriefing(final EntityMobGriefingEvent eventIn) {
		if (eventIn.getEntity().getPersistentData().contains("AntiGrief") && eventIn.getEntity() != null) {
			if (!PSConfigValues.common.invasionAntiGrief)
				eventIn.setResult(Result.DENY);
		}
	}
}
