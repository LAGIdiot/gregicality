package gregicadditions.machines.multi.override;

import gregicadditions.item.GAMetaBlocks;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.unification.material.Materials;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import static gregtech.api.unification.material.Materials.Titanium;

public class MetaTileEntityDieselEngine extends gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityDieselEngine {
	public MetaTileEntityDieselEngine(ResourceLocation metaTileEntityId) {
		super(metaTileEntityId);
	}

	public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
		return new MetaTileEntityDieselEngine(this.metaTileEntityId);
	}

	@Override
	public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
		return GAMetaBlocks.METAL_CASING.get(Materials.Titanium);
	}

	@Override
	public IBlockState getCasingState() {
		return GAMetaBlocks.getMetalCasingBlockState(Titanium);
	}
}
