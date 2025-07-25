package fr.dynamx.addons.immersive.common.modules;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.client.controllers.RadioController;
import fr.dynamx.addons.immersive.common.helpers.ConfigReader;
import fr.dynamx.addons.immersive.ImmersiveAddonConfig;
import fr.dynamx.addons.immersive.common.infos.RadioAddonInfos;
import fr.dynamx.api.entities.modules.IPhysicsModule;
import fr.dynamx.api.entities.modules.IVehicleController;
import fr.dynamx.api.network.sync.EntityVariable;
import fr.dynamx.api.network.sync.SynchronizationRules;
import fr.dynamx.api.network.sync.SynchronizedEntityVariable;
import fr.dynamx.addons.immersive.common.network.ImmersiveAddonPacketHandler;
import fr.dynamx.addons.immersive.common.network.packets.PacketUpdateRadioState;
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
    }

    /**
     * Update the active radio each client tick so playback stops if the player
     * leaves the vehicle or it unloads.
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

        // Stop playback if the player left the vehicle
        if (!ent.getPassengers().contains(mc.player)) {
            clearActive();
            return;
        }

        if (ImmersiveAddon.radioPlayer != null) {
            float base = mc.gameSettings.getSoundLevel(SoundCategory.MUSIC);
            ImmersiveAddon.radioPlayer.setGain(base);
        }
    }

    private final RadioAddonInfos infos;

    @SynchronizedEntityVariable(name = "currentRadioIndex")
    private final EntityVariable<Integer> currentRadioIndex = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, 0);

    @SynchronizedEntityVariable(name = "isRadioOn")
    private final EntityVariable<Boolean> isRadioOn = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, false);

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

    net.minecraft.client.entity.EntityPlayerSP player = Minecraft.getMinecraft().player;
    if (player == null)
        return;

    boolean inVehicle = entity.getPassengers().contains(player);
    if (!inVehicle) {
        if (cachedRadioOn) {
            ImmersiveAddon.LOGGER.debug("Player left {} - stopping radio", entity.getName());
            ImmersiveAddon.radioPlayer.stopRadio();
            resetCached();
        }
        if (activeModule == this) {
            clearActive();
        }
        return;
    }

    // Ensure this module is active if the radio is on
    if (isRadioOn.get()) {
        if (activeModule != this) {
            clearActive();
            activeModule = this;
        }
        // Ensure gain is consistent while inside the vehicle
    } else if (activeModule == this) {
        clearActive();
    }

    if (ImmersiveAddon.radioPlayer != null) {
        float base = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MUSIC);
        ImmersiveAddon.radioPlayer.setGain(base);
    } else {
        return;
    }

    // Make sure seats module is present
    SeatsModule seatsModule = entity.getModuleByType(SeatsModule.class);
    if (seatsModule == null)
        return;

    // Prevent duplicated startRadio() calls in the same tick
    boolean shouldStartRadio = false;

    if (cachedRadioOn != isRadioOn.get()) {
        cachedRadioOn = isRadioOn.get();
        if (isRadioOn.get()) {
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
        if (isRadioOn.get()) {
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
        return currentRadioIndex.get();
    }

    public void setCurrentRadioIndex(int currentRadioIndex) {
        this.currentRadioIndex.set(currentRadioIndex);
        if (entity.world != null && entity.world.isRemote) {
            ImmersiveAddonPacketHandler.getInstance().getNetwork().sendToServer(
                    new PacketUpdateRadioState(entity.getEntityId(), currentRadioIndex, isRadioOn.get()));
        }
    }

    public boolean isRadioOn() {
        return isRadioOn.get();
    }

    public void setRadioOn(boolean radioOn) {
        this.isRadioOn.set(radioOn);
        if (entity.world != null && entity.world.isRemote) {
            ImmersiveAddonPacketHandler.getInstance().getNetwork().sendToServer(
                    new PacketUpdateRadioState(entity.getEntityId(), currentRadioIndex.get(), radioOn));
        }
    }

    public RadioAddonInfos getInfos() {
        return infos;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("currentRadioIndex", currentRadioIndex.get());
        tag.setBoolean("isRadioOn", isRadioOn.get());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        currentRadioIndex.set(tag.getInteger("currentRadioIndex"));
        isRadioOn.set(tag.getBoolean("isRadioOn"));
    }
}