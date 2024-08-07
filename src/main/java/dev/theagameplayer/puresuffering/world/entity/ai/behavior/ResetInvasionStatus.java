package dev.theagameplayer.puresuffering.world.entity.ai.behavior;

import dev.theagameplayer.puresuffering.invasion.InvasionSession;
import dev.theagameplayer.puresuffering.world.level.saveddata.InvasionLevelData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.schedule.Activity;

public final class ResetInvasionStatus {
	public final static BehaviorControl<LivingEntity> create() {
		return BehaviorBuilder.create(builder -> builder.point((level, entity, gameTime) -> {
			if (level.random.nextInt(20) != 0) {
				return false;
			} else {
				final InvasionSession session = InvasionLevelData.get(level).getInvasionManager().getActiveSession(level);
				if (session != null) return true;
				final Brain<?> brain = entity.getBrain();
				brain.setDefaultActivity(Activity.IDLE);
				brain.updateActivityFromSchedule(level.getDayTime(), level.getGameTime());
				return true;
			}
		}));
	}
}
