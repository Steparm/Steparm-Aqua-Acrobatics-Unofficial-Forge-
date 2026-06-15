package sheg1_steparm.aquaacrobaticsunofficial.integration.witchery;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WitcheryResurrectedIntegration {
    public static boolean HAS_TRANSFORMED = false;
    private static Transformation currentTransformation = Transformation.PLAYER;
    private static boolean isRegistered = false;

    public static void register() {
        if (isRegistered) return;
        if (!isWitcheryLoaded()) return;
        
        try {
            // Используем Reflection для подписки на событие трансформации
            Class<?> creatureFormClass = Class.forName("net.msrandom.witchery.common.transformation.CreatureForm");
            Object eventBus = creatureFormClass.getDeclaredField("PLAYER_TRANSFORM_EVENT").get(null);
            
            // Создаём подписчика через анонимный класс
            Object subscriber = new Object() {
                @SubscribeEvent
                public void onPlayerTransform(EntityPlayer player) {
                    try {
                        // Получаем текущую форму через Reflection
                        Class<?> transformationHelper = Class.forName("net.msrandom.witchery.common.transformation.TransformationHelper");
                        Object transformationData = transformationHelper.getMethod("getTransformationData", EntityPlayer.class).invoke(null, player);
                        
                        if (transformationData != null) {
                            Object currentForm = transformationData.getClass().getMethod("getCurrentForm").invoke(transformationData);
                            if (currentForm != null) {
                                String formName = currentForm.toString().toLowerCase();
                                if (formName.contains("bat")) {
                                    currentTransformation = Transformation.BAT;
                                } else if (formName.contains("wolfman")) {
                                    currentTransformation = Transformation.WOLFMAN;
                                } else if (formName.contains("wolf")) {
                                    currentTransformation = Transformation.WOLF;
                                } else if (formName.contains("toad")) {
                                    currentTransformation = Transformation.TOAD;
                                } else {
                                    currentTransformation = Transformation.PLAYER;
                                }
                                HAS_TRANSFORMED = true;
                            }
                        }
                    } catch (Exception e) {
                        // Игнорируем ошибки Reflection
                    }
                }
            };
            
            // Регистрируем подписчика
            eventBus.getClass().getMethod("subscribe", Object.class).invoke(eventBus, subscriber);
            isRegistered = true;
        } catch (Exception e) {
            // Witchery не загружен или API недоступен
            System.out.println("Witchery Resurrected integration failed to register: " + e.getMessage());
        }
    }

    public static boolean isWitcheryLoaded() {
        return Loader.isModLoaded("witchery");
    }

    public static Transformation getCurrentTransformation() {
        if (!isWitcheryLoaded()) return Transformation.PLAYER;
        return currentTransformation;
    }

    public static float getPlayerEyeHeight(EntityPlayer player, float defaultEyeHeight) {
        if (!isWitcheryLoaded()) return defaultEyeHeight;
        
        Transformation current = getCurrentTransformation();
        if (current == Transformation.BAT || current == Transformation.WOLF) {
            return 0.5f;
        }
        return defaultEyeHeight;
    }

    public enum Transformation {
        PLAYER, WOLF, WOLFMAN, BAT, TOAD
    }
}