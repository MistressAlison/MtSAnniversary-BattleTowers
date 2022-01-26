package BattleTowers.monsters;

import BattleTowers.BattleTowers;
import BattleTowers.util.TextureLoader;
import BattleTowers.vfx.LouseMonsterParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.RollMoveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.MonsterStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.StrengthPower;
import com.megacrit.cardcrawl.powers.WeakPower;

public class LouseHorde extends AbstractMonster {

    public static String ID = BattleTowers.makeID("LouseHorde");

    private static final MonsterStrings STRINGS = CardCrawlGame.languagePack.getMonsterStrings(ID);
    private static final String NAME = STRINGS.NAME;
    private static final int BASE_DAMAGE = 20;
    private static final int BASE_DEFEND = 15;
    private static final int BASE_POWER = 2;
    private static final int BASE_BUFF = 5;

    private boolean curled = false;

    private final LouseMonsterParticleEmitter particleEmitter;
    private int defend = BASE_DEFEND;
    private int atkDamage = BASE_DAMAGE;
    private int powerAmount = BASE_POWER;
    private int buffAmount = BASE_BUFF;

    public LouseHorde() {
        super(NAME, ID, 80, 0f, 0f, 200f, 200f, null, 0f, 0f);

        img = TextureLoader.getTexture("battleTowersResources/img/ui/emptyTexture.png");
        particleEmitter = new LouseMonsterParticleEmitter(5, new Vector2(hb.cX, hb.cY));

        this.setHp(78, 82);

        if (AbstractDungeon.ascensionLevel >= 2) {
            atkDamage += 2;
        }

        if (AbstractDungeon.ascensionLevel >= 7) {
            defend += 5;
        }

        if (AbstractDungeon.ascensionLevel >= 17) {
            powerAmount += 1;
        }

        this.damage.add(new DamageInfo(this, atkDamage));
    }

    @Override
    public void update() {
        super.update();

        particleEmitter.update();
    }

    @Override
    public void render(SpriteBatch sb) {
        particleEmitter.render(sb);

        super.render(sb);
    }

    @Override
    public void takeTurn() {
        String atkType = curled ? "attack_curled" : "attack";

        switch (nextMove) {
            case 0:
                particleEmitter.setAnimation("curlup");
                this.curled = true;

                addToBot(new GainBlockAction(this, defend));
                addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, powerAmount), powerAmount));
                break;
            case 1:
                particleEmitter.setAnimation(atkType);
                particleEmitter.throwLouse();

                addToBot(new DamageAction(AbstractDungeon.player, damage.get(0), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new WeakPower(AbstractDungeon.player, powerAmount, true), powerAmount));
                break;
            case 2:
                particleEmitter.setAnimation(atkType);

                addToBot(new ApplyPowerAction(this, this, new StrengthPower(this, buffAmount), buffAmount));
                addToBot(new ApplyPowerAction(AbstractDungeon.player, this, new WeakPower(AbstractDungeon.player, powerAmount, true), powerAmount));
                break;
        }

        addToBot(new RollMoveAction(this));
    }

    @Override
    public void applyStartOfTurnPowers() {
        super.applyStartOfTurnPowers();
        if (curled) {
            particleEmitter.setAnimation("open");
            this.curled = false;
        }
    }

    @Override
    public void damage(DamageInfo info) {
        super.damage(info);
        particleEmitter.takeDamage();
    }

    @Override
    protected void getMove(int i) {
        if (!lastMove(MoveBytes.ATTACK)) {
            setMove(MoveBytes.ATTACK, Intent.ATTACK_DEBUFF, this.damage.get(0).base);
        } else {
            if (i > 50) {
                setMove(MoveBytes.DEFEND, Intent.DEFEND_BUFF);
            } else {
                setMove(MoveBytes.DEBUFF, Intent.MAGIC);
            }
        }
    }

    private static class MoveBytes {
        public static byte DEFEND = (byte)0;
        public static byte ATTACK = (byte)1;
        public static byte DEBUFF = (byte)2;
    }
}
