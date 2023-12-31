package com.search.pokejava;

import com.search.pokejava.types.DamageType;

public abstract class Effect {

    public enum PokeStat {
        SPECIAL_ATTACK,
        ATTACK,
        DEFENSE,
        SPECIAL_DEFENSE,
        SPEED
    }

    // Multiplier = max(2, 2 + s)/max(2, 2 - s)

    private static void changeBuffStage(Battle battle, Battle.PokeStatus buffTarget, int deltaStage, PokeStat stat) {
        switch (stat) {
            case PokeStat.SPECIAL_ATTACK:
                buffTarget.spAttackStage += deltaStage;
                if (buffTarget.spAttackStage < -6) {
                    buffTarget.spAttackStage = -6;
                } else if (buffTarget.spAttackStage > 6) {
                    buffTarget.spAttackStage = 6;
                }
                if (deltaStage < 0f) {
                    battle.logs.add("O ataque especial de " + buffTarget.name + " diminui em " + deltaStage + " estágios!");
                } else {
                    battle.logs.add("O ataque especial de " + buffTarget.name + " aumenta em " + deltaStage + " estágios!");
                }
                break;
            case PokeStat.ATTACK:
                buffTarget.attackStage += deltaStage;
                if (buffTarget.attackStage < -6) {
                    buffTarget.attackStage = -6;
                } else if (buffTarget.attackStage > 6) {
                    buffTarget.attackStage = 6;
                }
                if (deltaStage < 0f) {
                    battle.logs.add("O ataque de " + buffTarget.name + " diminui em " + deltaStage + " estágios!");
                } else {
                    battle.logs.add("O ataque de " + buffTarget.name + " aumenta em " + deltaStage + " estágios!");
                }
                break;
            case PokeStat.DEFENSE:
                buffTarget.defenseStage += deltaStage;
                if (buffTarget.defenseStage < -6) {
                    buffTarget.defenseStage = -6;
                } else if (buffTarget.defenseStage > 6) {
                    buffTarget.defenseStage = 6;
                }
                if (deltaStage < 0f) {
                    battle.logs.add("A defesa de " + buffTarget.name + " diminui em " + deltaStage + " estágios!");
                } else {
                    battle.logs.add("A defesa de " + buffTarget.name + " aumenta em " + deltaStage + " estágios!");
                }
                break;
            case PokeStat.SPECIAL_DEFENSE:
                buffTarget.spDefenseStage += deltaStage;
                if (buffTarget.spDefenseStage < -6) {
                    buffTarget.spDefenseStage = -6;
                } else if (buffTarget.spDefenseStage > 6) {
                    buffTarget.spDefenseStage = 6;
                }
                if (deltaStage < 0f) {
                    battle.logs.add("A defesa especial de " + buffTarget.name + " diminui em " + deltaStage + " estágios!");
                } else {
                    battle.logs.add("A defesa especial de " + buffTarget.name + " aumenta em " + deltaStage + " estágios!");
                }
                break;
            case PokeStat.SPEED:
                buffTarget.speedStage += deltaStage;
                if (buffTarget.speedStage < -6) {
                    buffTarget.speedStage = -6;
                } else if (buffTarget.speedStage > 6) {
                    buffTarget.speedStage = 6;
                }
                if (deltaStage < 0f) {
                    battle.logs.add("A velocidade de " + buffTarget.name + " diminui em " + deltaStage + " estágios!");
                } else {
                    battle.logs.add("A velocidade de " + buffTarget.name + " aumenta em " + deltaStage + " estágios!");
                }
                break;
        }
    }

    private static void regularDamage(Battle battle, Battle.PokeStatus attacker, Battle.PokeStatus target, float moveMod, Move move) {
        float attack, defense;
        if (move.damageType == DamageType.PHYSICAL) {
            attack = attacker.attack*Math.max(2f, 2f + ((float) attacker.attackStage))/Math.max(2f, 2f - ((float) attacker.attackStage));
            defense = target.defense*Math.max(2f, 2f + ((float) target.defenseStage))/Math.max(2f, 2f - ((float) target.defenseStage));
        } else {
            attack = attacker.specialAttack*Math.max(2f, 2f + ((float) attacker.spAttackStage))/Math.max(2f, 2f - ((float) attacker.spAttackStage));
            defense = target.specialDefense*Math.max(2f, 2f + ((float) target.spDefenseStage))/Math.max(2f, 2f - ((float) target.spDefenseStage));
        }
        float stab = 1.0f;
        if (move.pokeType == attacker.pokeType || move.pokeType == attacker.secondPokeType) {
            stab = 1.5f;
        }
        float typeEffectiveness = Battle.getTypeEffectiveness(move.pokeType, target.pokeType);
        float typeEffectiveness2 = Battle.getTypeEffectiveness(move.pokeType, target.secondPokeType);
        float totalEffectiveness = typeEffectiveness * typeEffectiveness2;
        float damage = (((2f+(2f/5f)) * move.power * (attack / defense)) / 50f + 2) * stab * totalEffectiveness * 1.125f * moveMod;

        if (totalEffectiveness > 1f) {
            battle.logs.add("É super efetivo! x" + totalEffectiveness);
        } else if (totalEffectiveness < 1f) {
            battle.logs.add("É pouco efetivo! x" + totalEffectiveness);
        }

        if (damage > 0f && damage < 1f) {
            damage = 1f;
        }

        battle.logs.add(target.name + " recebe " + damage + " de dano!");
        target.health -= damage;
        if (target.health <= 0f) {
            target.health = 0f;
            target.fainted = true;
        }
    }

    public Effect() {

    }

    public static Effect identifyEffect(String moveName) {
        moveName = moveName.toLowerCase();
        if (moveName.equals("recover")) {
            return new Effect() {
                @Override
                void apply(Battle battle, Battle.PokeStatus attacker, Battle.PokeStatus target, Move move) {
                    if (attacker.name.equalsIgnoreCase(battle.statusA.name)) {
                        attacker.health += battle.pokemonA.health * 0.5f;
                        if (attacker.health > battle.pokemonA.health) {
                            attacker.health = battle.pokemonA.health;
                        }
                    } else {
                        attacker.health += battle.pokemonB.health * 0.5f;
                        if (attacker.health > battle.pokemonB.health) {
                            attacker.health = battle.pokemonB.health;
                        }
                    }
                }
            };
        } else if (moveName.equals("synthesis")) {
            return new Effect() {
                @Override
                void apply(Battle battle, Battle.PokeStatus attacker, Battle.PokeStatus target, Move move) {
                    if (attacker.name.equalsIgnoreCase(battle.statusA.name)) {
                        attacker.health += battle.pokemonA.health * 0.33f;
                        if (attacker.health > battle.pokemonA.health) {
                            attacker.health = battle.pokemonA.health;
                        }
                    } else {
                        attacker.health += battle.pokemonB.health * 0.33f;
                        if (attacker.health > battle.pokemonB.health) {
                            attacker.health = battle.pokemonB.health;
                        }
                    }
                }
            };
        } else if (moveName.equals("growth")) {
            return new Effect() {
                @Override
                void apply(Battle battle, Battle.PokeStatus attacker, Battle.PokeStatus target, Move move) {
                    Effect.changeBuffStage(battle, attacker, 1, PokeStat.SPECIAL_ATTACK);
                }
            };
        } else if (moveName.equals("swords-dance")) {
            return new Effect() {
                @Override
                void apply(Battle battle, Battle.PokeStatus attacker, Battle.PokeStatus target, Move move) {
                    Effect.changeBuffStage(battle, attacker, 2, PokeStat.ATTACK);
                }
            };
        } else if (moveName.equals("harden")) {
            return new Effect() {
                @Override
                void apply(Battle battle, Battle.PokeStatus attacker, Battle.PokeStatus target, Move move) {
                    Effect.changeBuffStage(battle, attacker, 1, PokeStat.DEFENSE);
                }
            };
        } else if (moveName.equals("sharpen")) {
            return new Effect() {
                @Override
                void apply(Battle battle, Battle.PokeStatus attacker, Battle.PokeStatus target, Move move) {
                    Effect.changeBuffStage(battle, attacker, 1, PokeStat.ATTACK);
                }
            };
        } else if (moveName.equals("agility")) {
            return new Effect() {
                @Override
                void apply(Battle battle, Battle.PokeStatus attacker, Battle.PokeStatus target, Move move) {
                    Effect.changeBuffStage(battle, attacker, 2, PokeStat.SPEED);
                }
            };
        } else if (moveName.equals("calm-mind")) {
            return new Effect() {
                @Override
                void apply(Battle battle, Battle.PokeStatus attacker, Battle.PokeStatus target, Move move) {
                    Effect.changeBuffStage(battle, attacker, 2, PokeStat.SPECIAL_ATTACK);
                }
            };
        } else if (moveName.equals("hammer-arm")) {
            return new Effect() {
                @Override
                void apply(Battle battle, Battle.PokeStatus attacker, Battle.PokeStatus target, Move move) {
                    Effect.changeBuffStage(battle, attacker, -1, PokeStat.SPEED);
                    Effect.regularDamage(battle, attacker, target, 1, move);
                }
            };
        } else if (moveName.equals("bulk-up")) {
            return new Effect() {
                @Override
                void apply(Battle battle, Battle.PokeStatus attacker, Battle.PokeStatus target, Move move) {
                    Effect.changeBuffStage(battle, attacker, 1, PokeStat.ATTACK);
                    Effect.changeBuffStage(battle, attacker, 1, PokeStat.DEFENSE);
                }
            };
        }
        else if (moveName.equals("growl")) {
            return new Effect() {
                @Override
                void apply(Battle battle, Battle.PokeStatus attacker, Battle.PokeStatus target, Move move) {
                    target.attackStage -= 1;
                    if (target.attackStage < -6) {
                        target.attackStage = -6;
                    }
                }
            };
        }
        return new Effect() {
            @Override
            void apply(Battle battle, Battle.PokeStatus attacker, Battle.PokeStatus target, Move move) {
                Effect.regularDamage(battle, attacker, target, 1f, move);
            }
        };
    }
    abstract void apply(Battle battle, Battle.PokeStatus attacker, Battle.PokeStatus target, Move move);

}
