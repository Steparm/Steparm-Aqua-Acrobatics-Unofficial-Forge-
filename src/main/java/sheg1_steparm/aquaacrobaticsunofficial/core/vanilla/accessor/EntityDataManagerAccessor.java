package sheg1_steparm.aquaacrobaticsunofficial.core.vanilla.accessor;

import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityDataManager.class)
public interface EntityDataManagerAccessor {
    @Invoker("getEntry")
    <T> EntityDataManager.DataEntry<T> invokeGetEntry(DataParameter<T> key);
}