/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2023 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class ScrollOfForgetting extends Scroll {

	{
		icon = ItemSpriteSheet.Icons.SCROLL_IDENTIFY;

		bones = true;
	}

	private ArrayList<Class<? extends Item>> forgettableClasses() {
		ArrayList<Class<? extends Item>> known = new ArrayList<>();
		known.addAll(Ring.getKnown());
		known.addAll(Scroll.getKnown());
		known.addAll(Potion.getKnown());
		return known;
	}
	protected void forgetSomething( Hero hero ) {
		ArrayList<Class<? extends Item>> classes = forgettableClasses();
		if ( classes.size() == 0 ) {
			return; // nothing left to forget!
		}
		Class<? extends Item> cls = Random.element(classes);
		Item it = (Item) Reflection.newInstance(cls);
		it.setUnknown();
		GLog.i(Messages.get(this, "msg", it.name()));
	}

	@Override
	public void doRead() {
		forgetSomething( curUser );
	}

	@Override
	public int value() {
		return isKnown() ? 30 * quantity : super.value();
	}
}
