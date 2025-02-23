package ru.cr1zyb0y.pipevacuumpump.mixin;

import alexiil.mc.mod.pipes.pipe.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.cr1zyb0y.pipevacuumpump.common.PartSpPipeEngineConnector;

@Mixin(value = PipeSpBehaviourWood.class, remap = false)
public abstract class PipeSpBehaviourWoodMixin extends PipeSpBehaviourSided {

    @Shadow public abstract void tryExtractItems(Direction dir, int count);
    @Shadow public abstract void tryExtractFluids(Direction dir);

    //engine connector class
    @Unique
    PartSpPipeEngineConnector EngineConnector;

    public PipeSpBehaviourWoodMixin(PartSpPipe pipe) {
        super(pipe);
    }

    //constructor
    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    public void init(PartSpPipe pipe, CallbackInfo ci) {
        if (PartSpPipeEngineConnector.contains(pipe)) {
            //get and remove
            EngineConnector = PartSpPipeEngineConnector.remove(pipe);
        } else {
            EngineConnector = new PartSpPipeEngineConnector();
            PartSpPipeEngineConnector.add(pipe, EngineConnector);
        }
    }

    //save from tag
    @Override
    public NbtCompound toNbt(RegistryWrapper.WrapperLookup lookup) {
        NbtCompound nbt = super.toNbt(lookup);
        EngineConnector.saveToNbt(nbt);
        return nbt;
    }

    //load to tag
    @Override
    public void fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.fromNbt(nbt, lookup);
        EngineConnector.loadFromNbt(nbt);
    }

    //main logic of extraction
    @Inject(method = "tick", at = @At("TAIL"), remap = false)
    public void tick(CallbackInfo ci) {
        World world = pipe.getPipeWorld();

        //don't run on client
        if (world == null || world.isClient) {
            return;
        }

        //check that inventory connected
        Direction dir = currentDirection();
        if (dir == null) {
            return;
        }

        //now need check all things
        PartSpPipeEngineConnector.AllowedExtraction allowedExtraction = EngineConnector.canExtract(world);
        switch (allowedExtraction) {
            case ITEMS:
                tryExtractItems(dir, 1);
                break;
            case FLUIDS:
                tryExtractFluids(dir);
                break;
            case ITEMS_FLUIDS: // currently we have no blocks that support both
            default:
                // no-op
                break;
        }
    }
}