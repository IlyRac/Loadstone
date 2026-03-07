package com.ilyrac.loadstone.loader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.util.datafix.DataFixTypes;
import java.util.Map;
import java.util.HashMap;

public class LoadstoneData extends SavedData {
    public final Map<BlockPos, LoaderTier> activeLoaders;

    private record LoaderEntry(BlockPos pos, LoaderTier tier) {
        public static final Codec<LoaderEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        BlockPos.CODEC.fieldOf("pos").forGetter(LoaderEntry::pos),
                        Codec.STRING.xmap(LoaderTier::valueOf, Enum::name).fieldOf("tier").forGetter(LoaderEntry::tier)
                ).apply(instance, LoaderEntry::new)
        );
    }

    public static final Codec<LoadstoneData> CODEC = LoaderEntry.CODEC.listOf().xmap(
            list -> {
                Map<BlockPos, LoaderTier> map = new HashMap<>();
                list.forEach(entry -> map.put(entry.pos, entry.tier));
                return new LoadstoneData(map);
            },
            data -> data.activeLoaders.entrySet().stream()
                    .map(e -> new LoaderEntry(e.getKey(), e.getValue()))
                    .toList()
    );

    public static final SavedDataType<LoadstoneData> TYPE = new SavedDataType<>(
            "loadstone_data",
            LoadstoneData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    public LoadstoneData(Map<BlockPos, LoaderTier> loaders) {
        this.activeLoaders = new HashMap<>(loaders);
    }

    public LoadstoneData() {
        this.activeLoaders = new HashMap<>();
    }

    public static LoadstoneData getServerState(MinecraftServer server) {
        ServerLevel storageLevel = server.getLevel(Level.OVERWORLD);
        if (storageLevel == null) return new LoadstoneData();
        return storageLevel.getDataStorage().computeIfAbsent(TYPE);
    }
}