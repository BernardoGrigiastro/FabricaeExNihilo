package wraith.fabricaeexnihilo.api.registry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import wraith.fabricaeexnihilo.api.crafting.ItemIngredient;
import wraith.fabricaeexnihilo.api.crafting.Lootable;
import wraith.fabricaeexnihilo.api.recipes.ToolRecipe;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public interface IToolRegistry extends IRegistry<ToolRecipe> {

    default boolean register(ItemIngredient target, Collection<Lootable> loot) {
        return register(new ToolRecipe(target, loot.stream().toList()));
    }

    default boolean register(Identifier target, Collection<Lootable> loot) {
        return register(Registry.BLOCK.get(target), loot);
    }

    default boolean register(Identifier target, Lootable... loot) {
        return register(Registry.BLOCK.get(target), List.of(loot));
    }

    default boolean register(ItemIngredient target, Lootable... loot) {
        return register(target, List.of(loot));
    }

    default boolean register(ItemConvertible target, Collection<Lootable> loot) {
        return register(new ItemIngredient(target.asItem()), loot);
    }

    default boolean register(ItemConvertible target, Lootable... loot) {
        return register(new ItemIngredient(target.asItem()), List.of(loot));
    }

    default boolean register(ItemConvertible target, ItemConvertible result) {
        return register(new ItemIngredient(target.asItem()), new Lootable(result, 1.0));
    }

    default boolean register(ItemConvertible target, ItemConvertible result, double... chances) {
        return register(new ItemIngredient(target.asItem()), new Lootable(result, chances));
    }

    default boolean register(Tag.Identified<Item> target, Collection<Lootable> loot) {
        return register(new ItemIngredient(target), loot);
    }

    default boolean register(Tag.Identified<Item> target, Lootable... loot) {
        return register(new ItemIngredient(target), List.of(loot));
    }

    boolean isRegistered(ItemConvertible target);

    List<ItemStack> getResult(ItemConvertible target, Random rand);

    List<Lootable> getAllResults(ItemConvertible target);

    // All recipes, chunked/broken up for pagination
    Collection<ToolRecipe> getREIRecipes();

}