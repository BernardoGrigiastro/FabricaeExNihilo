package wraith.fabricaeexnihilo.modules.base;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentContainer implements NBTSerializable {

    private final Map<Identifier, Integer> enchantments = new HashMap<>();

    public Map<Identifier, Integer> getEnchantments() {
        return enchantments;
    }

    public NbtCompound writeNbt() {
        var nbt = new NbtCompound();
        enchantments.forEach((enchantment, level) -> nbt.putInt(enchantment.toString(), level));
        return nbt;
    }

    public void readNbt(NbtCompound nbt) {
        nbt.getKeys().forEach(key -> enchantments.put(new Identifier(key), nbt.getInt(key)));
    }

    public int getEnchantmentLevel(Enchantment enchantment) {
        var enchantmentIdentifier = Registry.ENCHANTMENT.getId(enchantment);
        if (enchantmentIdentifier == null) {
            return 0;
        }
        return getEnchantmentLevel(enchantmentIdentifier);
    }

    public int getEnchantmentLevel(Identifier enchantment) {
        return enchantments.getOrDefault(enchantment, 0);
    }

    public void setEnchantmentLevel(Enchantment enchantment, int level) {
        var enchantmentIdentifier = Registry.ENCHANTMENT.getId(enchantment);
        if (enchantmentIdentifier == null) {
            return;
        }
        setEnchantmentLevel(enchantmentIdentifier, level);
    }

    public void setEnchantmentLevel(Identifier enchantment, int level) {
        enchantments.put(enchantment, level);
    }

    public void setAllEnchantments(EnchantmentContainer other) {
        enchantments.clear();
        enchantments.putAll(other.enchantments);
    }

    public static void addEnchantments(ItemStack itemStack, EnchantmentContainer container) {
        container.enchantments.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .forEach(entry -> {
                    var enchantment = Registry.ENCHANTMENT.get(entry.getKey());
                    if (enchantment != null) {
                        itemStack.addEnchantment(enchantment, entry.getValue());
                    }
                });
    }
}
