package wraith.fabricaeexnihilo.modules.witchwater;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.fabricaeexnihilo.FabricaeExNihilo;
import wraith.fabricaeexnihilo.api.registry.FabricaeExNihiloRegistries;
import wraith.fabricaeexnihilo.modules.ModEffects;
import wraith.fabricaeexnihilo.modules.base.BaseFluidBlock;

public class WitchWaterBlock extends BaseFluidBlock {

    public WitchWaterBlock(FlowableFluid fluid, Settings settings) {
        super(fluid, settings);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if(world == null || entity == null || !entity.isAlive() || entity.isRemoved()) {
            return;
        }
        if (entity instanceof LivingEntity livingEntity && !isMarked(livingEntity)) {
            if (livingEntity instanceof CreeperEntity creeper) {
                markEntity(livingEntity);
                if (!creeper.isIgnited()) {
                    var lightning = EntityType.LIGHTNING_BOLT.create(world);
                    if (world instanceof ServerWorld serverWorld && lightning != null) {
                        lightning.setPos(creeper.getPos().x, creeper.getPos().y, creeper.getPos().z);
                        creeper.onStruckByLightning(serverWorld, lightning);
                    }
                }
                creeper.setHealth(creeper.getMaxHealth());
                return;
            }
            if (livingEntity instanceof RabbitEntity rabbit) {
                markEntity(rabbit);
                // Killer Rabbit.
                if(rabbit.getRabbitType() != 99) {
                    rabbit.setRabbitType(99);
                }
                return;
            }
            if (livingEntity instanceof PlayerEntity player) {
                FabricaeExNihilo.CONFIG.modules.witchwater.effects.forEach((effect, durationLevel) ->
                        applyStatusEffect(player,
                                new StatusEffectInstance(Registry.STATUS_EFFECT.get(effect),
                                        durationLevel.getLeft(),
                                        durationLevel.getRight()
                                )
                        )
                );
                return;
            }
            var toSpawn = FabricaeExNihiloRegistries.WITCHWATER_ENTITY.getSpawn(livingEntity);
            if (toSpawn != null) {
                replaceMob(world, livingEntity, toSpawn);
                return;
            }
            markEntity(livingEntity);
            return;
        }
        if (entity instanceof ArrowEntity arrow) {
            // Replace arrows with shulker bullets
            var bullet = EntityType.SHULKER_BULLET.create(world);
            if (bullet != null) {
                bullet.setVelocity(arrow.getVelocity());
                bullet.refreshPositionAndAngles(arrow.getBlockPos(), arrow.getYaw(), arrow.getPitch());
            }
            replaceMob(world, arrow, bullet);
        }
        // TODO item changes
    }

    public static boolean receiveNeighborFluids(WitchWaterBlock witchWaterBlock, World world, BlockPos pos, BlockState state) {
        if (world == null || pos == null || state == null) {
            return true;
        }
        for (var direction : Direction.values()) {
            var fluidState = world.getFluidState(pos.offset(direction));
            if (fluidState.isEmpty()) {
                continue;
            }
            if (fluidInteraction(world, pos, pos.offset(direction)) && direction != Direction.DOWN) {
                return true;
            }
        }
        return true;
    }

    // A status effect is used to mark entities that have been processed so that they are no longer processed.
    public static boolean isMarked(LivingEntity entity) {
        return entity.hasStatusEffect(ModEffects.WITCH_WATERED);
    }

    public static void markEntity(LivingEntity entity) {
        applyStatusEffect(entity, ModEffects.WITCH_WATERED.getInstance());
    }


    public static boolean fluidInteraction(World world, BlockPos witchPos, BlockPos otherPos) {
        var fluidState = world.getFluidState(otherPos);
        if (fluidState.isEmpty()) {
            return false;
        }
        var block = FabricaeExNihiloRegistries.WITCHWATER_WORLD.getResult(fluidState.getFluid(), world.random);
        if (block == null) {
            return false;
        }
        var changePos = witchPos.offset(Direction.DOWN) == otherPos ? otherPos : witchPos;
        world.setBlockState(changePos, block.getDefaultState());
        world.playSound(null, changePos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.7f,0.8f + world.random.nextFloat() * 0.2f);
        return true;
    }

    public static void replaceMob(World world, LivingEntity toKill, EntityType<?> spawnType) {
        var toSpawn = spawnType.create(world);
        if (toSpawn instanceof LivingEntity livingEntity) {
            // Set position and angles
            livingEntity.refreshPositionAndAngles(toKill.getBlockPos(), toKill.getYaw(), toKill.getPitch());
            livingEntity.setVelocity(toKill.getVelocity());
            livingEntity.headYaw = toKill.headYaw;

            // Slime -> Magma Slime
            if (toKill instanceof SlimeEntity slimeEntity && livingEntity instanceof MagmaCubeEntity magmaCubeEntity) {
                //TODO mixin for setting slime size
            }

            // Set Health
            livingEntity.setHealth(livingEntity.getMaxHealth() * toKill.getHealth() / toKill.getMaxHealth());
        }
        replaceMob(world, toKill, toSpawn);
    }
    public static void replaceMob(World world, Entity toKill, @Nullable Entity toSpawn) {
        toKill.remove(Entity.RemovalReason.DISCARDED);
        if(toSpawn != null) {
            if (toSpawn instanceof LivingEntity livingEntity){
                markEntity(livingEntity);
            }
            world.spawnEntity(toSpawn);
        }
    }

    public static void applyStatusEffect(LivingEntity entity, StatusEffectInstance statusEffect) {
        // Grab the potion effect on the entity (null if not active) compare its duration (defaulting to 0) to the new duration
        boolean hasEffect = entity.getActiveStatusEffects().containsKey(statusEffect.getEffectType());
        var entityEffectDuration = hasEffect ? entity.getActiveStatusEffects().get(statusEffect.getEffectType()).getDuration() : Integer.MIN_VALUE;
        if(entityEffectDuration <= statusEffect.getDuration() - 20) {
            entity.addStatusEffect(statusEffect);
        }
    }

}
