![DynamX](https://dynamx.fr/img/head-logo.png)

# DynamX - ImmersiveAddon

This addon adds new features to DynamX :

- Harrow
- Anti puncture
- Hands on the steering wheel
- Optional Dynamic Lights support for vehicle beacons and headlights
- Requires the Dynamic Lights mod on the client.
- Engine upgrades via `/immersiveaddon mec vehicle engine <level> <player>` command
- Web radio playback. If WebDisplays is installed, the MCEF mod is required for
  integration (https://github.com/montoyo/mcef). If WebDisplays or MCEF is
  available, streams are played via an invisible screen so anyone within 16
  blocks of the vehicle hears them. If WebDisplays' screen API isn't available
  the addon falls back to MCEF's browser. Playback volume follows the
  Minecraft music slider and automatically stops when leaving a 16‑block
  radius. Enable debug mode in the config to print playback details in the
  console.
  Note that the official WebDisplays release for Minecraft 1.12.2 does not
  provide the `montoyo.webdisplays.api.ScreenHelper` class, so only the local
  MCEF fallback will work with that version.
 

### How to add modules:

To use modules ingame you have to add specific code blocks.
Here are some examples:

#### Harrow

To add this module to your prop, you need to edit the prop file inside your DynamX pack. This file have a name like block_NAME.
```
prop_NAME{
  #...
  HarrowAddon{
    IsHarrow: true
  }
}
```

#### Anti puncture

To add this module to your wheel, you need to edit the wheel file inside your DynamX pack. This file have a name like wheel_NAME.
```
HarrowAddon{
    CanFlattened: false
}
```

#### TowTruck

To add this module to your tow truck, you need to edit the car file inside your DynamX pack. This file have a name like vehicle_NAME.
```
TowTruckInfo{
    VehicleAttachPoint: 0 0 0
}
```

#### PesoDeVeiculo

Each vehicle stores its weight class in NBT using the `PesoDeVeiculo` tag. If
none is present the addon assumes the `medio` type. Use the console command

```
/immersiveaddon mec vehicle peso <type> <player>
```

while looking at a vehicle within five blocks to change its mass for the
specified player. The available `<type>` values are those defined in
`assets/dynamx_immersive/level1.json`.
The command forces a resynchronization so the vehicle instantly updates for
that player. Defaults are always loaded server‑side and sent to the client,
so editing your local `level1.json` will not affect vehicle mass.
Weight changes are now applied on both the server and the client.

#### Engine Tuning

You can also upgrade a vehicle's engine using the console command

```
/immersiveaddon mec vehicle engine <level> <player>
```

Again, aim at the vehicle within five blocks. Levels range from `1` to `5` and
correspond to the values stored in `assets/dynamx_immersive/engine_levels.json`.
The file follows the engine's original `.dynx` syntax with sections like
`Power`, `MaxRPM`, `Braking`, `Point_N`, and `Gear_N`. The parameters are
enforced server‑side and synchronized to the client immediately, so editing your
local copy won't change anything. Every vehicle spawns with engine level `1`,
ignoring the engine data from its pack. The command also forces a
resynchronization so clients apply the new values right away.

#### Wheel Tuning

Wheels can also be customized using

```
/immersiveaddon mec vehicle wheel <model|friction|susRestLength|susStiffness|skidParticle> <value> <player>
```

Look at the vehicle within five blocks and specify the option to change.
Allowed ranges and particles are defined in `assets/dynamx_immersive/wheel_config.json`.
Values outside these limits are clamped server‑side and synchronized to the client immediately.


## Links

DynamX website: https://dynamx.fr  
Addon download: https://dynamx.fr/addons  
DynamX wiki: https://dynamx.fr/wiki/  
Discord: https://discord.gg/y53KGzD 

## Authors

* **DynamX** - *Initial work* - [DynamX](https://dynamx.fr)
* **Ertinox45** - *Project initiator* - [Ertinox45](https://github.com/Ertinox45)
* **Gabidut76** - *Developer* - [Gabidut76](https://github.com/gabidut)
* **BlackNite** - *Thanks for the radio* - [BlackNite](https://github.com/BlackNiteHD)