package minicraft.item;

import minicraft.entity.furniture.Furniture;
import org.jetbrains.annotations.Nullable;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Inventory
{
	private final Random random;
	private final List<Item> items;

	public Inventory() {
		this.random = new Random();
		this.items = new ArrayList<Item>();
	}

	public List<Item> getItems() {
		return new ArrayList<Item>(this.items);
	}

	public void clearInv() {
		this.items.clear();
	}

	public int invSize() {
		return this.items.size();
	}

	public Item get(final int idx) {
		return this.items.get(idx);
	}

	public Item remove(final int idx) {
		return this.items.remove(idx);
	}

	public void addAll(final Inventory other) {
		for (final Item i : other.getItems()) {
			this.add(i.clone());
		}
	}

	public void add(@Nullable final Item item) {
		if (item != null) {
			this.add(this.items.size(), item);
		}
	}

	public void add(final Item item, final int num) {
		for (int i = 0; i < num; ++i) {
			this.add(item.clone());
		}
	}

	public void add(final int slot, final Item item) {
		if (item instanceof PowerGloveItem) {
			System.out.println("WARNING: tried to add power glove to inventory. stack trace:");
			Thread.dumpStack();
			return;
		}
		if (item instanceof StackableItem) {
			final StackableItem toTake = (StackableItem)item;
			boolean added = false;
			for (final Item value : this.items) {
				if (toTake.stacksWith(value)) {
					final StackableItem stackableItem = (StackableItem)value;
					stackableItem.count += toTake.count;
					added = true;
					break;
				}
			}
			if (!added) {
				this.items.add(slot, (Item)toTake);
			}
		}
		else {
			this.items.add(slot, item);
		}
	}

	private int removeFromStack(final StackableItem given, final int count) {
		int removed = 0;
		for (int i = 0; i < this.items.size(); ++i) {
			if (this.items.get(i) instanceof StackableItem) {
				final StackableItem curItem = (StackableItem)this.items.get(i);
				if (curItem.stacksWith((Item)given)) {
					final int amountRemoving = Math.min(count - removed, curItem.count);
					final StackableItem stackableItem = curItem;
					stackableItem.count -= amountRemoving;
					if (curItem.count == 0) {
						this.remove(i);
						--i;
					}
					removed += amountRemoving;
					if (removed == count) {
						break;
					}
					if (removed > count) {
						System.out.println("SCREW UP while removing items from stack: " + (removed - count) + " too many.");
						break;
					}
				}
			}
		}
		if (removed < count) {
			System.out.println("Inventory: could not remove all items; " + (count - removed) + " left.");
		}
		return removed;
	}

	public void removeItem(final Item i) {
		if (i instanceof StackableItem) {
			this.removeItems(i.clone(), ((StackableItem)i).count);
		}
		else {
			this.removeItems(i.clone(), 1);
		}
	}

	public void removeItems(final Item given, int count) {
		if (given instanceof StackableItem) {
			count -= this.removeFromStack((StackableItem)given, count);
		}
		else {
			for (int i = 0; i < this.items.size(); ++i) {
				final Item curItem = this.items.get(i);
				if (curItem.equals(given)) {
					this.remove(i);
					if (--count == 0) {
						break;
					}
				}
			}
		}
		if (count > 0) {
			System.out.println("WARNING: could not remove " + count + " " + given + ((count > 1) ? "s" : "") + " from inventory");
		}
	}

	public int count(final Item given) {
		if (given == null) {
			return 0;
		}
		int found = 0;
		for (final Item curItem : this.items) {
			if (curItem instanceof StackableItem && ((StackableItem)curItem).stacksWith(given)) {
				found += ((StackableItem)curItem).count;
			}
			else {
				if (!curItem.equals(given)) {
					continue;
				}
				++found;
			}
		}
		return found;
	}

	public String getItemData() {
		StringBuilder itemdata = new StringBuilder();
		for (final Item i : this.items) {
			itemdata.append(i.getData()).append(":");
		}
		if (itemdata.length() > 0) {
			itemdata = new StringBuilder(itemdata.substring(0, itemdata.length() - 1));
		}
		return itemdata.toString();
	}

	public void updateInv(final String items) {
		this.clearInv();
		if (items.length() == 0) {
			return;
		}
		for (final String item : items.split(":")) {
			this.add(Items.get(item));
		}
	}

	public void tryAdd(final int chance, final Item item, final int num, final boolean allOrNothing) {
		if (!allOrNothing || this.random.nextInt(chance) == 0) {
			for (int i = 0; i < num; ++i) {
				if (allOrNothing || this.random.nextInt(chance) == 0) {
					this.add(item.clone());
				}
			}
		}
	}

	public void tryAdd(final int chance, @Nullable final Item item, final int num) {
		if (item == null) {
			return;
		}
		if (item instanceof StackableItem) {
			final StackableItem stackableItem = (StackableItem)item;
			stackableItem.count *= num;
			this.tryAdd(chance, item, 1, true);
		}
		else {
			this.tryAdd(chance, item, num, false);
		}
	}

	public void tryAdd(final int chance, @Nullable final Item item) {
		this.tryAdd(chance, item, 1);
	}

	public void tryAdd(final int chance, final ToolType type, final int lvl) {
		this.tryAdd(chance, (Item)new ToolItem(type, lvl));
	}

	public void tryAdd(final int chance, final Furniture type) {
		this.tryAdd(chance, (Item)new FurnitureItem(type));
	}
}