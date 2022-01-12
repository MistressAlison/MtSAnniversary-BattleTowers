package BattleTowers.events;

import BattleTowers.events.phases.TextPhase;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.vfx.cardManip.PurgeCardEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static BattleTowers.BattleTowers.makeID;

public class NewBonfireEvent extends PhasedEvent {
    public static final String ID = makeID("NewBonfireEvent");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;
    private static final String title = eventStrings.NAME;

    private AbstractRelic relicChoice;

    public NewBonfireEvent() {
        super(title, "images/events/bonfire.jpg");

        ArrayList<AbstractRelic> relics = new ArrayList<>();
        relics.addAll(AbstractDungeon.player.relics);
        Collections.shuffle(relics, new Random(AbstractDungeon.miscRng.randomLong()));
        this.relicChoice = relics.get(0);

        //set up event
        registerPhase(0, new TextPhase(DESCRIPTIONS[0] + DESCRIPTIONS[1] + DESCRIPTIONS[2]).addOption(OPTIONS[0] + relicChoice.name + OPTIONS[1], (i)->transitionKey("Relic")).addOption(OPTIONS[2], (i)->transitionKey("Card")));
        registerPhase("Brazil", new TextPhase(DESCRIPTIONS[1]).addOption(OPTIONS[2], (i)->transitionKey("Antarctica")).addOption(OPTIONS[4], (i)->transitionKey(1)));
        registerPhase("Japan", new TextPhase(DESCRIPTIONS[2]).addOption(OPTIONS[0], (i)->transitionKey("Brazil")).addOption(OPTIONS[3], (i)->transitionKey("You can also use numbers")));
        registerPhase("Antarctica", new TextPhase(DESCRIPTIONS[3]).addOption(OPTIONS[3], (i)->transitionKey("You can also use numbers")));
        registerPhase("Curse", new TextPhase(DESCRIPTIONS[6]).addOption(OPTIONS[4], (t)->this.openMap()));
        registerPhase("Basic", new TextPhase(DESCRIPTIONS[6]).addOption(OPTIONS[4], (t)->this.openMap()));
        registerPhase("Common", new TextPhase(DESCRIPTIONS[6]).addOption(OPTIONS[4], (t)->this.openMap()));
        registerPhase("Special", new TextPhase(DESCRIPTIONS[6]).addOption(OPTIONS[4], (t)->this.openMap()));
        registerPhase("Uncommon", new TextPhase(DESCRIPTIONS[6]).addOption(OPTIONS[4], (t)->this.openMap()));
        registerPhase("Rare", new TextPhase(DESCRIPTIONS[6]).addOption(OPTIONS[4], (t)->this.openMap()));

        registerPhase("Relic", new TextPhase(DESCRIPTIONS[7]).addOption(OPTIONS[5] + relicChoice.name, (i)->{
            AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE;
            AbstractDungeon.player.loseRelic(relicChoice.relicId);
            AbstractDungeon.getCurrRoom().baseRareCardChance = 1000; //Always give rares
            AbstractDungeon.combatRewardScreen.open(DESCRIPTIONS[8]);
            transitionKey("Curse");
        }));

        registerPhase("Card", new TextPhase(DESCRIPTIONS[3])
        {
            @Override
            public void update() {
                if (!AbstractDungeon.isScreenUp && !AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
                    AbstractCard c = AbstractDungeon.gridSelectScreen.selectedCards.remove(0);

                    switch(c.rarity) {
                        case CURSE:
                            AbstractDungeon.player.damage(new DamageInfo(null, 10));
                            transitionKey("Curse");
                            break;
                        case BASIC:
                            AbstractDungeon.player.damage(new DamageInfo(null, 10));
                            transitionKey("Basic");
                            break;
                        case SPECIAL:
                            transitionKey("Special");
                            break;
                        case UNCOMMON:
                            transitionKey("Uncommon");
                            break;
                        case RARE:
                            transitionKey("Rare");
                            break;
                        default:
                            transitionKey("Common");
                            break;
                    }
                    AbstractDungeon.gridSelectScreen.selectedCards.clear();
                    AbstractDungeon.topLevelEffects.add(new PurgeCardEffect(c, (Settings.WIDTH / 2), (Settings.HEIGHT / 2)));
                    AbstractDungeon.player.masterDeck.removeCard(c);
                }
            }
        }.addOption(OPTIONS[3], (i)->{
            AbstractDungeon.getCurrRoom().phase = AbstractRoom.RoomPhase.COMPLETE; //If it's not over don't do this
            AbstractDungeon.gridSelectScreen.open(AbstractDungeon.player.masterDeck.getPurgeableCards(), 1, OPTIONS[2], false, false, false, true);
        }));

        transitionKey(0); //starting point
    }
}
