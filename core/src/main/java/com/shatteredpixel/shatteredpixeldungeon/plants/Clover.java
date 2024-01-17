/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2023 Evan Debenham
 *
 * Clover Pixel Dungeon
 * Copyright (C) 2022-2024 Seth Golub
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

package com.shatteredpixel.shatteredpixeldungeon.plants;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class Clover extends Plant {
	public static final float DURATION = 30f;
	{
		image = 15;
	}

	@Override
	public void activate(Char ch) {
		if (ch instanceof Hero) {
			if (((Hero) ch).subClass == HeroSubClass.WARDEN) {
				Buff.prolong(ch, Luck.class, DURATION * 2);
			} else {
				Buff.prolong(ch, Luck.class, DURATION);
			}
		}
	}

	// seed is never dropped, but we need it to create plants
	public static class Seed extends Plant.Seed {
		{
			plantClass = Clover.class;
		}
	}

	public static class CloverRing extends RingOfWealth {
		public void deactivate() {
			if (buff != null) {
				buff.detach();
				buff = null;
			}
		}
	}

	public static class Luck extends FlavourBuff {
		private Clover.CloverRing ring;
		private int level = 1;
		{
			type = buffType.POSITIVE;
		}

		public boolean attachTo(Char target) {
			if (super.attachTo(target)) {
				if (target instanceof Hero) {
					ring = new Clover.CloverRing();
					ring.upgrade(); // +1 Wealth
					if (((Hero) target).subClass == HeroSubClass.WARDEN) {
						level += 2;
						ring.upgrade(2); // +3 Wealth
					}
					ring.activate(target);
				}
				return true;
			}
			return false;
		}

		@Override
		public void detach() {
			super.detach();
			if (ring != null) {
				ring.deactivate();
				ring = null;
			}
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", level, dispTurns());
		}

		@Override
		public int icon() {
			return BuffIndicator.LUCK;
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - visualcooldown()) / DURATION);
		}
	}
}
