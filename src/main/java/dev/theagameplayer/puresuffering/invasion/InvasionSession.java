package dev.theagameplayer.puresuffering.invasion;

import java.util.ArrayList;
import java.util.Iterator;

import dev.theagameplayer.puresuffering.invasion.InvasionType.WeatherType;
import dev.theagameplayer.puresuffering.network.AddInvasionPacket;
import dev.theagameplayer.puresuffering.network.ClearInvasionsPacket;
import dev.theagameplayer.puresuffering.network.RemoveInvasionPacket;
import dev.theagameplayer.puresuffering.registries.other.PSPackets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

public final class InvasionSession implements Iterable<Invasion> {
	private final ArrayList<Invasion> invasions = new ArrayList<>();
	private final InvasionSessionType sessionType;
	private final InvasionDifficulty difficulty;
	private final Style style;
	private WeatherType weatherType = WeatherType.DEFAULT;
	private int weatherChangeDelay, lightLevel;
	private boolean stopsConversions, forceNoSleep;

	public InvasionSession(final InvasionSessionType sessionTypeIn, final InvasionDifficulty difficultyIn) {
		this.sessionType = sessionTypeIn;
		this.difficulty = difficultyIn;
		this.style = Style.EMPTY.withBold(difficultyIn.isHyper()).withItalic(difficultyIn.isNightmare());
	}

	public final Invasion getPrimary() {
		return this.invasions.get(0);
	}

	public final InvasionSessionType getSessionType() {
		return this.sessionType;
	}

	public final InvasionDifficulty getDifficulty() {
		return this.difficulty;
	}

	public final Style getStyle() {
		return this.style;
	}
	
	public final boolean replaceMob(final Mob oldMobIn, final Mob mobIn) {
		for (final Invasion invasion : this.invasions) {
			if (invasion.hasMob(oldMobIn)) {
				invasion.replaceMob(oldMobIn, mobIn);
				return true;
			}
		}
		return false;
	}

	public final void relocateMob(final Mob mobIn) {
		for (final Invasion invasion : this.invasions) {
			if (invasion.hasMob(mobIn)) invasion.relocateMob(mobIn);
		}
	}
	
	public final boolean hasMob(final Mob mobIn) {
		for (final Invasion invasion : this.invasions) {
			if (invasion.hasMob(mobIn)) return true;
		}
		return false;
	}
	
	public final void loadMob(final Mob mobIn) {
		for (final Invasion invasion : this.invasions) {
			if (invasion.loadMob(mobIn)) return;
		}
	}

	private final void update() {
		this.weatherType = WeatherType.DEFAULT;
		this.stopsConversions = false;
		this.forceNoSleep = this.difficulty.isNightmare();
		int lightLevel = 0, lc = 0;
		for (final Invasion invasion : this.invasions) {
			if (this.weatherType == WeatherType.DEFAULT)
				this.weatherType = invasion.getType().getWeatherType();
			this.stopsConversions |= invasion.getType().stopsConversions();
			this.forceNoSleep |= invasion.getSeverityInfo().forcesNoSleep();
			if (this.difficulty.isNightmare()) continue;
			final int ll = invasion.getSeverityInfo().getSkyRenderInfo().getLightLevel();
			if (ll < 0) continue;
			lightLevel += ll;
			lc++;
		}
		this.lightLevel = this.difficulty.isNightmare() ? 15 : (lc > 0 ? lightLevel/lc : -1);
	}

	public final boolean stopOrTick(final ServerLevel levelIn) {
		if (this.invasions.isEmpty()) return true;
		switch (this.weatherType) {
		case DEFAULT: break;
		case CLEAR: {
			if (levelIn.isRaining() || levelIn.isThundering())
				levelIn.setWeatherParameters(12000 - (int)(levelIn.dayTime() % 12000L), 0, false, false);
			break;
		}
		case RAIN: {
			if (!levelIn.isRaining())
				levelIn.setWeatherParameters(0, 12000 - (int)(levelIn.dayTime() % 12000L), true, false);
			break;
		}
		case THUNDER: {
			if (!levelIn.isThundering())
				levelIn.setWeatherParameters(0, 12000 - (int)(levelIn.dayTime() % 12000L), true, true);
			break;
		}
		case UNSTABLE: {
			if (this.weatherChangeDelay < 0) {
				final int type = levelIn.random.nextInt(5);
				final int time = Math.min(150 + levelIn.random.nextInt(Invasion.HALF_TRANSITION + 1), 12000 - (int)(levelIn.dayTime() % 12000L));
				levelIn.setWeatherParameters(type < 2 ? time : 0, type < 2 ? 0 : time, type == 2, type > 2);
				this.weatherChangeDelay = time;
			} else {
				this.weatherChangeDelay--;
			}
			break;
		}
		}
		this.invasions.get((int)(levelIn.getDayTime() % this.invasions.size())).tick(levelIn, this.difficulty, this.invasions.size());
		return false;
	}

	public final boolean stopsConversions() {
		return this.stopsConversions;
	}

	public final boolean forcesNoSleep() {
		return this.forceNoSleep;
	}

	public final int getLightLevelOrDefault(final int lightLevelIn) {
		return this.lightLevel > 0 ? this.lightLevel : lightLevelIn;
	}

	public static final InvasionSession load(final ServerLevel levelIn, final CompoundTag nbtIn) {
		final InvasionSession session = new InvasionSession(InvasionSessionType.getActive(levelIn), InvasionDifficulty.values()[nbtIn.getInt("Difficulty")]);
		final ListTag invasionsNBT = nbtIn.getList(session.sessionType.getDefaultName() + "Invasions", Tag.TAG_COMPOUND);
		for (final Tag inbt : invasionsNBT) {
			if (inbt instanceof CompoundTag nbt)
				session.add(levelIn, Invasion.load(levelIn, nbt));
		}
		return session;
	}

	public final CompoundTag save() {
		final CompoundTag nbt = new CompoundTag();
		final ListTag invasionsNBT = new ListTag();
		nbt.putInt("Difficulty", this.difficulty.ordinal());
		for (final Invasion invasion : this.invasions)
			invasionsNBT.add(invasion.save());
		nbt.put(this.sessionType.getDefaultName() + "Invasions", invasionsNBT);
		return nbt;
	}

	//Sync to Client
	public final void add(final ServerLevel levelIn, final Invasion invasionIn) {
		this.invasions.add(invasionIn);
		this.update();
		PSPackets.sendToClientsIn(new AddInvasionPacket(this.sessionType, this.difficulty, invasionIn), levelIn);
	}

	public final void remove(final ServerLevel levelIn, final Invasion invasionIn) {
		if (invasionIn.isPrimary()) {
			this.clear(levelIn);
			return;
		}
		this.invasions.remove(invasionIn);
		this.update();
		PSPackets.sendToClientsIn(new RemoveInvasionPacket(invasionIn.getSeverityInfo().getSkyRenderInfo()), levelIn);
	}

	public final void clear(final ServerLevel levelIn) {
		this.invasions.clear();
		PSPackets.sendToClientsIn(new ClearInvasionsPacket(), levelIn);
	}

	public final void updateClient(final ServerPlayer playerIn) {
		PSPackets.sendToClient(new ClearInvasionsPacket(), playerIn);
		for (int index = 0; index < this.invasions.size(); index++) {
			final Invasion invasion = this.invasions.get(index);
			PSPackets.sendToClient(new AddInvasionPacket(this.sessionType, this.difficulty, invasion), playerIn);
		}
	}

	@Override
	public final String toString() {
		return this.invasions.toString();
	}

	@Override
	public final Iterator<Invasion> iterator() {
		return this.invasions.iterator();
	}
}
