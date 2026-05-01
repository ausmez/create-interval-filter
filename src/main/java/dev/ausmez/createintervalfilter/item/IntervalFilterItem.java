package dev.ausmez.createintervalfilter.item;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.item.ItemHelper;
import dev.ausmez.createintervalfilter.init.ModDataComponents;
import dev.ausmez.createintervalfilter.init.ModMenuTypes;
import dev.ausmez.createintervalfilter.menu.IntervalFilterMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntervalFilterItem extends FilterItem {
    public static final ThreadLocal<Integer> ACTIVE_SLOT_INDEX = new ThreadLocal<>();

    public IntervalFilterItem(Properties properties) {
        super(properties);
    }

    public static int getInterval(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.INTERVAL.get(), 1);
    }

    public static int getOffset(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.OFFSET.get(), 0);
    }

    public static ExecutionMode getExecutionMode(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.EXECUTION_MODE.get(), ExecutionMode.STRICT);
    }

    public static PlacementMethod getPlacementMethod(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.PLACEMENT_METHOD.get(), PlacementMethod.SEQUENTIAL);
    }

    @Override
    public List<Component> makeSummary(ItemStack filter) {
        final String CIF_GUI_PREFIX = "createintervalfilter.gui.interval_filter.";
        List<Component> list = new ArrayList<>();

        ItemStackHandler filterItems = getItemStackHandler(filter);
        int interval = getInterval(filter);
        int offset = getOffset(filter);
        ExecutionMode executionMode = getExecutionMode(filter);

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

        PlacementMethod placementMethod = getPlacementMethod(filter);
        int nonEmptyCount = 0;
        for (int i = 0; i < filterItems.getSlots(); i++)
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
        for (int i = 0; i < filterItems.getSlots(); i++) {
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
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new IntervalFilterMenu(ModMenuTypes.INTERVAL_FILTER.get(), id, inv, player.getMainHandItem());
    }

    @Override
    public DataComponentType<?> getComponentType() {
        return AllDataComponents.FILTER_ITEMS;
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
        ItemContainerContents contents = stack.getOrDefault(AllDataComponents.FILTER_ITEMS, ItemContainerContents.EMPTY);
        ItemHelper.fillItemStackHandler(contents, handler);
        return handler;
    }

    public static class IntervalFilterItemStack extends FilterItemStack {

        public List<FilterItemStack> containedItems;
        public boolean shouldRespectNBT;

        public IntervalFilterItemStack(ItemStack filter) {
            super(filter);
            boolean hasFilterItems = filter.has(AllDataComponents.FILTER_ITEMS);

            containedItems = new ArrayList<>();
            ItemStackHandler items = ((IntervalFilterItem) filter.getItem()).getItemStackHandler(filter);
            for (int i = 0; i < items.getSlots(); i++) {
                ItemStack stackInSlot = items.getStackInSlot(i);
                if (!stackInSlot.isEmpty())
                    containedItems.add(FilterItemStack.of(stackInSlot));
            }

            shouldRespectNBT = hasFilterItems && filter.getOrDefault(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, false);
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
