package mekanism.common.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.client;
import mekanism.api.MekanismConfig.general;
import mekanism.api.MekanismConfig.machines;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.energy.IEnergizedItem;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DefIcon;
import mekanism.client.render.MekanismRenderer.ICustomBlockIcon;
import mekanism.common.CTMData;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.FluidTankTier;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBlockCTM;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.IFactory;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ISpecialBounds;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.ITierItem;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.recipe.ShapedMekanismRecipe;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.TileEntityAdvancedFactory;
import mekanism.common.tile.TileEntityAmbientAccumulator;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.TileEntityChemicalInfuser;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.TileEntityChemicalOxidizer;
import mekanism.common.tile.TileEntityChemicalWasher;
import mekanism.common.tile.TileEntityCombiner;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.tile.TileEntityCrusher;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEliteFactory;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.TileEntityFuelwoodHeater;
import mekanism.common.tile.TileEntityLaser;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPRC;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Block class for handling multiple machine block IDs.
 * 0:0: Enrichment Chamber
 * 0:1: Osmium Compressor
 * 0:2: Combiner
 * 0:3: Crusher
 * 0:4: Digital Miner
 * 0:5: Basic Factory
 * 0:6: Advanced Factory
 * 0:7: Elite Factory
 * 0:8: Metallurgic Infuser
 * 0:9: Purification Chamber
 * 0:10: Energized Smelter
 * 0:11: Teleporter
 * 0:12: Electric Pump
 * 0:13: Electric Chest
 * 0:14: Chargepad
 * 0:15: Logistical Sorter
 * 1:0: Rotary Condensentrator
 * 1:1: Chemical Oxidizer
 * 1:2: Chemical Infuser
 * 1:3: Chemical Injection Chamber
 * 1:4: Electrolytic Separator
 * 1:5: Precision Sawmill
 * 1:6: Chemical Dissolution Chamber
 * 1:7: Chemical Washer
 * 1:8: Chemical Crystallizer
 * 1:9: Seismic Vibrator
 * 1:10: Pressurized Reaction Chamber
 * 1:11: Fluid Tank
 * 1:12: Fluidic Plenisher
 * 1:13: Laser
 * 1:14: Laser Amplifier
 * 1:15: Laser Tractor Beam
 * 2:0: Quantum Entangloporter
 * 2:1: Solar Neutron Activator
 * 2:2: Ambient Accumulator
 * 2:3: Oredictionificator
 * 2:4: Resistive Heater
 * 2:5: Formulaic Assemblicator
 * 2:6: Fuelwood Heater
 * 
 * @author AidanBrady
 *
 */
public class BlockMachine extends BlockContainer implements ISpecialBounds, IBlockCTM, ICustomBlockIcon
{
	public IIcon[][] icons = new IIcon[16][16];
	public IIcon[][][] factoryIcons = new IIcon[4][16][16];
	
	public CTMData[][] ctms = new CTMData[16][4];
	
	public IIcon BASE_ICON;

	public MachineBlock blockType;

	public BlockMachine(MachineBlock type)
	{
		super(Material.iron);
		setHardness(3.5F);
		setResistance(16F);
		setCreativeTab(Mekanism.tabMekanism);
		blockType = type;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register)
	{
		BASE_ICON = register.registerIcon("mekanism:SteelCasing");
		DefIcon def = DefIcon.getAll(BASE_ICON).setOverrides(false);
		
		switch(blockType)
		{
			case MACHINE_BLOCK_1:
				ctms[11][0] = new CTMData("ctm/Teleporter", this, Arrays.asList(11)).addOtherBlockConnectivities(MekanismBlocks.BasicBlock, Arrays.asList(7)).registerIcons(register);
				
				MekanismRenderer.loadDynamicTextures(register, "enrichment_chamber/" + MachineType.ENRICHMENT_CHAMBER.name, icons[0], def);
				MekanismRenderer.loadDynamicTextures(register, "osmium_compressor/" + MachineType.OSMIUM_COMPRESSOR.name, icons[1], def);
				MekanismRenderer.loadDynamicTextures(register, "combiner/" + MachineType.COMBINER.name, icons[2], def);
				MekanismRenderer.loadDynamicTextures(register, "crusher/" + MachineType.CRUSHER.name, icons[3], def);
				
				for(RecipeType type : RecipeType.values())
				{
					MekanismRenderer.loadDynamicTextures(register, "factory/basic/" + type.getUnlocalizedName().toLowerCase() + "/" + BaseTier.BASIC.getName() + type.getUnlocalizedName() + MachineType.BASIC_FACTORY.name, factoryIcons[0][type.ordinal()],
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/basic/BasicFactoryFront"), 2).setOverrides(false),
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/basic/BasicFactoryTop"), 1).setOverrides(false), 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/basic/BasicFactoryBottom"), 0).setOverrides(false), 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/basic/BasicFactorySide"), 3, 4, 5).setOverrides(false));
					MekanismRenderer.loadDynamicTextures(register, "factory/advanced/" + type.getUnlocalizedName().toLowerCase() + "/" + BaseTier.ADVANCED.getName() + type.getUnlocalizedName() + MachineType.ADVANCED_FACTORY.name, factoryIcons[1][type.ordinal()], 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/advanced/AdvancedFactoryFront"), 2).setOverrides(false), 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/advanced/AdvancedFactoryTop"), 1).setOverrides(false), 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/advanced/AdvancedFactoryBottom"), 0).setOverrides(false), 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/advanced/AdvancedFactorySide"), 3, 4, 5).setOverrides(false));
					MekanismRenderer.loadDynamicTextures(register, "factory/elite/" + type.getUnlocalizedName().toLowerCase() + "/" + BaseTier.ELITE.getName() + type.getUnlocalizedName() + MachineType.ELITE_FACTORY.name, factoryIcons[2][type.ordinal()], 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/elite/EliteFactoryFront"), 2).setOverrides(false), 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/elite/EliteFactoryTop"), 1).setOverrides(false), 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/elite/EliteFactoryBottom"), 0).setOverrides(false), 
							DefIcon.getActivePair(register.registerIcon("mekanism:factory/elite/EliteFactorySide"), 3, 4, 5).setOverrides(false));
				}
				
				MekanismRenderer.loadDynamicTextures(register, "purification_chamber/" + MachineType.PURIFICATION_CHAMBER.name, icons[9], def);
				MekanismRenderer.loadDynamicTextures(register, "energized_smelter/" + MachineType.ENERGIZED_SMELTER.name, icons[10], def);
				icons[11][0] = ctms[11][0].mainTextureData.icon;
				
				break;
			case MACHINE_BLOCK_2:
				MekanismRenderer.loadDynamicTextures(register, "chemical_injection_chamber/" + MachineType.CHEMICAL_INJECTION_CHAMBER.name, icons[3], def);
				MekanismRenderer.loadDynamicTextures(register, "precision_sawmill/" + MachineType.PRECISION_SAWMILL.name, icons[5], def);
				
				break;
			case MACHINE_BLOCK_3:
				icons[0][0] = BASE_ICON;
				icons[2][0] = BASE_ICON;
				MekanismRenderer.loadDynamicTextures(register, "oredictionificator/" + MachineType.OREDICTIONIFICATOR.name, icons[3]);
				icons[4][0] = BASE_ICON;
				MekanismRenderer.loadDynamicTextures(register, "formulaic_assemblicator/" + MachineType.FORMULAIC_ASSEMBLICATOR.name, icons[5]);
				MekanismRenderer.loadDynamicTextures(register, "fuelwood_heater/" + MachineType.FUELWOOD_HEATER.name, icons[6]);
				
				break;
		}
	}
	
	@Override
	public IIcon getIcon(ItemStack stack, int side)
	{
		MachineType type = MachineType.get(stack);
		ItemBlockMachine item = (ItemBlockMachine)stack.getItem();
		
		if(type == MachineType.BASIC_FACTORY)
		{
			return factoryIcons[0][item.getRecipeType(stack)][side];
		}
		else if(type == MachineType.ADVANCED_FACTORY)
		{
			return factoryIcons[1][item.getRecipeType(stack)][side];
		}
		else if(type == MachineType.ELITE_FACTORY)
		{
			return factoryIcons[2][item.getRecipeType(stack)][side];
		}
		
		return getIcon(side, stack.getItemDamage());
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(x, y, z);
		int side = MathHelper.floor_double((entityliving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		int height = Math.round(entityliving.rotationPitch);
		int change = 3;

		if(tileEntity == null)
		{
			return;
		}

		if(tileEntity.canSetFacing(0) && tileEntity.canSetFacing(1))
		{
			if(height >= 65)
			{
				change = 1;
			}
			else if(height <= -65)
			{
				change = 0;
			}
		}

		if(change != 0 && change != 1)
		{
			switch(side)
			{
				case 0: change = 2; break;
				case 1: change = 5; break;
				case 2: change = 3; break;
				case 3: change = 4; break;
			}
		}

		if(tileEntity instanceof TileEntityLogisticalSorter)
		{
			TileEntityLogisticalSorter transporter = (TileEntityLogisticalSorter)tileEntity;

			if(!transporter.hasInventory())
			{
				for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
				{
					TileEntity tile = Coord4D.get(transporter).getFromSide(dir).getTileEntity(world);

					if(tile instanceof IInventory)
					{
						change = dir.getOpposite().ordinal();
						break;
					}
				}
			}
		}

		tileEntity.setFacing((short)change);
		tileEntity.redstone = world.isBlockIndirectlyGettingPowered(x, y, z);

		if(tileEntity instanceof IBoundingBlock)
		{
			((IBoundingBlock)tileEntity).onPlace();
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(x, y, z);

		if(tileEntity instanceof IBoundingBlock)
		{
			((IBoundingBlock)tileEntity).onBreak();
		}

		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random random)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(x, y, z);

		if(MekanismUtils.isActive(world, x, y, z) && ((IActiveState)tileEntity).renderUpdate() && client.machineEffects)
		{
			float xRandom = (float)x + 0.5F;
			float yRandom = (float)y + 0.0F + random.nextFloat() * 6.0F / 16.0F;
			float zRandom = (float)z + 0.5F;
			float iRandom = 0.52F;
			float jRandom = random.nextFloat() * 0.6F - 0.3F;

			int side = tileEntity.facing;

			if(tileEntity instanceof TileEntityMetallurgicInfuser)
			{
				side = ForgeDirection.getOrientation(side).getOpposite().ordinal();
			}

			if(side == 4)
			{
				world.spawnParticle("smoke", (xRandom - iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("reddust", (xRandom - iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
			}
			else if(side == 5)
			{
				world.spawnParticle("smoke", (xRandom + iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("reddust", (xRandom + iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
			}
			else if(side == 2)
			{
				world.spawnParticle("smoke", (xRandom + jRandom), yRandom, (zRandom - iRandom), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("reddust", (xRandom + jRandom), yRandom, (zRandom - iRandom), 0.0D, 0.0D, 0.0D);
			}
			else if(side == 3)
			{
				world.spawnParticle("smoke", (xRandom + jRandom), yRandom, (zRandom + iRandom), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("reddust", (xRandom + jRandom), yRandom, (zRandom + iRandom), 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		if(client.enableAmbientLighting)
		{
			TileEntity tileEntity = world.getTileEntity(x, y, z);
	
			if(tileEntity instanceof IActiveState)
			{
				if(((IActiveState)tileEntity).getActive() && ((IActiveState)tileEntity).lightUpdate())
				{
					return client.ambientLightingLevel;
				}
			}
		}

		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		switch(blockType)
		{
			case MACHINE_BLOCK_1:
				switch(meta)
				{
					case 0:
					case 1:
					case 2:
					case 3:
					case 9:
					case 10:
						return icons[meta][side];
					default:
						return icons[meta][0] != null ? icons[meta][0] : BASE_ICON;
				}
			case MACHINE_BLOCK_2:
				switch(meta)
				{
					case 3:
					case 5:
						return icons[meta][side];
					default:
						return icons[meta][0] != null ? icons[meta][0] : BASE_ICON;
				}
			case MACHINE_BLOCK_3:
				switch(meta)
				{
					case 3:
					case 5:
					case 6:
						return icons[meta][side];
					default:
						return icons[meta][0] != null ? icons[meta][0] : BASE_ICON;
				}
			default:
				return BASE_ICON;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		int meta = world.getBlockMetadata(x, y, z);
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(x, y, z);

		switch(blockType)
		{
			case MACHINE_BLOCK_1:
				switch(meta)
				{
					case 0:
					case 1:
					case 2:
					case 3:
					case 9:
					case 10:
						boolean active = MekanismUtils.isActive(world, x, y, z);
						return icons[meta][MekanismUtils.getBaseOrientation(side, tileEntity.facing)+(active ? 6 : 0)];
					case 5:
					case 6:
					case 7:
						TileEntityFactory factory = (TileEntityFactory)tileEntity;
						active = MekanismUtils.isActive(world, x, y, z);
						
						return factoryIcons[factory.tier.ordinal()][factory.recipeType.ordinal()][MekanismUtils.getBaseOrientation(side, tileEntity.facing)+(active ? 6 : 0)];
					default:
						return icons[meta][0];
				}
			case MACHINE_BLOCK_2:
				switch(meta)
				{
					case 3:
					case 5:
						boolean active = MekanismUtils.isActive(world, x, y, z);
						return icons[meta][MekanismUtils.getBaseOrientation(side, tileEntity.facing)+(active ? 6 : 0)];
					default:
						return icons[meta][0];
				}
			case MACHINE_BLOCK_3:
				switch(meta)
				{
					case 3:
					case 5:
					case 6:
						boolean active = MekanismUtils.isActive(world, x, y, z);
						return icons[meta][MekanismUtils.getBaseOrientation(side, tileEntity.facing)+(active ? 6 : 0)];
					default:
						return icons[meta][0];
				}
		}
		
		return null;
	}

	@Override
	public int damageDropped(int i)
	{
		return i;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List list)
	{
		for(MachineType type : MachineType.getValidMachines())
		{
			if(type.typeBlock == blockType && type.isEnabled())
			{
				switch(type)
				{
					case BASIC_FACTORY:
					case ADVANCED_FACTORY:
					case ELITE_FACTORY:
						for(RecipeType recipe : RecipeType.values())
						{
							ItemStack stack = new ItemStack(item, 1, type.meta);
							((IFactory)stack.getItem()).setRecipeType(recipe.ordinal(), stack);
							list.add(stack);
						}
						
						break;
					case FLUID_TANK:
						ItemBlockMachine itemMachine = (ItemBlockMachine)item;
						
						for(FluidTankTier tier : FluidTankTier.values())
						{
							ItemStack stack = new ItemStack(item, 1, type.meta);
							itemMachine.setBaseTier(stack, tier.getBaseTier());
							list.add(stack);
						}

						if(general.prefilledFluidTanks)
						{
							for(Fluid f : FluidRegistry.getRegisteredFluids().values())
							{
								try { //Prevent bad IDs
									ItemStack filled = new ItemStack(item, 1, type.meta);
									itemMachine.setBaseTier(filled, BaseTier.ULTIMATE);
									itemMachine.setFluidStack(new FluidStack(f, itemMachine.getCapacity(filled)), filled);
									list.add(filled);
								} catch(Exception e) {}
							}
						}
						
						break;
					default:
						list.add(new ItemStack(item, 1, type.meta));
				}
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int side, float posX, float posY, float posZ)
	{
		if(world.isRemote)
		{
			return true;
		}

		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(x, y, z);
		int metadata = world.getBlockMetadata(x, y, z);

		if(entityplayer.getCurrentEquippedItem() != null)
		{
			Item tool = entityplayer.getCurrentEquippedItem().getItem();

			if(MekanismUtils.hasUsableWrench(entityplayer, x, y, z))
			{
				if(SecurityUtils.canAccess(entityplayer, tileEntity))
				{
					if(entityplayer.isSneaking())
					{
						dismantleBlock(world, x, y, z, false);
						
						return true;
					}
	
					if(MekanismUtils.isBCWrench(tool))
					{
						((IToolWrench)tool).wrenchUsed(entityplayer, x, y, z);
					}
	
					int change = ForgeDirection.ROTATION_MATRIX[ForgeDirection.UP.ordinal()][tileEntity.facing];
	
					if(tileEntity instanceof TileEntityLogisticalSorter)
					{
						if(!((TileEntityLogisticalSorter)tileEntity).hasInventory())
						{
							for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
							{
								TileEntity tile = Coord4D.get(tileEntity).getFromSide(dir).getTileEntity(world);
	
								if(tile instanceof IInventory)
								{
									change = dir.getOpposite().ordinal();
									break;
								}
							}
						}
					}
	
					tileEntity.setFacing((short)change);
					world.notifyBlocksOfNeighborChange(x, y, z, this);
				}
				else {
					SecurityUtils.displayNoAccess(entityplayer);
				}
				
				return true;
			}
		}

		if(tileEntity != null)
		{
			MachineType type = MachineType.get(blockType, metadata);

			switch(type)
			{
				case PERSONAL_CHEST:
					if(!entityplayer.isSneaking() && !world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN))
					{
						TileEntityPersonalChest chest = (TileEntityPersonalChest)tileEntity;
						
						if(SecurityUtils.canAccess(entityplayer, tileEntity))
						{
							MekanismUtils.openPersonalChestGui((EntityPlayerMP)entityplayer, chest, null, true);
						}
						else {
							SecurityUtils.displayNoAccess(entityplayer);
						}
						
						return true;
					}
					
					break;
				case FLUID_TANK:
					if(!entityplayer.isSneaking())
					{
						if(SecurityUtils.canAccess(entityplayer, tileEntity))
						{
							if(entityplayer.getCurrentEquippedItem() != null && FluidContainerRegistry.isContainer(entityplayer.getCurrentEquippedItem()))
							{
								if(manageInventory(entityplayer, (TileEntityFluidTank)tileEntity))
								{
									entityplayer.inventory.markDirty();
									return true;
								}
							} 
							else {
								entityplayer.openGui(Mekanism.instance, type.guiId, world, x, y, z);
							}
						}
						else {
							SecurityUtils.displayNoAccess(entityplayer);
						}
						
						return true;
					}
					
					break;
				case LOGISTICAL_SORTER:
					if(!entityplayer.isSneaking())
					{
						if(SecurityUtils.canAccess(entityplayer, tileEntity))
						{
							LogisticalSorterGuiMessage.openServerGui(SorterGuiPacket.SERVER, 0, world, (EntityPlayerMP)entityplayer, Coord4D.get(tileEntity), -1);
						}
						else {
							SecurityUtils.displayNoAccess(entityplayer);
						}
						
						return true;
					}
					
					break;
				case TELEPORTER:
				case QUANTUM_ENTANGLOPORTER:
					if(!entityplayer.isSneaking())
					{
						String owner = ((ISecurityTile)tileEntity).getSecurity().getOwner();
						
						if(MekanismUtils.isOp((EntityPlayerMP)entityplayer) || owner == null || entityplayer.getCommandSenderName().equals(owner))
						{
							entityplayer.openGui(Mekanism.instance, type.guiId, world, x, y, z);
						}
						else {
							SecurityUtils.displayNoAccess(entityplayer);
						}
						
						return true;
					}
					
					break;
				default:
					if(!entityplayer.isSneaking() && type.guiId != -1)
					{
						if(SecurityUtils.canAccess(entityplayer, tileEntity))
						{
							entityplayer.openGui(Mekanism.instance, type.guiId, world, x, y, z);
						}
						else {
							SecurityUtils.displayNoAccess(entityplayer);
						}
						
						return true;
					}
					
					break;
			}
		}
		
		return false;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		if(MachineType.get(blockType, metadata) == null)
		{
			return null;
		}

		return MachineType.get(blockType, metadata).create();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return null;
	}

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        return new ArrayList<ItemStack>(Arrays.asList(getPickBlock(null, world, x, y, z, null)));
    }

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return Mekanism.proxy.CTM_RENDER_ID;
	}

	@Override
	public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tile = world.getTileEntity(x, y, z);
		
		return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(player, world, x, y, z) : 0.0F;
	}
	
	@Override
	public float getExplosionResistance(Entity entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ)
    {
		if(MachineType.get(blockType, world.getBlockMetadata(x, y, z)) != MachineType.PERSONAL_CHEST)
		{
			return blockResistance;
		}
		else {
			return -1;
		}
    }

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
	{
		if(!player.capabilities.isCreativeMode && !world.isRemote && willHarvest)
		{
			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, getPickBlock(null, world, x, y, z, player));

			world.spawnEntityInWorld(entityItem);
		}

		return world.setBlockToAir(x, y, z);
	}
	
	@Override
	public boolean hasComparatorInputOverride()
	{
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int par5)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		
		if(tileEntity instanceof TileEntityFluidTank)
		{
			return ((TileEntityFluidTank)tileEntity).getRedstoneLevel();
		}
		
		if(tileEntity instanceof TileEntityLaserAmplifier)
		{
			TileEntityLaserAmplifier amplifier = (TileEntityLaserAmplifier)tileEntity;
			
			if(amplifier.outputMode == TileEntityLaserAmplifier.RedstoneOutput.ENERGY_CONTENTS)
			{
				return amplifier.getRedstoneLevel();
			}
			else {
				return isProvidingWeakPower(world, x, y, z, par5);
			}
		}
		
		return 0;
	}
	
	private boolean manageInventory(EntityPlayer player, TileEntityFluidTank tileEntity)
	{
		ItemStack itemStack = player.getCurrentEquippedItem();

		if(itemStack != null)
		{
			if(FluidContainerRegistry.isEmptyContainer(itemStack))
			{
				if(tileEntity.fluidTank.getFluid() != null && tileEntity.fluidTank.getFluid().amount >= FluidContainerRegistry.BUCKET_VOLUME)
				{
					ItemStack filled = FluidContainerRegistry.fillFluidContainer(tileEntity.fluidTank.getFluid(), itemStack);

					if(filled != null)
					{
						if(player.capabilities.isCreativeMode)
						{
							tileEntity.fluidTank.drain(FluidContainerRegistry.getFluidForFilledItem(filled).amount, true);

							return true;
						}

						if(itemStack.stackSize > 1)
						{
							if(player.inventory.addItemStackToInventory(filled))
							{
								itemStack.stackSize--;

								tileEntity.fluidTank.drain(FluidContainerRegistry.getFluidForFilledItem(filled).amount, true);
							}
						}
						else if(itemStack.stackSize == 1)
						{
							player.setCurrentItemOrArmor(0, filled);

							tileEntity.fluidTank.drain(FluidContainerRegistry.getFluidForFilledItem(filled).amount, true);

							return true;
						}
					}
				}
			}
			else if(FluidContainerRegistry.isFilledContainer(itemStack))
			{
				FluidStack itemFluid = FluidContainerRegistry.getFluidForFilledItem(itemStack);
				int needed = tileEntity.getCurrentNeeded();
				
				if((tileEntity.fluidTank.getFluid() == null && itemFluid.amount <= tileEntity.fluidTank.getCapacity()) || itemFluid.amount <= needed)
				{
					if(tileEntity.fluidTank.getFluid() != null && !tileEntity.fluidTank.getFluid().isFluidEqual(itemFluid))
					{
						return false;
					}
					
					boolean filled = false;
					
					if(player.capabilities.isCreativeMode)
					{
						filled = true;
					}
					else {
						ItemStack containerItem = itemStack.getItem().getContainerItem(itemStack);
	
						if(containerItem != null)
						{
							if(itemStack.stackSize == 1)
							{
								player.setCurrentItemOrArmor(0, containerItem);
								filled = true;
							}
							else {
								if(player.inventory.addItemStackToInventory(containerItem))
								{
									itemStack.stackSize--;
	
									filled = true;
								}
							}
						}
						else {
							itemStack.stackSize--;
	
							if(itemStack.stackSize == 0)
							{
								player.setCurrentItemOrArmor(0, null);
							}
	
							filled = true;
						}
					}

					if(filled)
					{
						int toFill = Math.min(tileEntity.fluidTank.getCapacity()-tileEntity.fluidTank.getFluidAmount(), itemFluid.amount);
						
						tileEntity.fluidTank.fill(itemFluid, true);
						
						if(itemFluid.amount-toFill > 0)
						{
							tileEntity.pushUp(PipeUtils.copy(itemFluid, itemFluid.amount-toFill), true);
						}
						
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(x, y, z);

			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onNeighborChange(block);
			}
			
			if(tileEntity instanceof TileEntityLogisticalSorter)
			{
				TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter)tileEntity;

				if(!sorter.hasInventory())
				{
					for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
					{
						TileEntity tile = Coord4D.get(tileEntity).getFromSide(dir).getTileEntity(world);

						if(tile instanceof IInventory)
						{
							sorter.setFacing((short)dir.getOpposite().ordinal());
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(x, y, z);
		ItemStack itemStack = new ItemStack(this, 1, world.getBlockMetadata(x, y, z));

		if(itemStack.stackTagCompound == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		
		if(tileEntity instanceof TileEntityFluidTank)
		{
			ITierItem tierItem = (ITierItem)itemStack.getItem();
			tierItem.setBaseTier(itemStack, ((TileEntityFluidTank)tileEntity).tier.getBaseTier());
		}
		
		if(tileEntity instanceof ISecurityTile)
		{
			ISecurityItem securityItem = (ISecurityItem)itemStack.getItem();
			
			if(securityItem.hasSecurity(itemStack))
			{
				securityItem.setOwner(itemStack, ((ISecurityTile)tileEntity).getSecurity().getOwner());
				securityItem.setSecurity(itemStack, ((ISecurityTile)tileEntity).getSecurity().getMode());
			}
		}

		if(tileEntity instanceof IUpgradeTile)
		{
			((IUpgradeTile)tileEntity).getComponent().write(itemStack.stackTagCompound);
		}

		if(tileEntity instanceof ISideConfiguration)
		{
			ISideConfiguration config = (ISideConfiguration)tileEntity;

			config.getConfig().write(itemStack.stackTagCompound);
		}
		
		if(tileEntity instanceof ISustainedData)
		{
			((ISustainedData)tileEntity).writeSustainedData(itemStack);
		}

		if(tileEntity instanceof IRedstoneControl)
		{
			IRedstoneControl control = (IRedstoneControl)tileEntity;
			itemStack.stackTagCompound.setInteger("controlType", control.getControlType().ordinal());
		}

		if(tileEntity instanceof IStrictEnergyStorage)
		{
			IEnergizedItem energizedItem = (IEnergizedItem)itemStack.getItem();
			energizedItem.setEnergy(itemStack, ((IStrictEnergyStorage)tileEntity).getEnergy());
		}

		if(tileEntity instanceof TileEntityContainerBlock && ((TileEntityContainerBlock)tileEntity).inventory.length > 0)
		{
			ISustainedInventory inventory = (ISustainedInventory)itemStack.getItem();
			inventory.setInventory(((ISustainedInventory)tileEntity).getInventory(), itemStack);
		}

		if(((ISustainedTank)itemStack.getItem()).hasTank(itemStack))
		{
			if(tileEntity instanceof ISustainedTank)
			{
				if(((ISustainedTank)tileEntity).getFluidStack() != null)
				{
					((ISustainedTank)itemStack.getItem()).setFluidStack(((ISustainedTank)tileEntity).getFluidStack(), itemStack);
				}
			}
		}

		if(tileEntity instanceof TileEntityFactory)
		{
			IFactory factoryItem = (IFactory)itemStack.getItem();
			factoryItem.setRecipeType(((TileEntityFactory)tileEntity).recipeType.ordinal(), itemStack);
		}

		return itemStack;
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);

		if(!world.isRemote)
		{
			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onAdded();
			}
		}
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side)
	{
		MachineType type = MachineType.get(blockType, world.getBlockMetadata(x, y, z));

		switch(type)
		{
			case LASER_AMPLIFIER:
				return true;
			default:
				return false;
		}
	}

	public ItemStack dismantleBlock(World world, int x, int y, int z, boolean returnBlock)
	{
		ItemStack itemStack = getPickBlock(null, world, x, y, z, null);

		world.setBlockToAir(x, y, z);

		if(!returnBlock)
		{
			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, itemStack);

			world.spawnEntityInWorld(entityItem);
		}

		return itemStack;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		MachineType type = MachineType.get(blockType, world.getBlockMetadata(x, y, z));
		
		if(type == null)
		{
			return;
		}

		switch(type)
		{
			case CHARGEPAD:
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.06F, 1.0F);
				break;
			case FLUID_TANK:
				setBlockBounds(0.125F, 0.0F, 0.125F, 0.875F, 1.0F, 0.875F);
				break;
			default:
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				break;
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		setBlockBoundsBasedOnState(world, x, y, z);
		
		if(world.getTileEntity(x, y, z) instanceof TileEntityChargepad)
		{
			return null;
		}

		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		MachineType type = MachineType.get(blockType, world.getBlockMetadata(x, y, z));

		switch(type)
		{
			case CHARGEPAD:
			case PERSONAL_CHEST:
				return false;
			case FLUID_TANK:
				return side == ForgeDirection.UP || side == ForgeDirection.DOWN;
			default:
				return true;
		}
	}

	public static enum MachineBlock
	{
		MACHINE_BLOCK_1,
		MACHINE_BLOCK_2,
		MACHINE_BLOCK_3;

		public Block getBlock()
		{
			switch(this)
			{
				case MACHINE_BLOCK_1:
					return MekanismBlocks.MachineBlock;
				case MACHINE_BLOCK_2:
					return MekanismBlocks.MachineBlock2;
				case MACHINE_BLOCK_3:
					return MekanismBlocks.MachineBlock3;
				default:
					return null;
			}
		}
	}

	public static enum MachineType
	{
		ENRICHMENT_CHAMBER(MachineBlock.MACHINE_BLOCK_1, 0, "EnrichmentChamber", 3, TileEntityEnrichmentChamber.class, true, false, true),
		OSMIUM_COMPRESSOR(MachineBlock.MACHINE_BLOCK_1, 1, "OsmiumCompressor", 4, TileEntityOsmiumCompressor.class, true, false, true),
		COMBINER(MachineBlock.MACHINE_BLOCK_1, 2, "Combiner", 5, TileEntityCombiner.class, true, false, true),
		CRUSHER(MachineBlock.MACHINE_BLOCK_1, 3, "Crusher", 6, TileEntityCrusher.class, true, false, true),
		DIGITAL_MINER(MachineBlock.MACHINE_BLOCK_1, 4, "DigitalMiner", 2, TileEntityDigitalMiner.class, true, true, true),
		BASIC_FACTORY(MachineBlock.MACHINE_BLOCK_1, 5, "Factory", 11, TileEntityFactory.class, true, false, true),
		ADVANCED_FACTORY(MachineBlock.MACHINE_BLOCK_1, 6, "Factory", 11, TileEntityAdvancedFactory.class, true, false, true),
		ELITE_FACTORY(MachineBlock.MACHINE_BLOCK_1, 7, "Factory", 11, TileEntityEliteFactory.class, true, false, true),
		METALLURGIC_INFUSER(MachineBlock.MACHINE_BLOCK_1, 8, "MetallurgicInfuser", 12, TileEntityMetallurgicInfuser.class, true, true, true),
		PURIFICATION_CHAMBER(MachineBlock.MACHINE_BLOCK_1, 9, "PurificationChamber", 15, TileEntityPurificationChamber.class, true, false, true),
		ENERGIZED_SMELTER(MachineBlock.MACHINE_BLOCK_1, 10, "EnergizedSmelter", 16, TileEntityEnergizedSmelter.class, true, false, true),
		TELEPORTER(MachineBlock.MACHINE_BLOCK_1, 11, "Teleporter", 13, TileEntityTeleporter.class, true, false, false),
		ELECTRIC_PUMP(MachineBlock.MACHINE_BLOCK_1, 12, "ElectricPump", 17, TileEntityElectricPump.class, true, true, false),
		PERSONAL_CHEST(MachineBlock.MACHINE_BLOCK_1, 13, "PersonalChest", -1, TileEntityPersonalChest.class, false, true, false),
		CHARGEPAD(MachineBlock.MACHINE_BLOCK_1, 14, "Chargepad", -1, TileEntityChargepad.class, true, true, false),
		LOGISTICAL_SORTER(MachineBlock.MACHINE_BLOCK_1, 15, "LogisticalSorter", -1, TileEntityLogisticalSorter.class, false, true, false),
		ROTARY_CONDENSENTRATOR(MachineBlock.MACHINE_BLOCK_2, 0, "RotaryCondensentrator", 7, TileEntityRotaryCondensentrator.class, true, true, false),
		CHEMICAL_OXIDIZER(MachineBlock.MACHINE_BLOCK_2, 1, "ChemicalOxidizer", 29, TileEntityChemicalOxidizer.class, true, true, true),
		CHEMICAL_INFUSER(MachineBlock.MACHINE_BLOCK_2, 2, "ChemicalInfuser", 30, TileEntityChemicalInfuser.class, true, true, false),
		CHEMICAL_INJECTION_CHAMBER(MachineBlock.MACHINE_BLOCK_2, 3, "ChemicalInjectionChamber", 31, TileEntityChemicalInjectionChamber.class, true, false, true),
		ELECTROLYTIC_SEPARATOR(MachineBlock.MACHINE_BLOCK_2, 4, "ElectrolyticSeparator", 32, TileEntityElectrolyticSeparator.class, true, true, false),
		PRECISION_SAWMILL(MachineBlock.MACHINE_BLOCK_2, 5, "PrecisionSawmill", 34, TileEntityPrecisionSawmill.class, true, false, true),
		CHEMICAL_DISSOLUTION_CHAMBER(MachineBlock.MACHINE_BLOCK_2, 6, "ChemicalDissolutionChamber", 35, TileEntityChemicalDissolutionChamber.class, true, true, true),
		CHEMICAL_WASHER(MachineBlock.MACHINE_BLOCK_2, 7, "ChemicalWasher", 36, TileEntityChemicalWasher.class, true, true, false),
		CHEMICAL_CRYSTALLIZER(MachineBlock.MACHINE_BLOCK_2, 8, "ChemicalCrystallizer", 37, TileEntityChemicalCrystallizer.class, true, true, true),
		SEISMIC_VIBRATOR(MachineBlock.MACHINE_BLOCK_2, 9, "SeismicVibrator", 39, TileEntitySeismicVibrator.class, true, true, false),
		PRESSURIZED_REACTION_CHAMBER(MachineBlock.MACHINE_BLOCK_2, 10, "PressurizedReactionChamber", 40, TileEntityPRC.class, true, true, false),
		FLUID_TANK(MachineBlock.MACHINE_BLOCK_2, 11, "FluidTank", 41, TileEntityFluidTank.class, false, true, false),
		FLUIDIC_PLENISHER(MachineBlock.MACHINE_BLOCK_2, 12, "FluidicPlenisher", 42, TileEntityFluidicPlenisher.class, true, true, false),
		LASER(MachineBlock.MACHINE_BLOCK_2, 13, "Laser", -1, TileEntityLaser.class, true, true, false),
		LASER_AMPLIFIER(MachineBlock.MACHINE_BLOCK_2, 14, "LaserAmplifier", 44, TileEntityLaserAmplifier.class, false, true, false),
		LASER_TRACTOR_BEAM(MachineBlock.MACHINE_BLOCK_2, 15, "LaserTractorBeam", 45, TileEntityLaserTractorBeam.class, false, true, false),
		QUANTUM_ENTANGLOPORTER(MachineBlock.MACHINE_BLOCK_3, 0, "QuantumEntangloporter", 46, TileEntityQuantumEntangloporter.class, true, true, false),
		SOLAR_NEUTRON_ACTIVATOR(MachineBlock.MACHINE_BLOCK_3, 1, "SolarNeutronActivator", 47, TileEntitySolarNeutronActivator.class, false, true, false),
		AMBIENT_ACCUMULATOR(MachineBlock.MACHINE_BLOCK_3, 2, "AmbientAccumulator", 48, TileEntityAmbientAccumulator.class, true, false, false),
		OREDICTIONIFICATOR(MachineBlock.MACHINE_BLOCK_3, 3, "Oredictionificator", 52, TileEntityOredictionificator.class, false, false, false),
		RESISTIVE_HEATER(MachineBlock.MACHINE_BLOCK_3, 4, "ResistiveHeater", 53, TileEntityResistiveHeater.class, true, true, false),
		FORMULAIC_ASSEMBLICATOR(MachineBlock.MACHINE_BLOCK_3, 5, "FormulaicAssemblicator", 56, TileEntityFormulaicAssemblicator.class, true, false, true),
		FUELWOOD_HEATER(MachineBlock.MACHINE_BLOCK_3, 6, "FuelwoodHeater", 58, TileEntityFuelwoodHeater.class, false, false, false);

		public MachineBlock typeBlock;
		public int meta;
		public String name;
		public int guiId;
		public double baseEnergy;
		public Class<? extends TileEntity> tileEntityClass;
		public boolean isElectric;
		public boolean hasModel;
		public boolean supportsUpgrades;
		public Collection<ShapedMekanismRecipe> machineRecipes = new HashSet<ShapedMekanismRecipe>();

		private MachineType(MachineBlock block, int i, String s, int j, Class<? extends TileEntity> tileClass, boolean electric, boolean model, boolean upgrades)
		{
			typeBlock = block;
			meta = i;
			name = s;
			guiId = j;
			tileEntityClass = tileClass;
			isElectric = electric;
			hasModel = model;
			supportsUpgrades = upgrades;
		}
		
		public boolean isEnabled()
		{
			return machines.isEnabled(this.name);
		}
		
		public void addRecipes(Collection<ShapedMekanismRecipe> recipes)
		{
			machineRecipes.addAll(recipes);
		}
		
		public void addRecipe(ShapedMekanismRecipe recipe)
		{
			machineRecipes.add(recipe);
		}
		
		public Collection<ShapedMekanismRecipe> getRecipes()
		{
			return machineRecipes;
		}
		
		public static List<MachineType> getValidMachines()
		{
			List<MachineType> ret = new ArrayList<MachineType>();
			
			for(MachineType type : MachineType.values())
			{
				if(type != AMBIENT_ACCUMULATOR)
				{
					ret.add(type);
				}
			}
			
			return ret;
		}

		public static MachineType get(Block block, int meta)
		{
			if(block instanceof BlockMachine)
			{
				return get(((BlockMachine)block).blockType, meta);
			}

			return null;
		}

		public static MachineType get(MachineBlock block, int meta)
		{
			for(MachineType type : values())
			{
				if(type.meta == meta && type.typeBlock == block)
				{
					return type;
				}
			}

			return null;
		}

		public TileEntity create()
		{
			try {
				return tileEntityClass.newInstance();
			} catch(Exception e) {
				Mekanism.logger.error("Unable to indirectly create tile entity.");
				e.printStackTrace();
				return null;
			}
		}

		/** Used for getting the base energy storage. */
		public double getUsage()
		{
			switch(this)
			{
				case ENRICHMENT_CHAMBER:
					return usage.enrichmentChamberUsage;
				case OSMIUM_COMPRESSOR:
					return usage.osmiumCompressorUsage;
				case COMBINER:
					return usage.combinerUsage;
				case CRUSHER:
					return usage.crusherUsage;
				case DIGITAL_MINER:
					return usage.digitalMinerUsage;
				case BASIC_FACTORY:
					return usage.factoryUsage * 3;
				case ADVANCED_FACTORY:
					return usage.factoryUsage * 5;
				case ELITE_FACTORY:
					return usage.factoryUsage * 7;
				case METALLURGIC_INFUSER:
					return usage.metallurgicInfuserUsage;
				case PURIFICATION_CHAMBER:
					return usage.purificationChamberUsage;
				case ENERGIZED_SMELTER:
					return usage.energizedSmelterUsage;
				case TELEPORTER:
					return 12500;
				case ELECTRIC_PUMP:
					return usage.electricPumpUsage;
				case CHARGEPAD:
					return 25;
				case LOGISTICAL_SORTER:
					return 0;
				case ROTARY_CONDENSENTRATOR:
					return usage.rotaryCondensentratorUsage;
				case CHEMICAL_OXIDIZER:
					return usage.oxidationChamberUsage;
				case CHEMICAL_INFUSER:
					return usage.chemicalInfuserUsage;
				case CHEMICAL_INJECTION_CHAMBER:
					return usage.chemicalInjectionChamberUsage;
				case ELECTROLYTIC_SEPARATOR:
					return general.FROM_H2 * 2;
				case PRECISION_SAWMILL:
					return usage.precisionSawmillUsage;
				case CHEMICAL_DISSOLUTION_CHAMBER:
					return usage.chemicalDissolutionChamberUsage;
				case CHEMICAL_WASHER:
					return usage.chemicalWasherUsage;
				case CHEMICAL_CRYSTALLIZER:
					return usage.chemicalCrystallizerUsage;
				case SEISMIC_VIBRATOR:
					return usage.seismicVibratorUsage;
				case PRESSURIZED_REACTION_CHAMBER:
					return usage.pressurizedReactionBaseUsage;
				case FLUID_TANK:
					return 0;
				case FLUIDIC_PLENISHER:
					return usage.fluidicPlenisherUsage;
				case LASER:
					return usage.laserUsage;
				case LASER_AMPLIFIER:
					return 0;
				case LASER_TRACTOR_BEAM:
					return 0;
				case QUANTUM_ENTANGLOPORTER:
					return 0;
				case SOLAR_NEUTRON_ACTIVATOR:
					return 0;
				case AMBIENT_ACCUMULATOR:
					return 0;
				case RESISTIVE_HEATER:
					return 100;
				case FORMULAIC_ASSEMBLICATOR:
					return usage.formulaicAssemblicatorUsage;
				default:
					return 0;
			}
		}

		public static void updateAllUsages()
		{
			for(MachineType type : values())
			{
				type.updateUsage();
			}
		}

		public void updateUsage()
		{
			baseEnergy = 400 * getUsage();
		}

		public String getDescription()
		{
			return LangUtils.localize("tooltip." + name);
		}

		public ItemStack getStack()
		{
			return new ItemStack(typeBlock.getBlock(), 1, meta);
		}

		public static MachineType get(ItemStack stack)
		{
			return get(Block.getBlockFromItem(stack.getItem()), stack.getItemDamage());
		}
	}

	@Override
	public void setRenderBounds(Block block, int metadata) {}

	@Override
	public boolean doDefaultBoundSetting(int metadata)
	{
		return false;
	}

	@Override
	public ForgeDirection[] getValidRotations(World world, int x, int y, int z)
	{
		TileEntity tile = world.getTileEntity(x, y, z);
		ForgeDirection[] valid = new ForgeDirection[6];
		
		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock basicTile = (TileEntityBasicBlock)tile;
			
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				if(basicTile.canSetFacing(dir.ordinal()))
				{
					valid[dir.ordinal()] = dir;
				}
			}
		}
		
		return valid;
	}

	@Override
	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis)
	{
		TileEntity tile = world.getTileEntity(x, y, z);
		
		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock basicTile = (TileEntityBasicBlock)tile;
			
			if(basicTile.canSetFacing(axis.ordinal()))
			{
				basicTile.setFacing((short)axis.ordinal());
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side)
    {
		TileEntity tile = world.getTileEntity(x, y, z);
		
		if(tile instanceof TileEntityLaserAmplifier)
		{
			return ((TileEntityLaserAmplifier)tile).emittingRedstone ? 15 : 0;
		}
		
        return 0;
    }
	
	@Override
	public CTMData getCTMData(IBlockAccess world, int x, int y, int z, int meta)
	{
		if(ctms[meta][1] != null && MekanismUtils.isActive(world, x, y, z))
		{
			return ctms[meta][1];
		}
		
		return ctms[meta][0];
	}
	
	@Override
	public boolean shouldRenderBlock(IBlockAccess world, int x, int y, int z, int meta)
	{
		return !MachineType.get(this, meta).hasModel;
	}
}
