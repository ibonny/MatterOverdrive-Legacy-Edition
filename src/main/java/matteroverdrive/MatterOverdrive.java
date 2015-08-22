/*
 * This file is part of Matter Overdrive
 * Copyright (c) 2015., Simeon Radivoev, All rights reserved.
 *
 * Matter Overdrive is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Matter Overdrive is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Matter Overdrive.  If not, see <http://www.gnu.org/licenses>.
 */

package matteroverdrive;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import matteroverdrive.commands.AndoidCommands;
import matteroverdrive.commands.MatterRegistryCommands;
import matteroverdrive.compat.MatterOverdriveCompat;
import matteroverdrive.dialog.DialogRegistry;
import matteroverdrive.handler.*;
import matteroverdrive.imc.MOIMCHandler;
import matteroverdrive.init.*;
import matteroverdrive.matter_network.MatterNetworkRegistry;
import matteroverdrive.network.PacketPipeline;
import matteroverdrive.proxy.CommonProxy;
import matteroverdrive.util.MOLog;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, guiFactory = Reference.GUI_FACTORY_CLASS, dependencies = Reference.DEPEDNENCIES)
public class MatterOverdrive 
{
	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static CommonProxy proxy;
	
	public static final MatterOverdriveTab tabMatterOverdrive = new MatterOverdriveTab("tabMatterOverdrive");
	public static final MatterOverdriveTab tabMatterOverdrive_modules = new MatterOverdriveTab("tabMatterOverdrive_modules");
	public static final MatterOverdriveTab tabMatterOverdrive_upgrades = new MatterOverdriveTab("tabMatterOverdrive_upgrades");
	public static final MatterOverdriveTab tabMatterOverdrive_food = new MatterOverdriveTab("tabMatterOverdrive_food");
	public static final MatterOverdriveTab tabMatterOverdrive_ships = new MatterOverdriveTab("tabMatterOverdrive_ships");
	public static final MatterOverdriveTab tabMatterOverdrive_buildings = new MatterOverdriveTab("tabMatterOverdrive_buildings");

    public static TickHandler tickHandler;
    public static PlayerEventHandler playerEventHandler;
	public static ConfigurationHandler configHandler;
    public static GuiHandler guiHandler;
    public static PacketPipeline packetPipeline;
	public static BucketHandler bucketHandler;
	@Instance(Reference.MOD_ID)
	public static MatterOverdrive instance;
	public static MatterOverdriveWorld moWorld;
	public static EntityHandler entityHandler;
	public static MatterRegistry matterRegistry;
	public static AndroidStatRegistry statRegistry;
	public static DialogRegistry dialogRegistry;
    public static MatterRegistrationHandler matterRegistrationHandler;
	public static final ExecutorService threadPool = Executors.newFixedThreadPool(2);

	public static MOLog log = new MOLog();

	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
        checkJavaVersion();
		matterRegistry = new MatterRegistry();
		statRegistry = new AndroidStatRegistry();
		dialogRegistry = new DialogRegistry();
        guiHandler = new GuiHandler();
		packetPipeline = new PacketPipeline();
		entityHandler = new EntityHandler();
        configHandler = new ConfigurationHandler(new File(event.getModConfigurationDirectory().getAbsolutePath() + File.separator + "MatterOverdrive" + File.separator + Reference.MOD_NAME + ".cfg"));
		playerEventHandler = new PlayerEventHandler(configHandler);
		bucketHandler = new BucketHandler();
        matterRegistrationHandler = new MatterRegistrationHandler(event.getModConfigurationDirectory().getAbsolutePath() + File.separator + "MatterOverdrive" + File.separator + "Registry" + ".matter");

		FMLCommonHandler.instance().bus().register(configHandler);
		tickHandler = new TickHandler(configHandler,playerEventHandler);
        FMLCommonHandler.instance().bus().register(tickHandler);
        FMLCommonHandler.instance().bus().register(playerEventHandler);
		MinecraftForge.EVENT_BUS.register(playerEventHandler);
		MinecraftForge.EVENT_BUS.register(bucketHandler);
		MatterOverdriveFluids.init(event);
        MatterOverdriveBlocks.init(event);
		MatterOverdriveItems.init(event);
		moWorld = new MatterOverdriveWorld(configHandler);
		MatterOverdriveEntities.init(event, configHandler);
		MatterOverdriveEnchantments.init(event, configHandler);
		MatterOverdriveDialogs.init(event, configHandler, dialogRegistry);
		moWorld.register();
		MatterNetworkRegistry.register();
        packetPipeline.registerPackets();
		statRegistry.init();
		statRegistry.registerAll(configHandler);
		MatterOverdriveMatter.init(configHandler);
		MatterOverdriveMatter.registerBlacklistFromConfig(configHandler);
		MatterOverdriveMatter.registerFromConfig(configHandler);
		MatterOverdriveMatter.registerBasic(configHandler);
		matterRegistry.preInit(event,configHandler);
		UpdateTabs();

		proxy.registerCompatModules();
		MatterOverdriveCompat.preInit(event);
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
        guiHandler.register(event.getSide());
		NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);
		MinecraftForge.EVENT_BUS.register(entityHandler);
		proxy.init(event);
		configHandler.init();
		MatterOverdriveCompat.init(event);

		MatterOverdriveBlocks.register(event);
		MatterOverdriveItems.register(event);
		MatterOverdriveFluids.register(event);
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		MatterOverdriveCompat.postInit(event);
		MatterOverdriveEntities.register(event);
		MatterOverdriveItems.addToDungons();

		configHandler.postInit();
	}


	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new AndoidCommands());
		event.registerServerCommand(new MatterRegistryCommands());
	}

    @EventHandler
    public void serverStart(FMLServerStartedEvent event)
    {


		tickHandler.onServerStart(event);
    }

	@EventHandler
	public void imcCallback(FMLInterModComms.IMCEvent event) {
		MOIMCHandler.imcCallback(event);
	}

	private void UpdateTabs()
	{
		tabMatterOverdrive.item = MatterOverdriveItems.matter_scanner;
		tabMatterOverdrive_modules.item = MatterOverdriveItems.weapon_module_color;
		tabMatterOverdrive_upgrades.item = MatterOverdriveItems.item_upgrade;
		tabMatterOverdrive_food.item = MatterOverdriveItems.earl_gray_tea;
		tabMatterOverdrive_ships.item = MatterOverdriveItems.colonizerShip;
		tabMatterOverdrive_buildings.item = MatterOverdriveItems.buildingBase;
	}

	private void checkJavaVersion()
	{
        String versionString = System.getProperty("java.version");
        int pos = versionString.indexOf('.');
        pos = versionString.indexOf('.', pos+1);
        double version = Double.parseDouble(versionString.substring(0, pos));
        if (version < 1.8)
        {
			MatterOverdrive.log.warn("Matter Overdrive only supports Java 8 and above. Please update your Java version. You are currently using: " + version);
        }
	}
}
