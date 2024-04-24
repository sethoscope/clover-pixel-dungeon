package com.shatteredpixel.shatteredpixeldungeon.items.stones;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.effects.Enchanting;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class StoneOfDoubleEnchantment extends InventoryStone {
    {
        preferredBag = Belongings.Backpack.class;
        image = ItemSpriteSheet.STONE_DOUBLEENCHANT;
        unique = true;
    }

    @Override
    protected boolean usableOnItem(Item item) {
        return (item instanceof MeleeWeapon || item instanceof SpiritBow);
    }

    @Override
    protected void onItemSelected(Item item) {
        curItem.detach( curUser.belongings.backpack );
        ((Weapon)item).enchant2();

        curUser.sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.1f, 5 );
        Enchanting.show( curUser, item );
        GLog.p(Messages.get(this, "enchant"));
        useAnimation();
    }

    @Override
    public int value() {
        return 60 * quantity;
    }

    @Override
    public int energyVal() {
        return 8 * quantity;
    }


    public static class StoneRecipe extends Recipe {
        @Override
        public boolean testIngredients(ArrayList<Item> ingredients) {
            if (ingredients.size() != 2) return false;
            return ingredients.get(0) instanceof StoneOfEnchantment
                    && ingredients.get(1) instanceof StoneOfEnchantment;
        }

        @Override
        public int cost(ArrayList<Item> ingredients) {
            return 8;
        }

        @Override
        public Item brew(ArrayList<Item> ingredients) {
            if (!testIngredients(ingredients)) return null;
            ingredients.get(0).quantity(ingredients.get(0).quantity() - 1);
            ingredients.get(1).quantity(ingredients.get(1).quantity() - 1);
            return new StoneOfDoubleEnchantment();
        }

        @Override
        public Item sampleOutput(ArrayList<Item> ingredients) {
            if (!testIngredients(ingredients)) return null;
            return new StoneOfDoubleEnchantment();
        }
    }
}