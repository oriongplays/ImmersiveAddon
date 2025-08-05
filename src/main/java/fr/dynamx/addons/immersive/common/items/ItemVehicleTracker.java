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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
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

        this.addPropertyOverride(new ResourceLocation("angle"), new IItemPropertyGetter() {
            private double rotation;
            private double rota;
            private long lastUpdate;

            @Override
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
                if (entity == null && !stack.isOnItemFrame()) {
                    return 0.0F;
                }
                boolean flag = entity != null;
                Entity entity1 = flag ? entity : stack.getItemFrame();
                if (world == null) {
                    world = entity1.world;
                }

                Entity target = getTrackedEntity(stack, world);
                if (target == null) {
                    // return a value outside the 0-1 range so model overrides don't apply
                    return 2.0F;
                }

                double yaw = flag ? entity1.rotationYaw : ItemVehicleTracker.this.getFrameRotation((EntityItemFrame) entity1);
                yaw = MathHelper.positiveModulo(yaw / 360.0D, 1.0D);
                double targetAngle = Math.atan2(target.posZ - entity1.posZ, target.posX - entity1.posX) / (Math.PI * 2D);
                double angle = 0.5D - (yaw - 0.25D - targetAngle);
                if (flag) {
                    angle = wobble(world, angle);
                }
                return MathHelper.positiveModulo((float) angle, 1.0F);
            }

            @SideOnly(Side.CLIENT)
            private double wobble(World world, double angle) {
                if (world.getTotalWorldTime() != this.lastUpdate) {
                    this.lastUpdate = world.getTotalWorldTime();
                    double d0 = angle - this.rotation;
                    d0 = MathHelper.positiveModulo(d0 + 0.5D, 1.0D) - 0.5D;
                    this.rota += d0 * 0.1D;
                    this.rota *= 0.8D;
                    this.rotation = MathHelper.positiveModulo(this.rotation + this.rota, 1.0D);
                }
                return this.rotation;
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