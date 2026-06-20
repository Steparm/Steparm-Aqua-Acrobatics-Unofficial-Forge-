package sheg1_steparm.aquaacrobaticsunofficial.client.entity;

public interface IPlayerSPSwimming {
    boolean aquaAcrobatics$isActuallySneaking();

    boolean aquaAcrobatics$isForcedDown();

    boolean aquaAcrobatics$isUsingSwimmingAnimation();

    boolean aquaAcrobatics$isUsingSwimmingAnimation(float moveForward, float moveStrafe);

    boolean aquaAcrobatics$canSwim();

    boolean aquaAcrobatics$isMovingForward(float moveForward, float moveStrafe);

    boolean aquaAcrobatics$canPerformElytraTakeoff();
}