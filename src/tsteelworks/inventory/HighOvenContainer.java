package tsteelworks.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import tconstruct.inventory.ActiveContainer;
import tconstruct.inventory.ActiveSlot;
import tsteelworks.TSteelworks;
import tsteelworks.blocks.logic.HighOvenLogic;


public class HighOvenContainer extends ActiveContainer
{
	public HighOvenLogic	logic;
	public InventoryPlayer	playerInv;
	public int				fuel	= 0;

	public HighOvenContainer (InventoryPlayer inventoryplayer, HighOvenLogic highoven)
	{
		logic = highoven;
		playerInv = inventoryplayer;
		/* HighOven inventory */
		for (int y = 0; y < highoven.layers; y++)
			for (int x = 0; x < 1; x++)
				addDualSlotToContainer(new ActiveSlot(highoven, x + y, 64, 8 + (y * 18), y < 8));
		/* Player inventory */
		for (int column = 0; column < 3; column++)
			for (int row = 0; row < 9; row++)
				addSlotToContainer(new Slot(inventoryplayer, row + (column * 9) + 9, 90 + (row * 18), 84 + (column * 18)));
		for (int column = 0; column < 9; column++)
			addSlotToContainer(new Slot(inventoryplayer, column, 90 + (column * 18), 142));
	}

	@Override
	public void detectAndSendChanges ()
	{
		super.detectAndSendChanges();
	}

	@Override
	public void updateProgressBar (int id, int value)
	{
		if (id == 0) logic.fuelGague = value;
	}

	@Override
	public boolean canInteractWith (EntityPlayer entityplayer)
	{
		return logic.isUseableByPlayer(entityplayer);
	}

	@Override
	public ItemStack transferStackInSlot (EntityPlayer player, int slotID)
	{
		ItemStack stack = null;
		final Slot slot = (Slot) inventorySlots.get(slotID);
		if ((slot != null) && slot.getHasStack())
		{
			final ItemStack slotStack = slot.getStack();
			stack = slotStack.copy();
			TSteelworks.logger.info("Size: " + logic.getSizeInventory());
			if (slotID < logic.getSizeInventory())
			{
				if (!mergeItemStack(slotStack, logic.getSizeInventory(), inventorySlots.size(), true)) return null;
			}
			else if (!mergeItemStack(slotStack, 0, logic.getSizeInventory(), false)) return null;
			if (slotStack.stackSize == 0) slot.putStack((ItemStack) null);
			else slot.onSlotChanged();
		}
		return stack;
	}

	@Override
	protected boolean mergeItemStack (ItemStack inputStack, int startSlot, int endSlot, boolean flag)
	{
		boolean merged = false;
		int slotPos = startSlot;
		if (flag) slotPos = endSlot - 1;
		Slot slot;
		ItemStack slotStack;
		if (inputStack.isStackable()) while ((inputStack.stackSize > 0)
				&& ((!flag && (slotPos < endSlot)) || (flag && (slotPos >= startSlot))))
		{
			slot = (Slot) inventorySlots.get(slotPos);
			slotStack = slot.getStack();
			if ((slotStack != null) && (slotStack.itemID == inputStack.itemID)
					&& (!inputStack.getHasSubtypes() || (inputStack.getItemDamage() == slotStack.getItemDamage()))
					&& ItemStack.areItemStackTagsEqual(inputStack, slotStack))
			{
				final int totalSize = slotStack.stackSize + inputStack.stackSize;
				if (totalSize <= inputStack.getMaxStackSize())
				{
					inputStack.stackSize = 0;
					slotStack.stackSize = totalSize;
					slot.onSlotChanged();
					merged = true;
				}
				else if (slotStack.stackSize < inputStack.getMaxStackSize())
				{
					inputStack.stackSize -= inputStack.getMaxStackSize() - slotStack.stackSize;
					slotStack.stackSize = inputStack.getMaxStackSize();
					slot.onSlotChanged();
					merged = true;
				}
			}
			if (flag) --slotPos;
			else ++slotPos;
		}
		if (inputStack.stackSize > 0)
		{
			if (flag) slotPos = endSlot - 1;
			else slotPos = startSlot;
			while ((!flag && (slotPos < endSlot)) || (flag && (slotPos >= startSlot)))
			{
				slot = (Slot) inventorySlots.get(slotPos);
				slotStack = slot.getStack();
				if (slotStack == null)
				{
					slot.putStack(inputStack.copy());
					slot.onSlotChanged();
					inputStack.stackSize = 0;
					merged = true;
					break;
				}
				if (flag) --slotPos;
				else ++slotPos;
			}
		}
		return merged;
	}
}
