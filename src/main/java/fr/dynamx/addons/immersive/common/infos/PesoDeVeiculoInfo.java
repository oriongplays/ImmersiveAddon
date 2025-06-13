package fr.dynamx.addons.immersive.common.infos;

import fr.dynamx.api.contentpack.object.subinfo.ISubInfoTypeOwner;
import fr.dynamx.api.contentpack.object.subinfo.SubInfoType;
import fr.dynamx.api.contentpack.registry.DefinitionType;
import fr.dynamx.api.contentpack.registry.PackFileProperty;
import fr.dynamx.api.contentpack.registry.RegisteredSubInfoType;
import fr.dynamx.api.contentpack.registry.SubInfoTypeRegistries;
import fr.dynamx.common.contentpack.type.vehicle.ModularVehicleInfo;

@RegisteredSubInfoType(name = "PesoDeVeiculo", registries = {SubInfoTypeRegistries.WHEELED_VEHICLES, SubInfoTypeRegistries.HELICOPTER, SubInfoTypeRegistries.BOATS}, strictName = false)
public class PesoDeVeiculoInfo extends SubInfoType<ModularVehicleInfo> {

    @PackFileProperty(configNames = {"Type"}, type = DefinitionType.DynamXDefinitionTypes.STRING, required = false)
    public String type = "leve";

    public PesoDeVeiculoInfo(ISubInfoTypeOwner<ModularVehicleInfo> owner) {
        super(owner);
    }

    @Override
    public void appendTo(ModularVehicleInfo owner) {
        owner.addSubProperty(this);
    }

    @Override
    public String getName() {
        return "PesoDeVeiculo of " + getOwner().getFullName();
    }

    @Override
    public String getPackName() {
        return owner.getPackName();
    }
}