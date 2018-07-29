package game.spells.targets;

import engine.Behavior;
import static game.GraphicsEffect.createGraphicsEffect;
import game.spells.SpellInstance;
import game.spells.SpellNode.SpellTarget;
import graphics.Graphics;
import org.joml.Vector4d;

public class ST_Repeat extends SpellTarget {

    @Override
    public void cast(SpellInstance si) {
        int repeats = (int) Math.min(si.mana / minCost(), 40);
        double manaPer = si.mana / repeats;
        ST_RepeatBehavior rb = new ST_RepeatBehavior();
        rb.repeats = repeats;
        rb.onHit = () -> hit(manaPer, si);
        rb.create();
        createGraphicsEffect(repeats * .25, () -> Graphics.drawCircle(si.position.get(), 20, new Vector4d(1, .2, 0, .2)));
    }

    @Override
    public double personalCost() {
        return .5;
    }

    public static class ST_RepeatBehavior extends Behavior {

        public double timeElapsed;
        public int repeats;
        public Runnable onHit;

        @Override
        public void update(double dt) {
            timeElapsed += dt;
            if (timeElapsed >= .25) {
                timeElapsed -= .25;
                repeats -= 1;
                onHit.run();
                if (repeats == 0) {
                    destroy();
                }
            }
        }
    }
}