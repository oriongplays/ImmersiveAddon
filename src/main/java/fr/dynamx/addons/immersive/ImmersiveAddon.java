package fr.dynamx.addons.immersive;

import fr.dynamx.addons.immersive.client.ClientEventHandler;
import fr.dynamx.addons.immersive.client.HandAnimClientEventHandler;
import fr.dynamx.addons.immersive.client.keybind.KeyBindings;
import fr.dynamx.addons.immersive.common.HandAnimationEventHandler;
import fr.dynamx.addons.immersive.common.ImmersiveEventHandler;
import fr.dynamx.addons.immersive.common.GuiHandler;
import fr.dynamx.addons.immersive.common.items.RegisterHandler;
import fr.dynamx.addons.immersive.common.items.SoundRegister;
import fr.dynamx.addons.immersive.common.helpers.IRadioPlayer;
import fr.dynamx.addons.immersive.common.helpers.JLayerRadioPlayer;
import fr.dynamx.addons.immersive.common.helpers.MCEFRadioPlayer;
import fr.dynamx.addons.immersive.common.network.ImmersiveAddonPacketHandler;
import fr.dynamx.addons.immersive.proxy.CommonProxy;
import fr.dynamx.addons.immersive.server.commands.CommandShowNames;
import fr.dynamx.addons.immersive.server.commands.CommandImmersiveAddon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import fr.dynamx.addons.immersive.utils.ModSyncedDataKeys;
import fr.dynamx.addons.immersive.utils.Utils;
import fr.dynamx.api.contentpack.DynamXAddon;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = ImmersiveAddon.ID, name = ImmersiveAddon.NAME, version = "1.0", dependencies = "before: dynamxmod")
@DynamXAddon(modid = ImmersiveAddon.ID, name = ImmersiveAddon.NAME, version = "1.0")
public class ImmersiveAddon {
    public static final String ID = "dynamx_immersive";
    public static final String NAME = "ImmersiveAddon";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @Mod.Instance
    public static ImmersiveAddon INSTANCE;


    @SidedProxy(modId = ID, clientSide = "fr.dynamx.addons.immersive.proxy.ClientProxy", serverSide = "fr.dynamx.addons.immersive.proxy.ServerProxy")
    public static CommonProxy proxy;
    /**
     * Active radio player implementation. Defaults to the JLayer based player
     * but will use the WebDisplays backend when the MCEF mod is available.
     */
    public static IRadioPlayer radioPlayer;

    @DynamXAddon.AddonEventSubscriber
    public static void initAddon() {
    }

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        ImmersiveAddonConfig.init(event.getSuggestedConfigurationFile());
        proxy.preInit(event);
        if(event.getSide().isClient()) {
            if(Loader.isModLoaded("mcef")) {
                LOGGER.info("Using MCEF based radio player");
                radioPlayer = new MCEFRadioPlayer();
            } else {
                LOGGER.info("Using JLayer based radio player");
                radioPlayer = new JLayerRadioPlayer();
            }
        } else {
            LOGGER.info("Pre-initializing on dedicated server");
        }
        MinecraftForge.EVENT_BUS.register(new RegisterHandler());
        MinecraftForge.EVENT_BUS.register(new SoundRegister());
        if(Utils.isUsingMod("com.mrcrayfish.obfuscate.Obfuscate") || Loader.isModLoaded("obfuscate")) {
            MinecraftForge.EVENT_BUS.register(new HandAnimationEventHandler());

            if (event.getSide().isClient()) {
                MinecraftForge.EVENT_BUS.register(new HandAnimClientEventHandler());
            }
        }

        if (event.getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
            KeyBindings.register();
        }

        MinecraftForge.EVENT_BUS.register(new ImmersiveEventHandler());
        ImmersiveAddonPacketHandler.getInstance().registerPackets();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

        //network = NetworkRegistry.INSTANCE.newSimpleChannel(ImmersiveAddon.ID);
        //network.registerMessage(PacketAttachTrailer.Handler.class, PacketAttachTrailer.class, 0, Side.SERVER);
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {

        if(Utils.isUsingMod("com.mrcrayfish.obfuscate.Obfuscate") || Loader.isModLoaded("obfuscate")) {
            ModSyncedDataKeys.initKeys();
        }
        //network = NetworkRegistry.INSTANCE.newSimpleChannel(ImmersiveAddon.ID);
        //network.registerMessage(PacketAttachTrailer.Handler.class, PacketAttachTrailer.class, 0, Side.SERVER);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandShowNames());
        event.registerServerCommand(new CommandImmersiveAddon());
    }
}