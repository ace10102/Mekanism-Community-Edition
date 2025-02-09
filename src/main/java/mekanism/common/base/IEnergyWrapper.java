package mekanism.common.base;

import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.tile.IEnergyStorage;
import mekanism.api.energy.ICableOutputter;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyStorage;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;

@InterfaceList({
	@Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2"),
	@Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = "IC2"),
	@Interface(iface = "ic2.api.tile.IEnergyStorage", modid = "IC2")
})
public interface IEnergyWrapper extends IStrictEnergyStorage, IEnergyHandler, IEnergySink, IEnergySource, IEnergyStorage, IStrictEnergyAcceptor, ICableOutputter, IInventory
{
	public EnumSet<ForgeDirection> getOutputtingSides();

	public EnumSet<ForgeDirection> getConsumingSides();

	public double getMaxOutput();
}
