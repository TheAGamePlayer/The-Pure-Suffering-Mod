package dev.theagameplayer.puresuffering.util;

import java.util.ArrayList;
import java.util.Iterator;

import dev.theagameplayer.puresuffering.invasion.Invasion;
import dev.theagameplayer.puresuffering.network.InvasionListType;
import dev.theagameplayer.puresuffering.network.PSPacketHandler;
import dev.theagameplayer.puresuffering.network.packet.AddInvasionPacket;
import dev.theagameplayer.puresuffering.network.packet.ClearInvasionsPacket;
import dev.theagameplayer.puresuffering.network.packet.RemoveInvasionPacket;
import dev.theagameplayer.puresuffering.network.packet.UpdateCountPacket;
import net.minecraft.entity.player.ServerPlayerEntity;

public final class InvasionList implements Iterable<Invasion> {
	private final ArrayList<Invasion> invasionList = new ArrayList<>();
	private final InvasionListType type;
	
	public InvasionList(InvasionListType typeIn) {
		this.type = typeIn;
	}
	
	public boolean add(Invasion invasionIn) {
		boolean result = this.invasionList.add(invasionIn);
		if (!invasionIn.getType().getSkyRenderer().isEmpty() && (this.type != InvasionListType.LIGHT || invasionIn.getType().getSkyRenderer().get(invasionIn.getSeverity() - 1).getBrightness() != 0.0F)) {
			PSPacketHandler.sendToAllClients(new AddInvasionPacket(invasionIn.getType().getSkyRenderer().get(invasionIn.getSeverity() - 1), this.type));
		}
		PSPacketHandler.sendToAllClients(new UpdateCountPacket(this.size(), this.type));
		return result;
	}
	
	public boolean remove(Invasion invasionIn) {
		boolean result = this.invasionList.remove(invasionIn);
		if (!invasionIn.getType().getSkyRenderer().isEmpty()) {
			PSPacketHandler.sendToAllClients(new RemoveInvasionPacket(invasionIn.getType().getSkyRenderer().get(invasionIn.getSeverity() - 1), this.type));
		}
		PSPacketHandler.sendToAllClients(new UpdateCountPacket(this.size(), this.type));
		return result;
	}

	public void clear() {
		this.invasionList.clear();
		PSPacketHandler.sendToAllClients(new ClearInvasionsPacket(this.type));
		PSPacketHandler.sendToAllClients(new UpdateCountPacket(this.size(), this.type));
	}
	
	public void update(ServerPlayerEntity playerIn) {
		PSPacketHandler.sendToClient(new ClearInvasionsPacket(this.type), playerIn);
		for (int index = 0; index < this.size(); index++) {
			Invasion invasion = this.get(index);
			if (!invasion.getType().getSkyRenderer().isEmpty() && (this.type != InvasionListType.LIGHT || invasion.getType().getSkyRenderer().get(invasion.getSeverity() - 1).getBrightness() != 0.0F)) {
				PSPacketHandler.sendToClient(new AddInvasionPacket(invasion.getType().getSkyRenderer().get(invasion.getSeverity() - 1), this.type), playerIn);
			}
		}
		if (this.type != InvasionListType.LIGHT)
			PSPacketHandler.sendToClient(new UpdateCountPacket(this.size(), this.type), playerIn);
	}
	
	//ArrayList methods
    public int size() {
    	return this.invasionList.size();
    }
	
    public boolean isEmpty() {
        return this.invasionList.isEmpty();
    }
    
    public boolean contains(Object objIn) {
    	return this.invasionList.contains(objIn);
    }
    
    public Invasion get(int indexIn) {
    	return this.invasionList.get(indexIn);
    }
    
    @Override
    public String toString() {
    	return this.invasionList.toString();
    }

    //Iterable<Invasion>
	@Override
	public Iterator<Invasion> iterator() {
		return this.invasionList.iterator();
	}
}
