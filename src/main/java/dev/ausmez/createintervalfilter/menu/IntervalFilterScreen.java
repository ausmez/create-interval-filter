package dev.ausmez.createintervalfilter.menu;

import dev.ausmez.createintervalfilter.CreateIntervalFilter;
import dev.ausmez.createintervalfilter.init.ModNetwork;
import dev.ausmez.createintervalfilter.item.ExecutionMode;
import dev.ausmez.createintervalfilter.item.PlacementMethod;
import dev.ausmez.createintervalfilter.network.IntervalFilterPacket;
import com.simibubi.create.content.logistics.filter.AbstractFilterScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class IntervalFilterScreen extends AbstractFilterScreen<IntervalFilterMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            CreateIntervalFilter.MODID, "textures/gui/interval_filter.png");

    private static final String CREATE_PREFIX = "gui.filter.";
    private static final String INTERVAL_PREFIX = "createintervalfilter.gui.interval_filter.";

    private final Component respectDataN = CreateLang.translateDirect(CREATE_PREFIX + "respect_data");
    private final Component respectDataDESC = CreateLang.translateDirect(CREATE_PREFIX + "respect_data.description");
    private final Component ignoreDataN = CreateLang.translateDirect(CREATE_PREFIX + "ignore_data");
    private final Component ignoreDataDESC = CreateLang.translateDirect(CREATE_PREFIX + "ignore_data.description");

    private final Component strictN = Component.translatable(INTERVAL_PREFIX + "strict");
    private final Component strictDESC = Component.translatable(INTERVAL_PREFIX + "strict.description");
    private final Component bestEffortN = Component.translatable(INTERVAL_PREFIX + "best_effort");
    private final Component bestEffortDESC = Component.translatable(INTERVAL_PREFIX + "best_effort.description");

    private IconButton respectNBT, ignoreNBT;
    private IconButton strict, bestEffort;

    public IntervalFilterScreen(IntervalFilterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, AllGuiTextures.FILTER);
    }

    @Override
    protected void init() {
        setWindowOffset(-11, 7);
        super.init();

        int x = leftPos;
        int y = topPos;

        strict = new IconButton(x + 18, y + 75, AllIcons.I_TARGET);
        strict.withCallback(() -> {
            menu.executionMode = ExecutionMode.STRICT;
            ModNetwork.sendToServer(new IntervalFilterPacket(IntervalFilterPacket.Option.SET_EXECUTION_MODE, ExecutionMode.STRICT.ordinal()));
        });
        strict.setToolTip(strictN);

        bestEffort = new IconButton(x + 36, y + 75, AllIcons.I_REFRESH);
        bestEffort.withCallback(() -> {
            menu.executionMode = ExecutionMode.BEST_EFFORT;
            ModNetwork.sendToServer(new IntervalFilterPacket(IntervalFilterPacket.Option.SET_EXECUTION_MODE, ExecutionMode.BEST_EFFORT.ordinal()));
        });
        bestEffort.setToolTip(bestEffortN);

        respectNBT = new IconButton(x + 60, y + 75, AllIcons.I_RESPECT_NBT);
        respectNBT.withCallback(() -> {
            menu.respectNBT = true;
            ModNetwork.sendToServer(new IntervalFilterPacket(IntervalFilterPacket.Option.RESPECT_DATA, 0));
        });
        respectNBT.setToolTip(respectDataN);

        ignoreNBT = new IconButton(x + 78, y + 75, AllIcons.I_IGNORE_NBT);
        ignoreNBT.withCallback(() -> {
            menu.respectNBT = false;
            ModNetwork.sendToServer(new IntervalFilterPacket(IntervalFilterPacket.Option.IGNORE_DATA, 0));
        });
        ignoreNBT.setToolTip(ignoreDataN);

        addRenderableWidgets(respectNBT, ignoreNBT, strict, bestEffort);

        Label intervalLabel = new Label(x + 89, y + 38, Component.empty())
                .colored(0xF3EBDE).withShadow();
        addRenderableWidget(intervalLabel);

        ScrollInput intervalScroll = new ScrollInput(x + 86, y + 33, 18, 18)
                .withRange(1, 65)
                .titled(Component.translatable(INTERVAL_PREFIX + "interval"))
                .addHint(Component.translatable(INTERVAL_PREFIX + "interval.description"))
                .setState(menu.interval)
                .writingTo(intervalLabel)
                .calling(i -> {
                    menu.interval = i;
                    ModNetwork.sendToServer(new IntervalFilterPacket(IntervalFilterPacket.Option.SET_INTERVAL, i));
                });
        addRenderableWidget(intervalScroll);

        Label offsetLabel = new Label(x + 111, y + 38, Component.empty())
                .colored(0xF3EBDE).withShadow();
        addRenderableWidget(offsetLabel);

        ScrollInput offsetScroll = new ScrollInput(x + 108, y + 33, 18, 18)
                .withRange(0, 17)
                .titled(Component.translatable(INTERVAL_PREFIX + "offset"))
                .addHint(Component.translatable(INTERVAL_PREFIX + "offset.description"))
                .setState(menu.offset)
                .writingTo(offsetLabel)
                .calling(i -> {
                    menu.offset = i;
                    ModNetwork.sendToServer(new IntervalFilterPacket(IntervalFilterPacket.Option.SET_OFFSET, i));
                });
        addRenderableWidget(offsetScroll);

        Label placementLabel = new Label(x + 133, y + 38, Component.empty())
                .colored(0xF3EBDE).withShadow();
        addRenderableWidget(placementLabel);

        SelectionScrollInput placementScroll = new SelectionScrollInput(x + 130, y + 33, 61, 18);
        placementScroll.forOptions(Arrays.asList(
                        Component.translatable(INTERVAL_PREFIX + "sequential"),
                        Component.translatable(INTERVAL_PREFIX + "random")))
                .titled(Component.translatable(INTERVAL_PREFIX + "placement_mode"))
                .addHint(Component.translatable(INTERVAL_PREFIX + "placement_mode.description"))
                .setState(menu.placementMethod.ordinal())
                .writingTo(placementLabel)
                .calling(i -> {
                    menu.placementMethod = PlacementMethod.values()[i];
                    ModNetwork.sendToServer(new IntervalFilterPacket(IntervalFilterPacket.Option.SET_PLACEMENT_METHOD, i));
                });
        addRenderableWidget(placementScroll);

        handleIndicators();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int invX = getLeftOfCentered(AllGuiTextures.PLAYER_INVENTORY.getWidth());
        int invY = topPos + background.getHeight() + 4;
        renderPlayerInventory(graphics, invX, invY);

        int x = leftPos;
        int y = topPos;

        graphics.blit(TEXTURE, x, y, 0, 0, background.getWidth(), background.getHeight(),
                background.getWidth(), background.getHeight());
        graphics.drawString(font, title,
                x + (background.getWidth() - 8) / 2 - font.width(title) / 2, y + 4,
                getTitleColor(), false);

        GuiGameElement.of(menu.contentHolder)
                .<GuiGameElement.GuiRenderBuilder>at(x + background.getWidth() + 8, y + background.getHeight() - 52, -200)
                .scale(4)
                .render(graphics);
    }

    @Override
    protected List<IconButton> getTooltipButtons() {
        return Arrays.asList(respectNBT, ignoreNBT, strict, bestEffort);
    }

    @Override
    protected List<MutableComponent> getTooltipDescriptions() {
        return Arrays.asList(
                respectDataDESC.plainCopy(),
                ignoreDataDESC.plainCopy(),
                strictDESC.copy(),
                bestEffortDESC.copy()
        );
    }

    @Override
    protected boolean isButtonEnabled(IconButton button) {
        if (button == respectNBT) return !menu.respectNBT;
        if (button == ignoreNBT) return menu.respectNBT;
        if (button == strict) return menu.executionMode != ExecutionMode.STRICT;
        if (button == bestEffort) return menu.executionMode != ExecutionMode.BEST_EFFORT;
        return true;
    }

    @Override
    protected int getTitleColor() {
        return 0x303030;
    }
}
