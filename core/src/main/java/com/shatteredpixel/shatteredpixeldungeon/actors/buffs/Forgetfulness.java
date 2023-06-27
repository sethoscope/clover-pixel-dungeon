package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class Forgetfulness extends Buff {
    {
        revivePersists = true;
        announced = true;
    }

    public boolean act() {
        forgetSomething();
        // forget something every so often
        spend(Random.NormalFloat( 10.0F, 300.0F));
        // It might be fun to have it start as very low probability and increase a tiny amount
        // every turn based on health and hunger levels until something is forgotten and
        // the likelihood resets.
        return true;
    }

    private ArrayList<Class<? extends Item>> forgettableClasses() {
        ArrayList<Class<? extends Item>> known = new ArrayList<>();
        known.addAll(Ring.getKnown());
        known.addAll(Scroll.getKnown());
        known.addAll(Potion.getKnown());
        return known;
    }
    protected void forgetSomething() {
        ArrayList<Class<? extends Item>> classes = forgettableClasses();
        if ( classes.size() == 0 ) {
            return; // nothing left to forget!
        }
        Class<? extends Item> cls = Random.element(classes);
        Item it = (Item) Reflection.newInstance(cls);
        it.setUnknown();
        Item.updateQuickslot();
        GLog.i(Messages.get(this, "msg", it.name()));
    }

    @Override
    public int icon() {
        return BuffIndicator.ANTIMAGIC;
    }
}
