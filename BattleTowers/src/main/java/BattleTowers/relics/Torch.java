package BattleTowers.relics;

import BattleTowers.room.BattleTowerRoom;
import BattleTowers.util.UC;
import basemod.abstracts.CustomRelic;
import basemod.abstracts.CustomSavable;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInDrawPileAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardSave;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

import static BattleTowers.BattleTowers.logger;
import static BattleTowers.BattleTowers.makeID;

public class Torch extends CustomRelic implements CustomSavable<CardSave> {
    public static final String ID = makeID(Torch.class.getSimpleName());
    private static RelicStrings relicStrings = CardCrawlGame.languagePack.getRelicStrings(ID);

    public AbstractCard card = null;

    public Torch() {
        super(ID, UC.getTexture("relics", "Torch"), UC.getTexture("relics", "Torch_Outline"), RelicTier.SPECIAL, LandingSound.FLAT);
    }

    public Torch(AbstractCard card) {
        this();
        this.card = card;
        resetDescriptionAndTooltip();
    }

    @Override
    public void justEnteredRoom(AbstractRoom room) {
        if(AbstractDungeon.getCurrRoom() instanceof BattleTowerRoom) {
            setTextureOutline(UC.getTexture("relics", "Torch"), UC.getTexture("relics", "Torch_Outline"));
        } else {
            setTextureOutline(UC.getTexture("relics", "UnlitTorch"), UC.getTexture("relics", "UnlitTorch_Outline"));
        }
        resetDescriptionAndTooltip();
    }

    @Override
    public void atBattleStart() {
        if(card != null) {
            if (AbstractDungeon.getCurrRoom() instanceof BattleTowerRoom) {
                flash();
                UC.atb(new RelicAboveCreatureAction(UC.p(), this));
                UC.atb(new MakeTempCardInDrawPileAction(card.makeStatEquivalentCopy(), 2, true, true));
            }
        } else {
            logger.warn("Torch relic card is null.");
        }
    }

    @Override
    public String getUpdatedDescription() {
        if(!CardCrawlGame.isInARun() || card == null)  {
            this.flavorText = relicStrings.FLAVOR;
            return DESCRIPTIONS[2];
        }
        if(AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom() instanceof BattleTowerRoom) {
            grayscale = false;
            this.flavorText = relicStrings.FLAVOR;
        } else {
            grayscale = true;
            this.flavorText = DESCRIPTIONS[3];
        }
        return DESCRIPTIONS[0] + FontHelper.colorString(card.name, "y") + DESCRIPTIONS[1];
    }

    private void resetDescriptionAndTooltip() {
        description = getUpdatedDescription();
        tips.clear();
        tips.add(new PowerTip(name, description));
        initializeTips();
    }

    @Override
    public CardSave onSave() {
        if(card != null) {
            return new CardSave(card.cardID, card.timesUpgraded, card.misc);
        }
        return null;
    }

    @Override
    public void onLoad(CardSave cardSave) {
        if(cardSave != null) {
            AbstractCard savedCard = CardLibrary.getCard(cardSave.id);
            savedCard.timesUpgraded = cardSave.upgrades;
            savedCard.misc = cardSave.misc;

            this.card = savedCard;
        }
    }
}
