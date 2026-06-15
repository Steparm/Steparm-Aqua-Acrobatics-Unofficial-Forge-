package sheg1_steparm.aquaacrobaticsunofficial.client.entity;

public interface IPlayerSPSwimming {

    boolean isActuallySneaking();

    boolean isForcedDown();

    boolean isUsingSwimmingAnimation();

    boolean isUsingSwimmingAnimation(float moveForward, float moveStrafe);

    boolean canSwim();

    boolean isMovingForward(float moveForward, float moveStrafe);
    
    boolean canPerformElytraTakeoff();

}