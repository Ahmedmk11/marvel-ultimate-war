package model.effects;

import model.world.Champion;

public class Dodge extends Effect {

    public Dodge(int duration) {
        super("Dodge", duration, EffectType.BUFF);

    }

    public void apply(Champion c) {
        c.setSpeed((int) (c.getSpeed() * 1.05));
        c.getAppliedEffects().add(this);
    }

    public void remove(Champion c) {
        c.setSpeed((int) (c.getSpeed() / 1.05));
        c.getAppliedEffects().remove(this);
    }
}