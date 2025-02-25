package wraith.fabricaeexnihilo.mixins;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.fabricaeexnihilo.api.registry.FabricaeExNihiloRegistries;
import wraith.fabricaeexnihilo.modules.tools.CrookTool;
import wraith.fabricaeexnihilo.modules.tools.HammerTool;

import java.util.List;

@Mixin(AbstractBlock.class)
public abstract class BlockHarvestMixin {

    /**
     * Injects calls to the Hammer and Crook registries if the tool used is identified as a Hammer or Crook
     */
    @Inject(at = @At("RETURN"), method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/loot/context/LootContext$Builder;)Ljava/util/List;", cancellable = true)
    public void getDroppedStacks(BlockState state, LootContext.Builder builder, CallbackInfoReturnable<List<ItemStack>> info) {
        ItemStack tool = builder.get(LootContextParameters.TOOL);
        if(CrookTool.isCrook(tool) && FabricaeExNihiloRegistries.CROOK.isRegistered(state.getBlock())){
            info.setReturnValue(FabricaeExNihiloRegistries.CROOK.getResult(state.getBlock(), builder.getWorld().random));
        }
        else if (HammerTool.isHammer(tool) && FabricaeExNihiloRegistries.HAMMER.isRegistered(state.getBlock())) {
            info.setReturnValue(FabricaeExNihiloRegistries.HAMMER.getResult(state.getBlock(), builder.getWorld().random));
        }

    }
}
