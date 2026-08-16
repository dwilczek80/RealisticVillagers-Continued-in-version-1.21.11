package me.matsubara.realisticvillagers.village;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.structure.Palette;
import org.bukkit.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A building the settlement knows how to raise, read from a saved structure file.
 * <p>
 * These are ordinary Minecraft {@code .nbt} structures — the ones a <b>structure block</b> saves.
 * You build the thing you want in game, save it, drop the file in the schematics folder, and the
 * settlement can build it. Nothing else is needed: no WorldEdit, no other plugin, no format of
 * this project's own invention. Bukkit reads them itself.
 * <p>
 * This replaced a text format where each building was a palette of letters and a stack of rows.
 * That format could describe a building but not <i>faithfully</i>: every block's state had to be
 * written out by hand, so stairs came out facing the wrong way, doors arrived without their top
 * half, and panes and fences stood unconnected because nothing recorded what they were joined to.
 * A structure file has none of those problems by construction — it stores the exact state of every
 * block as it stood in the world, connections and all, because it was copied from a building that
 * really existed.
 */
public final class Blueprint {

    private final String id;
    private final String name;
    private final Material icon;
    private final int seconds;

    /** {@code [y][z][x]}, exactly as the structure was saved. Null entries are open air. */
    private final BlockData[][][] blocks;

    private final Map<Material, Integer> cost;

    /**
     * The region this building belongs to, or {@code null} for one that suits anywhere.
     * <p>
     * Replaces swapping materials by biome, which was a bad idea honestly held: substituting
     * sandstone into a building designed in oak produces something nobody drew and nobody wants,
     * and the more carefully a building was designed the worse the result. Sorting buildings by
     * the land they suit gets the same thing — desert villages that look like desert villages —
     * out of buildings that were each designed for the place they end up in.
     */
    private final @Nullable String region;

    /**
     * The player who discovered this, or {@code null} for one the server provides.
     * <p>
     * Discoveries are personal. Finding a village is the interesting part, and a shared list
     * hands everyone else the reward for it the moment the first player walks in — on a busy
     * server the list would be complete within a day and finding anything would stop mattering.
     * <p>
     * It also settles who may rename what without inventing a permission for it: you can rename
     * what you found, because nobody else can see it, and you cannot rename what the server put
     * there, because it isn't yours.
     */
    private final @Nullable java.util.UUID owner;

    /**
     * The world it was discovered in, or {@code null} for one the server provides.
     * <p>
     * Discoveries are stored inside the world they were made in, so deleting a world takes its
     * blueprints with it. Kept in the plugin folder they would outlive the place they came from —
     * a new world would open with a menu full of buildings from a world that no longer exists,
     * and there would be no way to tell which those were.
     */
    private final @Nullable String world;

    private Blueprint(String id, String name, Material icon, int seconds, BlockData[][][] blocks,
                      @Nullable String region, @Nullable java.util.UUID owner, @Nullable String world) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.seconds = seconds;
        this.blocks = blocks;
        this.region = region;
        this.owner = owner;
        this.world = world;
        this.cost = countCost(blocks);
    }

    /**
     * A fingerprint of the blocks this building is made of.
     * <p>
     * Vanilla builds the same handful of houses in every village it generates, so recording the
     * second village you walk into would otherwise save another copy of the plains cottage you
     * already have, and the third would save a third. Comparing what a building is <i>made of</i>
     * catches that no matter which village it was found in or what it ended up being called.
     * <p>
     * Size is folded in first, so two buildings can only match if they are the same shape.
     */
    public long fingerprint() {
        long hash = 1125899906842597L;

        hash = hash * 31 + getWidth();
        hash = hash * 31 + getHeight();
        hash = hash * 31 + getDepth();

        for (BlockData[][] layer : blocks) {
            for (BlockData[] row : layer) {
                for (BlockData data : row) {
                    hash = hash * 31 + (data == null ? 0 : data.getAsString().hashCode());
                }
            }
        }

        return hash;
    }

    /** Who found this, or {@code null} for one the server provides to everybody. */
    public @Nullable java.util.UUID getOwner() {
        return owner;
    }

    public @Nullable String getWorld() {
        return world;
    }

    /**
     * Whether this player may see it here: everyone sees the server's, you see your own — and
     * only in the world you found them in.
     */
    public boolean visibleTo(@Nullable java.util.UUID player, @Nullable String inWorld) {
        if (owner == null) return true;
        return owner.equals(player) && (world == null || world.equals(inWorld));
    }

    /** The region this suits, or {@code null} for anywhere. */
    public @Nullable String getRegion() {
        return region;
    }

    /** Whether this building may be raised in a settlement of the given region. */
    public boolean suits(@Nullable String villageRegion) {
        return region == null || region.equalsIgnoreCase(villageRegion);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Material getIcon() {
        return icon;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getHeight() {
        return blocks.length;
    }

    public int getDepth() {
        return blocks.length == 0 ? 0 : blocks[0].length;
    }

    public int getWidth() {
        return getDepth() == 0 ? 0 : blocks[0][0].length;
    }

    /** Everything it takes to build, in items. */
    public @NotNull Map<Material, Integer> getCost() {
        return cost;
    }

    public int getBlockCount() {
        int total = 0;
        for (int value : cost.values()) total += value;
        return total;
    }

    /**
     * The block at this spot, turned to match the building, or {@code null} for open air.
     * <p>
     * The block's own state is turned along with its position, so a rotated house keeps its door
     * in the doorway and its roof running the right way.
     */
    public @Nullable BlockData blockAt(int x, int y, int z, int rotation) {
        if (y < 0 || y >= getHeight()) return null;

        int width = getWidth();
        int depth = getDepth();

        int localX;
        int localZ;

        switch (((rotation % 4) + 4) % 4) {
            case 1 -> {
                localX = z;
                localZ = width - 1 - x;
            }
            case 2 -> {
                localX = width - 1 - x;
                localZ = depth - 1 - z;
            }
            case 3 -> {
                localX = depth - 1 - z;
                localZ = x;
            }
            default -> {
                localX = x;
                localZ = z;
            }
        }

        if (localZ < 0 || localZ >= depth || localX < 0 || localX >= width) return null;

        BlockData data = blocks[y][localZ][localX];
        if (data == null) return null;

        StructureRotation turn = switch (((rotation % 4) + 4) % 4) {
            case 1 -> StructureRotation.CLOCKWISE_90;
            case 2 -> StructureRotation.CLOCKWISE_180;
            case 3 -> StructureRotation.COUNTERCLOCKWISE_90;
            default -> StructureRotation.NONE;
        };

        if (turn == StructureRotation.NONE) return data;

        BlockData turned = data.clone();
        try {
            turned.rotate(turn);
        } catch (Throwable ignored) {
            // Older API: the building still turns, its blocks just keep their original facing.
        }
        return turned;
    }

    public int getWidth(int rotation) {
        return (rotation % 2 == 0) ? getWidth() : getDepth();
    }

    public int getDepth(int rotation) {
        return (rotation % 2 == 0) ? getDepth() : getWidth();
    }

    /**
     * What the building costs, in items rather than blocks.
     * <p>
     * The two differ more often than they look. A door is one item and two blocks, a bed is one
     * item and two blocks, and a tall flower likewise — so only the lower half of each is counted.
     * Water and other things that exist in the world but not in an inventory are placed free,
     * since a settlement can no more stockpile water than it can stockpile fire.
     */
    private static @NotNull Map<Material, Integer> countCost(BlockData[][][] blocks) {
        Map<Material, Integer> counted = new EnumMap<>(Material.class);

        for (BlockData[][] layer : blocks) {
            for (BlockData[] row : layer) {
                for (BlockData data : row) {
                    if (data == null) continue;

                    Material material = data.getMaterial();
                    if (!material.isItem()) continue;

                    if (data instanceof Bisected bisected && bisected.getHalf() == Bisected.Half.TOP) continue;
                    if (data instanceof Bed bed && bed.getPart() == Bed.Part.HEAD) continue;

                    counted.merge(material, 1, Integer::sum);
                }
            }
        }

        return Map.copyOf(counted);
    }

    /** Builds one from an unpacked .schem, which arrives as a plain block array. */
    static @Nullable Blueprint of(
            @NotNull String id,
            SchematicReader.@NotNull Schematic schematic,
            int secondsPerBlock,
            @Nullable String region,
            @Nullable java.util.UUID owner,
            @Nullable String world) {

        Material commonest = Material.OAK_PLANKS;
        Map<Material, Integer> tally = new EnumMap<>(Material.class);

        for (BlockData[][] layer : schematic.blocks()) {
            for (BlockData[] row : layer) {
                for (BlockData data : row) {
                    if (data == null) continue;

                    int seen = tally.merge(data.getMaterial(), 1, Integer::sum);
                    if (seen > tally.getOrDefault(commonest, 0)) commonest = data.getMaterial();
                }
            }
        }

        Blueprint counted = new Blueprint(id, prettify(id),
                commonest.isItem() ? commonest : Material.OAK_PLANKS, 0, schematic.blocks(), region, owner, world);

        if (counted.getBlockCount() <= 0) return null;

        int seconds = Math.max(10, counted.getBlockCount() / Math.max(1, secondsPerBlock));
        return new Blueprint(id, counted.getName(), counted.getIcon(), seconds, schematic.blocks(), region, owner, world);
    }

    /**
     * Reads a blueprint out of a loaded structure.
     *
     * @return {@code null} for an empty structure, which is a file worth reporting rather than an
     * entry worth listing.
     */
    static @Nullable Blueprint of(@NotNull String id, @NotNull Structure structure, int secondsPerBlock,
                                  @Nullable String region, @Nullable java.util.UUID owner, @Nullable String world) {
        List<Palette> palettes = structure.getPalettes();
        if (palettes.isEmpty()) return null;

        var size = structure.getSize();
        int width = size.getBlockX();
        int height = size.getBlockY();
        int depth = size.getBlockZ();

        if (width <= 0 || height <= 0 || depth <= 0) return null;

        BlockData[][][] blocks = new BlockData[height][depth][width];

        Material commonest = Material.OAK_PLANKS;
        Map<Material, Integer> tally = new EnumMap<>(Material.class);

        // The first palette only. A structure can hold several as random variants, and building a
        // different one each time would mean the price shown was not the price charged.
        for (org.bukkit.block.BlockState state : palettes.get(0).getBlocks()) {
            int x = state.getX();
            int y = state.getY();
            int z = state.getZ();

            if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth) continue;

            BlockData data = state.getBlockData();
            if (data.getMaterial().isAir()) continue;

            blocks[y][z][x] = data;

            int seen = tally.merge(data.getMaterial(), 1, Integer::sum);
            if (seen > tally.getOrDefault(commonest, 0)) commonest = data.getMaterial();
        }

        Blueprint blueprint = new Blueprint(
                id,
                prettify(id),
                commonest.isItem() ? commonest : Material.OAK_PLANKS,
                0,
                blocks,
                region,
                owner,
                world);

        if (blueprint.getBlockCount() <= 0) return null;

        // Timed by how much there is to build, so a cottage goes up quickly and a keep takes a
        // while, without anybody having to write a number next to every file.
        int seconds = Math.max(10, blueprint.getBlockCount() / Math.max(1, secondsPerBlock));
        return new Blueprint(id, blueprint.getName(), blueprint.getIcon(), seconds, blocks, region, owner, world);
    }

    /**
     * {@code plains/town_hall} becomes {@code Town Hall}.
     * <p>
     * The folder is dropped. It is part of the id so two regions can each have a cottage, but
     * showing it turns a building's name into a file path — and "Plains/brightstead 3" is not a
     * name, it is a location and a number.
     */
    private static @NotNull String prettify(@NotNull String id) {
        int slash = id.lastIndexOf('/');
        if (slash >= 0) id = id.substring(slash + 1);

        String[] words = id.replace('_', ' ').replace('-', ' ').trim().split("\\s+");

        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1).toLowerCase(Locale.ROOT));
        }

        return out.length() == 0 ? id : out.toString();
    }
}
