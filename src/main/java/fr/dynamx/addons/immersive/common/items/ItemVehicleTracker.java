package fr.dynamx.addons.immersive.common.items;

import fr.dynamx.common.items.DynamXItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemCompass;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Compass item that always points to the tracked vehicle entity.
 */
public class ItemVehicleTracker extends ItemCompass {
    public ItemVehicleTracker(String name) {
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(DynamXItemRegistry.objectTab);
        setMaxStackSize(1);
        ItemsRegister.INSTANCE.getItems().add(this);

        // Needle rotation toward the tracked vehicle
        this.addPropertyOverride(new ResourceLocation("angle"), new IItemPropertyGetter() {
            private double rotation;
            private double rota;
            private long lastUpdate;

            @Override
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entityIn) {
                if (entityIn == null && !stack.isOnItemFrame()) {
                    return 0.0F;
                }
                boolean flag = entityIn != null;
                Entity entity = flag ? entityIn : stack.getItemFrame();
                if (world == null) {
                    world = entity.world;
                }

                double d0;
                Entity target = getTrackedEntity(stack, world);
                if (target != null && target.world == world) {
                    double yaw = flag ? entityIn.rotationYaw : ItemVehicleTracker.this.getFrameRotation((EntityItemFrame) entity);
                    yaw = MathHelper.positiveModulo(yaw / 360.0D, 1.0D);
                    double angle = Math.atan2(target.posZ - entity.posZ, target.posX - entity.posX) / (Math.PI * 2D);
                    d0 = 0.5D - (yaw - 0.25D - angle);
                } else {
                    d0 = Math.random();
                }

                if (flag) {
                    if (this.lastUpdate != world.getTotalWorldTime()) {
                        this.lastUpdate = world.getTotalWorldTime();
                        double d3 = d0 - this.rotation;
                        d3 = MathHelper.positiveModulo(d3 + 0.5D, 1.0D) - 0.5D;
                        this.rota += d3 * 0.1D;
                        this.rota *= 0.8D;
                        this.rotation = MathHelper.positiveModulo(this.rotation + this.rota, 1.0D);
                    }
                    return (float) this.rotation;
                }
                return MathHelper.positiveModulo((float) d0, 1.0F);
            }
        });

        // Texture swap when facing the tracked vehicle
        this.addPropertyOverride(new ResourceLocation("tracking"), new IItemPropertyGetter() {
            @Override
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entityIn) {
                if (entityIn == null) {
                    return 2.0F;
                }
                if (world == null) {
                    world = entityIn.world;
                }
                Entity target = getTrackedEntity(stack, world);
                if (target != null && target.world == world) {
                    double angle = Math.toDegrees(Math.atan2(target.posZ - entityIn.posZ, target.posX - entityIn.posX));
                    double diff = MathHelper.wrapDegrees(entityIn.rotationYaw - angle);
                    return Math.abs(diff) <= 15 ? 1.0F : 0.0F;
                }
                return 2.0F;
            }
        });
    }

    @SideOnly(Side.CLIENT)
    private double getFrameRotation(EntityItemFrame frame) {
        return MathHelper.wrapDegrees(180 + frame.facingDirection.getHorizontalIndex() * 90 + frame.getRotation() * 45);
    }

    /**
     * Stores the id and dimension of the tracked vehicle in the stack's NBT
     */
    public static void setTrackedEntity(ItemStack stack, Entity entity) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setInteger("TrackedEntity", entity.getEntityId());
        tag.setInteger("TrackedDim", entity.dimension);
    }

    /**
     * Returns the entity tracked by this stack, or null if not present
     */
    @Nullable
    public static Entity getTrackedEntity(ItemStack stack, World world) {
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag.hasKey("TrackedEntity")) {
                int id = tag.getInteger("TrackedEntity");
                int dim = tag.hasKey("TrackedDim") ? tag.getInteger("TrackedDim") : world.provider.getDimension();
                World targetWorld = world.provider.getDimension() == dim ? world : DimensionManager.getWorld(dim);
                if (targetWorld != null) {
                    return targetWorld.getEntityByID(id);
                }
            }
        }
        return null;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (!worldIn.isRemote) {
            Entity target = getTrackedEntity(stack, worldIn);
            if (target != null && target.world == playerIn.world) {
                double distance = playerIn.getDistance(target);
                TextFormatting color;
                if (distance < 10) {
                    color = TextFormatting.GREEN;
                } else if (distance <= 50) {
                    color = TextFormatting.YELLOW;
                } else {
                    color = TextFormatting.RED;
                }
                playerIn.sendMessage(new TextComponentString(color + Integer.toString((int) distance)));
            } else {
                playerIn.sendMessage(new TextComponentString("Sinal com interferencia"));
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        World world = worldIn != null ? worldIn : Minecraft.getMinecraft().world;
        Entity target = getTrackedEntity(stack, world);
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (target != null && player != null && target.world == player.world) {
            tooltip.add(I18n.format("tooltip.dynamx_immersive.vehicle_tracker.distance", (int) player.getDistance(target)));
        } else {
            tooltip.add(I18n.format("tooltip.dynamx_immersive.vehicle_tracker.no_target"));
        }
    }
}