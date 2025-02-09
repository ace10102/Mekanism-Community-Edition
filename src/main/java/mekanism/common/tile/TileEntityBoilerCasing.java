package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.content.boiler.BoilerCache;
import mekanism.common.content.boiler.BoilerUpdateProtocol;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityBoilerCasing extends TileEntityMultiblock<SynchronizedBoilerData> implements IHeatTransfer
{
	/** A client-sided set of valves on this tank's structure that are currently active, used on the client for rendering fluids. */
	public Set<ValveData> valveViewing = new HashSet<ValveData>();

	/** The capacity this tank has on the client-side. */
	public int clientWaterCapacity;
	public int clientSteamCapacity;

	public float prevWaterScale;

	public TileEntityBoilerCasing()
	{
		this("BoilerCasing");
	}

	public TileEntityBoilerCasing(String name)
	{
		super(name);
		inventory = new ItemStack[2];
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(worldObj.isRemote)
		{
			if(structure != null && clientHasStructure && isRendering)
			{
				float targetScale = (float)(structure.waterStored != null ? structure.waterStored.amount : 0)/clientWaterCapacity;

				if(Math.abs(prevWaterScale - targetScale) > 0.01)
				{
					prevWaterScale = (9*prevWaterScale + targetScale)/10;
				}
			}

			if(!clientHasStructure || !isRendering)
			{
				for(ValveData data : valveViewing)
				{
					TileEntityBoilerCasing tileEntity = (TileEntityBoilerCasing)data.location.getTileEntity(worldObj);

					if(tileEntity != null)
					{
						tileEntity.clientHasStructure = false;
					}
				}

				valveViewing.clear();
			}
		}

		if(!worldObj.isRemote)
		{
			if(structure != null)
			{
				if(structure.waterStored != null && structure.waterStored.amount <= 0)
				{
					structure.waterStored = null;
					markDirty();
				}
				
				if(structure.steamStored != null && structure.steamStored.amount <= 0)
				{
					structure.steamStored = null;
					markDirty();
				}
				
				if(isRendering)
				{
					boolean needsValveUpdate = false;
					
					for(ValveData data : structure.valves)
					{
						if(data.activeTicks > 0)
						{
							data.activeTicks--;
						}
						
						if(data.activeTicks > 0 != data.prevActive)
						{
							needsValveUpdate = true;
						}
						
						data.prevActive = data.activeTicks > 0;
					}
					
					boolean needsHotUpdate = false;
					boolean newHot = structure.temperature >= SynchronizedBoilerData.BASE_BOIL_TEMP-0.01F;
					
					if(newHot != structure.clientHot)
					{
						needsHotUpdate = true;
						structure.clientHot = newHot;
					}
					
					double[] d = structure.simulateHeat();
					structure.applyTemperatureChange();
					structure.lastEnvironmentLoss = d[1];
					
					if(structure.temperature >= SynchronizedBoilerData.BASE_BOIL_TEMP && structure.waterStored != null)
					{
						int steamAmount = structure.steamStored != null ? structure.steamStored.amount : 0;
						double heatAvailable = structure.getHeatAvailable();
						
						structure.lastMaxBoil = (int)Math.floor(heatAvailable / SynchronizedBoilerData.getHeatEnthalpy());
								
						int amountToBoil = Math.min(structure.lastMaxBoil, structure.waterStored.amount);
						amountToBoil = Math.min(amountToBoil, (structure.steamVolume*BoilerUpdateProtocol.STEAM_PER_TANK)-steamAmount);
						structure.waterStored.amount -= amountToBoil;
						
						if(structure.steamStored == null)
						{
							structure.steamStored = new FluidStack(FluidRegistry.getFluid("steam"), amountToBoil);
						}
						else {
							structure.steamStored.amount += amountToBoil;
						}
						
						structure.temperature -= (amountToBoil*SynchronizedBoilerData.getHeatEnthalpy())/structure.locations.size();
						structure.lastBoilRate = amountToBoil;
					}
					else {
						structure.lastBoilRate = 0;
						structure.lastMaxBoil = 0;
					}
					
					if(needsValveUpdate || structure.needsRenderUpdate() || needsHotUpdate)
					{
						sendPacketToRenderer();
					}
					
					structure.prevWater = structure.waterStored != null ? structure.waterStored.copy() : null;
					structure.prevSteam = structure.steamStored != null ? structure.steamStored.copy() : null;
					
					MekanismUtils.saveChunk(this);
				}
			}
		}
	}
	
	@Override
	public boolean onActivate(EntityPlayer player)
	{
		if(!player.isSneaking() && structure != null)
		{
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
			player.openGui(Mekanism.instance, 54, worldObj, xCoord, yCoord, zCoord);
			
			return true;
		}
		
		return false;
	}

	@Override
	protected SynchronizedBoilerData getNewStructure()
	{
		return new SynchronizedBoilerData();
	}
	
	@Override
	public BoilerCache getNewCache()
	{
		return new BoilerCache();
	}

	@Override
	protected BoilerUpdateProtocol getProtocol()
	{
		return new BoilerUpdateProtocol(this);
	}

	@Override
	public MultiblockManager<SynchronizedBoilerData> getManager()
	{
		return Mekanism.boilerManager;
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		if(structure != null)
		{
			data.add(structure.waterVolume*BoilerUpdateProtocol.WATER_PER_TANK);
			data.add(structure.steamVolume*BoilerUpdateProtocol.STEAM_PER_TANK);
			data.add(structure.lastEnvironmentLoss);
			data.add(structure.lastBoilRate);
			data.add(structure.superheatingElements);
			data.add(structure.temperature);
			data.add(structure.lastMaxBoil);

			if(structure.waterStored != null)
			{
				data.add(1);
				data.add(structure.waterStored.getFluidID());
				data.add(structure.waterStored.amount);
			}
			else {
				data.add(0);
			}

			if(structure.steamStored != null)
			{
				data.add(1);
				data.add(structure.steamStored.getFluidID());
				data.add(structure.steamStored.amount);
			}
			else {
				data.add(0);
			}

			structure.upperRenderLocation.write(data);
			
			if(isRendering)
			{
				data.add(structure.clientHot);
				
				Set<ValveData> toSend = new HashSet<ValveData>();

				for(ValveData valveData : structure.valves)
				{
					if(valveData.activeTicks > 0)
					{
						toSend.add(valveData);
					}
				}
				
				data.add(toSend.size());
				
				for(ValveData valveData : toSend)
				{
					valveData.location.write(data);
					data.add(valveData.side.ordinal());
				}
			}
		}

		return data;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);

		if(worldObj.isRemote)
		{
			if(clientHasStructure)
			{
				clientWaterCapacity = dataStream.readInt();
				clientSteamCapacity = dataStream.readInt();
				structure.lastEnvironmentLoss = dataStream.readDouble();
				structure.lastBoilRate = dataStream.readInt();
				structure.superheatingElements = dataStream.readInt();
				structure.temperature = dataStream.readDouble();
				structure.lastMaxBoil = dataStream.readInt();
				
				if(dataStream.readInt() == 1)
				{
					structure.waterStored = new FluidStack(FluidRegistry.getFluid(dataStream.readInt()), dataStream.readInt());
				}
				else {
					structure.waterStored = null;
				}
		
				if(dataStream.readInt() == 1)
				{
					structure.steamStored = new FluidStack(FluidRegistry.getFluid(dataStream.readInt()), dataStream.readInt());
				}
				else {
					structure.steamStored = null;
				}
				
				structure.upperRenderLocation = Coord4D.read(dataStream);
		
				if(isRendering)
				{
					structure.clientHot = dataStream.readBoolean();
					SynchronizedBoilerData.clientHotMap.put(structure.inventoryID, structure.clientHot);
					
					int size = dataStream.readInt();
					
					valveViewing.clear();
	
					for(int i = 0; i < size; i++)
					{
						ValveData data = new ValveData();
						data.location = Coord4D.read(dataStream);
						data.side = ForgeDirection.getOrientation(dataStream.readInt());
						
						valveViewing.add(data);
	
						TileEntityBoilerCasing tileEntity = (TileEntityBoilerCasing)data.location.getTileEntity(worldObj);
	
						if(tileEntity != null)
						{
							tileEntity.clientHasStructure = true;
						}
					}
				}
			}
		}
	}

	public int getScaledWaterLevel(int i)
	{
		if(clientWaterCapacity == 0 || structure.waterStored == null)
		{
			return 0;
		}

		return structure.waterStored.amount*i / clientWaterCapacity;
	}

	public int getScaledSteamLevel(int i)
	{
		if(clientSteamCapacity == 0 || structure.steamStored == null)
		{
			return 0;
		}

		return structure.steamStored.amount*i / clientSteamCapacity;
	}

	@Override
	public double getTemp()
	{
		return 0;
	}

	@Override
	public double getInverseConductionCoefficient()
	{
		return SynchronizedBoilerData.CASING_INVERSE_CONDUCTION_COEFFICIENT;
	}

	@Override
	public double getInsulationCoefficient(ForgeDirection side)
	{
		return SynchronizedBoilerData.CASING_INSULATION_COEFFICIENT;
	}

	@Override
	public void transferHeatTo(double heat)
	{
		if(structure != null)
		{
			structure.heatToAbsorb += heat;
		}
	}

	@Override
	public double[] simulateHeat()
	{
		return new double[] {0, 0};
	}

	@Override
	public double applyTemperatureChange()
	{
		return 0;
	}

	@Override
	public boolean canConnectHeat(ForgeDirection side)
	{
		return structure != null;
	}

	@Override
	public IHeatTransfer getAdjacent(ForgeDirection side)
	{
		return null;
	}
	
	@Override
	public String getInventoryName()
	{
		return LangUtils.localize("gui.thermoelectricBoiler");
	}
	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}
	@Override
	public void setInventorySlotContents(int slotID, ItemStack itemstack)
	{
		inventory[slotID] = itemstack;

		if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}
	}
}
