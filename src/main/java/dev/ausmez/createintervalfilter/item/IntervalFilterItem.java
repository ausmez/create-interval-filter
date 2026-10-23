package dev.ausmez.createintervalfilter.item;

import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.item.ItemHelper;
import dev.ausmez.createintervalfilter.init.ModMenuTypes;
import dev.ausmez.createintervalfilter.menu.IntervalFilterMenu;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntervalFilterItem extends FilterItem {
    public static final ThreadLocal<Integer> ACTIVE_SLOT_INDEX = new ThreadLocal<>();

    public IntervalFilterItem(Item.Properties properties) {
        super(properties);
    }

    public static int getInterval(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("Interval") ? Math.max(1, tag.getInt("Interval")) : 1;
    }

    public static int getOffset(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("Offset") ? Math.max(0, tag.getInt("Offset")) : 0;
    }

    public static ExecutionMode getExecutionMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("ExecutionMode")) return ExecutionMode.STRICT;
        int idx = tag.getInt("ExecutionMode");
        return idx >= 0 && idx < ExecutionMode.values().length ? ExecutionMode.values()[idx] : ExecutionMode.STRICT;
    }

    public static PlacementMethod getPlacementMethod(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("PlacementMethod")) return PlacementMethod.SEQUENTIAL;
        int idx = tag.getInt("PlacementMethod");
        return idx >= 0 && idx < PlacementMethod.values().length ? PlacementMethod.values()[idx] : PlacementMethod.SEQUENTIAL;
    }

    @Override
    public List<Component> makeSummary(ItemStack stack) {
        final String CIF_GUI_PREFIX = "createintervalfilter.gui.interval_filter.";
        List<Component> list = new ArrayList<>();

        ItemStackHandler filterItems = getItemStackHandler(stack);
        int interval = getInterval(stack);
        int offset = getOffset(stack);
        ExecutionMode executionMode = getExecutionMode(stack);

        MutableComponent offsetTooltip = Component.empty();
        if (offset > 0)
            offsetTooltip = Component.translatable("item.createintervalfilter.interval_filter.tooltip.offset", offset);

        list.add(Component.translatable("item.createintervalfilter.interval_filter.tooltip.interval", interval, (interval == 1 ? "" : "s"))
                .append((offsetTooltip.equals(Component.empty())) ? offsetTooltip : Component.literal(" ").append(offsetTooltip))
                .withStyle(ChatFormatting.AQUA));

        MutableComponent executionModeTooltip;
        if (executionMode == ExecutionMode.STRICT)
            executionModeTooltip = Component.translatable(CIF_GUI_PREFIX + "strict");
        else
            executionModeTooltip = Component.translatable(CIF_GUI_PREFIX + "best_effort");

        PlacementMethod placementMethod = getPlacementMethod(stack);
        int nonEmptyCount = 0;
        for (int i = 0; i < filterItems.getSlotCount(); i++)
            if (!filterItems.getStackInSlot(i).isEmpty()) nonEmptyCount++;

        MutableComponent placementMethodTooltip = Component.empty();
        if (nonEmptyCount > 1) {
            if (placementMethod == PlacementMethod.SEQUENTIAL)
                placementMethodTooltip = Component.translatable(CIF_GUI_PREFIX + "sequential");
            else
                placementMethodTooltip = Component.translatable(CIF_GUI_PREFIX + "random");
        }

        list.add(executionModeTooltip.copy()
                .append(
                        (placementMethodTooltip.equals(Component.empty()))
                                ? placementMethodTooltip
                                : Component.literal(" ").append(placementMethodTooltip))
                .withStyle(ChatFormatting.DARK_AQUA)
        );

        int count = 0;
        for (int i = 0; i < filterItems.getSlotCount(); i++) {
            if (count > 3) {
                list.add(Component.literal("- ...").withStyle(ChatFormatting.DARK_GRAY));
                break;
            }
            ItemStack filterStack = filterItems.getStackInSlot(i);
            if (filterStack.isEmpty())
                continue;
            list.add(Component.literal("- ")
                    .append(filterStack.getHoverName())
                    .withStyle(ChatFormatting.GRAY));
            count++;
        }

        if (count == 0 && interval == 1 && offset == 0 && executionMode == ExecutionMode.STRICT && nonEmptyCount <= 1)
            return Collections.emptyList();

        return list;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new IntervalFilterMenu(ModMenuTypes.INTERVAL_FILTER.get(), containerId, playerInventory, player.getMainHandItem());
    }

    @Override
    public FilterItemStack makeStackWrapper(ItemStack filter) {
        return new IntervalFilterItemStack(filter);
    }

    @Override
    public ItemStack[] getFilterItems(ItemStack stack) {
        return ItemHelper.getNonEmptyStacks(getItemStackHandler(stack)).toArray(ItemStack[]::new);
    }

    public ItemStackHandler getItemStackHandler(ItemStack stack) {
        ItemStackHandler handler = new ItemStackHandler(6);
        CompoundTag tag = stack.getOrCreateTagElement("Items");
        if (!tag.isEmpty()) handler.deserializeNBT(tag);
        return handler;
    }

    public static class IntervalFilterItemStack extends FilterItemStack {
        public List<FilterItemStack> containedItems;
        public boolean shouldRespectNBT;

        public IntervalFilterItemStack(ItemStack filter) {
            super(filter);
            boolean hasTag = filter.hasTag();

            containedItems = new ArrayList<>();
            ItemStackHandler items = ((IntervalFilterItem) filter.getItem()).getItemStackHandler(filter);
            for (int i = 0; i < items.getSlotCount(); i++) {
                ItemStack stackInSlot = items.getStackInSlot(i);
                if (!stackInSlot.isEmpty())
                    containedItems.add(FilterItemStack.of(stackInSlot));
            }

            shouldRespectNBT = hasTag && filter.getOrCreateTag().getBoolean("RespectNBT");
        }

        @Override
        public boolean test(Level world, ItemStack stack, boolean matchNBT) {
            Integer activeSlot = IntervalFilterItem.ACTIVE_SLOT_INDEX.get();
            if (activeSlot != null) {
                if (activeSlot < containedItems.size())
                    return containedItems.get(activeSlot).test(world, stack, shouldRespectNBT);
                return false;
            }
            for (FilterItemStack filterItemStack : containedItems)
                if (filterItemStack.test(world, stack, shouldRespectNBT))
                    return true;
            return false;
        }
    }
}
