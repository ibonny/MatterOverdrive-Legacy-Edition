/*
 * This file is part of Matter Overdrive
 * Copyright (C) 2018, Horizon Studio <contact@hrznstudio.com>, All rights reserved.
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
package matteroverdrive.data.dialog;

import com.google.gson.JsonObject;
import matteroverdrive.api.dialog.IDialogMessage;
import matteroverdrive.api.dialog.IDialogNpc;
import matteroverdrive.gui.GuiDialog;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Simeon on 8/10/2015.
 */
public class DialogMessageBack extends DialogMessage {
    public DialogMessageBack(JsonObject object) {
        super(object);
    }

    public DialogMessageBack() {
    }

    public DialogMessageBack(String message) {
        super(message);
    }

    public DialogMessageBack(String message, String question) {
        super(message, question);
    }

    public DialogMessageBack(String[] messages, String[] questions) {
        super(messages, questions);
    }

    @SideOnly(Side.CLIENT)
    protected void setAsGuiActiveMessage(IDialogNpc npc, EntityPlayer player) {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiDialog) {
            IDialogMessage message = ((GuiDialog) Minecraft.getMinecraft().currentScreen).getCurrentMessage();
            if (message != null && message.getParent(npc, player) != null) {
                ((GuiDialog) Minecraft.getMinecraft().currentScreen).setCurrentMessage(message.getParent(npc, player));
            }
        }
    }
}
