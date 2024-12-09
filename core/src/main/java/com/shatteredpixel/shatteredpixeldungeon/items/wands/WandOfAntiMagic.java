/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.utils.Callback;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.HashMap;

public class WandOfAntiMagic extends Wand {


	private float storedEnergy;
	{
		image = ItemSpriteSheet.WAND_ANTIMAGIC;
	}

	private int energyFrom(Char ch) {
		// Returns the amount of charge we get from zapping target ch.
		// Assumes they are a magic target and not antimagicked.
		int min = buffedLvl();
		int max = 3 * (1+buffedLvl());
		return Random.IntRange(min, max);
	}

	private float maxEnergy() {
		// The maximum possible charge
		return 10 * (1+buffedLvl());
	}

	private void absorbMagic(Char ch) {
		if (ch.buff( AntiMagic.class ) == null) {
			float energyGain = Math.min(energyFrom(ch), maxEnergy() - storedEnergy);
			storedEnergy += energyGain;
			Dungeon.hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString((int) energyGain), FloatingText.ENERGY);
			Sample.INSTANCE.play(Assets.Sounds.CHARGEUP, 1, 0.8f * Random.Float(0.87f, 1.15f));
		}
		Buff.prolong(ch, AntiMagic.class, 2 + buffedLvl());
	}

	@Override
	public void onZap(Ballistica bolt) {
		Char ch = Actor.findChar(bolt.collisionPos);
		if (ch != null){
			if (!(ch instanceof Mob)){
				return;
			}
			wandProc(ch, chargesPerCast());
			Mob enemy = (Mob) ch;
			if (ch.properties().contains(Char.Property.MAGIC)
				&& ((ch.buff( AntiMagic.class ) == null) || storedEnergy == 0)) {
				absorbMagic(enemy);
			} else {
				storedEnergy = Math.min(storedEnergy, maxEnergy());
				ch.damage((int) Math.ceil(storedEnergy), this);
				storedEnergy = 0;
				Sample.INSTANCE.play(Assets.Sounds.HIT_MAGIC, 1, 0.8f * Random.Float(0.87f, 1.15f));
			}
		} else {
			Dungeon.level.pressCell(bolt.collisionPos);
		}
	}
	
	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
		if (defender.properties().contains(Char.Property.MAGIC)
		    && Random.Int( buffedLvl() + 4 ) >= 3) {
			absorbMagic(defender);
		}
	}

	@Override
	public void fx(Ballistica beam, Callback callback) {
		int cell = beam.path.get(beam.dist);
		curUser.sprite.parent.add(new Beam.AntiMagicRay(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld( cell )));
		callback.call();
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		if (Random.Int(4) == 0) {
			particle.color(0xFFFFFF);
			particle.am = 0.6f;
			particle.setLifespan(0.6f);
			particle.acc.set(0, +10);
			particle.speed.polar(-Random.Float(3.1415926f), 6f);
			particle.setSize(0f, 1.5f);
			particle.sizeJitter = 1f;
			particle.shuffleXY(1f);
			float dst = Random.Float(1f);
			particle.x -= dst;
			particle.y += dst;
		} else {
			particle.am = 0.3f; // alpha
			particle.acc.set(0, +6);
			particle.setLifespan(5f);
			particle.setSize(1f, 2f);
			particle.speed.polar(Random.Float(PointF.PI2) * 0.75f, 0.3f);
			if (Random.Int(2) == 0) {
				particle.color(ColorMath.random(0xEE0000, 0x880000));
			} else {
				particle.color(ColorMath.random(0x000000, 0x000000));
			}
			particle.radiateXY(3.0f);
			particle.x -= 2;
			particle.y += 2;
		}
	}
}
