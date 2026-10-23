package dev.ausmez.createintervalfilter.menu;

import com.simibubi.create.content.logistics.filter.AbstractFilterMenu;
import dev.ausmez.createintervalfilter.item.ExecutionMode;
import dev.ausmez.createintervalfilter.item.PlacementMethod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class IntervalFilterMenu extends AbstractFilterMenu {
    public boolean respectNBT;
    public int interval;
    public int offset;
    public ExecutionMode executionMode;
    public PlacementMethod placementMethod;

    public IntervalFilterMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public IntervalFilterMenu(MenuType<?> type, int id, Inventory inv, ItemStack stack) {
        super(type, id, inv, stack);
    }

    @Override
    protected int getPlayerInventoryXOffset() {
        return 38;
    }

    @Override
    protected int getPlayerInventoryYOffset() {
        return 121;
    }

    @Override
    protected void addFilterSlots() {
        int x = 23;
        int y = 25;
        for (int col = 0; col < 3; ++col)
            addSlot(new SlotItemHandler(ghostInventory, col, x + col * 18, y));
        for (int col = 0; col < 3; ++col)
            addSlot(new SlotItemHandler(ghostInventory, 3 + col, x + col * 18, y + 18));
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        ItemStackHandler handler = new ItemStackHandler(6);
        CompoundTag tag = contentHolder.getOrCreateTagElement("Items");
        if (!tag.isEmpty()) handler.deserializeNBT(tag);
        return handler;
    }

    @Override
    protected void initAndReadInventory(ItemStack filterItem) {
        super.initAndReadInventory(filterItem);
        CompoundTag tag = filterItem.getOrCreateTag();
        respectNBT = tag.getBoolean("RespectNBT");
        interval = Math.max(1, tag.contains("Interval") ? tag.getInt("Interval") : 1);
        offset = Math.max(0, tag.contains("Offset") ? tag.getInt("Offset") : 0);
        int emOrdinal = tag.contains("ExecutionMode") ? tag.getInt("ExecutionMode") : 0;
        executionMode = emOrdinal >= 0 && emOrdinal < ExecutionMode.values().length
                ? ExecutionMode.values()[emOrdinal] : ExecutionMode.STRICT;
        int pmOrdinal = tag.contains("PlacementMethod") ? tag.getInt("PlacementMethod") : 0;
        placementMethod = pmOrdinal >= 0 && pmOrdinal < PlacementMethod.values().length
                ? PlacementMethod.values()[pmOrdinal] : PlacementMethod.SEQUENTIAL;
    }

    @Override
    protected void saveData(ItemStack filterItem) {
        super.saveData(filterItem);
        CompoundTag tag = filterItem.getOrCreateTag();
        tag.putBoolean("RespectNBT", respectNBT);
        tag.putInt("Interval", Math.max(1, interval));
        tag.putInt("Offset", Math.max(0, offset));
        tag.putInt("ExecutionMode", executionMode.ordinal());
        tag.putInt("PlacementMethod", placementMethod.ordinal());

        if (respectNBT)
            return;
        for (int i = 0; i < ghostInventory.getSlots(); i++)
            if (!ghostInventory.getStackInSlot(i).isEmpty())
                return;
        tag.remove("RespectNBT");
    }
}
