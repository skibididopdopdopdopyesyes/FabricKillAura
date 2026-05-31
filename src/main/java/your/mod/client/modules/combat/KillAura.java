package your.mod.client.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class KillAura {
    
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean enabled = true;
    private LivingEntity target = null;
    private double attackRange = 3.5;
    private float rotationSpeed = 30.0f;
    
    public boolean isEnabled() { return enabled; }
    public void toggle() { enabled = !enabled; if (!enabled) target = null; }
    
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;
        target = findBestTarget();
        if (target == null) return;
        rotateToEntity(target);
        if (canAttack(target)) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
    
    private LivingEntity findBestTarget() {
        LivingEntity closest = null;
        double closestDist = attackRange;
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;
            LivingEntity living = (LivingEntity) entity;
            if (living == mc.player) continue;
            if (!living.isAlive()) continue;
            double dist = mc.player.distanceTo(living);
            if (dist < closestDist) {
                closestDist = dist;
                closest = living;
            }
        }
        return closest;
    }
    
    private void rotateToEntity(LivingEntity entity) {
        float[] rotations = getRotationsToEntity(entity);
        float targetYaw = rotations[0];
        float targetPitch = rotations[1];
        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();
        float yawDelta = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDelta = MathHelper.wrapDegrees(targetPitch - currentPitch);
        mc.player.setYaw(currentYaw + MathHelper.clamp(yawDelta, -rotationSpeed, rotationSpeed));
        mc.player.setPitch(currentPitch + MathHelper.clamp(pitchDelta, -rotationSpeed, rotationSpeed));
    }
    
    private float[] getRotationsToEntity(LivingEntity entity) {
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d targetPos = entity.getBoundingBox().getCenter();
        double dx = targetPos.x - eyePos.x;
        double dy = targetPos.y - eyePos.y;
        double dz = targetPos.z - eyePos.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, dist)));
        pitch = MathHelper.clamp(pitch, -90.0f, 90.0f);
        return new float[]{yaw, pitch};
    }
    
    private boolean canAttack(LivingEntity entity) {
        if (entity == null) return false;
        if (!entity.isAlive()) return false;
        if (mc.player.distanceTo(entity) > attackRange) return false;
        return mc.player.getAttackCooldownProgress(0.5f) >= 0.9f;
    }
}
