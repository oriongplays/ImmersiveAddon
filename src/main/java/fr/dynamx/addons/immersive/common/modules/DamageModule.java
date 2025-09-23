package fr.dynamx.addons.immersive.common.modules;


import com.jme3.math.Vector3f;
import java.util.List;
import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.ImmersiveAddonConfig;
import fr.dynamx.addons.immersive.client.controllers.VehicleHealthController;
import fr.dynamx.api.entities.modules.IPhysicsModule;
import fr.dynamx.api.entities.modules.IVehicleController;
import fr.dynamx.api.network.sync.EntityVariable;
import fr.dynamx.api.network.sync.SynchronizationRules;
import fr.dynamx.api.network.sync.SynchronizedEntityVariable;
import fr.dynamx.common.contentpack.parts.PartWheel;
import fr.dynamx.common.entities.PackPhysicsEntity;
import fr.dynamx.common.entities.modules.engines.CarEngineModule;
import fr.dynamx.common.entities.vehicles.CarEntity;
import fr.dynamx.common.physics.entities.AbstractEntityPhysicsHandler;
import fr.dynamx.common.physics.entities.parts.engine.Engine;
import fr.dynamx.utils.maths.DynamXGeometry;
import fr.dynamx.utils.optimization.Vector3fPool;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextComponentString;

@SynchronizedEntityVariable.SynchronizedPhysicsModule(modid = ImmersiveAddon.ID)
public class DamageModule implements IPhysicsModule<AbstractEntityPhysicsHandler<?, ?>>, IPhysicsModule.IEntityUpdateListener {

    private final PackPhysicsEntity<?, ?> entity;
    private VehicleHealthController healthController;

    private static final int UNDERWATER_DAMAGE_INTERVAL = 20;
    private static final float UNDERWATER_DAMAGE_AMOUNT = 10.0F;

    private int underwaterTicks;
    
    @SynchronizedEntityVariable(name = "damage")
    private final EntityVariable<Float> damage = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, 0.0F);
    @SynchronizedEntityVariable(name = "lastDamage")
    private final EntityVariable<Float> lastDamage = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, 0.0F);

    public DamageModule(PackPhysicsEntity<?, ?> entity) {
        this.entity = entity;
        if (entity.world != null && entity.world.isRemote) {
            this.healthController = new VehicleHealthController(this);
        }
    }

    public float getDamage() {
        return damage.get();
    }

    public void addDamage(float dam) {
        if(this.getLastDamage() > 0) return;
        this.damage.set(Math.min(100, getDamage() + dam));
        this.setLastDamage(40);
    }
    
    public void addDamageInstant(float dam) {
        this.damage.set(Math.min(100, getDamage() + dam));
        this.setLastDamage(0);
    }

    public void repair(int repair){
        this.damage.set(Math.max(0, getDamage() - (float)repair));
    }

    public float getLastDamage() {
        return lastDamage.get();
    }

    public void setLastDamage(float lastDamage) {
        this.lastDamage.set(lastDamage);
    }
    
    @Override
    public IVehicleController createNewController() {
        return healthController;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setFloat("damage", damage.get());
        tag.setFloat("lastDamage", lastDamage.get());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        damage.set(tag.getFloat("damage"));
        lastDamage.set(tag.getFloat("lastDamage"));
    }

    @Override
    public void updateEntity() {
        if(this.getLastDamage() > 0){
            this.setLastDamage(this.getLastDamage() - 1);
        }
        if (entity instanceof CarEntity) {
            CarEntity<?> carEntity = (CarEntity<?>) entity;
            List<PartWheel> wheels = carEntity.getPackInfo().getPartsByType(PartWheel.class);
            boolean hasWheels = !wheels.isEmpty();

            if (!entity.world.isRemote && hasWheels) {
                if (carEntity.isInsideOfMaterial(Material.WATER) || carEntity.isInWater()) {
                    if (100 - this.getDamage() >= 1) {
                        underwaterTicks++;
                        if (underwaterTicks >= UNDERWATER_DAMAGE_INTERVAL) {
                            addDamageInstant(UNDERWATER_DAMAGE_AMOUNT);
                            underwaterTicks = 0;
                        }
                    } else {
                        underwaterTicks = 0;
                    }
                } else {
                    underwaterTicks = 0;
                }
            } else if (!hasWheels) {
                underwaterTicks = 0;
            }
            float percentDamage = 100 - this.getDamage();
            Vector3f vec3f = null;
            if (hasWheels) {
                PartWheel partWheel = wheels.get(0);
                Vector3f vector3f = Vector3fPool.get(0, partWheel.getPosition().y + 1, partWheel.getPosition().z);
                vec3f = DynamXGeometry.rotateVectorByQuaternion(vector3f, entity.physicsRotation)
                        .addLocal(Vector3fPool.get(entity.posX, entity.posY, entity.posZ));
            }

            CarEngineModule engineModule = carEntity.getModuleByType(CarEngineModule.class);

            if(engineModule.getPhysicsHandler() == null) return;
            if(engineModule.getPhysicsHandler().getEngine() == null) return;
            Engine engine = engineModule.getPhysicsHandler().getEngine();

            if(percentDamage <= 5){
                engine.setPower(0);
            }
            if (vec3f != null && percentDamage <= 40) {
                entity.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, vec3f.x, vec3f.y, vec3f.z, 0D, 0.1D, 0D, new int[2]);
            } else if (vec3f != null && percentDamage <= 65) {
                entity.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, vec3f.x, vec3f.y, vec3f.z, 0D, 0.1D, 0D, new int[2]);
            }

            if(!engineModule.isAccelerating()) return;
        }

        if (ImmersiveAddonConfig.debug) {
            if (!entity.getPassengers().isEmpty()) {
                for (Entity entitypassager : entity.getPassengers()) {
                    if (entitypassager instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer) entitypassager;
                        player.sendStatusMessage(new TextComponentString("Etat véhicule: " + (100 - this.getDamage())), true);
                    }
                }
            }
        }
    }

}