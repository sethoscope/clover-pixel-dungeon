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

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;


import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class FlyingCarpet extends Artifact {

/**
level  charges   speed   range upgrade cost
   0      3       2.0      6      4
   2      4       2.4     10      5
   4      5       2.8     14      6
   6      6       3.2     19      7
   8      7       3.6     25      8
  10      8       4.0     32      
 **/
	{
		image = ItemSpriteSheet.ARTIFACT_CARPET;

		levelCap = 10;

		partialCharge = 0;
		chargeCap = chargeCap_();
		charge = chargeCap;

		defaultAction = AC_FLY;  // really it just toggles
	}

	public static final String AC_FLY = "FLY";
	public static final String AC_STOP = "STOP";

	int chargeCap_() {
		return level()/2 + 3;
	}

	public float speedFactor() {
		return 2.0f + level()/5.0f;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped( hero )
				&& !cursed
				&& hero.buff(MagicImmune.class) == null) {
			if ( activeBuff != null ) {
				actions.add(AC_STOP);
			} else if (charge > 0) {
				actions.add(AC_FLY);
			}
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute(hero, action);

		if (hero.buff(MagicImmune.class) != null) return;

		if ((action.equals(AC_FLY) || action.equals(AC_STOP)) && (activeBuff == null)) {
			if (!isEquipped(hero)) GLog.i(Messages.get(Artifact.class, "need_to_equip"));
			else if (cursed) GLog.i(Messages.get(this, "cursed"));
			else if (charge <= 0) GLog.i(Messages.get(this, "no_charge"));
			else {
				hero.spend(1f);
				hero.busy();
				Sample.INSTANCE.play(Assets.Sounds.MELD);
				activeBuff = activeBuff();
				activeBuff.attachTo(hero);
				Talent.onArtifactUsed(Dungeon.hero);
				hero.sprite.operate(hero.pos);
			}
		} else if ((action.equals(AC_STOP) || action.equals(AC_FLY)) && (activeBuff != null)) {
			activeBuff.detach();
			activeBuff = null;
			hero.sprite.operate( hero.pos );
		}
	}

	@Override
	public void activate(Char ch){
		super.activate(ch);
		if (activeBuff != null && activeBuff.target == null){
			activeBuff.attachTo(ch);
		}
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)){
			if (!collect){
				if (activeBuff != null){
					activeBuff.detach();
					activeBuff = null;
				}
			} else {
				activate(hero);
			}

			return true;
		} else
			return false;
	}

	@Override
	protected void onDetach() {
		if (passiveBuff != null){
			passiveBuff.detach();
			passiveBuff = null;
		}
		if (activeBuff != null && !isEquipped((Hero) activeBuff.target)){
			activeBuff.detach();
			activeBuff = null;
		}
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new carpetRecharge();
	}

	@Override
	protected ArtifactBuff activeBuff( ) {
          carpetFlying buff = new carpetFlying();
          buff.speedFactor(speedFactor());
          return buff;
          
	}
	
	@Override
	public void charge(Hero target, float amount) {
		if (cursed || target.buff(MagicImmune.class) != null) return;

		if (charge < chargeCap) {
			partialCharge += 0.25f*amount;
			if (partialCharge >= 1){
				partialCharge--;
				charge++;
				updateQuickslot();
			}
		}
	}

	@Override
	public void level(int value) {
		super.level(Math.min(value, levelCap));
                chargeCap = chargeCap_();
		if (activeBuff != null) ((carpetFlying) activeBuff).speedFactor(speedFactor());
	}

	@Override
	public Item upgrade() {
		if ( level() >= levelCap ) return this;
		super.upgrade();
		chargeCap = chargeCap_();
		if (activeBuff != null) ((carpetFlying) activeBuff).speedFactor(speedFactor());
		return this;
	}

	private static final String BUFF = "buff";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		if (activeBuff != null) bundle.put(BUFF, activeBuff);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(BUFF)){
			activeBuff = new carpetFlying();
			activeBuff.restoreFromBundle(bundle.getBundle(BUFF));
		}
	}

	public class carpetRecharge extends ArtifactBuff{
		@Override
		public boolean act() {
			if (cursed) {
				if ((Random.Int(8) == 0) && (target.buff(Levitation.class) != null)) {
					GLog.i( Messages.get(FlyingCarpet.class, "no_levitate") );
					Buff.detach(target, Levitation.class);
				}
				if ((Random.Int(6) == 0) && (target.buff(Haste.class) != null)) {
					GLog.i( Messages.get(FlyingCarpet.class, "no_haste") );
					Buff.detach(target, Haste.class);
				}
			} else if (charge < chargeCap && target.buff(MagicImmune.class) == null) {
				LockedFloor lock = target.buff(LockedFloor.class);
				if (activeBuff == null && (lock == null || lock.regenOn())) {
					float missing = (chargeCap - charge);
					if (level() > 7) missing += 5*(level() - 7)/3f;
					float turnsToCharge = (45 - missing);
					turnsToCharge /= RingOfEnergy.artifactChargeMultiplier(target);
					float chargeToGain = (1f / turnsToCharge);
					partialCharge += chargeToGain;
				}

				if (partialCharge >= 1) {
					charge++;
					partialCharge -= 1;
					if (charge == chargeCap){
						partialCharge = 0;
					}

				}
			} else {
				partialCharge = 0;
			}

			if (cooldown > 0)
				cooldown --;

			updateQuickslot();

			spend( TICK );

			return true;
		}

	}

	public class carpetFlying extends ArtifactBuff{
		
		{
			type = buffType.POSITIVE;
		}
		
		void carpetFlying(float speedFactor) {
			this.speedFactor_ = speedFactor;
		}

		int turnsToCost = 0;
		public float speedFactor_ = 2.0f;

		public float speedFactor() {
			return speedFactor_;
		}
          
		public float speedFactor(float speedFactor) {
			this.speedFactor_ = speedFactor;
			return speedFactor_;
		}
          
		@Override
		public int icon() {
			return BuffIndicator.LEVITATION;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.brightness(0.6f);
		}

		@Override
		public float iconFadePercent() {
			return (3f - turnsToCost) / 3f;
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(turnsToCost);
		}

		@Override
		public boolean attachTo( Char target ) {
			if (super.attachTo( target )) {
				target.flying = true;
				Roots.detach( target, Roots.class );
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean act(){
			turnsToCost--;
			
			if (turnsToCost <= 0){
				charge--;
				if (charge < 0) {
					charge = 0;
					detach();
					GLog.w(Messages.get(this, "no_charge"));
					((Hero) target).interrupt();
				} else {
					turnsToCost = 3;
				}
				updateQuickslot();
			}

			spend( TICK );

			return true;
		}

		public void dispel(){
			updateQuickslot();
			detach();
		}

		@Override
		public void fx(boolean on) {
			if (on) target.sprite.add( CharSprite.State.LEVITATING );
			else if (!target.flying) target.sprite.remove( CharSprite.State.LEVITATING );
		}

		@Override
		public void detach() {
			activeBuff = null;
			if (target.buff(Levitation.class) == null) target.flying = false;
			updateQuickslot();
			super.detach();
			if (!target.flying && ShatteredPixelDungeon.scene() instanceof GameScene) {
				Dungeon.level.occupyCell(target);
			}
		}
		
		private static final String TURNSTOCOST = "turnsToCost";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			
			bundle.put( TURNSTOCOST , turnsToCost);
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			
			turnsToCost = bundle.getInt( TURNSTOCOST );
		}
	}
	@Override
	public String desc() {
		String desc = super.desc();

			if ( isIdentified() ) {
			desc += "\n\n" + Messages.get(this, "stats", Messages.decimalFormat("#.#", speedFactor()));
                }

		if ( level() < levelCap ) {
			desc += "\n\n" + Messages.get(this, "desc_upgradable");
		}

		if ( isEquipped( Dungeon.hero ) ){
			if (cursed) {
				desc += "\n\n" +Messages.get(this, "desc_cursed");
			}
		}
		return desc;
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		protected int levelsPerBrew;

		{
			inputs =  new Class[]{
					FlyingCarpet.class, PotionOfLevitation.class, PotionOfHaste.class
			};
			inQuantity = new int[]{1, 1, 1};
			levelsPerBrew = 2;
		}

		private boolean canUpgrade(FlyingCarpet carpet) {
			return carpet.level() < carpet.levelCap;
		}

		public int cost(ArrayList<Item> ingredients){
			return 4 + findCarpet(ingredients).level()/2;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			FlyingCarpet newCarpet = null;
			Hero hero = Dungeon.hero;
			for (Item ingredient : ingredients) {
				if (ingredient.getClass() == FlyingCarpet.class) {
					final FlyingCarpet carpet = (FlyingCarpet) ingredient;
					if (!canUpgrade(carpet)) return null;
					newCarpet = (FlyingCarpet) carpet.duplicate();
					newCarpet.upgrade(levelsPerBrew);
					carpet.quantity(0);
				} else {
					ingredient.quantity(ingredient.quantity() - 1);
				}
			}
			return newCarpet;
		}

		public ArrayList<Item> getIngredients() {
			ArrayList<Item> result = new ArrayList<>();
			// Use their carpet in the guide if they have one. That way it will show
			// the upgrade effect and cost for the carpet they have.
			FlyingCarpet carpet = Dungeon.hero.belongings.getItem(FlyingCarpet.class);
			if (carpet == null)
				carpet = new FlyingCarpet();
			else
				carpet = (FlyingCarpet) carpet.duplicate();  // to make getAllSimilar() work, for QuickRecipe
			result.add(carpet);
			result.add(new PotionOfLevitation());
			result.add(new PotionOfHaste());
			return result;
		}

		public boolean testIngredients(ArrayList<Item> ingredients) {
			FlyingCarpet carpet = findCarpet(ingredients);
			if (carpet == null || !canUpgrade(carpet)) return false;
			Hero hero = Dungeon.hero;
			return super.testIngredients(ingredients);
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			FlyingCarpet carpet = findCarpet(ingredients);
			if (ingredients == null) {
				// This must be for the guide. Use their carpet if they have one.
				carpet = Dungeon.hero.belongings.getItem(FlyingCarpet.class);
			}
			if (carpet == null) carpet = new FlyingCarpet();
			if (!canUpgrade(carpet)) return null;
			return carpet.duplicate().upgrade(2);
		}

		public FlyingCarpet findCarpet(ArrayList<Item> ingredients) {
			if (ingredients == null) return null;
			for (Item ingredient : ingredients) {
				if (ingredient.getClass() == FlyingCarpet.class) {
					return (FlyingCarpet) ingredient;
				}
			}
			return null;
		}
	}

}
