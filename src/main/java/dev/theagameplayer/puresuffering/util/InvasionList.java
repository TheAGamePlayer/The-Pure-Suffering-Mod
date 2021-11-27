package dev.theagameplayer.puresuffering.util;

import java.util.ArrayList;
import java.util.Iterator;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.AddInvasionPacket;
import dev.theagameplayer.puresuffering.network.packet.ClearInvasionsPacket;
import dev.theagameplayer.puresuffering.network.packet.UpdateCountPacket;
import net.minecraft.entity.player.ServerPlayerEntity;

public final class InvasionList implements Iterable<Invasion> {
	private final ArrayList<Invasion> invasionList = new ArrayList<>();
	private final boolean isDay;
	
	public InvasionList(boolean isDayIn) {
		this.isDay = isDayIn;
	}
	
	public boolean add(Invasion invasionIn) {
		boolean result = this.invasionList.add(invasionIn);
		if (invasionIn.getType().getSeverityInfo().get(invasionIn.getSeverity()).getSkyRenderer() != null) {
			PSPacketHandler.sendToAllClients(new AddInvasionPacket(invasionIn.getType().getSeverityInfo().get(invasionIn.getSeverity()).getSkyRenderer(), this.isDay, invasionIn.isPrimary()));
		}
		PSPacketHandler.sendToAllClients(new UpdateCountPacket(this.size(), this.isDay));
		return result;
	}

	public void clear() {
		this.invasionList.clear();
		PSPacketHandler.sendToAllClients(new ClearInvasionsPacket(this.isDay));
		PSPacketHandler.sendToAllClients(new UpdateCountPacket(this.size(), this.isDay));
	}
	
	public void update(ServerPlayerEntity playerIn) {
		PSPacketHandler.sendToClient(new ClearInvasionsPacket(this.isDay), playerIn);
		for (int index = 0; index < this.size(); index++) {
			Invasion invasion = this.get(index);
			if (invasion.getType().getSeverityInfo().get(invasion.getSeverity()).getSkyRenderer() != null) {
				PSPacketHandler.sendToClient(new AddInvasionPacket(invasion.getType().getSeverityInfo().get(invasion.getSeverity()).getSkyRenderer(), this.isDay, invasion.isPrimary()), playerIn);
			}
		}
		PSPacketHandler.sendToClient(new UpdateCountPacket(this.size(), this.isDay), playerIn);
	}
	
	//ArrayList methods
    public int size() {
    	return this.invasionList.size();
    }
	
    public boolean isEmpty() {
        return this.invasionList.isEmpty();
    }
    
    public boolean contains(Invasion invasionIn) {
    	return this.invasionList.contains(invasionIn);
    }
    
    public Invasion get(int indexIn) {
    	return this.invasionList.get(indexIn);
    }
    
    @Override
    public String toString() {
    	return this.invasionList.toString();
    }

	@Override
	public Iterator<Invasion> iterator() {
		return this.invasionList.iterator();
	}
}