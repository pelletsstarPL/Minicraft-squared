
package minicraft.screen;

		import minicraft.item.Item;
		import minicraft.entity.mob.Player;
		import minicraft.item.StackableItem;
		import minicraft.core.Game;
		import minicraft.core.io.InputHandler;
		import minicraft.screen.entry.ItemEntry;
		import minicraft.entity.Entity;
		import minicraft.item.Inventory;

class InventoryMenu extends ItemListMenu
{
	private final Inventory inv;
	private final Entity holder;

	InventoryMenu(final Entity holder, final Inventory inv, final String title) {
		super(ItemListMenu.getBuilder(), ItemEntry.useItems(inv.getItems()), title);
		this.inv = inv;
		this.holder = holder;
	}

	InventoryMenu(final InventoryMenu model) {
		super(ItemListMenu.getBuilder(), ItemEntry.useItems(model.inv.getItems()), model.getTitle());
		this.inv = model.inv;
		this.holder = model.holder;
		this.setSelection(model.getSelection());
	}

	public void tick(final InputHandler input) {
		super.tick(input);
		final boolean dropOne = input.getKey("drop-one").clicked && !(Game.getMenu() instanceof ContainerDisplay);
		if (this.getNumOptions() > 0 && (dropOne || input.getKey("drop-stack").clicked)) {
			final ItemEntry entry = (ItemEntry)this.getCurEntry();
			if (entry == null) {
				return;
			}
			final Item invItem = entry.getItem();
			final Item drop = invItem.clone();
			if (dropOne && drop instanceof StackableItem && ((StackableItem)drop).count > 1) {
				((StackableItem)drop).count = 1;
				final StackableItem stackableItem = (StackableItem)invItem;
				--stackableItem.count;
			}
			else if (!Game.isMode("creative") || !(this.holder instanceof Player)) {
				this.removeSelectedEntry();
			}
			if (this.holder.getLevel() != null) {
				if (Game.isValidClient()) {
					Game.client.dropItem(drop);
				}
				else {
					this.holder.getLevel().dropItem(this.holder.x, this.holder.y, drop);
				}
			}
		}
	}

	public void removeSelectedEntry() {
		this.inv.remove(this.getSelection());
		super.removeSelectedEntry();
	}
};
