package me.matsubara.realisticvillagers.hologram;

public enum MenuAction {
    // Main menu
    TALK, TRADE, INTERACTIONS,
    // Talk sub-menu
    CHAT, GREET, STORY, JOKE, INSULT, FLIRT, PROUD_OF,
    // Interactions sub-menu
    ORDER, FOLLOW_ME, STAY_HERE, GIFT, INSPECT_INVENTORY, SET_HOME, COMBAT,
    DIVORCE, DIVORCE_CONFIRM, CANCEL_DIVORCE,
    PROCREATE, INFORMATIONS,
    // Info panel navigation (left side)
    INFO_PREV, INFO_NEXT, CHILDREN_PREV, CHILDREN_NEXT,
    // Navigation
    BACK
}
