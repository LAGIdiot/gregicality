package gregicadditions.machines.energy;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregicadditions.item.GAMetaBlocks;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;

import static gregtech.api.unification.material.Materials.Aluminium;

public class TileEntityLargeTransformer extends MultiblockWithDisplayBase {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.OUTPUT_ENERGY, MultiblockAbility.INPUT_ENERGY};

    private IEnergyContainer input;
    private IEnergyContainer output;
    private boolean isActive = false;

    public TileEntityLargeTransformer(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }


    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        if (isActive)
            setActive(false);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    private void initializeAbilities() {
        this.input = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.output = new EnergyContainerList(getAbilities(MultiblockAbility.OUTPUT_ENERGY));
    }

    private void resetTileAbilities() {
        this.input = new EnergyContainerList(Lists.newArrayList());
        this.output = new EnergyContainerList(Lists.newArrayList());
    }


    @Override
    protected void updateFormedValid() {
        if (!getWorld().isRemote) {
            if (!isActive)
                setActive(true);
            if (output.getEnergyStored() < output.getEnergyCapacity()) {
                if (input.getEnergyStored() < output.getEnergyCapacity() - output.getEnergyStored()) {
                    output.addEnergy(input.getEnergyStored());
                    input.removeEnergy(input.getEnergyStored());
                } else {
                    long left = output.getEnergyCapacity() - output.getEnergyStored();
                    output.addEnergy(left);
                    input.removeEnergy(left);
                }
            }
        }
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("ASA")
                .where('S', selfPredicate())
                .where('A', statePredicate(getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .build();
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return abilities.containsKey(MultiblockAbility.INPUT_ENERGY) && abilities.containsKey(MultiblockAbility.OUTPUT_ENERGY);
    }

    public IBlockState getCasingState() {
        return GAMetaBlocks.getMetalCasingBlockState(Aluminium);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return GAMetaBlocks.METAL_CASING.get(Aluminium);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new TileEntityLargeTransformer(metaTileEntityId);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, getFrontFacing(), isActive);
    }

    protected void setActive(boolean active) {
        this.isActive = active;
        markDirty();
        if (!getWorld().isRemote) {
            writeCustomData(1, buf -> buf.writeBoolean(active));
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 1) {
            this.isActive = buf.readBoolean();
            getHolder().scheduleChunkForRenderUpdate();
        }
    }
}
