package com.ludvig.combatplus;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;

public final class Attributes {

    private Attributes() {
    }

    public static Attribute get(String key) {
        Attribute attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(key));
        if (attribute == null) {
            attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic_" + key));
        }
        if (attribute == null) {
            attribute = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic." + key));
        }
        return attribute;
    }
}
