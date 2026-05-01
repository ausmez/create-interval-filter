package dev.ausmez.createintervalfilter.menu;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.filter.AbstractFilterMenu;
import com.simibubi.create.foundation.item.ItemHelper;
import dev.ausmez.createintervalfilter.init.ModDataComponents;
import dev.ausmez.createintervalfilter.item.ExecutionMode;
import dev.ausmez.createintervalfilter.item.PlacementMethod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class IntervalFilterMenu extends AbstractFilterMenu {
    public boolean respectNBT;
    public int interval;
    public int offset;
    public ExecutionMode executionMode;
    public PlacementMethod placementMethod;

    public IntervalFilterMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
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
        ItemContainerContents contents = contentHolder.getOrDefault(AllDataComponents.FILTER_ITEMS, ItemContainerContents.EMPTY);
        ItemHelper.fillItemStackHandler(contents, handler);
        return handler;
    }

    @Override
    protected void initAndReadInventory(ItemStack filterItem) {
        super.initAndReadInventory(filterItem);
        respectNBT = filterItem.getOrDefault(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, false);
        interval = filterItem.getOrDefault(ModDataComponents.INTERVAL.get(), 1);
        offset = filterItem.getOrDefault(ModDataComponents.OFFSET.get(), 0);
        executionMode = filterItem.getOrDefault(ModDataComponents.EXECUTION_MODE.get(), ExecutionMode.STRICT);
        placementMethod = filterItem.getOrDefault(ModDataComponents.PLACEMENT_METHOD.get(), PlacementMethod.SEQUENTIAL);
    }

    @Override
    protected void saveData(ItemStack filterItem) {
        super.saveData(filterItem);
        filterItem.set(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, respectNBT);
        filterItem.set(ModDataComponents.INTERVAL.get(), Math.max(1, interval));
        filterItem.set(ModDataComponents.OFFSET.get(), Math.max(0, offset));
        filterItem.set(ModDataComponents.EXECUTION_MODE.get(), executionMode);
        filterItem.set(ModDataComponents.PLACEMENT_METHOD.get(), placementMethod);

        if (respectNBT)
            return;
        for (int i = 0; i < ghostInventory.getSlots(); i++)
            if (!ghostInventory.getStackInSlot(i).isEmpty())
                return;
        filterItem.remove(AllDataComponents.FILTER_ITEMS_RESPECT_NBT);
    }
}
