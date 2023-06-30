package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class Forgetful extends Buff {
    private boolean first = true;

    {
        revivePersists = true;
    }

    public boolean act() {
        if ( first ) {
            first = false;
        } else {
            forgetSomething();
        }
        // The deeper you go, the faster you forget.
        spend(Random.NormalFloat( 20.0F, 400.0F - 10* Statistics.deepestFloor));
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
}
