package fr.dynamx.addons.immersive.common.modules;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.client.controllers.RadioController;
import fr.dynamx.addons.immersive.common.helpers.ConfigReader;
import fr.dynamx.addons.immersive.ImmersiveAddonConfig;
import fr.dynamx.addons.immersive.common.infos.RadioAddonInfos;
import fr.dynamx.api.entities.modules.IPhysicsModule;
import fr.dynamx.api.entities.modules.IVehicleController;
import fr.dynamx.api.network.sync.SynchronizedEntityVariable;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.common.entities.modules.SeatsModule;
import fr.dynamx.common.physics.entities.AbstractEntityPhysicsHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;

@SynchronizedEntityVariable.SynchronizedPhysicsModule(modid = ImmersiveAddon.ID)
public class RadioModule implements IPhysicsModule<AbstractEntityPhysicsHandler<?, ?>>, IPhysicsModule.IEntityUpdateListener {
    private RadioController controller;
    private final BaseVehicleEntity<?> entity;

        /**
     * Currently active radio module playing audio on the client.
     */
    @SideOnly(Side.CLIENT)
    private static RadioModule activeModule;

    /** Last known position of the active module. */
    @SideOnly(Side.CLIENT)
    private static net.minecraft.util.math.Vec3d activePos;

    /**
     * @return the radio module currently playing, or {@code null} if none.
     */
    @SideOnly(Side.CLIENT)
    public static RadioModule getActiveModule() {
        return activeModule;
    }

    /**
     * Stop playback and clear the active radio reference.
     */
    @SideOnly(Side.CLIENT)
    public static void clearActive() {
        if (activeModule != null && ImmersiveAddon.radioPlayer != null) {
            ImmersiveAddon.LOGGER.debug("Clearing active radio for {}", activeModule.entity.getName());
            ImmersiveAddon.radioPlayer.stopRadio();
            activeModule.resetCached();
        }
        activeModule = null;
        activePos = null;
    }

    /**
     * Update the active radio each client tick to handle distance checks even
     * when the vehicle unloads.
     */
    @SideOnly(Side.CLIENT)
    public static void tickActive() {
        if (activeModule == null)
            return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) {
            clearActive();
            return;
        }

        BaseVehicleEntity<?> ent = activeModule.entity;
        if (ent.isDead) {
            clearActive();
            return;
        }

        activePos = ent.getPositionVector();
        double distSq = mc.player.getDistanceSq(activePos.x, activePos.y, activePos.z);
        if (distSq > 256) {
            ImmersiveAddon.LOGGER.debug("Player too far from {} - stopping radio", ent.getName());
            ImmersiveAddon.radioPlayer.stopRadio();
            activeModule.resetCached();
            clearActive();
            return;
        }

        // Update volume fade with distance
        if (ImmersiveAddon.radioPlayer != null) {
            float base = mc.gameSettings.getSoundLevel(SoundCategory.MUSIC);
            float distGain = 1.0f - (float) Math.sqrt(distSq) / 16f;
            if (distGain < 0f) distGain = 0f;
            ImmersiveAddon.radioPlayer.setGain(base * distGain);
        }
    }

    private final RadioAddonInfos infos;

    private int currentRadioIndex = 0;
    private boolean isRadioOn;

    public RadioModule(BaseVehicleEntity<?> entity, RadioAddonInfos<?> infos) {
        this.entity = entity;
        this.infos = infos;
        if (entity.world.isRemote) {
            controller = new RadioController(entity, this);
        }
    }

    @Override
    public boolean listenEntityUpdates(Side side) {
        return true;
    }

    private int cachedRadioIndex = -1;
    private boolean cachedRadioOn = false;

    public void resetCached() {
        cachedRadioOn = false;
        cachedRadioIndex = -1;
        if (ImmersiveAddonConfig.debug) {
            ImmersiveAddon.LOGGER.debug("Cached radio state reset for {}", entity.getName());
        }
    }

@Override
@SideOnly(Side.CLIENT)
public void updateEntity() {
    if (!FMLCommonHandler.instance().getSide().isClient() || !entity.world.isRemote)
        return;

    double distSq = entity.getDistanceSq(Minecraft.getMinecraft().player);
    if (distSq > 256) {
        if (cachedRadioOn) {
            ImmersiveAddon.LOGGER.debug("Player too far from {} - stopping radio", entity.getName());
            ImmersiveAddon.radioPlayer.stopRadio();
            resetCached();
        }
        if (activeModule == this) {
            clearActive();
        }
        return;
    }

    // Ensure this module is active if the radio is on
    if (isRadioOn) {
        if (activeModule != this) {
            clearActive();
            activeModule = this;
        }
        activePos = entity.getPositionVector();
    } else if (activeModule == this) {
        clearActive();
    }

    // Update volume based on distance
    if (ImmersiveAddon.radioPlayer != null) {
        float base = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MUSIC);
        float distGain = 1.0f - (float) Math.sqrt(distSq) / 16f;
        if (distGain < 0f) distGain = 0f;
        ImmersiveAddon.radioPlayer.setGain(base * distGain);
    } else {
        return;
    }

    // Make sure seats module is present
    SeatsModule seatsModule = entity.getModuleByType(SeatsModule.class);
    if (seatsModule == null)
        return;

    // Prevent duplicated startRadio() calls in the same tick
    boolean shouldStartRadio = false;

    if (cachedRadioOn != isRadioOn) {
        cachedRadioOn = isRadioOn;
        if (isRadioOn) {
            shouldStartRadio = true;
        } else {
            Minecraft.getMinecraft().player.sendStatusMessage(
                    new net.minecraft.util.text.TextComponentString("§c§lRadio: §r§eRadio is off"), true);
            ImmersiveAddon.LOGGER.debug("Stopping radio for {}", entity.getName());
            ImmersiveAddon.radioPlayer.stopRadio();
            resetCached();
            if (activeModule == this) {
                clearActive();
            }
        }
    }

    if (cachedRadioIndex != getCurrentRadioIndex()) {
        cachedRadioIndex = getCurrentRadioIndex();
        if (isRadioOn) {
            shouldStartRadio = true;
        }
    }

    if (shouldStartRadio) {
        startRadio();
    }
}
@SideOnly(Side.CLIENT)
private void startRadio() {
    if (ConfigReader.frequencies != null && !ConfigReader.frequencies.isEmpty()) {
        int idx = Math.min(Math.max(getCurrentRadioIndex(), 0), ConfigReader.frequencies.size() - 1);
        Minecraft.getMinecraft().player.sendStatusMessage(
                new net.minecraft.util.text.TextComponentString("§c§lRadio: §r§e" +
                        ConfigReader.frequencies.get(idx).getName()), true);
        try {
            // Always stop any existing playback to prevent duplicate audio
            ImmersiveAddon.radioPlayer.stopRadio();

            ImmersiveAddon.LOGGER.debug("Starting radio {} for {}", idx, entity.getName());
            ImmersiveAddon.radioPlayer.playRadio(new URL(ConfigReader.frequencies.get(idx).getUrl()));
            activeModule = this;
            activePos = entity.getPositionVector();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}

    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public IVehicleController createNewController() {
        return controller;
    }

    public int getCurrentRadioIndex() {
        return currentRadioIndex;
    }

    public void setCurrentRadioIndex(int currentRadioIndex) {
        this.currentRadioIndex = currentRadioIndex;
    }

    public boolean isRadioOn() {
        return isRadioOn;
    }

    public void setRadioOn(boolean radioOn) {
        isRadioOn = radioOn;
    }

    public RadioAddonInfos getInfos() {
        return infos;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("currentRadioIndex", currentRadioIndex);
        tag.setBoolean("isRadioOn", isRadioOn);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        currentRadioIndex = tag.getInteger("currentRadioIndex");
        isRadioOn = tag.getBoolean("isRadioOn");
    }
}