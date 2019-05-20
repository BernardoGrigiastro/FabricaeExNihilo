package exnihilocreatio.compatibility.jei.sieve;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import exnihilocreatio.ExNihiloCreatio;
import exnihilocreatio.registries.manager.ExNihiloRegistryManager;
import exnihilocreatio.registries.registries.SieveRegistry;
import exnihilocreatio.registries.types.Siftable;
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
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class SieveRecipeCategory implements IRecipeCategory<SieveRecipe> {

    public static final String UID = "exnihilocreatio:sieve";
    private static final ResourceLocation texture = new ResourceLocation(ExNihiloCreatio.MODID, "textures/gui/jei_mid.png");

    private final IDrawableStatic background;
    private final IDrawableStatic slotHighlight;

    public SieveRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(texture, 0, 0, 166, 58);
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
        return "Sieve";
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

    public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull SieveRecipe recipeWrapper, @Nonnull IIngredients ingredients) {
        //Mesh
        recipeLayout.getItemStacks().init(0, true, 10, 38);
        recipeLayout.getItemStacks().set(0, recipeWrapper.getMesh());

        //Input
        recipeLayout.getItemStacks().init(1, true, 10, 2);
        recipeLayout.getItemStacks().set(1, recipeWrapper.getSievables());

        IFocus<?> focus = recipeLayout.getFocus();

        int slotIndex = 2;
        for (int i = 0; i < recipeWrapper.getOutputs().size(); i++) {
            final int slotX = 38 + (i%7 * 18);
            final int slotY = 2 + i/7 * 18;
            ItemStack outputStack = (ItemStack) recipeWrapper.getOutputs().get(i);
            recipeLayout.getItemStacks().init(slotIndex + i, false, slotX, slotY);
            recipeLayout.getItemStacks().set(slotIndex + i, outputStack);

            if (focus != null) {
                ItemStack focusStack = (ItemStack) focus.getValue();
                if (focus.getMode() == IFocus.Mode.OUTPUT
                        && !focusStack.isEmpty()
                        && focusStack.getItem() == outputStack.getItem()
                        && focusStack.getItemDamage() == outputStack.getItemDamage()) {
                    recipeLayout.getItemStacks().setBackground(slotIndex + i,slotHighlight);
                }
            }
        }

        recipeLayout.getItemStacks().addTooltipCallback(new ITooltipCallback<ItemStack>() {
            @Override
            @SideOnly(Side.CLIENT)
            public void onTooltip(int slotIndex, boolean input, @Nonnull ItemStack ingredient, @Nonnull List<String> tooltip) {
                if (!input) {
                    ItemStack mesh = recipeWrapper.getMesh();
                    Multiset<String> condensedTooltips = HashMultiset.create();
                    for (Siftable siftable : ExNihiloRegistryManager.SIEVE_REGISTRY.getDrops(recipeWrapper.getSievables().get(0))) {
                        if (!SieveRegistry.canSieve(siftable.getMeshLevel(), mesh.getItemDamage()))
                            continue;
                        ItemStack sifted = siftable.getDrop().getItemStack();
                        if(!ItemUtil.areStacksEquivalent(sifted, ingredient))
                            continue;

                        condensedTooltips.add(StringUtils.formatPercent(siftable.getChance()));
                    }
                    tooltip.add(I18n.format("jei.sieve.dropChance"));
                    for (String line : condensedTooltips.elementSet()) {
                        tooltip.add(" * " + condensedTooltips.count(line) + "x " + line);
                    }
                }
            }
        });

    }

    @Override
    public IDrawable getIcon() {
        return null;
    }
}
