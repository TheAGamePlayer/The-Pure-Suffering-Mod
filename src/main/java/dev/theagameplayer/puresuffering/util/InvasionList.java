package dev.theagameplayer.puresuffering.util;

import java.util.ArrayList;
import java.util.Iterator;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.AddInvasionPacket;
import dev.theagameplayer.puresuffering.network.packet.ClearInvasionsPacket;
import dev.theagameplayer.puresuffering.network.packet.RemoveInvasionPacket;
import dev.theagameplayer.puresuffering.network.packet.UpdateCountPacket;
import net.minecraft.server.level.ServerPlayer;

public final class InvasionList implements Iterable<Invasion> {
	private final ArrayList<Invasion> invasionList = new ArrayList<>();
	private final InvasionListType listType;
	private boolean isCanceled;
	
	public InvasionList(final InvasionListType listTypeIn) {
		this.listType = listTypeIn;
	}
	
	public final void setCanceled(final boolean isCanceledIn) {
		this.isCanceled = isCanceledIn;
	}
	
	public final boolean isCanceled() {
		return this.isCanceled;
	}
	
	public final boolean add(final Invasion invasionIn) {
		final boolean result = this.invasionList.add(invasionIn);
		if (invasionIn.getType().getSeverityInfo().get(invasionIn.getSeverity()).getSkyRenderer() != null)
			PSPacketHandler.sendToAllClients(new AddInvasionPacket(invasionIn.getType().getSeverityInfo().get(invasionIn.getSeverity()).getSkyRenderer(), this.listType, invasionIn.isPrimary(), invasionIn.getHyperType()));
		PSPacketHandler.sendToAllClients(new UpdateCountPacket(this.size(), this.listType));
		return result;
	}
	
	public final boolean remove(final Invasion invasionIn) {
		if (invasionIn.isPrimary()) {
			this.clear();
			return true;
		}
		final boolean result = this.invasionList.remove(invasionIn);
		if (invasionIn.getType().getSeverityInfo().get(invasionIn.getSeverity()).getSkyRenderer() != null)
			PSPacketHandler.sendToAllClients(new RemoveInvasionPacket(invasionIn.getType().getSeverityInfo().get(invasionIn.getSeverity()).getSkyRenderer(), this.listType));
		return result;
	}

	public final void clear() {
		this.invasionList.clear();
		PSPacketHandler.sendToAllClients(new ClearInvasionsPacket(this.listType));
		PSPacketHandler.sendToAllClients(new UpdateCountPacket(this.size(), this.listType));
	}
	
	public final void update(final ServerPlayer playerIn) {
		PSPacketHandler.sendToClient(new ClearInvasionsPacket(this.listType), playerIn);
		for (int index = 0; index < this.size(); index++) {
			final Invasion invasion = this.get(index);
			if (invasion.getType().getSeverityInfo().get(invasion.getSeverity()).getSkyRenderer() != null)
				PSPacketHandler.sendToClient(new AddInvasionPacket(invasion.getType().getSeverityInfo().get(invasion.getSeverity()).getSkyRenderer(), this.listType, invasion.isPrimary(), invasion.getHyperType()), playerIn);
		}
		PSPacketHandler.sendToClient(new UpdateCountPacket(this.size(), this.listType), playerIn);
	}
	
	//ArrayList methods
    public final int size() {
    	return this.invasionList.size();
    }
	
    public final boolean isEmpty() {
        return this.invasionList.isEmpty();
    }
    
    public final boolean contains(final Invasion invasionIn) {
    	return this.invasionList.contains(invasionIn);
    }
    
    public final Invasion get(final int indexIn) {
    	return this.invasionList.get(indexIn);
    }
    
    @Override
    public final String toString() {
    	return this.invasionList.toString();
    }

	@Override
	public final Iterator<Invasion> iterator() {
		return this.invasionList.iterator();
	}
}