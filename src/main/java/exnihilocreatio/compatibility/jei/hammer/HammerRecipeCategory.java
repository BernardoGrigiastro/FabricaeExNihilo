package exnihilocreatio.compatibility.jei.hammer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import exnihilocreatio.ExNihiloCreatio;
import exnihilocreatio.registries.manager.ExNihiloRegistryManager;
import exnihilocreatio.registries.types.HammerReward;
import exnihilocreatio.util.ItemUtil;
import exnihilocreatio.util.StringUtils;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class HammerRecipeCategory implements IRecipeCategory<HammerRecipe> {
    public static final String UID = "exnihilocreatio:hammer";
    private static final ResourceLocation texture = new ResourceLocation(ExNihiloCreatio.MODID, "textures/gui/jei_mid.png");

    private final IDrawableStatic background;
    private final IDrawableStatic slotHighlight;

    public HammerRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(texture, 0, 56, 166, 58);
        this.slotHighlight = guiHelper.createDrawable(texture, 166, 0, 18, 18);
    }

    @Override
    @Nonnull
    public String getUid() {
        return UID;
    }

    @Override
    @Nonnull
    public String getTitle() {
        return "Hammer";
    }

    @Override
    @Nonnull
    public String getModName() {
        return ExNihiloCreatio.MODID;
    }

    @Override
    @Nonnull
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull HammerRecipe recipeWrapper, @Nonnull IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, true, 10, 38);
        recipeLayout.getItemStacks().set(0, recipeWrapper.getInputs());

        IFocus<?> focus = recipeLayout.getFocus();

        int slotIndex = 1;
        for (int i = 0; i < recipeWrapper.getOutputs().size(); i++) {
            final int slotX = 38 + (i%7 * 18);
            final int slotY = 2 + i/7 * 18;

            ItemStack outputStack = recipeWrapper.getOutputs().get(i);

            recipeLayout.getItemStacks().init(slotIndex + i, false, slotX, slotY);
            recipeLayout.getItemStacks().set(slotIndex + i, outputStack);

            if (focus != null) {
                ItemStack focusStack = (ItemStack) focus.getValue();
                if (focus.getMode() == IFocus.Mode.OUTPUT
                        && !focusStack.isEmpty()
                        && focusStack.getItem() == outputStack.getItem()
                        && focusStack.getItemDamage() == outputStack.getItemDamage()) {
                    recipeLayout.getItemStacks().setBackground(i+slotIndex,slotHighlight);
                }
            }
        }


        recipeLayout.getItemStacks().addTooltipCallback(new HammerTooltipCallback(recipeWrapper));
    }

    @Override
    public IDrawable getIcon() {
        return null;
    }

    private static class HammerTooltipCallback implements ITooltipCallback<ItemStack> {
        private final HammerRecipe recipe;

        private HammerTooltipCallback(HammerRecipe recipeWrapper) {
            this.recipe = recipeWrapper;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void onTooltip(int slotIndex, boolean input, @Nonnull ItemStack ingredient, @Nonnull List<String> tooltip) {
            if (!input) {
                ItemStack blockStack = recipe.getInputs().get(0);
                Block blockBase = Block.getBlockFromItem(blockStack.getItem());

                @SuppressWarnings("deprecation")
                IBlockState block = blockBase.getStateFromMeta(blockStack.getMetadata());

                List<HammerReward> allRewards = ExNihiloRegistryManager.HAMMER_REGISTRY.getRewards(block);

                allRewards.removeIf(reward -> !ItemUtil.areStacksEquivalent(reward.getStack(), ingredient));

                // Level, Outputs
                Map<Integer, List<HammerReward>> tieredOutputs = Maps.newHashMap();

                for (HammerReward reward : allRewards) {
                    List<HammerReward> stacks = tieredOutputs.get(reward.getMiningLevel());

                    if (stacks == null) {
                        stacks = Lists.newArrayList(reward);
                        tieredOutputs.put(reward.getMiningLevel(), stacks);
                    } else {
                        stacks.add(reward);
                    }
                }

                tieredOutputs.forEach((level, rewards) -> rewards.sort((rewardA, rewardB) -> Float.compare(rewardB.getChance(), rewardA.getChance())));

                List<Integer> levelOrder = Lists.newArrayList(tieredOutputs.keySet());
                levelOrder.sort((levelA, levelB) -> Integer.compare(levelB, levelA));

                for (int level : levelOrder) {
                    tooltip.add(I18n.format("jei.hammer.hammerLevel." + level));
                    tieredOutputs.get(level).forEach(reward ->
                            tooltip.add(String.format("%s (x%d)", StringUtils.formatPercent(reward.getChance()), reward.getStack().getCount())));
                }
            }
        }
    }
}
