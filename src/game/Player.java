package game;

import behaviors.*;
import engine.Behavior;
import engine.Input;
import game.attacktypes.AT_Arrow;
import game.attacktypes.AT_Spell;
import game.attacktypes.AT_SwordSwing;
import game.spells.SpellNode;
import game.spells.SpellNode.SpellEffect.SE_FireDamage;
import game.spells.SpellNode.SpellEffect.SE_Teleport;
import game.spells.SpellNode.SpellTarget;
import game.spells.targets.ST_Area;
import game.spells.targets.ST_NearbyCreature;
import game.spells.targets.ST_Projectile;
import game.spells.targets.ST_Repeat;
import game.spells.targets.ST_Revert;
import game.spells.targets.ST_Targeter;
import graphics.Animation;
import graphics.Camera;
import org.joml.Vector2d;
import static org.lwjgl.glfw.GLFW.*;

public class Player extends Behavior {

    static {
        track(Player.class);
    }

    public final PositionBehavior position = require(PositionBehavior.class);
    public final VelocityBehavior velocity = require(VelocityBehavior.class);
    public final PhysicsBehavior physics = require(PhysicsBehavior.class);
    public final FourDirAnimation fourDirAnimation = require(FourDirAnimation.class);
    public final AttackerBehavior attacker = require(AttackerBehavior.class);
    public final SpaceOccupierBehavior spaceOccupier = require(SpaceOccupierBehavior.class);

    @Override
    public void createInner() {
        physics.collider.collisionShape = new ColliderBehavior.Rectangle(position, new Vector2d(16, 24));
        fourDirAnimation.animation.animation = new Animation("skeleton_anim");
        fourDirAnimation.directionSupplier = () -> Input.mouseWorld().sub(position.position);
        fourDirAnimation.playAnimSupplier = () -> velocity.velocity.length() > 10;
        attacker.target = Monster.class;
        attacker.setAttackType(new AT_SwordSwing());
    }

    @Override
    public void update(double dt) {
        attacker.creature.health.modify(100);
        attacker.creature.mana.modify(100);
        attacker.creature.stamina.modify(100);

        Vector2d goalVelocity = new Vector2d();
        if (Input.keyDown(GLFW_KEY_W)) {
            goalVelocity.y += 1;
        }
        if (Input.keyDown(GLFW_KEY_A)) {
            goalVelocity.x -= 1;
        }
        if (Input.keyDown(GLFW_KEY_S)) {
            goalVelocity.y -= 1;
        }
        if (Input.keyDown(GLFW_KEY_D)) {
            goalVelocity.x += 1;
        }
        if (goalVelocity.length() > 1) {
            goalVelocity.normalize();
        }
        goalVelocity.mul(attacker.creature.moveSpeed);
        if (Input.keyDown(GLFW_KEY_LEFT_SHIFT) && goalVelocity.length() > 0 && attacker.creature.stamina.pay(40 * dt)) {
            goalVelocity.mul(2);
        }
        double acceleration = 2000;
        velocity.velocity.lerp(goalVelocity, 1 - Math.exp(acceleration * -dt));

        attacker.targetPos = Input.mouseWorld();
        if (Input.mouseJustPressed(0)) {
            attacker.startAttack();
        }
        if (Input.mouseJustReleased(0)) {
            attacker.attackWhenReady();
        }
        if (Input.keyJustPressed(GLFW_KEY_1)) {
            attacker.setAttackType(new AT_SwordSwing());
        }
        if (Input.keyJustPressed(GLFW_KEY_2)) {
            attacker.setAttackType(new AT_Arrow());
        }
        if (Input.keyJustPressed(GLFW_KEY_3)) {
            //attacker.setAttackType(new AT_Firebolt());
            SpellNode spell = new ST_Projectile().onHit(new SE_FireDamage());
            attacker.setAttackType(new AT_Spell(spell));
        }
        if (Input.keyJustPressed(GLFW_KEY_4)) {
            SpellNode spell = new ST_Projectile().onHit(new ST_Repeat().onHit(new SE_FireDamage()));
            attacker.setAttackType(new AT_Spell(spell));
        }
        if (Input.keyJustPressed(GLFW_KEY_5)) {
            SpellNode spell = new ST_Projectile().onHit(new ST_Area().onHit(new SE_FireDamage(), new ST_Repeat().onHit(new SE_FireDamage())));
            attacker.setAttackType(new AT_Spell(spell));
        }
        if (Input.keyJustPressed(GLFW_KEY_6)) {
            SpellTarget hitNearby = new ST_NearbyCreature().onHit(new SE_FireDamage());
            hitNearby.onHit(hitNearby, hitNearby);
            attacker.setAttackType(new AT_Spell(new ST_Projectile().onHit(new SE_FireDamage(), hitNearby)));
        }
        if (Input.keyJustPressed(GLFW_KEY_7)) {
            SpellNode spell = new ST_Repeat().onHit(new ST_NearbyCreature().onHit(new SE_FireDamage()));
            attacker.setAttackType(new AT_Spell(spell));
        }
        if (Input.keyJustPressed(GLFW_KEY_8)) {
            SpellTarget hitNearby = new ST_NearbyCreature().onHit(new SE_FireDamage());
            hitNearby.onNot(hitNearby);
            attacker.setAttackType(new AT_Spell(hitNearby));
        }
        if (Input.keyJustPressed(GLFW_KEY_9)) {
            SpellNode spell = new ST_Projectile().onHit(new ST_Area().onHit(new SE_Teleport()));
            //SpellNode spell = new ST_Projectile().onHit(new ST_Area().onHit(new ST_Revert().onHit(new SE_Teleport())));
            attacker.setAttackType(new AT_Spell(spell));
        }
        if (Input.keyJustPressed(GLFW_KEY_0)) {
            SpellNode spell = new ST_Projectile().onHit(new ST_Revert().onHit(new SE_Teleport()), new ST_Targeter().onHit(new SE_Teleport()));
            attacker.setAttackType(new AT_Spell(spell));
        }

        Camera.camera.position.lerp(position.position, 1 - Math.exp(5 * -dt));
    }
}
