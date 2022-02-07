package dev.theagameplayer.puresuffering.util;

import java.util.ArrayList;
import java.util.Iterator;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.AddInvasionPacket;
import dev.theagameplayer.puresuffering.network.packet.ClearInvasionsPacket;
import dev.theagameplayer.puresuffering.network.packet.RemoveInvasionPacket;
import dev.theagameplayer.puresuffering.network.packet.UpdateCountPacket;
import net.minecraft.entity.player.ServerPlayerEntity;

public final class InvasionList implements Iterable<Invasion> {
	private final ArrayList<Invasion> invasionList = new ArrayList<>();
	private final InvasionListType listType;
	private boolean isCanceled;
	
	public InvasionList(InvasionListType listTypeIn) {
		this.listType = listTypeIn;
	}
	
	public void setCanceled(boolean isCanceledIn) {
		this.isCanceled = isCanceledIn;
	}
	
	public boolean isCanceled() {
		return this.isCanceled;
	}
	
	public boolean add(Invasion invasionIn) {
		boolean result = this.invasionList.add(invasionIn);
		if (invasionIn.getType().getSeverityInfo().get(invasionIn.getSeverity()).getSkyRenderer() != null) {
			PSPacketHandler.sendToAllClients(new AddInvasionPacket(invasionIn.getType().getSeverityInfo().get(invasionIn.getSeverity()).getSkyRenderer(), this.listType, invasionIn.isPrimary()));
		}
		PSPacketHandler.sendToAllClients(new UpdateCountPacket(this.size(), this.listType));
		return result;
	}
	
	public boolean remove(Invasion invasionIn) {
		if (invasionIn.isPrimary()) {
			this.clear();
			return true;
		}
		boolean result = this.invasionList.remove(invasionIn);
		if (invasionIn.getType().getSeverityInfo().get(invasionIn.getSeverity()).getSkyRenderer() != null) {
			PSPacketHandler.sendToAllClients(new RemoveInvasionPacket(invasionIn.getType().getSeverityInfo().get(invasionIn.getSeverity()).getSkyRenderer(), this.listType));
		}
		return result;
	}

	public void clear() {
		this.invasionList.clear();
		PSPacketHandler.sendToAllClients(new ClearInvasionsPacket(this.listType));
		PSPacketHandler.sendToAllClients(new UpdateCountPacket(this.size(), this.listType));
	}
	
	public void update(ServerPlayerEntity playerIn) {
		PSPacketHandler.sendToClient(new ClearInvasionsPacket(this.listType), playerIn);
		for (int index = 0; index < this.size(); index++) {
			Invasion invasion = this.get(index);
			if (invasion.getType().getSeverityInfo().get(invasion.getSeverity()).getSkyRenderer() != null) {
				PSPacketHandler.sendToClient(new AddInvasionPacket(invasion.getType().getSeverityInfo().get(invasion.getSeverity()).getSkyRenderer(), this.listType, invasion.isPrimary()), playerIn);
			}
		}
		PSPacketHandler.sendToClient(new UpdateCountPacket(this.size(), this.listType), playerIn);
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